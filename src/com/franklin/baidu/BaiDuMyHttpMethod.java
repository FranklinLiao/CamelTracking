package com.franklin.baidu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class BaiDuMyHttpMethod {
	public String doGetMethod(String url) {
		String bodyInfo = null;
	    HttpClient httpClient = new DefaultHttpClient();
	    try {
	    	HttpGet httpGet = new HttpGet(new URI(url));
			HttpResponse httpResponse = httpClient.execute(httpGet);
			if(HttpStatus.SC_OK==httpResponse.getStatusLine().getStatusCode()) {
				HttpEntity httpEntity = httpResponse.getEntity();
				bodyInfo = EntityUtils.toString(httpEntity);
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bodyInfo;
	}
	
	   /**
	    * 
	    * @param urlAll:����ӿ�
	    * @param charset:�ַ����
	    * @return ����json���
	    */
	   public static String get(String urlAll,String charset){
		   BufferedReader reader = null;
		   String result = null;
		   StringBuffer sbf = new StringBuffer();
		   String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";//ģ�������
		   try {
			   URL url = new URL(urlAll);
			   HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			   connection.setRequestMethod("GET");
			   connection.setReadTimeout(30000);
			   connection.setConnectTimeout(30000);
			   connection.setRequestProperty("User-agent",userAgent);
			   connection.connect();
			   InputStream is = connection.getInputStream();
			   reader = new BufferedReader(new InputStreamReader(
						is, charset));
				String strRead = null;
				while ((strRead = reader.readLine()) != null) {
					sbf.append(strRead);
					sbf.append("\r\n");
				}
				reader.close();
				result = sbf.toString();
			   
		} catch (Exception e) {
			e.printStackTrace();
		}
		   return result;
	   }
}
