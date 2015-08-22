package com.franklin.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.franklin.db.DbUtil;
import com.franklin.packet.PacketHelper;

public class TcpThreadList extends Thread {
	private static Logger logger = Logger.getLogger(TcpThreadList.class);
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int sleepTime = 2;//2s
		while(TcpServer.flag) {   //判断Server是否停止工作  只要Server还在工作就工作
			if(TcpThreadServer.infoList!=null&&TcpThreadServer.infoList.size()>0) { //判断infoList中是否有数据
				ArrayList<Socket> arraylist = null;
				synchronized(TcpThreadServer.infoList) { //对infoList做同步处理
					arraylist = new ArrayList<Socket>(TcpThreadServer.infoList);//将infoList中数据去除
					TcpThreadServer.infoList.clear(); //清楚infoList
				}
				Iterator<Socket> infoIter = arraylist.iterator();
				while(infoIter.hasNext()) {//便利取出数据
					try {
						Socket socket = infoIter.next();
						socket.setSoTimeout(20 * 1000);
						BufferedReader br =new BufferedReader(new InputStreamReader(socket.getInputStream()));
						PrintWriter pw = new PrintWriter(socket.getOutputStream());
//						StringBuffer sb = new StringBuffer("");  
//			            String infoTemp = "";
//						while((infoTemp = br.readLine())!=null){ 
//							sb.append(infoTemp); 
//			            }
//			          
//			            String infoString = sb.toString();
						
						String infoString = br.readLine();
						//br.close();
						PacketHelper packetHelper = new PacketHelper();
						//判断包类型
						int type = packetHelper.getPacketType(infoString);
						logger.warn("tcpthreadlist handle tcp socket info:"+infoString);
						
//						pw.println("hd");
//						pw.write("/r/n");
//						pw.flush();
						
						
						switch (type) {
							case 1: { //初始化包
								String sendInfo = packetHelper.dealInitPacket(infoString);
								if(sendInfo!=null) {
									pw.write(sendInfo);
									pw.flush();
									
									//pw.close();
									logger.warn("send tcp socket info:"+sendInfo);
									//更改状态
									String deviceId = infoString.substring(5, 11);
									packetHelper.udpateStatus(deviceId);
								}
								break;
							}
//							
//							case 3: { //收到ACK包
//								packetHelper.dealAckPacket(infoString);
//								break;
//							}
							
							default: break;
						}
					
					} catch(SocketException e) {
						e.printStackTrace();
					}catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				logger.debug("********************tcplist done!*******************");
				System.out.println("********************tcplist done!*******************");
			} else {
				try {
					Thread.sleep(1000*sleepTime);
					logger.debug("**************************tcplist wake up!*********");
					System.out.println("**************************tcplist wake up!*********");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
	}
	
}
