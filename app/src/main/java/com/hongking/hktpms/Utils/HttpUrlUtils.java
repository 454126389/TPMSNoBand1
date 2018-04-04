package com.hongking.hktpms.Utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by yuhg on 16/7/7.
 */
public class HttpUrlUtils {
    private String TAG = "HttpURLUtils";

    //
    private static int CONN_OK = 0;
    private static int CONN_EXCEPTION = 1;
    //线程池大小
    private final int MAX_THREADS = 10;
    private final int TIMEOUT_CONN = 25 * 1000;
    private final int TIMEOUT_READ = 25 * 1000;

    private Context context;
    private ExecutorService threadPool;

    public HttpUrlUtils(Context context){
        this.context = context;
        threadPool = Executors.newFixedThreadPool(MAX_THREADS);
    }

    public ExecutorService getThreadPool(){
        if(threadPool == null){
            threadPool = Executors.newFixedThreadPool(MAX_THREADS);
        }
        return threadPool;
    }


    public void post(final String url , final String req, final int reqestCode, final Handler handler){
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"post req: " +  req);
                HttpRsp result = request(url,req,REQ_TYPE_POST);
                Message msg = new Message();
                msg.what = reqestCode;
                Bundle bundle = new Bundle();
                bundle.putString("data", result.getData());
                bundle.putInt("statusCode", result.getCode());
                msg.setData(bundle);

                handler.sendMessage(msg);
                Log.i(TAG,"post rsp: " +  result.getData());
            }
        });
    }

    public void get(final String url , final String req, final int reqestCode, final Handler handler){
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                HttpRsp result = doGet(url);
                Message msg = new Message();
                msg.what = reqestCode;
                Bundle bundle = new Bundle();
                bundle.putString("data", result.getData());
                bundle.putInt("statusCode", result.getCode());
                msg.setData(bundle);

                handler.sendMessage(msg);
            }
        });
    }

    private final String REQ_TYPE_GET = "GET";
    private final String REQ_TYPE_POST = "POST";
    private HttpRsp request(String url,String req,String reqType){
        HttpRsp result = new HttpRsp();
//        HttpURLConnection conn = null;
        HttpURLConnection conn = null;
        try {
        	
            URL reqURL = new URL(url);
            conn = (HttpURLConnection) reqURL.openConnection();
            conn.setUseCaches(false);//4)设置不使用缓存:
            conn.setConnectTimeout(TIMEOUT_CONN);//连接超时时间
            conn.setReadTimeout(TIMEOUT_READ);//读取超时时间
            conn.setRequestMethod(reqType);//5)设置使用POST的方式发送:
            conn.setRequestProperty("Connection", "Keep-Alive");//6)设置维持长连接:
            conn.setRequestProperty("Charset", "UTF-8");//7)设置文件字符集:
            conn.setRequestProperty("Content-Length", String.valueOf(req.getBytes().length));//8)设置文件长度:
            conn.setRequestProperty("Content-Type","application/json");//9)设置文件类型:
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            
//            conn.setUseCaches(false);//4)设置不使用缓存:
//            conn.setConnectTimeout(TIMEOUT_CONN);//连接超时时间
//            conn.setReadTimeout(TIMEOUT_READ);//读取超时时间
//            conn.setRequestProperty("Connection", "Keep-Alive");//6)设置维持长连接:
//            conn.setRequestProperty("Charset", "UTF-8");//7)设置文件字符集:
//            conn.setRequestProperty("Content-Type","application/json");//9)设置文件类型:
//            conn.setDoInput(true);// 允许输入
//            conn.setDoOutput(true);// 允许输出
//            conn.setRequestMethod("GET");
            
            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(req.getBytes());
            outStream.flush();
            outStream.close();
            if (conn == null) {
                result.setCode(CONN_EXCEPTION);
                result.setData("HttpURLConnection is null");
                return null;
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                InputStream input = conn.getInputStream();
                if (input != null) {
                    // 创建字节输出流对象
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len = 0;// 定义读取的长度
                    byte buffer[] = new byte[1024];// 定义缓冲区
                    while ((len = input.read(buffer)) != -1) {// 按照缓冲区的大小，循环读取
                        baos.write(buffer, 0, len);// 根据读取的长度写入到os对象中
                    }
                    // 释放资源
                    input.close();
                    baos.close();
                    // 返回字符串
                    result.setCode(CONN_OK);
                    result.setData(new String(baos.toByteArray()));
                }
            }else{
            	result.setCode(CONN_EXCEPTION);
                result.setData(responseMsg(responseCode));
            }

        }catch (Exception e) {
            e.printStackTrace();
            result.setCode(CONN_EXCEPTION);
            result.setData(e.getMessage());
        }
        return result;
    }


    private HttpRsp doGet(String url){
        HttpRsp result = new HttpRsp();
        HttpURLConnection conn = null;
        try {

            URL reqURL = new URL(url);
            conn = (HttpURLConnection) reqURL.openConnection();
            conn.setUseCaches(false);//4)设置不使用缓存:
            conn.setConnectTimeout(TIMEOUT_CONN);//连接超时时间
            conn.setReadTimeout(TIMEOUT_READ);//读取超时时间
            conn.setRequestProperty("Connection", "Keep-Alive");//6)设置维持长连接:
            conn.setRequestProperty("Charset", "UTF-8");//7)设置文件字符集:
            conn.setRequestProperty("Content-Type","application/json");//9)设置文件类型:
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setRequestMethod("POST");

            if (conn == null) {
                result.setCode(CONN_EXCEPTION);
                result.setData("HttpURLConnection is null");
                return null;
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                InputStream input = conn.getInputStream();
                if (input != null) {
                    // 创建字节输出流对象
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len = 0;// 定义读取的长度
                    byte buffer[] = new byte[1024];// 定义缓冲区
                    while ((len = input.read(buffer)) != -1) {// 按照缓冲区的大小，循环读取
                        baos.write(buffer, 0, len);// 根据读取的长度写入到os对象中
                    }
                    // 释放资源
                    input.close();
                    baos.close();
                    // 返回字符串
                    result.setCode(CONN_OK);
                    result.setData(new String(baos.toByteArray()));
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
            result.setCode(CONN_EXCEPTION);
            result.setData(e.getMessage());
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }
        return result;
    }
    
	private class MyHostnameVerifier implements HostnameVerifier {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	private class MyTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

    public class HttpRsp{

        private int code;
        private String data;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
    
    private String responseMsg(int statusCode){
		String errorMsg = "";
		if(201 <= statusCode && statusCode <= 204){
			errorMsg = "服务器未响应";
		}else if(300 <= statusCode && statusCode <= 306){
			errorMsg = "请求失败，重定向";
		}else if(statusCode == 400){
			errorMsg = "错误请求";
		}else if(statusCode == 401){
			errorMsg = "未授权的请求";
		}else if(statusCode == 402){
			errorMsg = "需要付款";
		}else if(statusCode == 403){
			errorMsg = "禁止访问";
		}else if(statusCode == 404){
			errorMsg = "找不到服务器";
		}else if(statusCode == 407){
			errorMsg = "代理认证请求";
		}else if(statusCode == 415){
			errorMsg = "服务器拒绝请求，请求错误";
		}else if(500 <= statusCode && statusCode <= 503){
			errorMsg = "服务器错误";
		}
		return errorMsg;
	}
}
