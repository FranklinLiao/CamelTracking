package com.franklin.server;

import java.util.ArrayList;
import java.util.Iterator;

import com.franklin.db.ContentExtract;
import com.franklin.db.DbUtil;

public class ThreadList extends Thread{  //用来处理infoList 将数据解析并插入到数据库
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
					ContentExtract contentExtract = new ContentExtract(infoIter.next());
					contentExtract.infoParser(); //解析数据并把数据放入到contentExtract
					DbUtil.insert(contentExtract);
					System.out.println("****************insert one data!*****************");
				}
			} else {
				try {
					Thread.sleep(1000*sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
	}
	
}
