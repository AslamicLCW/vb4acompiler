/*
 * Copyright 2009 Google Inc.
 * Lu Chengwei 2013, VB4A
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.simple.runtime.components.impl.android;

import com.google.devtools.simple.runtime.android.ApplicationImpl;
import com.google.devtools.simple.runtime.components.ComponentContainer;
import com.google.devtools.simple.runtime.components.Phone;
import com.google.devtools.simple.runtime.components.impl.ComponentImpl;

import java.net.HttpURLConnection;
import java.net.URL;import java.net.URLEncoder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.gsm.SmsManager; 
import android.widget.Toast; 

/*
import android.location.Location;   
import android.location.LocationListener;   
import android.location.LocationManager;   
*/

import java.io.BufferedReader;  
import java.io.BufferedWriter;  
import java.io.InputStreamReader;  
import java.io.OutputStreamWriter;  
import java.io.PrintWriter;  
import java.net.Socket;  
import java.io.IOException;  

/**
 * Implementation of phone related functions.
 * 
 * @author Herbert Czymontek
 */
public final class PhoneImpl extends ComponentImpl implements Phone {

  private final Vibrator vibrator;

  /**
   * Creates a new Phone component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}, for non-visible component, like this one
   *                   must be the form)
   */
  public PhoneImpl(ComponentContainer container) {
    super(container);

    vibrator = (Vibrator) ApplicationImpl.getContext().getSystemService(Context.VIBRATOR_SERVICE);
  }

  // Phone implementation

  @Override
  public boolean Available() {
    return true;
  }

  @Override
  public void Call(String phoneNumber) {
    if (null != phoneNumber && phoneNumber.length() > 0) {
      ApplicationImpl.getContext().startActivity(new Intent(Intent.ACTION_CALL,
          Uri.parse("tel:" + phoneNumber)));
    }
  }

  @Override
  public void SendSMS(String phoneNumber,String text,String warnings) {
	    if (null != phoneNumber && phoneNumber.length() > 0) {
			SmsManager smsManager = SmsManager.getDefault(); 
			smsManager.sendTextMessage(phoneNumber, null, text, null, null);
			if (warnings=="")
			{
				warnings="SMS has been sent to "+phoneNumber+".";
			}
			Toast.makeText(ApplicationImpl.getContext(), warnings,Toast.LENGTH_SHORT).show();
    }
  }


  @Override
  public void SendMail(String Address, String mailtitle, String mailtext) {
	    Intent data=new Intent(Intent.ACTION_SENDTO);  
        data.setData(Uri.parse("mailto:"+Address));  
        data.putExtra(Intent.EXTRA_SUBJECT, mailtitle);  
        data.putExtra(Intent.EXTRA_TEXT, mailtext);  
        ApplicationImpl.getContext().startActivity(data); 
  }


/*
  public String VB4APost(String pathUrl, String data) {
	try
	{
		URL url = new URL(pathURL);
		HttpURLConnection httpConn=(HttpURLConnection)url.openConnection();
		 httpConn.setDoOutput(true);//使用 URL 连接进行输出
		 httpConn.setDoInput(true);//使用 URL 连接进行输入
		 httpConn.setUseCaches(false);//忽略缓存
		 httpConn.setRequestMethod("POST");//设置URL请求方法
		 String requestString = data;
     //设置请求属性
    //获得数据字节数据，请求数据流的编码，必须和下面服务器端处理请求流的编码一致
          byte[] requestStringBytes = requestString.getBytes(ENCODING_UTF_8);
          httpConn.setRequestProperty("Content-length", "" + requestStringBytes.length);
          httpConn.setRequestProperty("Content-Type", "application/octet-stream");
          httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
          httpConn.setRequestProperty("Charset", "UTF-8");
          //
          String name=URLEncoder.encode("黄武艺","utf-8");
          httpConn.setRequestProperty("NAME", name);
          
          //建立输出流，并写入数据
          OutputStream outputStream = httpConn.getOutputStream();
          outputStream.write(requestStringBytes);
          outputStream.close();
         //获得响应状态
          int responseCode = httpConn.getResponseCode();
          if(HttpURLConnection.HTTP_OK == responseCode){//连接成功
           
           //当正确响应时处理数据
           StringBuffer sb = new StringBuffer();
              String readLine;
              BufferedReader responseReader;
             //处理响应流，必须与服务器响应流输出的编码一致
              responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), ENCODING_UTF_8));
              while ((readLine = responseReader.readLine()) != null) {
               sb.append(readLine).append("/n");
              }
              responseReader.close();
              return sb.toString();

	}
	catch (Exception ex)
	{
		ex.printStackTrace();
	}



  }
*/

  @Override
  public String SocketSend(String ip,int port,String data) {
	Socket s=null;
	BufferedReader din = null;  
    PrintWriter dout = null;  
	String str="";
	String str2="";
	try{
		s=new Socket(ip,port);
		s.setSoTimeout(5000);
		din = new BufferedReader(new InputStreamReader(s.getInputStream()));  
        dout = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))); 
		dout.println(data);
		dout.flush();
		while ((str = din.readLine()) != null) {
        str2=str2+str+"\n";
		}

		} catch (Exception e) {  
            //e.printStackTrace(); 
			return "Null";
        } finally {  
            try {  
                din.close();  
                dout.close();  
                s.close();  
            } catch (IOException e) {  
                //e.printStackTrace();  
				return "Null";
            }  
        }  
		return str2;
  }

  @Override
  public void JumpURL(String url) {
	Uri uri = Uri.parse(url);  
	Intent it = new Intent(Intent.ACTION_VIEW, uri);  
    ApplicationImpl.getContext().startActivity(it);
  }

  @Override
  public void Vibrate(int duration) {
    vibrator.vibrate(duration);
  }
}
