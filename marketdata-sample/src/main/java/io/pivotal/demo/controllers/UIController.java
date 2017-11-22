package io.pivotal.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import io.pivotal.demo.workers.MarketData;

@Component
@Controller
public class UIController {

	@Autowired
	private MarketData marketData;

	@RequestMapping(value = "/")
	public String home() {

		marketData.setHour_chg(0.05);
		marketData.setDay_chg(0.05);
		marketData.setMonth_chg(0.07);
		marketData.setYear_chg(0.1);

		return "index.html";
	}
}
