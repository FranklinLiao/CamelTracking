package com.franklin.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbCon {
	private static String urlString = "";//"jdbc:mysql://705.haoyoubang.com:3306/camelGPSsystem?characterEncoding=utf8";
	private static String userString = "";//"root";
	private static String passwdString = "";//"1234";
	private static DbCon dbCon = null;
	//private String urlStringOtherServer="jdbc:mysql://118.26.157.164:3306/animal1?characterEncoding=utf8";
	//private String passwdStringOtherServer = "pass4you";
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("数据库驱动失败");
		}
		urlString = PropertiesUtil.getUrlString();
		userString = PropertiesUtil.getUsername();
		passwdString = PropertiesUtil.getPassword();
	}
	
	public static DbCon getDbConInstance() {
		if(dbCon==null) {
			dbCon = new DbCon();
		}
		return dbCon;
	}
	
	public Connection getConnection() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(urlString,userString,passwdString);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connection;
	}
	/*	
	public Connection getOtherConnection() {
		try {
			connection = DriverManager.getConnection(urlStringOtherServer,userString,passwdStringOtherServer);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//System.out.println(e.getMessage());
			System.out.println("服务端数据库连接失败");
		}
		return connection;
	}
	*/
	public  void closeConnection(Connection conn) {
		if(null!=conn) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("数据库连接关闭失败");
			}
		}
	}
	
	public static String getUrlString() {
		return urlString;
	}
}
