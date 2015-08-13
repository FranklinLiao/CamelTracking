package com.franklin.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.franklin.db.ContentExtract;
import com.franklin.db.DbUtil;

import junit.framework.TestCase;

public class DbUtilTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void tetInsertOtherServer() {
		String info = "J:11600.0000W:3900.0000T:"+"20141231180000"+"ID:999111";
		ContentExtract contentExtract = new ContentExtract(info);
		contentExtract.infoParser();
		//DbUtil.insertOtherServer(contentExtract);
	}
	
	public void tetSimpleDateFormat() {
		String time = "20150404010101";
		SimpleDateFormat form = new SimpleDateFormat("yyyymmddhhmmss");
		Date date;
		try {
			date = form.parse(time);
			System.out.println(date);
			form = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			time = form.format(date);
			System.out.println(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
