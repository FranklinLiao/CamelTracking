package com.franklin.test;

import com.franklin.db.ContentExtract;
import com.franklin.server.UdpThreadList;

import junit.framework.TestCase;

public class ThreadListTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void SendData() {
		//String info = "J:11100.0000W:4000.0000T:"+"00000000000000"+"ID:030304";
		String info = "J:11620.6290W:3958.6924T:20150523111103ID:000678";
		//String info =  "J:0000000000W:000000000T:00000000000000ID:000063";
		ContentExtract contentExtract = new ContentExtract(info);
		contentExtract.infoParser(); 
		//new ThreadList().sendData(contentExtract);
	}
	
	public void test() {
		int i = 1;
		double x = 122.01212;
		//System.out.println(String.format("%04d",i));
		System.out.println(intToString(x));
	}
	
	public String intToString(double x) {
		String s = "";
		String sTemp = String.valueOf(x);
		if(sTemp.indexOf(".")!=-1) {
			int k = sTemp.indexOf(".");
			
			int m = sTemp.length()-k;
			while(m++<=6) {
				sTemp+='0';
			}
			while(k++<=2) {
				sTemp = '0'+sTemp;
			}
			
		}
		return sTemp;
	}
}
