package com.franklin.test;

import java.sql.Connection;

import com.franklin.db.DbCon;
import com.franklin.db.DbUtil;

import junit.framework.TestCase;

public class DbConTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		
	}
	public void myTest() {
		String info = "123";
		int k = info.charAt(0)-'0';
		System.out.println("kk : " + k);
	}
	
	public void stGetOtherServerConnection() {
		Connection conn = DbCon.getDbConInstance().getOtherServerConnection();
		//Connection conn = DbCon.getDbConInstance().getConnection();
		assertNotNull(conn);
		DbCon.getDbConInstance().closeOtherServerConnection(conn);
	}
	
	public void testdbTest() {
		String deviceId = "000001";
		Object obj = DbUtil.getDeviceExtraInfo(deviceId);
		assertNotNull(obj);
	}
}
