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
	private static byte[] buf = new byte[2000]; //packet的大小为2000字节
	private static DatagramPacket dp = new DatagramPacket(buf,buf.length);
	private static DatagramSocket socket;
	public static boolean flag = true; //用来判断Server是否已经停止
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
		final int port = PropertiesUtil.getPort();
		try {
			ThreadList threadList = new ThreadList();   //用来处理ThreadServer存放的信息
			threadList.start();
			socket = new DatagramSocket(port);
			while(true) {
				try {
					socket.receive(dp); //接收包
					String recvInfo = new String(dp.getData(),0,dp.getLength()); //将包中内容取出
					ThreadServer threadServer = new ThreadServer(recvInfo);  
					threadServer.start(); 
				} catch (IOException  e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if(socket!=null) {  
				socket.close(); //socket关闭
			}
			flag = false; //标记Server结束，并通知ThreadList终止工作
		}
	}
}
