package com.franklin.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class TcpServer extends Thread{
	private static Logger logger = Logger.getLogger(TcpServer.class);
	private int tcp_port = 0;
	private ServerSocket serverSocket;
	public static boolean flag = true;
	public TcpServer(int tcp_port) {
		this.tcp_port = tcp_port;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			TcpThreadList tcpThreadList = new TcpThreadList();
			tcpThreadList.start();
			serverSocket = new ServerSocket(tcp_port);
			while(true) {
				try {
					Socket recvSocket = serverSocket.accept();
					TcpThreadServer tcpThreadServer = new TcpThreadServer(recvSocket); 
					logger.debug("accept a tcp socket");
					tcpThreadServer.start(); 
				} catch (IOException  e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if(serverSocket!=null) {  
				try {
					serverSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //socket关闭
			}
			flag = false; //标记Server结束
		}
	}
}
