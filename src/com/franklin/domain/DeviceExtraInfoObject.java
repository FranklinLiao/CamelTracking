package com.franklin.domain;

public class DeviceExtraInfoObject {
	private int deviceId;
	private int alarmFlag;
	private int startHour;
	private int endHour;
	private int intervalHour;
	private int isAlarmSend;
	private int isDataSend;
	
	public DeviceExtraInfoObject() {
		super();
		// TODO Auto-generated constructor stub
	}
	public DeviceExtraInfoObject(int alarmFlag, int isAlarmSend,int startHour, int endHour,
			int intervalHour,int isDataSend) {
		super();
		this.alarmFlag = alarmFlag;
		this.isAlarmSend = isAlarmSend;
		this.startHour = startHour;
		this.endHour = endHour;
		this.intervalHour = intervalHour;
		this.isDataSend = isDataSend;
	}
	public int getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
	public int getAlarmFlag() {
		return alarmFlag;
	}
	public void setAlarmFlag(int alarmFlag) {
		this.alarmFlag = alarmFlag;
	}
	public int getStartHour() {
		return startHour;
	}
	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}
	public int getEndHour() {
		return endHour;
	}
	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}
	public int getIntervalHour() {
		return intervalHour;
	}
	public void setIntervalHour(int intervalHour) {
		this.intervalHour = intervalHour;
	}
	
	public int getIsAlarmSend() {
		return isAlarmSend;
	}
	public void setIsAlarmSend(int isAlarmSend) {
		this.isAlarmSend = isAlarmSend;
	}
	public int getIsDataSend() {
		return isDataSend;
	}
	public void setIsDataSend(int isDataSend) {
		this.isDataSend = isDataSend;
	}
	@Override
	public String toString() {
		if(isAlarmSend>0) { //已经发送了
			alarmFlag = 9; //设置为无效
		}
		if(isDataSend>0) {
			startHour = 99;
			endHour = 99;
			intervalHour = 99;
		}
		return alarmFlag + "" + intFormat(startHour) + ""
				+ intFormat(endHour) + "" + intFormat(intervalHour);
	}
	
	public String intFormat(int intvalue) {
		return String.format("%02d", intvalue);
	}
	
}
