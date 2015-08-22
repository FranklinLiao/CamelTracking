package com.franklin.server;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
public class UdpThreadServer extends Thread{
	private static Logger logger = Logger.getLogger(UdpThreadServer.class);
	private String clientInfo = null;
	private SocketAddress address = null;
	private Map<String,SocketAddress> myMap = new HashMap<String,SocketAddress>();
	public static List<Map<String,SocketAddress>> infoList = Collections.synchronizedList(new ArrayList<Map<String,SocketAddress>>()); //用来存放线程生成的sql信息
	public UdpThreadServer(String clientInfo,SocketAddress address) {
		this.clientInfo = clientInfo;
		this.address = address;
		myMap.put(clientInfo, address);
	}
	@Override
	public void run() {
		infoList.add(myMap);
		logger.debug("udpthreadserver add a udp packet");
	}
}

