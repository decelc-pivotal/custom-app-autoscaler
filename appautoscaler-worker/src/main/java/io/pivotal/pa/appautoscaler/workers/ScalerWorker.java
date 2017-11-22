package io.pivotal.pa.appautoscaler.workers;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.Cloud;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import io.pivotal.pa.appautoscaler.domain.Rule;
import io.pivotal.pa.appautoscaler.helper.Connection;
import io.pivotal.pa.appautoscaler.repositories.RuleRepository;



@Component
public class ScalerWorker {

	@Autowired
	RuleRepository ruleRepository;

	private boolean debug = false;

	private Cloud cloud;
	private String LOGIN_HOST = "";
	private String API_HOST = "";

	private static final Logger log = LoggerFactory.getLogger(ScalerWorker.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	private static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}
	} };

	@Scheduled(fixedRate = 30000)
	public void appScaler() {
		JSONParser parser = new JSONParser();
		JSONObject vcap = null;

		System.out.println("[ScalerWorker] " + dateFormat.format(new Date()));
		

		if (this.API_HOST.equals("")) {

			Map<String, String> env = System.getenv();
			for (String envName : env.keySet()) {
				System.out.println("[ScalerWorker] " + envName + "=" + env.get(envName) + "\n");
			}

			try {
				vcap = (JSONObject) parser.parse(env.get("VCAP_APPLICATION"));
				this.API_HOST = (String) vcap.get("cf_api");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// this.API_HOST = (String) vcap.get("cf_api");
		this.LOGIN_HOST = System.getenv("LOGIN_HOST");
		System.out.println("[ScalerWorker] LOGIN_HOST=" + LOGIN_HOST);
		System.out.println("[ScalerWorker] API_HOST=" + API_HOST);

		Iterable<Rule> rules = ruleRepository.findAll();

		for (Rule rule : rules) {
			String ruleExpression = rule.getRuleExpression();
			System.out.println("[ScalerWorker] app ruleExpression=" + ruleExpression);

			if (!StringUtils.isEmpty(ruleExpression)) {
				JSONObject jsonRules = null;

				// add logic here to handle rule for array items and metrics
				try {
					jsonRules = (org.json.simple.JSONObject) parser.parse(ruleExpression);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				System.out.println("[ScalerWorker] JSON app rule=" + jsonRules);

				JSONArray a = (JSONArray) jsonRules.get("rules");
				JSONObject jsonRule = (JSONObject) a.get(0);

				// Rule info
				String dataName = (String) jsonRule.get("field");
				String ruleCompareValue = (String) jsonRule.get("value");
				String operator = (String) jsonRule.get("operator");
				String ruleOperator = getOperator(operator);

				System.out.println("[ScalerWorker] JSON rule[0] dataName=" + dataName + " ruleCompareValue="
						+ ruleCompareValue + " operator conversion=" + ruleOperator);

				String appAPI = this.API_HOST + "/v2/apps/" + rule.getAppGUID();
				// get data for credentials
				System.out.println("[ScalerWorker] appAPI=" + appAPI);

				JSONObject rtn = getCurrentInstances(appAPI);
				Object r1 = getData1(rule.getRuleURL());
				Double metricValue = getDataValue(r1, dataName);

				System.out.println("[ScalerWorker] return of rule url=" + rule.getRuleURL() + " response=" + r1);

				// boolean scaleApp = compareValues(ruleCompareValue, new
				// Double((String) r.get(dataName)), ruleOperator);
				boolean scaleApp = compareValues(ruleCompareValue, metricValue, ruleOperator);
				System.out.println("[ScalerWorker] scale app=" + scaleApp);

				Long l = (Long) rtn.get("instances");
				System.out.println("[ScalerWorker] app summary number of instances=" + l);
				Long minInstances = Long.parseLong(rule.getMinInstances());
				Long maxInstances = Long.parseLong(rule.getMaxInstances());

				if (scaleApp) {
					if (l < maxInstances) {
						l = l + 1;
						System.out.println("[ScalerWorker] scale the app up=" + l);
						scaleApp(appAPI, l);
					}
				} else {
					// check to see if we need to scale down.....
					if (l > minInstances) {
						l = l - 1;
						System.out.println("[ScalerWorker] scale the app down=" + l);
						scaleApp(appAPI, l);
					}
					// check to see if we need to scale to
					// minimum.....
					else if (l < minInstances) {
						l = l + 1;
						System.out.println("[ScalerWorker] scale the app to mininum=" + l);
						scaleApp(appAPI, l);
					}
				}
		}
		}
	}

	private boolean compareValues(String ruleCompareValue, Double metricValue, String operator) {
		System.out.println("[ScalerWorker] compareValues ruleCompareValue=" + ruleCompareValue + " metricValue="
				+ metricValue
				+ " operator=" + operator);

		if (operator.equals("=")) {
			if (metricValue.compareTo(Double.valueOf(ruleCompareValue)) == 0) {
				return true;
			}
		} else if (operator.equals(">")) {
			if (metricValue > Double.valueOf(ruleCompareValue)) {
				return true;
			}
		} else if (operator.equals("<")) {
			if (metricValue < Double.valueOf(ruleCompareValue)) {
				return true;
			}
		}

		return false;
	}

	// get data for rule url
	private JSONObject getData(String url) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity;
		JSONParser parser = new JSONParser();
		JSONObject json = null;

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		LinkedMultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();

		// get data for rule url
		responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(postBody, headers), String.class);

		try {
			json = (org.json.simple.JSONObject) parser.parse(responseEntity.getBody());

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return json;
	}

	// get data for rule url
	private Object getData1(String url) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity;
		JSONParser parser = new JSONParser();
		JSONObject json = null;
		Object obj = null;

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		LinkedMultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();

		// get data for rule url
		responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(postBody, headers), String.class);

		try {
			obj = parser.parse(responseEntity.getBody());

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return obj;
	}

	public JSONObject scaleApp(String scaleAppURL, Long currentInstances) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity;
		JSONParser parser = new JSONParser();
		JSONObject json = null;

		HttpHeaders headers = Connection.getAuthorizationHeader(getOAuthToken(), API_HOST);
		headers.add("Content-Type", "application/json");

		JSONObject postBody = new JSONObject();
		postBody.put("instances", currentInstances);

		// System.out.println("[ScalerWorker] scaleApp headers=" + headers);
		System.out.println("[ScalerWorker] scaleApp scaleAppURL=" + scaleAppURL);
		System.out.println("[ScalerWorker] scaleApp postBody=" + postBody);

		// get current instances
		responseEntity = restTemplate.exchange(scaleAppURL, HttpMethod.PUT,
				new HttpEntity<>(postBody.toJSONString(), headers), String.class);
		// System.out.println("[ScalerWorker] scaleApp return=" +
		// responseEntity.getBody() + " status"
		// + responseEntity.getStatusCodeValue());

		try {
			json = (org.json.simple.JSONObject) parser.parse(responseEntity.getBody());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return json;
	}

	public JSONObject getCurrentInstances(String scaleAppURL) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity;
		JSONParser parser = new JSONParser();
		JSONObject json = null;

		HttpHeaders headers = Connection.getAuthorizationHeader(getOAuthToken(), API_HOST);

		// get current instances
		responseEntity = restTemplate.exchange(scaleAppURL + "/summary", HttpMethod.GET,
				new HttpEntity<>(null, headers), String.class);

		try {
			json = (org.json.simple.JSONObject) parser.parse(responseEntity.getBody());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return json;
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

		HttpHeaders headers1 = Connection.getBasicAuthorizationHeader("cf", "");

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", getAuthorizationHeader("cf", ""));

		LinkedMultiValueMap<String, String> postBody = new LinkedMultiValueMap<>();
		postBody.add("grant_type", "password");
		postBody.add("username", System.getenv("CF_ADMIN_USER"));
		postBody.add("password", System.getenv("CF_ADMIN_PASSWORD"));

		ResponseEntity<String> r = restTemplate.exchange(LOGIN_HOST + "/oauth/token", HttpMethod.POST,
				new HttpEntity<>(postBody, headers1), String.class);

		org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
		org.json.simple.JSONObject json = null;
		try {
			json = (org.json.simple.JSONObject) parser.parse(r.getBody());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return (String) json.get("access_token");

	}

	private JSONObject getCCAPIData(String url) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity;
		JSONParser parser = new JSONParser();
		JSONObject json = null;

		HttpHeaders headers = Connection.getAuthorizationHeader(getOAuthToken(), API_HOST);

		// get current instances
		responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null, headers), String.class);

		try {
			json = (org.json.simple.JSONObject) parser.parse(responseEntity.getBody());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return json;
	}

	private boolean isServiceInstanceBound() {
		return true;
	}

	private void printMessage(String message) {
		if (debug)
			System.out.println("[ScalerWorker] " + message);
	}

	private HttpHeaders getCCHeader(String accessToken, String host) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", "bearer " + accessToken);
		headers.add("Host", host);

		return headers;
	}

	private String getAuthorizationHeader(String clientId, String clientSecret) {
		String creds = String.format("%s:%s", clientId, clientSecret);
		try {
			return "Basic " + new String(Base64.encode(creds.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Could not convert String");
		}
	}

	private String getOperator(String englishValue) {
		if (englishValue.equals("equal")) {
			return "=";
		} else if (englishValue.equals("greater")) {
			return ">";
		} else if (englishValue.equals("less")) {
			return "<";
		} else if (englishValue.equals("greater or equal")) {
			return ">=";
		} else if (englishValue.equals("less or equal")) {
			return "<=";
		}

		return "=";
	}

	// custom get data value from the rule, handle generic name:value and
	// metrics data
	private Double getDataValue(Object obj, String dataName) {

		JSONObject json = new JSONObject();
		Double val = null;

		if (obj instanceof JSONObject) {
			JSONObject jsonCall = (JSONObject) obj;
			// Check for metrics json
			if (jsonCall.containsKey("metricName")) {
				System.out
						.println("[ScalerWorker] metricname = dataname=" + jsonCall.get("metricName").equals(dataName));
				if (jsonCall.get("metricName").equals(dataName)) {
					val = new Double(jsonCall.get("value").toString());
					System.out.println("[ScalerWorker] getDataValue() 0.0 val=" + val);
				}

			} else {
				val = new Double((String) jsonCall.get(dataName));
				System.out.println("[ScalerWorker] getDataValue() 0.1 val=" + val);
			}
		} else if (obj instanceof JSONArray) {

			JSONArray jsonArray = (JSONArray) obj;
			HashMap m = new HashMap();

			Iterator<JSONObject> flavoursIter = jsonArray.iterator();
			while (flavoursIter.hasNext()) {
				JSONObject jsonObject = flavoursIter.next();
				System.out.println("[Controller] checkRuleURL - flavoursIter.next()=" + flavoursIter.next());

				String metricName = (String) jsonObject.get("metricName");
				String aggregation = (String) jsonObject.get("aggregation");

				if (dataName.contains(jsonObject.get("metricName") + " (" + jsonObject.get("aggregation") + ")")) {
					val = new Double(jsonObject.get("value").toString());
				}
			}
		}

		return val;
	}
}
