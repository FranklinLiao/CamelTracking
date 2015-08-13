package com.franklin.domain;

import java.math.BigDecimal;

public class SectorBoundryObject {
	private int id;
	private double lat;
	private double lon;
	private String phoneNo;
	private int startangle;
	private int endangle;
	private int radius;
	private int throld;
	private int status;
	
	public SectorBoundryObject() {
		
	}
	
	public SectorBoundryObject(String phoneNo,double lat,double lon,int startangle,
			int endangle,int radius,int throld) {
		this.phoneNo = phoneNo;
		this.lat = lat;
		this.lon = lon;
		this.startangle = startangle;
		this.endangle = endangle;
		this.radius = radius;
		this.throld = throld;
	}
	
	
	@Override
	public String toString() {
		
		return "" + phoneNo + "" + stringFormat(numberPrecise(lon),2) + "" + stringFormat(numberPrecise(lat),1)
				+ ""+startangle + "" + endangle + ""+intFormat(radius) + ""
				+ intFormat(throld)+"eee";
	}

	public String intFormat(int intValue) {
		return String.format("%05d", intValue);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public String getPhoneNo() {
		return phoneNo;
	}
	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}
	public int getStartangle() {
		return startangle;
	}
	public void setStartangle(int startangle) {
		this.startangle = startangle;
	}
	public int getEndangle() {
		return endangle;
	}
	public void setEndangle(int endangle) {
		this.endangle = endangle;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public int getThrold() {
		return throld;
	}

	public void setThrold(int throld) {
		this.throld = throld;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	
	
	public String numberPrecise(double number) {
		int precise = 6;
		BigDecimal bigDecimal = new BigDecimal(number); 
		double num = bigDecimal.setScale(precise,BigDecimal.ROUND_HALF_UP).doubleValue();
		String formatString = String.valueOf(num);
		
		return formatString;
	}
	
	public String stringFormat(String sTemp,int intLen) {
		String s = sTemp;
		if(sTemp.indexOf(".")!=-1) {
			int k = sTemp.indexOf(".");
			
			int m = sTemp.length()-k;
			while(m++<=6) {
				sTemp+='0';
			}
			while(k++<=intLen) {
				sTemp = '0'+sTemp;
			}
			
		}
		return sTemp;
	}
}
