package com.franklin.db;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;


public class DbPool {
	private static DbPool instance;
	public ComboPooledDataSource ds;
	private static String c3p0Properties = "/config/c3p0.properties";
	DbPool() {
		Properties p = new Properties();
	    InputStream in = DbPool.class .getResourceAsStream(c3p0Properties);
	    try {
			p.load(in);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  ds = new ComboPooledDataSource();
		  ds.setUser(p.getProperty("user"));
		  ds.setPassword(p.getProperty("password"));
		  ds.setJdbcUrl(p.getProperty("jdbcUrl"));
		  try {
			ds.setDriverClass(p.getProperty("driverClass"));
		  } catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
		  String k = p.getProperty("initialPoolSize");
		  ds.setInitialPoolSize(Integer.parseInt(p.getProperty("initialPoolSize")));
		  ds.setMinPoolSize(Integer.parseInt(p.getProperty("minPoolSize")));
		  ds.setMaxPoolSize(Integer.parseInt(p.getProperty("maxPoolSize")));
		  ds.setMaxIdleTime(Integer.parseInt(p.getProperty("maxIdleTime")));
		  ds.setAcquireIncrement(Integer.parseInt(p.getProperty("acquireIncrement")));
		  ds.setAcquireRetryAttempts(Integer.parseInt(p.getProperty("acquireIncrement")));
	}
	
	 public static  DbPool getInstance() {
		  if (instance == null) {
		   try {
		    instance = new DbPool();
		   } catch (Exception e) {
		    e.printStackTrace();
		   }
		  }
		  return instance;
	}
	 
	 public synchronized final Connection getConnection() {
		  try {
		   return ds.getConnection();
		  } catch (SQLException e) {
		   e.printStackTrace();
		  }
		  return null;
	}
	 
	 protected void finalize() throws Throwable {
		  DataSources.destroy(ds); // �ر�datasource
		  super.finalize();
		 
	}
}
