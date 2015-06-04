package com.franklin.test;

import com.franklin.db.ContentExtract;
import com.franklin.server.ThreadList;

import junit.framework.TestCase;

public class ContentExtractTest extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testInfoParser() {
		//String info = "J:11100.0000W:4000.0000T:"+"20151231180301"+"ID:030304";
		String info = "J:0000000000W:000000000T:"+"20151231180301"+"ID:030304";
		ContentExtract content = new ContentExtract(info);
		content.infoParser();
		ThreadList threadList = new ThreadList();   //用来处理ThreadServer存放的信息
		System.out.println(threadList.getInfoString(content));
		//assertEquals("20160101020301",content.getTime());
		//assertEquals("30304",content.getdeviceID());
	}
}
