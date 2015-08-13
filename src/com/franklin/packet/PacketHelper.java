package com.franklin.packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.franklin.baidu.BaiDuJsonParser;
import com.franklin.db.ContentExtract;
import com.franklin.db.DbUtil;
import com.franklin.db.PropertiesUtil;
import com.franklin.domain.DeviceExtraInfoObject;
import com.franklin.domain.PolygonBoundryObject;
import com.franklin.domain.SectorBoundryObject;
import com.franklin.juhesdk.JsonParser;
import com.franklin.server.ThreadList;

public class PacketHelper {
	private static Logger logger = Logger.getLogger(PacketHelper.class);
	private static Logger loggerTime = Logger.getLogger("lasttime");
	public int getPacketType(String client) { //1:初始化包  2:定位包  3:确认包
		int type = 0;
		List<String>infoList = new ArrayList<String>();
		//String client = new String(dp.getData(),0,dp.getLength());
		//取出数据包中内容
		if(client!=null&&client!="") {
			if((client.startsWith("J") || client.startsWith("M"))) {
				type = 2;
			} else if(client.startsWith("bbb01")) {
				type = 1;
			} else if(client.startsWith("ACK01")) {	
				type = 3;
			}
		}
		return type;
	}
	
	public List<String> getPosData(DatagramPacket dp) {
		List<String>infoList = new ArrayList<String>();
		String client = new String(dp.getData(),0,dp.getLength());
		//取出数据包中内容
		if(client!=null&&client!="") {
			if((client.startsWith("J") || client.startsWith("M"))) { //是定位数据
				if(client.contains(",")) {
					String[] infoStrings = client.split(",");
					for(int i = infoStrings.length-1;i>=0;i--) {
						infoList.add(infoStrings[i]);
					}
				} else {	
					infoList.add(client);
				}
			} 
		}
		return infoList;
	}
	
	public boolean dealPosData(String info) { //对定位数据包的处理
		boolean dealFlag = false;
		ContentExtract contentExtract = null;
		boolean existFlag = true;
		if(info.startsWith("J")) { //gps定位
			contentExtract = new ContentExtract(info);
			contentExtract.infoParser(); //解析数据并把数据放入到contentExtract
		} else if(info.startsWith("M")){//基站定位		
			JsonParser json = new JsonParser();
			List<String> bsList = json.getBsList(info);
			contentExtract = json.getBsInfo(bsList);
			//将contentExtract作为参数  并作为返回值
			if(contentExtract!=null&&contentExtract.getLatString()!=null&&!(contentExtract.getLatString().startsWith("0"))) {
				contentExtract = new BaiDuJsonParser().getBaiDuPositionInfo(contentExtract);
			}
		} else {
			return dealFlag;
		}
		if(contentExtract!=null && contentExtract.getDeviceID()!=null && !contentExtract.getDeviceID().equals("000870") 
				&& !contentExtract.getDeviceID().equals("000863") )
		{
			long startTime=System.currentTimeMillis();   //获取开始时间
			existFlag = DbUtil.insert(contentExtract);
			long endTime=System.currentTimeMillis();   //获取开始时间
			long lastTime = endTime-startTime;
			if(!existFlag) {
				loggerTime.warn("insert db last "+lastTime+"ms");
			}
		}
		//将数据通过tcp发送给贾博士
		//existFlag = true; //测试使用
		if(!existFlag) {	//之前没有这些数据
			String SiChuanLat=contentExtract.getLatString().toString();
			double SiChuanLat0 = Double.parseDouble(SiChuanLat);		
			if(SiChuanLat0>=35.0) {
				int i = 5; //不成功最多发送次数
				long sendStartTime=System.currentTimeMillis();   
				boolean recvFlag = false;
				do {
					recvFlag = sendData(contentExtract);
				} while(!recvFlag&&(i-->0)); //尝试10次
				long sendEndTime=System.currentTimeMillis();   //获取开始时间
				long sendLastTime = sendEndTime-sendStartTime;
				loggerTime.warn("send data last "+sendLastTime+"ms");
			}	
			
		}
		dealFlag = true;
		return dealFlag;
	}

