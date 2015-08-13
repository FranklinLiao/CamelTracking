package com.franklin.baidu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.franklin.db.ContentExtract;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;


public class BaiDuJsonParser {
	private String apiKey = "2WQwNtr5W7P7OtQhRIuuZOTG";
	
	
	public ContentExtract getBaiDuPositionInfo(ContentExtract oldContentExtract) {
		ContentExtract newContentExtract = oldContentExtract;
		List<String> gpsInfo = new ArrayList<String>();
		String url = "http://api.map.baidu.com/geoconv/v1/";
		if(oldContentExtract!=null&&oldContentExtract.getLatString()!=null&&oldContentExtract.getLonString()!=null) {
			String latString = newContentExtract.getLatString();
			String lonString = newContentExtract.getLonString();
			String key = apiKey;
			String from = 1+"";
			String to = 5+"";
			String queryString = new StringBuffer("?").append("coords=").append(lonString).
					append(",").append(latString).append("&from=").append(from).
					append("&to=").append(to).append("&ak=").append(key).toString();
			url = new StringBuffer(url).append(queryString).toString();
			String charset = "UTF-8";
			String bodyInfo = new BaiDuMyHttpMethod().doGetMethod(url);
			try {
				if(bodyInfo==null) {
					return newContentExtract;
				}
				JSONObject object = JSONObject.fromObject(bodyInfo);
				if(object==null) {
					return newContentExtract;
				}
				String code = object.getString("status");
				if(code.equals("0")) { //正确获得信息
					JSONObject data = (JSONObject) object.getJSONArray("result").get(0);
					double lon = Double.valueOf(data.getString("x")); 
					double lat = Double.valueOf(data.getString("y"));  
					lonString = BaiDuMyNumberFormat.numberPrecise(lon); //精度控制
					latString = BaiDuMyNumberFormat.numberPrecise(lat); //精度控制
					newContentExtract.setLonString(lonString);
					newContentExtract.setLatString(latString);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return newContentExtract;
	}
}
