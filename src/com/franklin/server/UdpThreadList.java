package com.franklin.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.franklin.baidu.BaiDuJsonParser;
import com.franklin.db.ContentExtract;
import com.franklin.db.DbUtil;
import com.franklin.db.PropertiesUtil;
import com.franklin.juhesdk.JsonParser;
import com.franklin.packet.PacketHelper;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.LogFactory;

public class UdpThreadList extends Thread{  //用来处理infoList 将数据解析并插入到数据库
	private static Logger logger = Logger.getLogger(UdpThreadList.class);
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int sleepTime = 2;//2s
		while(UdpServer.flag) {   //判断Server是否停止工作  只要Server还在工作就工作
			if(UdpThreadServer.infoList!=null&&UdpThreadServer.infoList.size()>0) { //判断infoList中是否有数据
				ArrayList<Map<String,SocketAddress>> arraylist = null;
				synchronized(UdpThreadServer.infoList) { //对infoList做同步处理
					arraylist = new ArrayList<Map<String,SocketAddress>>(UdpThreadServer.infoList);//将infoList中数据去除
					UdpThreadServer.infoList.clear(); //清楚infoList
				}
				Iterator<Map<String,SocketAddress>> infoIter = arraylist.iterator();
				while(infoIter.hasNext()) {//便利取出数据
					Map<String,SocketAddress> myMap = infoIter.next();
					if(myMap.size()!=1) {
						continue;
					}
					Set<String> keySet = myMap.keySet();
					String infoString = keySet.iterator().next();
					Collection<SocketAddress> addressSet = myMap.values();
					SocketAddress address = addressSet.iterator().next();
					
					PacketHelper packetHelper = new PacketHelper();
					
					byte[] buffer = new byte[2000];
					//判断包类型
					int type = packetHelper.getPacketType(infoString);
					//String infoString = new String(dp.getData(),0,dp.getLength()); //取出包中数据
					logger.warn("udpthreadlist handle udp packet info:"+infoString);
					switch (type) {
						case 2: { //定位包
							List<String> infoList = new ArrayList<String>();
							if(infoString.contains(",")) {
								String[] infos = infoString.split(",");
								for(String info : infos) {
									infoList.add(info);
								}
							} else {
								infoList.add(infoString);
								
							}
							for(String info : infoList) {
								System.out.println("one info:"+info);
								packetHelper.dealPosData(info);
								
							}
							break;
						}
						default: break;
					}					
				}
				logger.debug("*******************udpthreadlist done!*******************");
				System.out.println("*******************udpthreadlist done!*******************");
			} else {
				try {
					Thread.sleep(1000*sleepTime);
					logger.debug("**************************udpthreadlist wake up!*********");
					System.out.println("**************************udpthreadlist wake up!*********");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
	}
	
	
	

	
}
