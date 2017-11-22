package io.pivotal.demo.workers;

import org.springframework.stereotype.Component;

@Component
public class MarketData {

	private double hour_chg = 0;
	private double day_chg = 0;
	private double month_chg = 0;
	private double year_chg = 0;

	public double getHour_chg() {
		return hour_chg;
	}

	public void setHour_chg(double hour_chg) {
		this.hour_chg = hour_chg;
	}

	public double getDay_chg() {
		return day_chg;
	}

	public void setDay_chg(double day_chg) {
		this.day_chg = day_chg;
	}

	public double getMonth_chg() {
		return month_chg;
	}

	public void setMonth_chg(double month_chg) {
		this.month_chg = month_chg;
	}

	public double getYear_chg() {
		return year_chg;
	}

	public void setYear_chg(double year_chg) {
		this.year_chg = year_chg;
	}

}
