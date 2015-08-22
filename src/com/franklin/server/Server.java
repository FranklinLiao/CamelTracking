package com.franklin.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Properties;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


import com.franklin.db.PropertiesUtil;
import com.franklin.db.DbUtil;

public class Server {
	@SuppressWarnings("deprecation")
	public static void main(String args[])  {
		Properties props = new Properties();
		try {
			props.load(Server.class.getResourceAsStream("/config/log4j.properties"));
			PropertyConfigurator.configure(props); // 
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		int udp_port = PropertiesUtil.getUdp_port();
		int tcp_port = PropertiesUtil.getTcp_port();

		TcpServer tcpServer = new TcpServer(tcp_port);
		tcpServer.start();		
		UdpServer udpServer = new UdpServer(udp_port);   //用来处理ThreadServer存放的信息
		udpServer.start();
		System.out.println("tcp && udp starts!");
	}
}
