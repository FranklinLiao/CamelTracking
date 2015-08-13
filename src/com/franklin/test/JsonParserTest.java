package com.franklin.test;

import java.util.List;

import com.franklin.db.ContentExtract;
import com.franklin.juhesdk.JsonParser;

import junit.framework.TestCase;

public class JsonParserTest extends TestCase {

	public void testJson() {
		//String info = "M:1L:14556C:42T:20150618111111ID:000012";
		//String info = "M:0L:12P:12T:00000000000000ID:000012";//m后面为mnc  l后面为lac  p后面为cellid  t后面为时间   id后面为设备id
		String info = "MNC:00CELL:63cbLAC:10ddT:00000000000000ID:000001";
		JsonParser json = new JsonParser();
		List<String> bsList = json.getBsList(info);
		ContentExtract contentExtract = json.getBsInfo(bsList);
		System.out.println(contentExtract.getLonString()+","+contentExtract.getLatString()+","
				+contentExtract.getTime()+","+contentExtract.getDeviceID()+","+contentExtract.getStatus());
	}

}
