package com.franklin.domain;
import java.math.BigDecimal;
import java.util.List;

public class PolygonBoundryObject {
	private int id;
	private double lat;
	private double lon;
	private String phoneNo;
	private List<Integer> rList; 
	private int throld;
	private int status;
	
	
	
	public PolygonBoundryObject(String phoneNo,double lat, double lon, 
			List<Integer> rList, int throld) {
		super();
		this.lat = lat;
		this.lon = lon;
		this.phoneNo = phoneNo;
		this.rList = rList;
		this.throld = throld;
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
	public List getrList() {
		return rList;
	}
	public void setrList(List rList) {
		this.rList = rList;
	}
	@Override
	public String toString() {
		//处理格式
		return  phoneNo + ""+ stringFormat(numberPrecise(lon),2) + ""+ stringFormat(numberPrecise(lat),1) + 
				rList.get(0) +"" + rList.get(1)+""+rList.get(2)+""+rList.get(3) +
				"" + rList.get(4) +"" + rList.get(5)+""+rList.get(6)+""+rList.get(7) +
				"" + rList.get(8) +"" + rList.get(9)+""+rList.get(10)+""+rList.get(11) +
				"" + rList.get(12) +"" + rList.get(13)+""+rList.get(14)+""+rList.get(15) +
				"" + rList.get(16) +"" + rList.get(17)+""+rList.get(18)+""+rList.get(19) +
				"" + rList.get(20) +"" + rList.get(21)+""+rList.get(22)+""+rList.get(23) +
				"" + rList.get(24) +"" + rList.get(25)+""+rList.get(26)+""+rList.get(27) +
				"" + rList.get(28) +"" + rList.get(29)+""+rList.get(30)+""+rList.get(31) +
				"" + rList.get(32) +"" + rList.get(33)+""+rList.get(34)+""+rList.get(35) +
				"" + rList.get(36) +"" + rList.get(37)+""+rList.get(38)+""+rList.get(39)
				+"" + intFormat(throld) + "eee";
	}
	

	public String intFormat(int intValue) {
		return String.format("%05d", intValue);
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
