package com.franklin.test;

import java.sql.Connection;

import com.franklin.db.DbCon;

import junit.framework.TestCase;

public class DbConTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testGetOtherServerConnection() {
		Connection conn = DbCon.getDbConInstance().getOtherServerConnection();
		//Connection conn = DbCon.getDbConInstance().getConnection();
		assertNotNull(conn);
		DbCon.getDbConInstance().closeOtherServerConnection(conn);
	}
}
