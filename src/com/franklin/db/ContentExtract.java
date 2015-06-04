package com.franklin.db;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
/*
 * ��Ҫ���ܣ��Ѵ�Ӳ���ն˴��͵���ݷֽ�ɾ��ȡ�γ�ȡ�ʱ�䡢�绰����
 */
public class ContentExtract {
	private String infoString = null;
	private String deviceID = null;
	private String lonString = null;
	private String latString = null;
	private String time = null;
	private static double lat0=0.0; 
	private static double lng0=0.0;
	
	static {   //类被加载时执行
		lat0 = PropertiesUtil.getLat0(); 
		lng0 = PropertiesUtil.getLng0();
	}
	
	public ContentExtract(String infoString) {
		this.infoString = infoString;
	}

	public void infoParser() {    //解析数据
		//*******************************经度*****************************//
		int firstmaohao = infoString.indexOf(":");
		int wchar = infoString.indexOf("W"); // 当没有查找到W 返回-1
		if(firstmaohao>0&&wchar>0) {  
			lonString = infoString.substring(firstmaohao+1,wchar);
			lonString = numFormater(lonString); 
			double tmplng = Double.parseDouble(lonString);
			if(tmplng>0) { //只有有效的经度才加上偏移量
				tmplng += lng0; 
			}
			BigDecimal bDec1 = new BigDecimal(tmplng);
			bDec1 = bDec1.setScale(6, BigDecimal.ROUND_DOWN);
			lonString = Double.toString(bDec1.doubleValue());
		}
		//*******************************纬度*******************************//
		int secondmaohao = infoString.indexOf(":",firstmaohao+1);
		int tchar = infoString.indexOf("T",firstmaohao+1);
		if(secondmaohao>0&&tchar>0) {
			latString = infoString.substring(secondmaohao+1,tchar);
			latString = numFormater(latString);
			double tmplat = Double.parseDouble(latString);
			if(tmplat>0) {  //只有有效的纬度才加上偏移量
				tmplat += lat0;
			}
			BigDecimal bDec = new BigDecimal(tmplat);
			bDec = bDec.setScale(6, BigDecimal.ROUND_DOWN);
			latString = Double.toString(bDec.doubleValue());
		}
		//******************************时间*******************************//
		int thirdmaohao = infoString.indexOf(":",secondmaohao+1);
		int fourthmaohao = infoString.indexOf(":",thirdmaohao+1);
		int idFlag = infoString.indexOf("I",thirdmaohao+1); 
		if(thirdmaohao>0&&idFlag>0) {
			time = infoString.substring(thirdmaohao+1,idFlag);
			time = getModifyTime(time); //utc转北京时间
		}
		//*******************************设备Id*******************************//
		if(fourthmaohao>0) {
			deviceID = infoString.substring(fourthmaohao+1,fourthmaohao+1+6); //6位设备Id
		}
	}

	public String numFormater(String gpsValue) {  	//将GPS特定的格式转换为一般的经纬度   分/60
		int minLength=7;
		String tmpMinute=gpsValue.substring(gpsValue.length()-minLength, gpsValue.length());				
		double realMinute = Double.parseDouble(tmpMinute);
		realMinute = realMinute / 60.0;
		String tmpDegree=gpsValue.substring(0, gpsValue.length()-minLength);
		double realDegree=Double.parseDouble(tmpDegree);
		double realData = realMinute + realDegree;			
		BigDecimal bDecimal = new BigDecimal(realData);//控制经度
		bDecimal = bDecimal.setScale(6, BigDecimal.ROUND_DOWN);
		String infoValue = Double.toString(bDecimal.doubleValue());
		return infoValue;
	}
	
	public String getModifyTime(String oldTime) { 
		String nowTime = oldTime;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); //string转为date
		try {
			Date date = sdf.parse(oldTime);
			date.setHours(date.getHours() + 8); //utc比北京时间小8个小时
			nowTime = sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			return nowTime;
		}
	}
	
	public String getInfoString() {
		return infoString;
	}

	public String getdeviceID() {
		return deviceID;
	}

	public String getLonString() {
		return lonString;
	}

	public String getLatString() {
		return latString;
	}

	public String getTime() {
		return time;
	}
}
