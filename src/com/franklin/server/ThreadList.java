package com.franklin.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.franklin.db.ContentExtract;
import com.franklin.db.DbUtil;
import com.franklin.db.PropertiesUtil;
import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.LogFactory;

public class ThreadList extends Thread{  //用来处理infoList 将数据解析并插入到数据库
	private static Logger logger = Logger.getLogger(ThreadList.class);
	private static Logger loggerTime = Logger.getLogger("lasttime");
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int sleepTime = 2;//2s
		while(Server.flag) {   //判断Server是否停止工作  只要Server还在工作就工作
			if(ThreadServer.infoList!=null&&ThreadServer.infoList.size()>0) { //判断infoList中是否有数据
				ArrayList<String> arraylist = null;
				synchronized(ThreadServer.infoList) { //对infoList做同步处理
					arraylist = new ArrayList<String>(ThreadServer.infoList);//将infoList中数据去除
					ThreadServer.infoList.clear(); //清楚infoList
				}
				Iterator<String> infoIter = arraylist.iterator();
				while(infoIter.hasNext()) {//便利取出数据
					//long parseStartTime=System.currentTimeMillis(); 
					String info = infoIter.next();
					ContentExtract contentExtract = new ContentExtract(info);
					contentExtract.infoParser(); //解析数据并把数据放入到contentExtract
					//long parseEndTime=System.currentTimeMillis();   //获取开始时间
					//long parseLastTime = parseEndTime-parseStartTime;
					//loggerTime.warn("parse last "+parseLastTime+"ms");
					//DbUtil.insertOtherServer(contentExtract); //注意 得先插入贾博士数据库  原因见DbUtil
					long startTime=System.currentTimeMillis();   //获取开始时间
					boolean existFlag = DbUtil.insert(contentExtract);
					long endTime=System.currentTimeMillis();   //获取开始时间
					long lastTime = endTime-startTime;
					if(!existFlag) {
						loggerTime.warn("insert db last "+lastTime+"ms");
					}
					//将数据通过tcp发送给贾博士
					//double lat = Double.parseDouble(contentExtract.getLonString());
					//if(lat>60) { //以60为分界  大于这个数值那么就肯定有效  无效一般为0.012
					if(!existFlag) { //之前没有这些数据
						int i = 5; //不成功最多发送次数
						long sendStartTime=System.currentTimeMillis();   
						boolean recvFlag = false;
						do {
							//recvFlag = sendData(contentExtract);
						} while(!recvFlag&&(i-->0)); //尝试10次
						long sendEndTime=System.currentTimeMillis();   //获取开始时间
						long sendLastTime = sendEndTime-sendStartTime;
						loggerTime.warn("send data last "+sendLastTime+"ms");
					}
					//}
					//System.out.println("*handle one message!***");
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
	
	public boolean sendData(ContentExtract contentExtract) {
		Socket socket = null;
		PrintWriter os = null;
		InputStreamReader is  = null;
		//BufferedReader br = null;
		String otherServerIp = PropertiesUtil.getOtherServerString();
		int otherServerPort = PropertiesUtil.getOtherServerPort();
		int recvTemp = 0;
		boolean recvFlag = false;
		try {
			String info = getInfoString(contentExtract);
			socket = new Socket(otherServerIp,otherServerPort);
			socket.setSoTimeout(5000); //read最多读5s
			os = new PrintWriter(socket.getOutputStream());
			InputStream isStream = socket.getInputStream();
			is = new InputStreamReader(isStream);
			//br = new BufferedReader(new InputStreamReader(isStream));
			os.println(info);
			os.flush();
			recvTemp=is.read();
			//String recvString = br.readLine();
			//recvTemp =recvString.charAt(0);
			System.out.println("recv:"+(char)recvTemp);
			if((recvTemp == 'S')||(recvTemp=='s')) {  //大小写不敏感
				recvFlag = true;
				logger.warn("send a message!--:"+info);
			} else {
				recvFlag = false;
			}
		
			if(os!=null) {
				os.close();
			}
			if(is!=null) {
				is.close();
			}
			
			/*
			if(br!=null) {
				br.close();
			}
			*/
			if(socket!=null)
				socket.close();
		} catch(SocketTimeoutException e) {
			System.out.println("socekt time out!");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return recvFlag;
	}
	
	public String getInfoString(ContentExtract contentExtract) {
		String deviceId = contentExtract.getdeviceID();
		String lonString = contentExtract.getLonString();
		String latString = contentExtract.getLatString();
		String time = contentExtract.getTime();
		if(deviceId!=null&&lonString!=null&&latString!=null&&time!=null) {
			if(time.startsWith("0")) { //time是以0开始的
				SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");  
		        Date date = new Date();  
		        time = f.format(date); //得到当前时间
			}
			//去除前导0
			int deviceIdInt = Integer.parseInt(deviceId);
			deviceId = String.valueOf(deviceIdInt);
		}
		//J:11100.0000W:4000.0000T:"+"00000000000000"+"ID:030304
		return "J:"+lonString+"W:"+latString+"T:"+time+"ID:"+deviceId;
	}
	
}
