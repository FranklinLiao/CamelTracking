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

public class DbUtil {
	private static PreparedStatement psmtPreparedStatement = null;
	private static String insertSql = "insert into AnimalGPSSystem (DeviceId,lon,lat,date) values(?,?,?,?);";
	private static String judgeAroundSql = "select count(*) from AnimalGPSSystem where date > ? and date < ? and DeviceId = ?";
	private static String judgeSameSql = "select count(*) from AnimalGPSSystem where date= ? and DeviceId = ?";
	private static int minutes = 0;
	
	static { //在类加载时执行
		minutes = PropertiesUtil.getMinutes();
	}
	
	public static void insert(ContentExtract contentExtract) { 
		boolean existflag = false;
		Connection conn = DbCon.getDbConInstance().getConnection();
		if(contentExtract==null||conn==null) { //为null,就退出
			return;
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
			        existflag = existAroundJudge(time,deviceId); //以like进行判断（同一天） 是不是同一条
				} else {
					existflag = existSameJudge(time,deviceId); //以time来判断是否是同一条
				}
				existflag = false; //全部插入
				if(!existflag) { //没有则插入
					psmtPreparedStatement = conn.prepareStatement(insertSql);
					psmtPreparedStatement.setString(1, deviceId); //判断是否是当天的数据 
					psmtPreparedStatement.setString(2, lonString);
					psmtPreparedStatement.setString(3, latString);
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
		DbCon.getDbConInstance().closeConnection(conn);
	}
	
	public static boolean existAroundJudge(String timeString,String deviceId) {
		boolean flag = true;
		ResultSet rs = null;
		String lowerTime = getAroundTime(timeString, 0-minutes); //  -5分钟
		String upperTime = getAroundTime(timeString,minutes);  //5分钟
		Connection conn = DbCon.getDbConInstance().getConnection();
		if(conn==null) {
			return false;
		}
		try {
			psmtPreparedStatement = conn.prepareStatement(judgeAroundSql);
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
		DbCon.getDbConInstance().closeConnection(conn);
		return flag;
	}
	
	public static boolean existSameJudge(String timeString,String deviceId) {
		boolean flag = true;
		ResultSet rs = null;
		Connection conn = DbCon.getDbConInstance().getConnection();
		if(conn==null) {
			return false;
		}
		try {
			psmtPreparedStatement = conn.prepareStatement(judgeSameSql);
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
		DbCon.getDbConInstance().closeConnection(conn);
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


