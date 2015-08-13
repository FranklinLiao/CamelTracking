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

public class ThreadList extends Thread{  //用来处理infoList 将数据解析并插入到数据库
	private static Logger logger = Logger.getLogger(ThreadList.class);
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int sleepTime = 2;//2s
		while(Server.flag) {   //判断Server是否停止工作  只要Server还在工作就工作
			if(ThreadServer.infoList!=null&&ThreadServer.infoList.size()>0) { //判断infoList中是否有数据
				ArrayList<Map<String,SocketAddress>> arraylist = null;
				synchronized(ThreadServer.infoList) { //对infoList做同步处理
					arraylist = new ArrayList<Map<String,SocketAddress>>(ThreadServer.infoList);//将infoList中数据去除
					ThreadServer.infoList.clear(); //清楚infoList
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
					logger.warn("recv packet:"+infoString);
					switch (type) {
						case 1: { //初始化包
							String sendInfo = packetHelper.dealInitPacket(infoString);
							if(sendInfo!=null) {
								buffer = sendInfo.getBytes();
								try {
									int len = buffer.length;
									DatagramPacket sendPacket = new DatagramPacket(buffer, 0, buffer.length, address);
									DatagramSocket dataSocket = new DatagramSocket();
									dataSocket.send(sendPacket);
									logger.warn("send packet:"+sendInfo);
								} catch (SocketException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							break;
						}
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
								packetHelper.dealPosData(info);
							}
							break;
						}
						case 3: { //收到ACK包
							packetHelper.dealAckPacket(infoString);
							break;
						}
						default: break;
					}					
				}
				System.out.println("********************done!*******************");
			} else {
				try {
					Thread.sleep(1000*sleepTime);
					System.out.println("**************************wake up!*********");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
	}
	
	
	

	
}
