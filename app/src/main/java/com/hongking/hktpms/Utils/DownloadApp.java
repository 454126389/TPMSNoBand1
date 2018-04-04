package com.hongking.hktpms.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class DownloadApp {
    /** 下载线程，用于完成安装包下载任务  */
    private Thread downloadThread;
    /** 进度条对话框 */
    private ProgressDialog progressDlg;
    /** 进度条进度  */
    private int progress;
    /** 用于创建下载安装包的连接请求   */
    private HttpURLConnection urlConnection;
    /** 请求的输入流  */
    private InputStream iStream;
    /** 文件输出流  */
    private FileOutputStream fos;
    /** 是否取消下载 ，初始值为false */
    private boolean isCancelDown = false;
    /** APK文件保存路径  */
    private String fileDir;
    
    /** APK文件名 */
    private static final String FILE_NAME = "tpms.apk";
    
    
    /** 正在下载标志  */
    private static final int DOWN_LOADING = 1;
    /** 下载结束标志  */
    private static final int DOWN_OVER = 2;
    
    /** 下载超时，10分钟  */
    private static final int TIME_OUT = 10 * 60 * 1000;
    
    /** 软件升级页面中的上下文  */
    private Context context;
    /** 用于用户在强制更新时，选择取消操作，来退出应用  */
    private Activity activity; 
    
    private String urlString;
    
    public DownloadApp(Context cxt, Activity act, String url) {
        this.activity = act;
        this.context = cxt;
        this.urlString = url;
    }
    
    
    public void showDownloadProgressDlg() {
        Log.d("UpdateApp", "显示软件下载进度条.");
        progressDlg = new ProgressDialog(this.context);
        progressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDlg.setTitle("正在下载……");
        progressDlg.setProgress(100);
//        progressDlg.setButton("取消", new DialogInterface.OnClickListener() {
//            
//            @Override
//            public void onClick(DialogInterface dlgInterface, int which) {
//                // TODO Auto-generated method stub
//                dlgInterface.dismiss();
//                isCancelDown = true;
//            }
//        });
        progressDlg.setCancelable(false);
        progressDlg.show();
        downloadApk();
    }
    
    private void downloadApk() {
        // 使用线程
        Log.d("UpdateApp", "开始下载安装包.");
        downloadThread = new Thread(downloadRunnable);
        downloadThread.start();
    }
    
    
    private Runnable downloadRunnable = new Runnable() {
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Looper.prepare();
            URL url;
            try {
                url = new URL(urlString); // 从更新信息中获取下载路径，并创建url
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setConnectTimeout(TIME_OUT); // 设置连接超时时间，这里设置为10分钟
                urlConnection.setReadTimeout(TIME_OUT); // 设置读取的超时时间，这里设置为10分钟 
                long length = urlConnection.getContentLength();
                iStream = urlConnection.getInputStream();
                if (iStream != null) { // 输入流中有内容
                    fileDir = getSDPath() + "/download"; // apk文件存储路径，为SD卡下的download文件夹下
                    File file = new File(fileDir); // 先获取download文件夹
                    if(!file.exists()) { // download是否存在，不存在则创建
                        file.mkdirs(); // 创建download文件夹
                    }
                    file = new File(fileDir, FILE_NAME); // 创建apk文件
                    fos = new FileOutputStream(file);   
                       
                    byte[] buf = new byte[1024];  
                    int count = 0; // 记录下载进度
                    do{                 
                        int numread = iStream.read(buf); // 从http输入流中读取内容，并返回内容长度
                        count += numread; // 统计从输入流中读取到的内容总长度
                        // 通过统计内容长度值与http读取的内容总长进行换算，得到下载百分比进度
                        progress =(int)(((float)count / length) * 100); 
                        //更新进度
                        downloadHandler.sendEmptyMessage(DOWN_LOADING);
                        if(numread <= 0){   
                            //下载完成通知安装
                            downloadHandler.sendEmptyMessage(DOWN_OVER);
                            break;
                        }
                        fos.write(buf,0,numread);
                    }while(!isCancelDown);//点击取消就停止下载.

                }   
                fos.flush();   
                if (fos != null) {   
                    fos.close();   
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                new Throwable("URL异常", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                new Throwable("IO流异常", e);
            } finally {
                urlConnection.disconnect();
            }
            Looper.loop();
        }
    };
    
    private Handler downloadHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DOWN_LOADING:  // 正在下载，设置下载进度
                if(progress > 0){
                    progressDlg.setProgress(progress);
                }
                break;
            case DOWN_OVER: // 下载结束，启动程序安装
                progressDlg.dismiss();
                installApp();
                break;
            default:
                break;
            }
        };
    };

    
    
    private String getSDPath(){ 
        Log.d("UpdateApp", "获取手机SD卡的根路径.");
        File sdDir = null; 
        boolean isSDCard = Environment.getExternalStorageState()
            .equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (isSDCard) { 
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录 
        } else {
            Log.e("ERROR!!!", "SDCard 不存在！");
        }
        return sdDir.toString(); 

    } 
    
    private void installApp() {
        Log.d("UpdateApp", "开始安装软件更新.");
        File apkfile = new File(fileDir, FILE_NAME);
        //自己程序读取apk文件并安装
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkfile),   
                "application/vnd.android.package-archive");   
        context.startActivity(intent);   
        activity.finish();
    }
}




