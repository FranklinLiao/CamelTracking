package com.franklin.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.crypto.Data;

import org.apache.log4j.Logger;

import com.franklin.domain.DeviceExtraInfoObject;
import com.franklin.domain.PolygonBoundryObject;
import com.franklin.domain.SectorBoundryObject;

public class DbUtil {
	private static PreparedStatement psmtPreparedStatement = null;
	private static double EARTH_RADIUS = 6378.137;//地球半径
	private static String insertSql = "insert into AnimalGPSSystem (DeviceId,lon,lat,date,status) values(?,?,?,?,?);";
	private static String judgeAroundSql = "select count(*) from AnimalGPSSystem where date > ? and date < ? and DeviceId = ?";
	private static String judgeSameSql = "select count(*) from AnimalGPSSystem where date= ? and DeviceId = ?";
	//deviceextrainfo
	private static String selectDeviceExtraInfoSql = "select * from Device where DeviceId = ?";
	private static String selectAroundSql = "select lat,lon from AnimalGPSSystem where date > ? and DeviceId = ? order by date desc limt 2";
	//userextrainfo
	private static String selectUserExtraInfoSql = "select * from User where phoneNum=?";
	//电子围栏 
	private static String getDeviceFenceStatusSql = "select isFenceSend from Device where DeviceId = ?";
	private static String getPhoneNoSql = "select UserPhone from Device where DeviceId = ?";
 	private static String getSectorBoundrySql = "select FenceID,Lat,Lon,BgnAng,EndAng,R,RThres from SectorFence where PhoneNum = ? and UseStatus=1";
 	
	private static String getPolygonBoundrySql = "select * from PolygonFence where PhoneNum = ? and UseStatus=1";
 	//更新信息
	private static String updateUserExtraInfoSql="update Device set isAlarmSend = ?,isDataSend = ? where DeviceId = ?";
	private static String updateBatterySql = "update Device set battery=? where DeviceId = ?";
	private static String udpateIsFenceSend = "update Device set isFenceSend=1 where DeviceId = ?";
	private static String updateUserAlarmFlagSql = "update User set alarmFlag = ? where phoneNum = ?";
	private static String updateDeviceAlarmSendFlagSql = "update Device set isAlarmSend = 0 where UserPhone in (select UserPhone from Device where DeviceId=?)";
	//otherServer 贾博士
	private static String otherServerInsertSql = "insert into t_devicetrajectory (Device,Longitude,Latitude,AddDate) value(?,?,?,?)";
	private static String getBatterySql = "select battery from Device where DeviceId = ?";
	//通过判断自己数据库中是否有重复数据来决定该数据是否插入贾博士和自己的数据库  因此先插贾博士数据库 然后插自己数据库
	private static Logger insertTimeLogger = Logger.getLogger("inserttime");
	private static int minutes = 0;
	private static double newlat0 = 0;
	private static double newlng0 = 0;
	private static double lat0 = 0;
	private static double lng0 = 0;
	static { //在类加载时执行
		minutes = PropertiesUtil.getMinutes();
		newlat0 = PropertiesUtil.getNewlat0();
		newlng0 = PropertiesUtil.getNewlng0();
		lat0 = PropertiesUtil.getLat0();
		lng0 = PropertiesUtil.getLng0();
	}
	
	public static ContentExtract modifyData(ContentExtract contentExtract) {
		ContentExtract newContentExtract = contentExtract;
		if(contentExtract==null) { //为null,就退出
			return contentExtract;
		}
		String deviceId = contentExtract.getDeviceID();
		String lonString = contentExtract.getLonString();
		String latString = contentExtract.getLatString();
		String time = contentExtract.getTime();
		int status = contentExtract.getStatus();
		if(deviceId!=null&&lonString!=null&&latString!=null&&time!=null) {
			//对偏移量进行调整
			lonString = new ContentExtract().modifyOffset(lonString, lng0, newlng0);
			//对偏移量进行调整
			latString = new ContentExtract().modifyOffset(latString, lat0, newlat0);
			if(time.startsWith("0")) { //time是以0开始的
				SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");  
		        Date date = new Date();  
		        time = f.format(date); //得到当前时间
			} 
			newContentExtract.setLatString(latString);
			newContentExtract.setLonString(lonString);
			newContentExtract.setTime(time);
		}
		return contentExtract;
	}
	
