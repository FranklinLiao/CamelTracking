package com.franklin.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class TcpThreadServer extends Thread{
	private static Socket socket;
	private static Logger logger = Logger.getLogger(TcpThreadServer.class);
	public static List<Socket> infoList = Collections.synchronizedList(new ArrayList<Socket>()); //用来存放线程生成的sql信息
	public TcpThreadServer(Socket socket) {
		this.socket = socket;
	}
	@Override
	public void run() {
		infoList.add(socket);
		logger.debug("tcpthreadserver add a tcpsocket");
	}
}
