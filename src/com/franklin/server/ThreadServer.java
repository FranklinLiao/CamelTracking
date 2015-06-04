package com.franklin.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
public class ThreadServer extends Thread{
	private String client = null;
	private static Logger logger = Logger.getLogger(ThreadServer.class);
	
	public static List<String> infoList = Collections.synchronizedList(new ArrayList<String>()); //用来存放线程生成的sql信息
	public ThreadServer(String clientInfo) {
		this.client = clientInfo;
	}
	@Override
	public void run() {//每个线程把信息放入到infoList中
		if(client!=null&&client!="") {
			if(client.contains(",")) {
				String[] infoStrings = client.split(",");
				for(String tempString : infoStrings) {
					infoList.add(tempString);
				}
			}else {	
				infoList.add(client);
			}
		}
		//synchronized(logger) {
		logger.warn("Client: "+client);
		//}
	}
}

