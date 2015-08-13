package com.franklin.test;

import java.util.List;

import com.franklin.baidu.BaiDuJsonParser;
import com.franklin.db.ContentExtract;
import com.franklin.juhesdk.JsonParser;

import junit.framework.TestCase;

public class baiduTest {
	
	public static  void main(String args[]) {
	//	String info = "J:11620.6520W:3958.7004T:20150428082710ID:000071";
		//String info = "J:11620.5296W:3958.7151T:20150711130000ID:000556";
		
		//String info = "J:11620.7427W:3958.6913T:20150711130323ID:001786";
		 
		//String info = "J:11620.6790W:3958.6960T:20150712090136ID:001786";
		//String info = "J:11620.6538W:3958.7355T:20150712085729ID:000556";
		String info = "MNC:44CELL:16,0LAC:255\"T:00000000000000ID:000556";
		ContentExtract contentExtract = new ContentExtract(info);
		com.franklin.juhesdk.JsonParser json = new com.franklin.juhesdk.JsonParser();
		List<String> bsList = json.getBsList(info);
		contentExtract = json.getBsInfo(bsList);
		//将contentExtract作为参数  并作为返回值
		if(!(contentExtract.getLatString().startsWith("0"))) {
			//contentExtract = new JsonParser().getBsInfo(contentExtract);
		}
		//contentExtract.infoParser();
		//new JsonParser().getBsInfo(contentExtract);
		
	}
	
}
