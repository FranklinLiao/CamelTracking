package com.franklin.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.apache.log4j.Logger;

public class UdpServer extends Thread{
	private static Logger logger = Logger.getLogger(UdpServer.class);
	private int udp_port = 0;
	private static byte[] buf = new byte[2000]; //packet的大小为2000字节
	private static DatagramPacket dp = new DatagramPacket(buf,buf.length);
	private static DatagramSocket socket;
	public static boolean flag = true; //用来判断Server是否已经停止
	
	public UdpServer(int udp_port) {
		this.udp_port = udp_port;
	}
	
	public void run() {
		try {
			UdpThreadList udpThreadList = new UdpThreadList();
			udpThreadList.start();
			socket = new DatagramSocket(udp_port);
			while(true) {
				try {
					socket.receive(dp); //接收包
					String recvInfo = new String(dp.getData(),0,dp.getLength()); //将包中内容取出
					UdpThreadServer udpThreadServer = new UdpThreadServer(recvInfo,dp.getSocketAddress());  
					udpThreadServer.start(); 
					logger.debug("recv a udp packet info:"+recvInfo);
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
			flag = false; //标记Server结束
		}
	}
}
