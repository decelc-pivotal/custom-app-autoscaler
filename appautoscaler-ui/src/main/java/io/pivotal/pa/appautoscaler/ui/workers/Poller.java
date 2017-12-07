package io.pivotal.pa.appautoscaler.ui.workers;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;

public class Poller {

	private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	private static String SPACES_URL = null;

	private static String METADATA = "metadata";
	private static String ENTITY = "entity";
	private static String RESOURCES = "resources";

	private String LOGIN_HOST = "";
	private String USERNAME = "";
	private String PASSWORD = "";
	private String HOST = "https://api.system.decelles.io";
	private String APPS_DOMAIN = "";

	private static boolean debug = false;

	private static Poller singleton = new Poller();

	/*
	 * A private Constructor prevents any other class from instantiating.
	 */
	private Poller() {
	}

	/* Static 'instance' method */
	public static Poller getInstance() {
		return singleton;
	}

	private static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}
	} };

	public HttpHeaders createHeaders(String username, String password) {
		return new HttpHeaders() {
			{
				String auth = username + ":" + password;
				set("Authorization", auth);
			}
		};
	}

	private String getAuthorizationHeader(String clientId, String clientSecret) {
		String creds = String.format("%s:%s", clientId, clientSecret);
		try {
			return "Basic " + new String(Base64.encode(creds.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Could not convert String");
		}
	}

	public JSONObject getAllApps() {
		JSONObject json = httpGet("/v2/organizations");

		JSONArray a = (JSONArray) json.get(RESOURCES);
		printMessage("ORGANIZATIONs= " + a.size());

		HashMap<String, String> spaces = new HashMap<String, String>();
		HashMap<String, String> orgs = new HashMap<String, String>();
		ArrayList k = new ArrayList();
		for (int i = 0; i < a.size(); i++) {

			JSONObject o = (JSONObject) a.get(i);

			// printMessage("org=" + o.toString());
			JSONObject md = (JSONObject) o.get(ENTITY);
			// printMessage("spaces =" + md.get("spaces_url"));

			JSONObject md2 = (JSONObject) o.get(METADATA);
			orgs.put((String) md2.get("guid"), (String) md.get("name"));

			SPACES_URL = (String) md.get("spaces_url");

			// add loop data here.....
			ArrayList l = getAllAppsForOrg(SPACES_URL, orgs, spaces);
			k.addAll(l);

		}

		json = new JSONObject();
		for (String key : spaces.keySet()) {
			json.put(key, spaces.get(key));
		}
		json.put(RESOURCES, k);

		return json;
	}

	public ArrayList getAllAppsForOrg(String spacesURL, HashMap<String, String> orgs, HashMap<String, String> spaces) {

		JSONObject json = httpGet(SPACES_URL);
		ArrayList resources = (ArrayList) json.get(RESOURCES);
		ArrayList apps = new ArrayList();
		
		JSONObject jsonReturn = new JSONObject();
		for (Object resource : resources) {
			JSONObject r = (JSONObject) resource;
			JSONObject entity = (JSONObject) r.get(ENTITY);
			JSONObject metadata = (JSONObject) r.get(METADATA);
			spaces.put((String) metadata.get("guid"), (String) entity.get("name"));
			spaces.put((String) metadata.get("guid") + "-org", orgs.get(entity.get("organization_guid")));
			json = httpGet((String) entity.get("apps_url"));
			long resultsSize = (long) json.get("total_results");

			if (resultsSize > 0) {
				ArrayList l1 = (ArrayList) json.get(RESOURCES);
				apps.addAll(l1);
			}
		}

		return apps;
	}

	public JSONObject getOrganization(String orgName) {
		JSONObject json = httpGet("/v2/organizations");
		return getJSONObjectData(json, "name", orgName, ENTITY, null);
	}

	public JSONObject login(String systemDomain, String username, String password) {

		printMessage("[LOGIN] domain=" + systemDomain);
		printMessage("[LOGIN] username=" + username);
		printMessage("[LOGIN] password=" + password);

		this.USERNAME = username;
		this.PASSWORD = password;
		this.APPS_DOMAIN = systemDomain.replace("system", "apps");
		this.LOGIN_HOST = "https://login." + systemDomain;

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", getAuthorizationHeader("cf", ""));

		LinkedMultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();
		postBody.add("grant_type", "password");
		postBody.add("username", username);
		postBody.add("password", password);

		printMessage("post body=" + postBody);

		ResponseEntity<String> r = null;

		try {
			printMessage("##loginURL" + this.LOGIN_HOST + "/oauth/token");

			r = restTemplate.exchange(this.LOGIN_HOST + "/oauth/token", HttpMethod.POST,
					new HttpEntity<>(postBody, headers),
					String.class);

			printMessage("Return = " + r.getBody() + " status" + r.getStatusCodeValue());

		} catch (Exception e) {

			printMessage("Exception");
			e.printStackTrace();
			if (r != null) {
				printMessage("EXCEPTION =" + r.getStatusCodeValue() + " object=" + r.getBody());
			}
			org.json.simple.JSONObject json = new JSONObject();
			json.put("return_code", "404");

			return json;
		}

		org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
		org.json.simple.JSONObject json = null;
		try {
			json = (org.json.simple.JSONObject) parser.parse(r.getBody());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		this.HOST = this.LOGIN_HOST.replace("login", "api");

		printMessage("this.APPS_DOMAIN=" + this.APPS_DOMAIN);
		printMessage("this.LOGIN_HOST=" + this.LOGIN_HOST);
		printMessage("this.HOST=" + this.HOST);
		printMessage("RETURN=" + r.getStatusCodeValue() + " object=" + r.getBody());
		printMessage("Access Token=" + json.get("access_token"));

		return json;
	}

	public JSONObject addRule(String rule) {

		printMessage("[addRule] rule=" + rule);
		printMessage("[addRule] username=" + USERNAME);
		printMessage("[addRule] password=[redacted]");

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

		RestTemplate restTemplate = new RestTemplate();

		// create request body
		JSONObject request = new JSONObject();

		// set headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();

		JSONObject rulejson = null;

		try {
			rulejson = (org.json.simple.JSONObject) parser.parse(rule);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		printMessage("rulejson=" + rulejson);
		request.put("spaceGUID", (String) rulejson.get("spaceGUID"));
		request.put("appGUID", (String) rulejson.get("appGUID"));
		request.put("ruleURL", (String) rulejson.get("ruleURL"));
		request.put("ruleAPIKey", (String) rulejson.get("ruleAPIKey"));
		request.put("ruleExpression", (String) rulejson.get("ruleExpression"));
		request.put("metricScale", (String) rulejson.get("metricScale"));
		request.put("minInstances", (String) rulejson.get("minInstances"));
		request.put("maxInstances", (String) rulejson.get("maxInstances"));

		String serviceInstanceGUID = checkServiceAvailability(rulejson);

		ResponseEntity<String> r = null;
		HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);

		try {
			r = restTemplate.exchange("http://appautoscaler-" + serviceInstanceGUID + "." + this.APPS_DOMAIN + "/rules",
					HttpMethod.POST, entity, String.class);
			printMessage("Return = " + r.getBody() + " status" + r.getStatusCodeValue());

		} catch (HttpClientErrorException e) {

			e.printStackTrace();
		}

		org.json.simple.JSONObject json = null;
		try {
			json = (org.json.simple.JSONObject) parser.parse(r.getBody());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		printMessage("RETURN=" + r.getStatusCodeValue() + " object=" + r.getBody());

		return json;
	}

	// get data for rule url
	public Object getData(String url) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity;
		JSONParser parser = new JSONParser();

		Object obj = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		LinkedMultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();

		// get data for rule url
		responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(postBody, headers), String.class);
		if (responseEntity == null) {
			return null;
		}

		HttpStatus status = responseEntity.getStatusCode();

		if (!status.is2xxSuccessful()) {
			return null;
		}

		try {
			obj = parser.parse(responseEntity.getBody());
		} catch (ParseException ex) {
			ex.printStackTrace();
			return null;
		}
		return obj;
	}

	private HttpHeaders getCCHeader(String accessToken, String host) {
		printMessage("getCCHeader() accessToken=" + accessToken + " host=" + host);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", "bearer " + accessToken);
		headers.add("Host", host);

		return headers;
	}

	private String getOAuthToken() {

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", getAuthorizationHeader("cf", ""));

		LinkedMultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();
		postBody.add("grant_type", "password");
		postBody.add("username", USERNAME);
		postBody.add("password", PASSWORD);

		ResponseEntity<String> r = restTemplate.exchange(LOGIN_HOST + "/oauth/token", HttpMethod.POST,
				new HttpEntity<>(postBody, headers), String.class);
		printMessage("Return = " + r.getBody() + " status" + r.getStatusCodeValue());

		org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
		org.json.simple.JSONObject json = null;
		try {
			json = (org.json.simple.JSONObject) parser.parse(r.getBody());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		printMessage("Access Token=" + json.get("access_token"));

		return (String) json.get("access_token");
	}

	private JSONObject getJSONObjectData(JSONObject json, String keyName, String keyValue, String type,
			String returnType) {

		JSONArray a = (JSONArray) json.get(RESOURCES);

		for (int i = 0; i < a.size(); i++) {
			JSONObject o = (JSONObject) a.get(i);

			// printMessage("org=" + o.toString());
			JSONObject md = (JSONObject) o.get(type);
			printMessage("spaces =" + md.get(keyName));

			String value = (String) md.get(keyName);

			if (value != null && keyValue.equals(value)) {
				printMessage("equalskeyvalue" + keyValue);

				if (returnType == null) {
					return o;
				} else if (type.equals(returnType))
					return md;
				else {
					md = (JSONObject) o.get(returnType);
					return md;
				}
			}
		}
		printMessage("Organiztions=" + json.toString());

		return null;
	}


	private String checkServiceAvailability(JSONObject rulejson) {

		JSONObject j = httpGet("/v2/spaces/" + rulejson.get("spaceGUID") + "/service_instances");
		JSONArray a = (JSONArray) j.get(RESOURCES);

		boolean serviceInstanceCreated = false;
		String serviceGUID = null;
		String serviceInstanceGUID = null;
		for (int i = 0; i < a.size(); i++) {

			JSONObject obj = (JSONObject) a.get(i);
			System.out.println("#### array = " + obj.toJSONString());
			JSONObject e = (JSONObject) obj.get(ENTITY);
			JSONObject m = (JSONObject) obj.get(METADATA);
			System.out.println("#### array instance id = " + e.get("service_instance_guid"));

			System.out.println("#### array instance id = " + e.get("service_instance_guid"));
			System.out.println("#### service instances name = " + e.get("name"));
			System.out.println("#### service instance e.toJSONString() = " + e.toJSONString());

			serviceInstanceGUID = (String) m.get("guid");
			String name = (String) e.get("name");
			serviceGUID = (String) e.get("service_guid");

			if (name.equals("custom-app-autoscaler")) {
				serviceInstanceCreated = true;

			}

		}

		System.out.println("#### serviceInstanceCreated = " + serviceInstanceCreated);

		if (!serviceInstanceCreated) {
			// create the service instance =

			JSONObject servicePlan = getServicePlan();

			System.out.println("###### serviceplan=" + servicePlan.get("guid"));

			// create the service instance.... need the plan to do that

			createServiceInstance((String) servicePlan.get("guid"),
					(String) rulejson.get("spaceGUID"),
					(String) rulejson.get("appGUID"));

		} else if (!isAppBoundToService((String) rulejson.get("appGUID"), serviceInstanceGUID)) {

			createServiceBinding(serviceInstanceGUID, (String) rulejson.get("appGUID"));

		}

		System.out.println("######## return from service bindings=" + j.toJSONString());

		return serviceInstanceGUID;

	}

	private void createServiceInstance(String servicePlanGUID, String spaceGUID,
			String appGUID) {

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		String accessToken = getOAuthToken();
		headers = getCCHeader(accessToken, HOST);

		// create request body
		JSONObject request = new JSONObject();

		// LinkedMultiValueMap<String, String> postBody = new
		// LinkedMultiValueMap<>();
		request.put("name", "custom-app-autoscaler");
		request.put("service_plan_guid", servicePlanGUID);
		request.put("space_guid", spaceGUID);

		printMessage("post body=" + request);
		System.out.println("############ createServiceInstance body=" + request);
		System.out.println("############ createServiceInstance servicePlanGUID=" + servicePlanGUID);
		System.out.println("############ createServiceInstance spaceGUID=" + spaceGUID);

		ResponseEntity<String> r = null;

		try {
			// printMessage("##loginURL" + loginURL + "/oauth/token");
			System.out.println("############ host=" + HOST + "/v2/service_instances?accepts_incomplete=false");

			r = restTemplate.exchange(HOST + "/v2/service_instances?accepts_incomplete=true", HttpMethod.POST,
					new HttpEntity<>(request.toString(), headers),
					String.class);

			printMessage("Return = " + r.getBody() + " status" + r.getStatusCodeValue());

			org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
			org.json.simple.JSONObject json = null;

			try {
				json = (org.json.simple.JSONObject) parser.parse(r.getBody());
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			JSONObject metadata = (JSONObject) json.get(METADATA);
			createServiceBinding((String) metadata.get("guid"), appGUID);

		} catch (Exception e) {

			printMessage("Exception");
			e.printStackTrace();
			if (r != null) {
				printMessage("EXCEPTION =" + r.getStatusCodeValue() + " object=" + r.getBody());
			}
			org.json.simple.JSONObject json = new JSONObject();
			json.put("return_code", "404");

			// return json;
			// json.put, value)
		}

		// printMessage("RETURN=" + r.getStatusCodeValue() + " object=" +
		// r.getBody());
	}

	private void createServiceBinding(String serviceInstanceGUID, String appGUID) {

		System.out.println("###### createServiceBinding serviceInstanceGUID=" + serviceInstanceGUID);
		System.out.println("###### createServiceBinding appGUID=" + appGUID);

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		String accessToken = getOAuthToken();
		headers = getCCHeader(accessToken, HOST);

		// create request body
		JSONObject request = new JSONObject();

		request.put("service_instance_guid", serviceInstanceGUID);
		request.put("app_guid", appGUID);

		printMessage("post body=" + request);
		System.out.println("###### post body==" + request.toJSONString());

		ResponseEntity<String> r = null;

		try {
			// printMessage("##loginURL" + loginURL + "/oauth/token");

			r = restTemplate.exchange(HOST + "/v2/service_bindings", HttpMethod.POST,
					new HttpEntity<>(request.toString(), headers), String.class);

			// printMessage("createServiceBinding = " + r.getBody() + " status"
			// + r.getStatusCodeValue());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// new one
	private JSONObject getServicePlan() {

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", getAuthorizationHeader("cf", ""));

		LinkedMultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();

		ResponseEntity<String> r;

		JSONParser parser = new JSONParser();
		JSONObject json = null;

		String accessToken = getOAuthToken();
		headers = getCCHeader(accessToken, HOST);

		// get list of organizations for host
		r = restTemplate.exchange(HOST + "/v2/services", HttpMethod.GET, new HttpEntity<>(postBody, headers),
				String.class);
		printMessage("Return servicebindings = " + r.getBody() + " status" + r.getStatusCodeValue());

		parser = new org.json.simple.parser.JSONParser();

		json = null;
		try {
			json = (org.json.simple.JSONObject) parser.parse(r.getBody());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JSONArray a = (JSONArray) json.get(RESOURCES);

		// System.out.println("#### array = " + a.size());

		for (int i = 0; i < a.size(); i++) {

			JSONObject obj = (JSONObject) a.get(i);
			// System.out.println("#### array = " + obj.toJSONString());
			JSONObject e = (JSONObject) obj.get("entity");
			// System.out.println("#### array instance id = " +
			// e.get("service_instance_guid"));

			// System.out.println("#### array instance id = " +
			// e.get("service_instance_guid"));
			// System.out.println("#### service instances name = " +
			// e.get("label"));

			String name = (String) e.get("label");
			// System.out.println("#### service instances name = " + name);

			if (name.equals("custom-appautoscaler")) {
				// System.out.println("#### service_plans_url = " + (String)
				// e.get("service_plans_url"));

				JSONObject j = httpGet((String) e.get("service_plans_url"));

				JSONArray resources = (JSONArray) j.get(RESOURCES);

				// only 1 service plan
				JSONObject servicePlan = (JSONObject) resources.get(0);
				JSONObject metadata = (JSONObject) servicePlan.get(METADATA);
				// System.out.println("#### get data service plans = " +
				// metadata.toJSONString());

				return metadata;
			}

		}

		return json;
	}

	private boolean isAppBoundToService(String appGUID, String serviceInstanceGUID) {
		JSONObject json = httpGet("/v2/apps/" + appGUID + "/service_bindings");
		JSONArray a = (JSONArray) json.get(RESOURCES);

		for (int i = 0; i < a.size(); i++) {

			JSONObject obj = (JSONObject) a.get(i);
			JSONObject e = (JSONObject) obj.get("entity");

			String bindingServiceInstanceGUID = (String) e.get("service_instance_guid");
			String bindingAppGUID = (String) e.get("app_guid");

			if (bindingServiceInstanceGUID.equals(serviceInstanceGUID) && bindingAppGUID.equals(appGUID)) {
				return true;
			}

		}

		return false;
	}

	private JSONObject httpGet(String url) {
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", getAuthorizationHeader("cf", ""));

		LinkedMultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();

		ResponseEntity<String> r;

		JSONParser parser = new JSONParser();
		JSONObject json = null;

		String accessToken = getOAuthToken();
		headers = getCCHeader(accessToken, HOST);
		System.out.println("########## httpGet url =" + HOST + url);

		r = restTemplate.exchange(HOST + url, HttpMethod.GET, new HttpEntity<>(postBody, headers), String.class);
		printMessage("Return servicebindings = " + r.getBody() + " status" + r.getStatusCodeValue());

		parser = new org.json.simple.parser.JSONParser();

		try {
			json = (org.json.simple.JSONObject) parser.parse(r.getBody());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return json;
	}

	private ResponseEntity httpPost(String url, JSONObject postData) {

		System.out.println("###### httpPost() url=" + url);
		System.out.println("###### httpPost() postData=" + postData.toJSONString());

		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		String accessToken = getOAuthToken();
		headers = getCCHeader(accessToken, HOST);

		ResponseEntity<String> r = null;

		try {
			r = restTemplate.exchange(HOST + url, HttpMethod.POST, new HttpEntity<>(postData.toString(), headers),
					String.class);
			return r;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return r;
	}

	private void printMessage(String message) {
		if (debug)
			System.out.println(message);
	}

}
