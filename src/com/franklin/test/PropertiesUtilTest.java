package com.franklin.test;

import com.franklin.db.PropertiesUtil;

import junit.framework.TestCase;

public class PropertiesUtilTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testGetLat0() {
		double lat0 = PropertiesUtil.getLat0();
		assertEquals(0.0068,lat0);
	}

	public void testGetLng0() {
		double lng0 = PropertiesUtil.getLng0();
		assertEquals(0.0120,lng0);
	}

	public void testGetPort() {
		int port = PropertiesUtil.getPort();
		assertEquals(7576,port);
	}

	public void testGetMinutes() {
		int minutes = PropertiesUtil.getMinutes();
		assertEquals(5,minutes);
	}

	public void testGetUrl() {
		String url = PropertiesUtil.getUrlString();
		assertEquals("jdbc:mysql://localhost:3306/camelgpssystem",url);
	}
}
