package com.franklin.db;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.PseudoColumnUsage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class DbUtil {
	private static PreparedStatement psmtPreparedStatement = null;
	private static String insertSql = "insert into AnimalGPSSystem (DeviceId,lon,lat,date) values(?,?,?,?);";
	private static String judgeAroundSql = "select count(*) from AnimalGPSSystem where date > ? and date < ? and DeviceId = ?";
	private static String judgeSameSql = "select count(*) from AnimalGPSSystem where date= ? and DeviceId = ?";
	//otherServer 贾博士
	private static String otherServerInsertSql = "insert into t_devicetrajectory (Device,Longitude,Latitude,AddDate) value(?,?,?,?)";
	//通过判断自己数据库中是否有重复数据来决定该数据是否插入贾博士和自己的数据库  因此先插贾博士数据库 然后插自己数据库
	private static Logger insertTimeLogger = Logger.getLogger("inserttime");
	private static int minutes = 0;
	
	static { //在类加载时执行
		minutes = PropertiesUtil.getMinutes();
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
			String deviceId = contentExtract.getdeviceID();
			String lonString = contentExtract.getLonString();
			String latString = contentExtract.getLatString();
			String time = contentExtract.getTime();
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
				//existflag = false; //全部插入
				if(!existflag) { //没有则插入
					psmtPreparedStatement = conn.prepareStatement(insertSql);
					psmtPreparedStatement.setString(1, deviceId); //判断是否是当天的数据 
					psmtPreparedStatement.setString(2, lonString);
					psmtPreparedStatement.setString(3, latString);
					psmtPreparedStatement.setString(4, time);
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
	
}


