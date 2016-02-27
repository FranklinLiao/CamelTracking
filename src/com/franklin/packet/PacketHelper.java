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
import com.franklin.server.UdpThreadList;

public class PacketHelper {
	private static int SPECIAL_PACKET = 1;
	private static Logger logger = Logger.getLogger(PacketHelper.class);
	private static Logger loggerTime = Logger.getLogger("lasttime");
	public int getPacketType(String client) { //1:初始化包  2:定位包  3:确认包
		int type = 0;
		List<String>infoList = new ArrayList<String>();
		//String client = new String(dp.getData(),0,dp.getLength());
		//取出数据包中内容
		if(client!=null&&client!="") {
			if(client.startsWith("bbb02") || client.startsWith("bbb03")) {
				type = 2;
			} else if(client.startsWith("bbb01")) {
				type = 1;
			} else if(client.startsWith("bbb00")) { //初始化包
				//特殊情况   不管有没有发送过，都发送，发送后把标志位置位
				type = 4;
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
	
	public boolean dealPosData(String info) { //对接收到的数据包的处理
		boolean dealFlag = false;
		ContentExtract contentExtract = null;
		boolean existFlag = true;
		if(!info.contains("bbb") || !info.contains("eee")) { //数据格式不对
			System.out.println("dealFlag:"+dealFlag);
			return dealFlag;
		}
		
		if(info.contains("J")) { //gps定位
			contentExtract = new ContentExtract(info);
			contentExtract.infoParser(); //解析数据并把数据放入到contentExtract
		} else if(info.contains("M")){//基站定位		
			JsonParser json = new JsonParser();
			List<String> bsList = json.getBsList(info);
			contentExtract = json.getBsInfo(bsList);
			//将contentExtract作为参数  并作为返回值
			if(contentExtract!=null&&contentExtract.getLatString()!=null&&!(contentExtract.getLatString().startsWith("0"))) {
				contentExtract = new BaiDuJsonParser().getBaiDuPositionInfo(contentExtract);
			}
		} else if(info.startsWith("bbb04") && info.length()>=15) {
			int alarmFlag = info.charAt(5)-'0';
			String deviceId = info.substring(6,12);
			String telPhone = DbUtil.getPhoneNo(deviceId);
			if(telPhone!=null) {
				//更新该用户的报警
				DbUtil.updateUserAlarmFlag(alarmFlag, telPhone);
				//更新该用户下所有设备的报警发送状态
				DbUtil.updateDeviceAlarmSendFlag(deviceId);
			}
			dealFlag = true;
			return dealFlag; //不管成功与否，都不需要再处理之后的信息
		}else {
			return dealFlag;
		}
		System.out.println("dealFlag:"+dealFlag);
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
			if(SiChuanLat0>=35.0) { //不对设备所在的经纬度做限制
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

	/**
	 * 发送给贾博士
	 * @param contentExtract
	 * @return
	 */
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
	
	/*
	 * 拼装发送给贾博士的数据
	 */
	public String getInfoString(ContentExtract contentExtract) {
		String deviceId = contentExtract.getDeviceID();
		String battery = DbUtil.getBattery(deviceId);//得到该设备的电量
		if(battery==null || battery.length()!=6) { //如果当前没有电量，那么设置为6个0    电量位数为6
			battery = "000000";
		}
		String lonString = contentExtract.getLonString();
		String latString = contentExtract.getLatString();
		String time = contentExtract.getTime();
		String status = String.valueOf(contentExtract.getStatus());
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
		//拼接这一块还需要和贾博士协商，加入电量和状态
		return "J:"+lonString+"W:"+latString+"T:"+time+"ID:"+deviceId+"S:"+status+"B:"+battery;
	}
	
	public String dealInitPacket(String info,int condition) { 
		//condition为1，已经发送了就不发  condition为2，不管有没有发送都发送
		String infoString = "";
		if(info.length()>=20) {
			infoString += "bbb01";
			String deviceId = info.substring(5, 11);
			String phoneNo = DbUtil.getPhoneNo(deviceId);
			if(phoneNo==null || phoneNo.length()!=11) {
				//phoneNo = "00000000000"; //如果没有对应的号码或号码位数不对，那么发送11个0的电话号码
				return null;
			}
			String battery = info.substring(11,17);
			
			//update battery
			DbUtil.updateBatteryStatus(battery, deviceId);
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
			if(isFenceSend>0 && condition==SPECIAL_PACKET) { //只有在已经发送了而且不是（一直发送）的那种特殊情况时 才会不发
				//do nothing  没有围栏需要发送
				infoString+="0";
				infoString += obj.myToString(condition);//报警+唤醒
				infoString += phoneNo;//即便没有围栏也要发送电话号码
				infoString += "eee";
			} else {
				//扇形
				SectorBoundryObject sector = DbUtil.getSectorBoundry(phoneNo);
				if(sector!=null) {
					infoString += "1";
					infoString += obj.myToString(condition); //报警+唤醒
					infoString += phoneNo;//电话号码
					infoString += sector.toString(); //拼接扇形信息
				} else {
					PolygonBoundryObject polygon = DbUtil.getPolyonBoundry(phoneNo);
					if(polygon!=null) {
						infoString += "2";
						infoString += obj.myToString(condition);//报警+唤醒
						infoString += phoneNo;//电话号码
						infoString += polygon.toString(); //拼接多边形
					} else {
						//return null;
						//如果没有围栏，那么也要发 围栏为0 
						infoString+="0";
						infoString += obj.myToString(condition);//报警+唤醒
						infoString += phoneNo;//电话号码
						infoString += "eee";
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
	
	public void udpateStatus(String deviceId) {
		int alarmFlag = 1;
		int hourFlag = 1;
		String phoneNo = DbUtil.getPhoneNo(deviceId);
		DbUtil.updateUserExtraInfo(alarmFlag,hourFlag,deviceId);
		DbUtil.updateIsFenceSend(deviceId);
	}
}