	public static boolean insert(ContentExtract contentExtract) { 
		boolean existflag = true;
		//Connection conn = DbCon.getDbConInstance().getConnection();
		Connection conn = DbPool.getInstance().getConnection();
		if(contentExtract==null||conn==null) { //为null,就退出
			return true;
		}
		try {
			//PreparedStatement起始从1开始
			String deviceId = contentExtract.getDeviceID();
			String lonString = contentExtract.getLonString();
			String latString = contentExtract.getLatString();
			String time = contentExtract.getTime();
			int status = contentExtract.getStatus();
			if(deviceId!=null&&lonString!=null&&latString!=null&&time!=null) {
				if(time.startsWith("0")) { //time是以0开始的
					SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");  
			        Date date = new Date();  
			        time = f.format(date); //得到当前时间
			        long existStartTime = System.currentTimeMillis();
			        existflag = existAroundJudge(judgeAroundSql,time,deviceId); //以like进行判断（同一天） 是不是同一条
			        long existEndTime = System.currentTimeMillis();
			       // insertTimeLogger.warn("AroundJudge last "+(existEndTime-existStartTime)+"ms");
				} else {
					long existStartTime = System.currentTimeMillis();
					existflag = existSameJudge(judgeSameSql,time,deviceId); //以time来判断是否是同一条
					long existEndTime = System.currentTimeMillis();
				  //  insertTimeLogger.warn("SameJudge last "+(existEndTime-existStartTime)+"ms");
				}
				//判断数据是否有效  偏移太多的数据抛弃不用
				if(!isValidGps(contentExtract)) {
					existflag = true; //设置为true 抛弃无效数据
				}
				
				//existflag = false; //全部插入
				if(!existflag) { //没有则插入
					psmtPreparedStatement = conn.prepareStatement(insertSql);
					psmtPreparedStatement.setString(1, deviceId); //判断是否是当天的数据 
					psmtPreparedStatement.setString(2, lonString);
					psmtPreparedStatement.setString(3, latString);
					psmtPreparedStatement.setString(4, time);
					psmtPreparedStatement.setInt(5, status);
					long insertStartTime = System.currentTimeMillis();
					psmtPreparedStatement.executeUpdate();
					long insertEndTime = System.currentTimeMillis();
				 //   insertTimeLogger.warn("insert last "+(insertEndTime-insertStartTime)+"ms");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();//释放
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//DbCon.getDbConInstance().closeConnection(conn);
		return existflag;
	}
	/*
	public static void insertOtherServer(ContentExtract contentExtract) { 
		boolean existflag = false;
		Connection conn = DbCon.getDbConInstance().getOtherServerConnection();
	
		if(contentExtract==null||conn==null) { //为null,就退出
			return;
		}
		try {
			//PreparedStatement起始从1开始
			
			//去除0
			String deviceId = contentExtract.getdeviceID();
			int deviceIdInt = Integer.parseInt(deviceId);
			deviceId =  String.valueOf(deviceIdInt);
			String lonString = contentExtract.getLonString();
			String latString = contentExtract.getLatString();
			String time = contentExtract.getTime();
			if(deviceId!=null&&lonString!=null&&latString!=null&&time!=null) {
				if(time.startsWith("0")) { //time是以0开始的
					SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");  
			        Date date = new Date();  
			        time = f.format(date); //得到当前时间
			        existflag = existAroundJudge(judgeAroundSql,time,deviceId); //考虑到贾博士的addDate时间格式判断间隔不便   所以直接判断自己的数据库中有没有再插入  因此得先插入贾博士数据库，再插自己的数据库
				} else {
					existflag = existSameJudge(judgeSameSql,time,deviceId); //以time来判断是否是同一条
				}
				//existflag = false; //全部插入
				if(!existflag) { //没有则插入
					psmtPreparedStatement = conn.prepareStatement(otherServerInsertSql);
					psmtPreparedStatement.setString(1, deviceId); //判断是否是当天的数据 
					psmtPreparedStatement.setString(2, lonString);
					psmtPreparedStatement.setString(3, latString);
					//对时间格式进行处理以匹配贾博士那边的数据库数据格式
					SimpleDateFormat form = new SimpleDateFormat("yyyyMMddHHmmss");
					Date date;
					try {
						date = form.parse(time); //string  to  date 
						form = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						time = form.format(date); //date to 符合格式的date
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					psmtPreparedStatement.setString(4, time);
					psmtPreparedStatement.executeUpdate();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();//释放
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		DbCon.getDbConInstance().closeOtherServerConnection(conn);
	}
	*/
	public static boolean existAroundJudge(String judgeAroundSqlString,String timeString,String deviceId) {
		boolean flag = true;
		ResultSet rs = null;
		String lowerTime = getAroundTime(timeString, 0-minutes); //  -5分钟
		String upperTime = getAroundTime(timeString,minutes);  //5分钟
		//Connection conn = DbCon.getDbConInstance().getConnection();
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null) {
			return false;
		}
		try {
			psmtPreparedStatement = conn.prepareStatement(judgeAroundSqlString);
			psmtPreparedStatement.setString(1, lowerTime); //判断是否是当天的数据 
			psmtPreparedStatement.setString(2, upperTime);
			psmtPreparedStatement.setString(3, deviceId);
			rs = psmtPreparedStatement.executeQuery();
			while(rs.next()) {
				int cnt = rs.getInt(1);
				if(cnt > 0) { //存在
					flag = true;
				} else {
					flag = false;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			if(null!=rs) {
				rs.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		//DbCon.getDbConInstance().closeConnection(conn);
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
	
	public static boolean existSameJudge(String judgeSameSqlString,String timeString,String deviceId) {
		boolean flag = true;
		ResultSet rs = null;
		//Connection conn = DbCon.getDbConInstance().getConnection();
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null) {
			return false;
		}
		try {
			psmtPreparedStatement = conn.prepareStatement(judgeSameSqlString);
			psmtPreparedStatement.setString(1, timeString); //判断是否是当天的数据 
			psmtPreparedStatement.setString(2, deviceId);
			rs = psmtPreparedStatement.executeQuery();
			while(rs.next()) {
				int cnt = rs.getInt(1);
				if(cnt > 0) { //存在
					flag = true;
				} else {
					flag = false;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			if(null!=rs) {
				rs.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	//	DbCon.getDbConInstance().closeConnection(conn);
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
	
	public static String getAroundTime(String nowTime,int minutes) { //得到当前时间的边界时间（用于判断是否这个边界范围内有同一个设备插入了信息）
		String aroundTime = nowTime;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); 
		try {
			Date date = sdf.parse(nowTime);
			date.setMinutes(date.getMinutes() + minutes); //对分进行操作
			aroundTime = sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			return aroundTime;
		}
	}
	
	public static void updateBatteryStatus(String battery,String deviceId) {
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null) {
			return;
		}			
		try {
			psmtPreparedStatement = conn.prepareStatement(updateBatterySql);
			psmtPreparedStatement.setString(1, battery); 
			psmtPreparedStatement.setString(2, deviceId); 
			psmtPreparedStatement.executeUpdate();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();//释放
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void updateIsFenceSend(String deviceId) {
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null) {
			return;
		}			
		try {
			psmtPreparedStatement = conn.prepareStatement(udpateIsFenceSend);
			psmtPreparedStatement.setString(1, deviceId); 
			psmtPreparedStatement.executeUpdate();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();//释放
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void updateDeviceAlarmSendFlag(String deviceId) {
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null) {
			return;
		}			
		try {
			psmtPreparedStatement = conn.prepareStatement(updateDeviceAlarmSendFlagSql);
			psmtPreparedStatement.setString(1, deviceId); 
			psmtPreparedStatement.executeUpdate();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();//释放
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void updateUserExtraInfo(int alarmFlag,int hourFlag,String deviceId) {
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null) {
			return;
		}			
		try {
			psmtPreparedStatement = conn.prepareStatement(updateUserExtraInfoSql);
			psmtPreparedStatement.setInt(1, alarmFlag); 
			psmtPreparedStatement.setInt(2, hourFlag); 
			psmtPreparedStatement.setString(3, deviceId); 
			psmtPreparedStatement.executeUpdate();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();//释放
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void updateUserAlarmFlag(int alarmFlag,String userPhone) {
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null) {
			return;
		}			
		try {
			psmtPreparedStatement = conn.prepareStatement(updateUserAlarmFlagSql);
			psmtPreparedStatement.setInt(1, alarmFlag); 
			psmtPreparedStatement.setString(2, userPhone); 
			psmtPreparedStatement.executeUpdate();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();//释放
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getPhoneNo(String deviceId) {
		String phoneNo = null;
		ResultSet rs = null;
		//Connection conn = DbCon.getDbConInstance().getConnection();
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null) {
			return null;
		}
		try {
			psmtPreparedStatement = conn.prepareStatement(getPhoneNoSql);
			psmtPreparedStatement.setString(1, deviceId); 
			rs = psmtPreparedStatement.executeQuery();
			while(rs.next()) {
				phoneNo = rs.getString(1);
				break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			if(null!=rs) {
				rs.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	//	DbCon.getDbConInstance().closeConnection(conn);
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return phoneNo;
	}
	
	public static String getBattery(String deviceId) {
		String battery = null;
		ResultSet rs = null;
		//Connection conn = DbCon.getDbConInstance().getConnection();
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null) {
			return null;
		}
		try {
			psmtPreparedStatement = conn.prepareStatement(getBatterySql);
			psmtPreparedStatement.setString(1, deviceId); 
			rs = psmtPreparedStatement.executeQuery();
			while(rs.next()) {
				battery = rs.getString(1);
				break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			if(null!=rs) {
				rs.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	//	DbCon.getDbConInstance().closeConnection(conn);
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return battery;
	}
	
	public static int getDeviceFenceStatus(String deviceId) {
		int isFenceSend  = 0;
		ResultSet rs = null;
		//Connection conn = DbCon.getDbConInstance().getConnection();
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null) {
			return 0;
		}
		try {
			psmtPreparedStatement = conn.prepareStatement(getDeviceFenceStatusSql);
			psmtPreparedStatement.setString(1, deviceId); 
			rs = psmtPreparedStatement.executeQuery();
			while(rs.next()) {
				isFenceSend = rs.getInt(1);
				break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			if(null!=rs) {
				rs.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	//	DbCon.getDbConInstance().closeConnection(conn);
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isFenceSend;
	}
	
	public static SectorBoundryObject getSectorBoundry(String phoneNo) {
		SectorBoundryObject boundry = null;
		ResultSet rs = null;
		//Connection conn = DbCon.getDbConInstance().getConnection();
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null || phoneNo==null) {
			return boundry;
		}
		try {
			psmtPreparedStatement = conn.prepareStatement(getSectorBoundrySql);
			psmtPreparedStatement.setString(1, phoneNo); //判断是否是当天的数据 
			rs = psmtPreparedStatement.executeQuery();
			while(rs.next()) {
				String phoneNum = phoneNo;
				double lat = rs.getDouble("Lat");
				double lon = rs.getDouble("Lon");
				int startangle = rs.getInt("BgnAng");
				int endangle = rs.getInt("EndAng");
				int radius = rs.getInt("R");
				int throld = rs.getInt("RThres");
				boundry = new SectorBoundryObject(phoneNum,lat,lon,startangle,endangle
						,radius,throld);
				break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			if(null!=rs) {
				rs.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	//	DbCon.getDbConInstance().closeConnection(conn);
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return boundry;
	}
	
	public static PolygonBoundryObject getPolyonBoundry(String phoneNo) {
		PolygonBoundryObject boundry = null;
		ResultSet rs = null;
		//Connection conn = DbCon.getDbConInstance().getConnection();
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null || phoneNo==null) {
			return boundry;
		}
		try {
			psmtPreparedStatement = conn.prepareStatement(getPolygonBoundrySql);
			psmtPreparedStatement.setString(1, phoneNo); //判断是否是当天的数据 
			rs = psmtPreparedStatement.executeQuery();
			while(rs.next()) {
				double lat = rs.getDouble("lat");
				double lon = rs.getDouble("lon");
				List rList = new ArrayList<>();
				for(int index=0;index<=39;index++) {
					rList.add(intFormat(rs.getInt("R"+indexFormat(index))));
				}
				int throld = rs.getInt("RThres");
				boundry = new PolygonBoundryObject(phoneNo,lat,lon,rList,throld);
				break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			if(null!=rs) {
				rs.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	//	DbCon.getDbConInstance().closeConnection(conn);
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return boundry;
	}
	
	public static DeviceExtraInfoObject getDeviceExtraInfo(String deviceId) {
		DeviceExtraInfoObject deviceExtraInfoObject = null;
		ResultSet rs = null;
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null || deviceId==null) {
			return deviceExtraInfoObject;
		}
		try {
			psmtPreparedStatement = conn.prepareStatement(selectDeviceExtraInfoSql);
			psmtPreparedStatement.setString(1, deviceId); 
			rs = psmtPreparedStatement.executeQuery();
			while(rs.next()) {
				int isAlarmSend = rs.getInt("isAlarmSend");
				int alarmFlag = 0;
				int startHour = 0;
				int endHour = 0;
				int intervalHour = 0;
				int isDataSend = rs.getInt("isDataSend");
				deviceExtraInfoObject = new DeviceExtraInfoObject(alarmFlag,isAlarmSend,startHour,endHour,intervalHour,isDataSend);
				break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			if(null!=rs) {
				rs.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	//	DbCon.getDbConInstance().closeConnection(conn);
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return deviceExtraInfoObject;
	}
	
	public static boolean isValidGps(ContentExtract contentExtract) {
		boolean flag = true;
		String deviceId = contentExtract.getDeviceID();
		Double lat = Double.parseDouble(contentExtract.getLatString());
		Double lon = Double.parseDouble(contentExtract.getLonString());
		String time = contentExtract.getTime();
		try {
			SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");  
			if(time.startsWith("0")) { //time是以0开始的
		        Date date = new Date();  
		        time = f.format(date); //得到当前时间
			}
			Date date = f.parse(time);
			date.setMinutes(date.getMinutes() - 5); //5分钟前
			time = f.format(date); 
			flag = validateGps(lat, lon, deviceId, time);
		} catch(ParseException e) {
			flag  = true;
			e.printStackTrace();
		} finally {
			return flag;
		}
	}
	
	public static boolean validateGps(Double deviceLat, Double deviceLon, String deviceId, String time) {
		Connection conn = DbPool.getInstance().getConnection();
		ResultSet rs = null;
		boolean flag = true;
		if(conn==null) {
			return flag;
		}
		try {
			psmtPreparedStatement = conn.prepareStatement(selectAroundSql);
			psmtPreparedStatement.setString(1, time); 
			psmtPreparedStatement.setString(2, deviceId); 
			rs = psmtPreparedStatement.executeQuery();
			while(rs.next()) {
				Double lat = rs.getDouble("lat");
				Double lon = rs.getDouble("lon");
				flag = flag && isValidOffset(deviceLat, deviceLon, lat, lon);
			}
			if(null!=rs) {
				rs.close();
			}
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();
			}
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			return flag;
		}
	}
	
	
	private static double rad(double d)
	{
	   return d * Math.PI / 180.0;
	} 

	public static double GetDistance(double lat1, double lng1, double lat2, double lng2)
	{
	   double radLat1 = rad(lat1);
	   double radLat2 = rad(lat2);
	   double a = radLat1 - radLat2;
	   double b = rad(lng1) - rad(lng2);

	   double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
	    Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
	   s = s * EARTH_RADIUS;
	   s = Math.round(s * 10000) / 10000;
	   return s;
	}
	
	public static boolean isValidOffset(Double deviceLat, Double deviceLon, Double lat, Double lon) {
		if(GetDistance(deviceLat, deviceLon, lat, lon) >= 5) { //按照1分钟1公里计算
			return false;
		} else {
			return true;
		}
	}
	public static DeviceExtraInfoObject getUserExtraInfo(String phoneNum) {
		DeviceExtraInfoObject deviceExtraInfoObject = null;
		ResultSet rs = null;
		Connection conn = DbPool.getInstance().getConnection();
		if(conn==null || phoneNum==null) {
			return deviceExtraInfoObject;
		}
		try {
			psmtPreparedStatement = conn.prepareStatement(selectUserExtraInfoSql);
			psmtPreparedStatement.setString(1, phoneNum); 
			rs = psmtPreparedStatement.executeQuery();
			while(rs.next()) {
				int isAlarmSend = 0;
				int alarmFlag = rs.getInt("alarmFlag");
				int startHour = rs.getInt("startHour");
				int endHour = rs.getInt("endHour");
				int intervalHour = rs.getInt("intervalHour");
				int isDataSend = 0;
				deviceExtraInfoObject = new DeviceExtraInfoObject(alarmFlag,isAlarmSend,startHour,endHour,intervalHour,isDataSend);
				break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		try {
			if(null!=rs) {
				rs.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		try {
			if(null!=psmtPreparedStatement) {
				psmtPreparedStatement.close();
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	//	DbCon.getDbConInstance().closeConnection(conn);
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return deviceExtraInfoObject;
	}
	

	public static String intFormat(int intValue) {
		return String.format("%05d", intValue);
	}
	
	public static String indexFormat(int index) {
		return String.format("%02d", index);
	}
}


