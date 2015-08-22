package com.franklin.db;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
	private final static String propFile = "/config/prop.properties"; //存放配置信息的文件
	private static String urlString="";
	private static String username="";
	private static String password="";
	private static String otherServerUrlString=""; //用于贾博士
	private static String otherServerUsername="";
	private static String otherServerPassword="";
	//tcp连接信息   贾博士
	private static String otherServerString="";
	private static int otherServerPort=0;
	
	private static int tcp_port=0;
	private static int udp_port=0;
	private static double lat0 =0;
	private static double lng0 = 0;
	private static int minutes = 0;
	private static double newlat0 = 0;
	private static double newlng0 = 0;

	static {   
        Properties prop = new Properties();  
        InputStream in = PropertiesUtil.class
			     .getResourceAsStream(propFile);
        try {   
            prop.load(in);   
            urlString = prop.getProperty("url");
            username=prop.getProperty("username");
            password=prop.getProperty("password");
            //otherServer --贾博士
            otherServerUrlString = prop.getProperty("otherServerUrl").trim();
            otherServerUsername = prop.getProperty("otherServerUsername").trim();
            otherServerPassword = prop.getProperty("otherServerPassword").trim();
            //tcp  贾博士
            otherServerString = prop.getProperty("otherServer").trim();
            otherServerPort = Integer.parseInt(prop.getProperty("otherServerPort").trim());
            
            tcp_port = Integer.parseInt(prop.getProperty("tcp_port").trim());
            udp_port=Integer.parseInt(prop.getProperty("udp_port").trim());
            lat0 = Double.parseDouble(prop.getProperty("lat0").trim());
            lng0 = Double.parseDouble(prop.getProperty("lng0").trim());
            newlat0 = Double.parseDouble(prop.getProperty("newlat0").trim());
            newlng0 = Double.parseDouble(prop.getProperty("newlng0").trim());
        	minutes = Integer.parseInt(prop.getProperty("minutes").trim());
        } catch (IOException e) {   
            e.printStackTrace();   
        }   
    }
	
	public static int getTcp_port() {
		return tcp_port;
	}
	public static int getUdp_port() {
		return udp_port;
	}
	public static int getMinutes() {
		return minutes;
	}
	public static double getLat0() {
		return lat0;
	}
	public static double getLng0() {
		return lng0;
	}
	public static String getUrlString() {
		return urlString;
	}
	public static String getUsername() {
		return username;
	}
	public static String getPassword() {
		return password;
	}
	public static String getOtherServerUrlString() {
		return otherServerUrlString;
	}
	public static String getOtherServerUsername() {
		return otherServerUsername;
	}
	public static String getOtherServerPassword() {
		return otherServerPassword;
	}
	public static String getOtherServerString() {
		return otherServerString;
	}
	public static int getOtherServerPort() {
		return otherServerPort;
	}
	public static double getNewlat0() {
		return newlat0;
	}
	public static double getNewlng0() {
		return newlng0;
	}
	
	
}
