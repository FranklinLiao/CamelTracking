package com.franklin.test;

import com.franklin.db.PropertiesUtil;

import junit.framework.TestCase;

public class PropertiesUtilTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void tetGetLat0() {
		double lat0 = PropertiesUtil.getLat0();
		assertEquals(0.0068,lat0);
	}

	public void tetGetLng0() {
		double lng0 = PropertiesUtil.getLng0();
		assertEquals(0.0120,lng0);
	}

	public void tetGetPort() {
		int port = PropertiesUtil.getPort();
		assertEquals(7576,port);
	}

	public void tetGetMinutes() {
		int minutes = PropertiesUtil.getMinutes();
		assertEquals(5,minutes);
	}

	public void tetGetUrl() {
		String url = PropertiesUtil.getUrlString();
		assertEquals("jdbc:mysql://localhost:3306/camelgpssystem",url);
	}
	
	public void tetGetOtherServerUrl() {
		String otherServerurl = PropertiesUtil.getOtherServerUrlString();
		System.out.print(otherServerurl);
	}
	
	public void tetGetOtherServerPasswd() {
		int otherServerPasswd = PropertiesUtil.getOtherServerPort();
		assertEquals(7878,otherServerPasswd);
	}
	
	public void tetGetOtherServerString() {
		int otherServerPort = PropertiesUtil.getOtherServerPort();
		System.out.print(otherServerPort);
		assertEquals(7878,otherServerPort);
	}
	
	public void testDeviceId() {
		String deviceId = "000065";
		int deviceIdInt = Integer.parseInt(deviceId);
		deviceId =  String.valueOf(deviceIdInt);
		System.out.println(deviceId);
	}
}
