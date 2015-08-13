package com.franklin.db;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.franklin.baidu.BaiDuJsonParser;
/*
 * ��Ҫ���ܣ��Ѵ�Ӳ���ն˴��͵���ݷֽ�ɾ��ȡ�γ�ȡ�ʱ�䡢�绰����
 */
public class ContentExtract {
	private String infoString = null;
	private String deviceID = null;
	private int status = 0;//默认是精确的   0表示精确  1表示不精确
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setLonString(String lonString) {
		this.lonString = lonString;
	}

	public void setLatString(String latString) {
		this.latString = latString;
	}

	public void setTime(String time) {
		this.time = time;
	}

	private String lonString = null;
	private String latString = null;
	private String time = null;
	private static double lat0=0.0; 
	private static double lng0=0.0;
	
	static {   //类被加载时执行
		lat0 = PropertiesUtil.getLat0(); 
		lng0 = PropertiesUtil.getLng0();
	}
	public ContentExtract() {
		
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
			if(lonString==null) {
				return;
			}
			double tmplng = Double.parseDouble(lonString);
			//if(tmplng>0) { //只有有效的经度才加上偏移量
				tmplng += lng0; 
			//}
			if(tmplng<0 || tmplng>180)	
			{
				return;
			}
			
			BigDecimal bDec1 = new BigDecimal(tmplng);
			bDec1 = bDec1.setScale(6, BigDecimal.ROUND_HALF_UP );
			lonString = Double.toString(bDec1.doubleValue());
			
		} else {
			return;
		}
		//*******************************纬度*******************************//
		int secondmaohao = infoString.indexOf(":",firstmaohao+1);
		int tchar = infoString.indexOf("T",firstmaohao+1);
		if(secondmaohao>0&&tchar>0) {
			latString = infoString.substring(secondmaohao+1,tchar);
			latString = numFormater(latString);
			if(latString==null) {
				return;
			}
			double tmplat = Double.parseDouble(latString);
			//if(tmplat>0) {  //只有有效的纬度才加上偏移量
				tmplat += lat0;
			//}
				if(tmplat<0 || tmplat>90)
				{
					return;
				}
			BigDecimal bDec = new BigDecimal(tmplat);
			bDec = bDec.setScale(6, BigDecimal.ROUND_HALF_UP );
			latString = Double.toString(bDec.doubleValue());
		} else {
			return;
		}
		//******************************时间*******************************//
		int thirdmaohao = infoString.indexOf(":",secondmaohao+1);
		
		int idFlag = infoString.indexOf("I",thirdmaohao+1); 
		if(thirdmaohao>0&&idFlag>0) {
			time = infoString.substring(thirdmaohao+1,idFlag);
			time = getModifyTime(time); //utc转北京时间
			/*
			if(time==null) {
				return;
			}
			*/
		} else {
			return;
		}
		//*******************************设备Id*******************************//
		int fourthmaohao = infoString.indexOf(":",thirdmaohao+1);
		int infoStringLength = infoString.length();
		if(fourthmaohao>0&&infoString.length()>=(fourthmaohao+1+6)) {
			deviceID = infoString.substring(fourthmaohao+1,fourthmaohao+1+6); //6位设备Id
		} else {
			return;
		}
		//百度地图坐标系转换
		if(!(latString.startsWith("0"))) {  //纬度有效,坐标系转换
			ContentExtract content = new BaiDuJsonParser().getBaiDuPositionInfo(this); //content为坐标转化后的数据
			this.latString = content.latString;
			this.lonString = content.lonString;
			this.time = content.time;
			this.deviceID = content.deviceID;
			this.status = content.status;
		} 
	}

	public String numFormater(String gpsValue) {  	//将GPS特定的格式转换为一般的经纬度   分/60
		int minLength=7;
		String tmpMinute=gpsValue.substring(gpsValue.length()-minLength, gpsValue.length());	
		String infoValue = null;
		try { 
			double realMinute = Double.parseDouble(tmpMinute);
			realMinute = realMinute / 60.0;
			String tmpDegree=gpsValue.substring(0, gpsValue.length()-minLength);
			double realDegree=Double.parseDouble(tmpDegree);
			double realData = realMinute + realDegree;			
			BigDecimal bDecimal = new BigDecimal(realData);//控制经度
			bDecimal = bDecimal.setScale(6, BigDecimal.ROUND_HALF_UP );
			infoValue = Double.toString(bDecimal.doubleValue());
		} catch(NumberFormatException e) {
			e.printStackTrace();
		}
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
	
	/**
	 * 用来调整偏移量      先减去之前的偏移量，然后再加上新的偏移量
	 * @param gpsInfo
	 * @param offsetValue
	 * @param offsetModifyValue
	 * @return
	 */
	public String modifyOffset(String gpsInfo,double offsetValue,double offsetModifyValue) {
		double tmplat = Double.parseDouble(gpsInfo);
		tmplat = tmplat - offsetValue;
		if(tmplat>0) { //判断调整前的经纬度是否为0   不为0 则加上新的偏移量
			tmplat += offsetModifyValue;
		}
		BigDecimal bDec = new BigDecimal(tmplat);
		bDec = bDec.setScale(6, BigDecimal.ROUND_HALF_UP );
		return Double.toString(bDec.doubleValue());
	}
	
	public String getInfoString() {
		return infoString;
	}

	public String getDeviceID() {
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
