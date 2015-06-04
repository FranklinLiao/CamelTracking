package com.franklin.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbCon {
	private static String urlString = "";//"jdbc:mysql://705.haoyoubang.com:3306/camelGPSsystem?characterEncoding=utf8";
	private static String userString = "";//"root";
	private static String passwdString = "";//"1234";
	//otherServer 贾博士
	private static String otherServerUrlString = "";
	private static String otherServerUserString = "";
	private static String otherServerPasswdString = "";
	
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
		//otherServer贾博士
		otherServerUrlString = PropertiesUtil.getOtherServerUrlString();
		otherServerUserString = PropertiesUtil.getOtherServerUsername();
		otherServerPasswdString = PropertiesUtil.getOtherServerPassword();
		//连接3s没连上就断开
		DriverManager.setLoginTimeout(3);
	}
	
	public static DbCon getDbConInstance() {
		if(dbCon==null) {
			dbCon = new DbCon();
		}
		return dbCon;
	}
	/*
	public Connection getConnection() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(urlString,userString,passwdString);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("此次数据库连接获取失败");
		}
		return connection;
	}
	*/
	public Connection getOtherServerConnection() {
		Connection otherServerConnection = null;
		try {
			otherServerConnection = DriverManager.getConnection(otherServerUrlString,otherServerUserString,otherServerPasswdString);
			//System.out.println(otherServerUrlString+","+otherServerUserString+","+otherServerPasswdString);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("OtherServer数据库连接获取失败");
		}
		return otherServerConnection;
	}
	
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
	
	public void closeOtherServerConnection(Connection otherServerConn) {
		if(null!=otherServerConn) {
			try {
				otherServerConn.close();
				otherServerConn = null;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("otherServer数据库连接关闭失败");
			}
		}
	}
}