	public boolean sendData(ContentExtract contentExtract) {
		Socket socket = null;
		PrintWriter os = null;
		InputStreamReader is  = null;
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
			os.println(info);
			os.flush();
			recvTemp=is.read();
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
			if(socket!=null) {
				socket.close();
			}
		} catch(SocketTimeoutException e) {
			System.out.println("socekt time out!");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return recvFlag;
	}
	
	public String getInfoString(ContentExtract contentExtract) {
		String deviceId = contentExtract.getDeviceID();
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
	
	public String dealInitPacket(String info) {
		String infoString = "bbb01";
		if(info.length()>=16) {
			String deviceId = info.substring(5, 11);
			String phoneNo = DbUtil.getPhoneNo(deviceId);
			if(phoneNo==null) {
				return null;
			}
			String battery = info.substring(11,13);
			int batteryInt = Integer.parseInt(battery);
			//update battery
			DbUtil.updateBatteryStatus(batteryInt, deviceId);
			//报警+唤醒+围栏
			//得到deviceId的报警+唤醒信息
			DeviceExtraInfoObject deviceObj = DbUtil.getDeviceExtraInfo(deviceId); //得到设备警报和定位时间的是否发送的信息
			if(deviceObj==null) {
				return null;
			}
			
			DeviceExtraInfoObject obj = DbUtil.getUserExtraInfo(phoneNo); //得到用户设定的报警和定位时间
			if(obj==null) {
				return null;
			}
			//把deviceObj和obj的数据统一到obj中，方便组装发送的信息  deviceObj中有是否发送的标记  obj有发送的信息
			obj.setIsAlarmSend(deviceObj.getIsAlarmSend()); //警报
			obj.setIsDataSend(deviceObj.getIsDataSend()); //定位时间
			
			int isFenceSend = DbUtil.getDeviceFenceStatus(deviceId);
			if(isFenceSend>0) {
				//do nothing  没有围栏需要发送
				infoString+="0";
				infoString += obj.toString();//报警+唤醒
			} else {
				//扇形
				SectorBoundryObject sector = DbUtil.getSectorBoundry(phoneNo);
				if(sector!=null) {
					infoString += "1";
					infoString += obj.toString(); //报警+唤醒
					infoString += sector.toString(); //拼接扇形信息
				} else {
					PolygonBoundryObject polygon = DbUtil.getPolyonBoundry(phoneNo);
					if(polygon!=null) {
						infoString += "2";
						infoString += obj.toString();//报警+唤醒
						infoString += polygon.toString(); //拼接多边形
					} else {
						//do nothing  进入这个部分 说明数据有问题或连接到数据库有问题
					}
				}
			}
			return infoString;
		} else {
			return null;
		}
	}
	
	public void dealAckPacket(String infoString) {
		if(infoString.length()>=17) {
			if(infoString.contains("eee")) {
				String deviceId = infoString.substring(8,14);
				//int deviceIdInt = Integer.parseInt(deviceId);
				String phoneNo = DbUtil.getPhoneNo(deviceId);
				if(phoneNo==null) {
					return;
				}
				//更新设备的信息
				int alarmFlag = infoString.charAt(5)-'0';
				int hourFlag = infoString.charAt(6)-'0';
				if(alarmFlag!=0 && alarmFlag !=1 && alarmFlag!=9) { //不是约定的数据 就之后再发送
					alarmFlag = 0;
				} else {
					alarmFlag = 1;
				}
				
				if(hourFlag!=0 && hourFlag !=1) { //不是约定的数据 就之后重新发送
					hourFlag = 0;
				} else {
					hourFlag = 1;
				}
				
				
				DbUtil.updateUserExtraInfo(alarmFlag,hourFlag,phoneNo);
				//围栏是否发送
				int fenceFlag = infoString.charAt(7)-'0';
				if(fenceFlag>0) { //收到了某种围栏
					DbUtil.updateIsFenceSend(deviceId);
				}
			
			}
		}
	}
}
