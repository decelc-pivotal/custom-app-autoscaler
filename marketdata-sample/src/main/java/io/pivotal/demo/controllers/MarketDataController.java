package io.pivotal.demo.controllers;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.pivotal.demo.workers.MarketData;

@Component
@RestController
public class MarketDataController {

	@Autowired
	private MarketData marketData;

	private static Logger log = LoggerFactory.getLogger(MarketDataController.class);

	@RequestMapping(value = "/nyse", method = RequestMethod.GET, produces = "application/json")
	public JSONObject getOrganizations(HttpServletRequest request) {
		log.info("Handling nyse");
		JSONObject json = new JSONObject();
		json.put("timestamp", new Date().toString());

		json.put("hour_chg", String.valueOf(marketData.getHour_chg()));
		json.put("day_chg", String.valueOf(marketData.getDay_chg()));
		json.put("month_chg", String.valueOf(marketData.getMonth_chg()));
		json.put("year_chg", String.valueOf(marketData.getYear_chg()));
		return json;
	}

	@RequestMapping(value = "/set", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public JSONObject getOrganizations1(@RequestParam("day") String day, HttpServletRequest request) {
		double day_chg = Double.parseDouble(day);
		marketData.setDay_chg(day_chg);
		JSONObject json = new JSONObject();
		json.put("success", true);
		return json;
	}

	@RequestMapping(value = "/setHour", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public JSONObject setHourChg(@RequestParam("hour") String hour, HttpServletRequest request) {
		double hour_chg = Double.parseDouble(hour);
		marketData.setDay_chg(hour_chg);
		JSONObject json = new JSONObject();
		json.put("success", true);
		return json;
	}
}
