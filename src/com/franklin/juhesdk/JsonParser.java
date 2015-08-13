package com.franklin.juhesdk;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.franklin.db.ContentExtract;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;


public class JsonParser {
	//private String apiKey = "b86e5cf5a183bb5aa589c0b8cf353428";
	private String apiKey =   "f04b0cdc79a9c3b57751092ab66ab771";
	public List<String> getBsList(String infoString) {
		List<String> bsInfo = new ArrayList<String>();
		String mnc = null;
		String lac=null;
		String cellId = null;
		String time = null;
		String deviceId = null;
		////MNC:00CELL:63cbLAC:10ddT:00000000000000ID:000001
		//*******************************经度*****************************//
		int firstmaohao = infoString.indexOf(":");
		int mchar = infoString.indexOf("CELL"); // 当没有查找到W 返回-1
		if(firstmaohao>0&&mchar>0) {  
			mnc = infoString.substring(firstmaohao+1,mchar).trim();
			if(mnc.equals("00")) {
				mnc = 0+"";
			} else if(mnc.equals("01")){
				mnc = 1+"";
			} else {
				return null;
			}
			bsInfo.add(mnc);
		} else {
			return null;
		}
		//*******************************纬度*******************************//
		int secondmaohao = infoString.indexOf(":",firstmaohao+1);
		int cchar = infoString.indexOf("LAC",firstmaohao+1);
		if(secondmaohao>0&&cchar>0) {
			cellId = infoString.substring(secondmaohao+1,cchar).trim();
			bsInfo.add(cellId);
		} else {
			return null;
		}
		//******************************时间*******************************//
		int thirdmaohao = infoString.indexOf(":",secondmaohao+1);
		int lchar = infoString.indexOf("T",secondmaohao+1);
		if(thirdmaohao>0&&lchar>0) {
			lac = infoString.substring(thirdmaohao+1,lchar).trim();
			bsInfo.add(lac);
		}  else {
			return null;
		}
		int fourthmaohao = infoString.indexOf(":",thirdmaohao+1);
		int idFlag = infoString.indexOf("I",thirdmaohao+1); 
		if(fourthmaohao>0&&idFlag>0) {
			time = infoString.substring(fourthmaohao+1,idFlag);
			time = new ContentExtract().getModifyTime(time); //utc转北京时间
			bsInfo.add(time);
		} else {
			return null;
		}
		//*******************************设备Id*******************************//
		int fivemaohao=  infoString.indexOf(":",fourthmaohao+1);
		if(fivemaohao>0&&infoString.length()>=(fivemaohao+1+6)) {
			deviceId = infoString.substring(fivemaohao+1,fivemaohao+1+6); //6位设备Id
			bsInfo.add(deviceId);
		} else {
			return null;
		}
		return bsInfo;
	}
	
	public ContentExtract getBsInfo(List<String> bsList) {
		ContentExtract contentExtract = new ContentExtract();
		List<String> gpsInfo = new ArrayList<String>();
		String url = "http://v.juhe.cn/cell/get";
		if(bsList!=null&&5==bsList.size()) {
			String mnc = bsList.get(0);
			String cellid = bsList.get(1);
			String lac = bsList.get(2);
			String key = apiKey;
			String queryString = new StringBuffer("?").append("mnc=").append(mnc).
					append("&lac=").append(lac).append("&cell=").append(cellid).
					append("&key=").append(key).append("&hex=16").toString();
			url = new StringBuffer(url).append(queryString).toString();
			String charset = "UTF-8";
			String bodyInfo = new MyHttpMethod().doGetMethod(url);
			try {
				if(bodyInfo==null) {
					return contentExtract;
				}
				
				JSONObject object = JSONObject.fromObject(bodyInfo);
				if(object==null) {
					return contentExtract;
				}
				String code = object.getString("error_code");
				if(code.equals("0")) { //正确获得信息
					JSONObject jsonObject = (JSONObject) object.getJSONObject("result");
					JSONObject data = (JSONObject) jsonObject.getJSONArray("data").get(0);
					double lon = Double.valueOf(data.getString("LNG")); 
					double lat = Double.valueOf(data.getString("LAT"));  
					String lonString = MyNumberFormat.numberPrecise(lon); //精度控制
					String latString = MyNumberFormat.numberPrecise(lat); //精度控制
					if(lonString.startsWith("-")||latString.startsWith("-")) {
						return contentExtract;
					}
 					contentExtract.setLonString(lonString);
 					contentExtract.setLatString(latString);
 					contentExtract.setTime(bsList.get(3));
 					contentExtract.setDeviceID(bsList.get(4));
 					contentExtract.setStatus(1);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return contentExtract;
	}
}
