package io.pivotal.pa.appautoscaler.ui.controllers;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.cloud.Cloud;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.pivotal.pa.appautoscaler.ui.workers.Poller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class UIController {
	private Cloud cloud;

	public HttpHeaders createHeaders(String username, String password) {
		return new HttpHeaders() {
			{
				String auth = username + ":" + password;
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);
				set("Authorization", authHeader);
			}
		};
	}

	@RequestMapping(value = "/log_out", method = { RequestMethod.GET, RequestMethod.POST })
	public String logOut() {
		return "index.html";
	}

	@GetMapping(value = "/login")
	public String login() {
		System.out.println("get mapping login...");
		return "apps.html";
	}

	@PostMapping(value = "/login")
	public String postlogin(@RequestParam("apiHost") String apiHost, @RequestParam("password") String password,
			@RequestParam("username") String username) {
		Poller p = Poller.getInstance();

		JSONObject r = p.login(apiHost, username, password);

		if (r.containsKey("return_code")) {
			return "redirect:/?login=failed";
		}

		return "redirect:/apps.html";
	}

	@RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.POST })
	public String home() {
		return "index.html";
	}

	@RequestMapping(value = "/organizations", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public JSONObject getOrganizations(@RequestParam("host") String hostName, HttpServletRequest request) {
		Poller p = Poller.getInstance();

		JSONObject json = p.getOrganization(hostName);

		return json;
	}

	@RequestMapping(value = "/addRule", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = "application/json")
	@ResponseBody
	public JSONObject getAddRule(@RequestBody MultiValueMap<String, String> formData, HttpServletRequest request) {
		log.info("addRule - formdata=" + formData.size());
		log.info("addRule - formdata=" + formData.entrySet());
		Map<String, String> m = formData.toSingleValueMap();

		String spaceGUID="", appGUID="", ruleURL="", ruleAPIKey="", ruleExpression="", metricScale="", minInstances="", maxInstances="";

		for (Entry<String, String> m1 : m.entrySet()) {
			if (m1.getKey().contains("spaceGUID"))
				spaceGUID = m1.getValue();
			if (m1.getKey().contains("appGUID"))
				appGUID = m1.getValue();
			if (m1.getKey().contains("ruleURL"))
				ruleURL = m1.getValue();
			if (m1.getKey().contains("ruleAPIKey"))
				ruleAPIKey = m1.getValue();
			if (m1.getKey().contains("ruleExpression"))
				ruleExpression = m1.getValue();
			if (m1.getKey().contains("minInstances"))
				minInstances = m1.getValue();
			if (m1.getKey().contains("maxInstances"))
				maxInstances = m1.getValue();

			log.info("addRule - entry set key= " + m1.getKey());
			log.info("addRule - entry set value= " + m1.getValue());

		}

		int appIndex, bindingIndex;

		appIndex = appGUID.indexOf("apps");
		bindingIndex = appGUID.indexOf("service_bindings");
		appGUID = appGUID.substring(appIndex + 5, bindingIndex - 1);
		
		log.info("addRule - appGUID=" + appGUID);
		log.info("addRule - spaceGUID=" + spaceGUID);
		log.info("addRule - ruleURL=" + ruleURL);
		log.info("addRule - ruleAPIKey=" + ruleAPIKey);
		log.info("addRule - ruleExpression=" + ruleExpression);
		log.info("addRule - minInstances=" + minInstances);
		log.info("addRule - maxInstances=" + maxInstances);

		JSONObject rule1 = new JSONObject();
		rule1.put("appGUID", appGUID);
		rule1.put("spaceGUID", spaceGUID);
		rule1.put("ruleURL", ruleURL);
		rule1.put("ruleAPIKey", ruleAPIKey);
		rule1.put("ruleExpression", ruleExpression);
		rule1.put("metricScale", metricScale);
		rule1.put("minInstances", minInstances);
		rule1.put("maxInstances", maxInstances);

		Poller p = Poller.getInstance();
		
		String h = "http://appautoscaler-" + spaceGUID + ".apps.decelles.io/";
		log.info("addRule - rule url=" + h);

		// we need the service instance id here to post to right app
		// so we need to check if it's there, if not we need to create it
		// from this app, steal code from worker app...

		JSONObject r = p.addRule(rule1.toJSONString());
		log.info("addRule - return=" + r);
		log.info("addRule - formdata rule=" + formData.get("rule"));
		log.info("addRule - formdata appGUID=" + formData.get("appGUID"));

		return r;
	}

	@RequestMapping(value = "/checkRuleURL", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = "application/json")
	@ResponseBody
	public JSONObject checkRuleURL(@RequestParam("ruleURL") String ruleURL, HttpServletRequest request) {
		System.out.println("[Controller] checkRuleURL - start ruleURL=" + ruleURL);
		JSONObject json = new JSONObject();
		Poller p = Poller.getInstance();
		// JSONObject jsonCall = p.getData(ruleURL);

		Object obj = p.getData(ruleURL);

		if (json == null) {
			json = new JSONObject();
			json.put("return-value", "failure");
			return json;
		}
		
		if (obj instanceof JSONObject) {
			JSONObject jsonCall = (JSONObject) obj;
			// Check for metrics json
			if (jsonCall.containsKey("metricName")) {
				HashMap<String, String> m = new HashMap<String, String>();

				for (Object key : jsonCall.keySet()) {
					m.put((String) jsonCall.get("metricName"), (String) jsonCall.get("metricName"));
				}

				json.put("return-elements", m.entrySet());

			} else {
				json.put("return-elements", jsonCall.entrySet());
			}
		} else if (obj instanceof JSONArray) {

			JSONArray jsonArray = (JSONArray) obj;
			HashMap m = new HashMap();


			Iterator<JSONObject> metricsIter = jsonArray.iterator();
			while (metricsIter.hasNext()) {
				JSONObject jsonObject = metricsIter.next();
				log.info("checkRuleURL - metricsIter.next()=" + jsonObject);
				m.put((String) jsonObject.get("metricName") + " (" + jsonObject.get("aggregation") + ")",
						(String) jsonObject.get("metricName") + " (" + jsonObject.get("aggregation") + ")");
				json.put("return-elements", m.entrySet());
			}
		}

		json.put("return-value", "success");

		log.info("checkRuleURL - end json=" + json);
		return json;
	}

	@RequestMapping(value = "/spaceApps", method = { RequestMethod.GET,
			RequestMethod.POST }, produces = "application/json")
	@ResponseBody
	public JSONObject getSpaceApps(@RequestHeader("host") String hostName, HttpServletRequest request) {
		log.info("getSpaceApps - start");
		Poller p = Poller.getInstance();
		JSONObject r = p.getAllApps();
		log.info("getSpaceApps - end");
		return r;
	}

}
