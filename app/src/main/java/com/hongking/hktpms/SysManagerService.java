/*
 * Copyright (C) 2011 Wireless Network and Multimedia Laboratory, NCU, Taiwan
 * 
 * You can reference http://wmlab.csie.ncu.edu.tw
 * 
 * This class is used to handle process or transfer the control messages about connection * 
 * 
 * @author Fiona
 * @version 0.0.1
 *
 */

package com.hongking.hktpms;


import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.hongking.hktpms.Utils.SharePreferenceUtil;
import com.hongking.oemtpms.R;

public class SysManagerService extends Service {
    private SysParam TPMSParam;
    private SharePreferenceUtil sp;
    SysApplication app;
    // Debugging
    private static final String TAG = "systemManager";
    private static final boolean D = true;
    private boolean isRunning;
    //private boolean isConnecting = false;

    public static final String STOP_SERVICE = "STOP_SELF";
    public static final String SERVICE_REPORT = "service is ok";
    public static final String AUTO_SW_MAINPAGE = "AUTO_SW_MAINPAGE";
    MessageReceiver mReceiver;

    Notification notification;
    CharSequence contentText;
    Thread ActivityManagerThread;

    @Override
    public void onCreate() {
        if (D) Log.i(TAG, "[onCreate]");
        app = (SysApplication) getApplicationContext();
        TPMSParam = app.TPMSParam;
        sp = new SharePreferenceUtil(this);
        isRunning = true;
        IntentFilter mFilter;
        mFilter = new IntentFilter(STOP_SERVICE);
        mReceiver = new MessageReceiver();

        registerReceiver(mReceiver, mFilter);


        ActivityManagerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SysStatusCheck();

            }
        });
        ActivityManagerThread.start();

        //pm =(PowerManager)getSystemService(Context.POWER_SERVICE);;
        //wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , "TPMSService");
        //wakeLock.acquire();


        super.onCreate();

    }

    private boolean isServiceRunning() {
        int connType = sp.getConnectType();
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(connType == SharePreferenceUtil.CONNECT_TYPE_BLUETOOTH){
                if ("com.hongking.hktpms.SysControllerService".equals(service.service.getClassName())) {
                    Log.i(TAG, "SysControllerService is running");
                    return true;
                }
            }else if(connType == SharePreferenceUtil.CONNECT_TYPE_USB){
                if ("com.hongking.hktpms.UsbControllerService".equals(service.service.getClassName())) {
                    Log.i(TAG, "UsbControllerService is running");
                    return true;
                }
            }

        }
        return false;
    }

    private void stopService() {
        Intent intent = new Intent(SysControllerService.STOP_SERVICE);
        //intent.putExtra("STATE", message);
        Log.i(TAG, "request stop servie");
        //if(context != null)
        sendBroadcast(intent);
    }

    private void setupService() {
        Log.d(TAG, "setupService()");

        // Initialize the BluetoothConnController to perform bluetooth connections
        int connType = sp.getConnectType();
        if(connType == SharePreferenceUtil.CONNECT_TYPE_BLUETOOTH) {
            Intent startIntent = new Intent(this, SysControllerService.class);
            startService(startIntent);
        }else if(connType == SharePreferenceUtil.CONNECT_TYPE_USB){
            Intent startIntent = new Intent(this, UsbControllerService.class);
            startService(startIntent);
        }

   	/*--------------------启动查找设备请求----------------------*/
        //Intent serverIntent = new Intent(this, BluetoothDeviceListActivity.class);
        //startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

    }

    public void showNotification(int mode) {
        Notification notification;
        CharSequence contentText;
        // 定义Notification的各种属性
        if (mode == 0x01) {
            notification = new Notification(R.drawable.ic_launcher2,
                    getResources().getString(R.string.BackgroundAlarm), System.currentTimeMillis());
            notification.sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warn);
            contentText = getResources().getString(R.string.BackgroundAlarm);// 通知栏内容
        } else {
            notification = new Notification(R.drawable.ic_launcher1,
                    null, System.currentTimeMillis());
            contentText = this.getResources().getString(R.string.app_name);// 通知栏内容

        }
        notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
        notification.flags |= Notification.FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // 设置通知的事件消息
        CharSequence contentTitle = this.getResources().getString(R.string.app_name); // 通知栏标题

        //Intent notificationIntent = new Intent(context,BluetoohtSetupActivity.class); // 点击该通知后要跳转的Activity
        //PendingIntent contentItent = PendingIntent.getActivity(context, 0,context.getIntent(), PendingIntent.FLAG_CANCEL_CURRENT);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        Toast.makeText(this,"打开AppEntryActivity",Toast.LENGTH_SHORT).show();
        intent.setClass(this, AppEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        notification.flags = Notification.FLAG_ONGOING_EVENT; // 设置常驻 Flag

        PendingIntent contextIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notification.setLatestEventInfo(this, contentTitle, contentText, contextIntent);

        startForeground(100, notification);
        //isNotificationClear = false;

    }

    void SysStatusCheck() {
       /* while (isRunning) {
            int activityStatus = TPMSParam.activityStatusListen();
            Log.i(TAG, "on start acitity num " + activityStatus);
            if (activityStatus == 0x00) {
                //app.changeMode(SysParam.SYS_MODE.MODE_NORM);
                //app.showNotification(0x00);
                Intent i = new Intent(SysActivity.AUTODESTORY);
                sendBroadcast(i);
                i = new Intent(AUTO_SW_MAINPAGE);
                sendBroadcast(i);
            } else {
                app.cancelNotification();
                if (TPMSParam.blueStatus.isConnecting())
                    app.WarnNotifation(this);

            }
            //else{}
        }*/
    }

    @Override
    public void onDestroy() {
        if (D) Log.e(TAG, "[onDestroy]");
        isRunning = false;
        stopForeground(true);
        unregisterReceiver(mReceiver);
//        if(receiver != null) unregisterReceiver(receiver);
        super.onDestroy();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (D) Log.i(TAG, "[onStartCommand]");
        if (isServiceRunning() == false) {
            setupService();

        }
//        showNotification(0x00);
        super.onStart(intent, startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        if (D) Log.e(TAG, "[onBind]");
        return null;
    }

    public class LocalBinder extends Binder {
        SysManagerService getService() {
            return SysManagerService.this;
        }
    }

    //系统初始化接口
    private synchronized int setupConnectTo() {
        int ret;

        //TPMSParam.mBluethoothData.adContext(this);

        ret = TPMSParam.mBluethoothData.protocolHandshake();
        if (ret != 0x00)
            return ret;
        ret = TPMSParam.mBluethoothData.ProtocolModeChange(SysParam.SYS_MODE.MODE_SETUP);
        if (ret != 0x00)
            return ret;
        ret = TPMSParam.mBluethoothData.ProtocolSysParamGet();
        if (ret != 0x00)
            return ret;
        ret = TPMSParam.mBluethoothData.ProtocolTireIDGet();
        if (ret != 0x00)
            return ret;
        ret = TPMSParam.mBluethoothData.ProtocolAbout();
        if (ret != 0x00)
            return ret;
        ret = TPMSParam.mBluethoothData.ProtocolRfCpGet();
        if (ret != 0x00)
            return ret;
        TPMSParam.mBluethoothData.ProtocolModeChange(SysParam.SYS_MODE.MODE_NORM);
        TPMSParam.tyreData.resetUpdateStatus();//清除所有数据
        //ret = TPMSParam.mBluethoothData.ProtocolModeChange(SysParam.SYS_MODE.MODE_NORM);
        //if(ret != 0x00)
        //	return ret;

        return 0;
    }

    void setupContectToThread() {
        int ret = setupConnectTo();
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (D) Log.e(TAG, "** ON RECEIVE **");
            String action = intent.getAction();

            if (action.equals(STOP_SERVICE)) {
                Log.i(TAG, "STOP service self");
                //if (mConnService != null) mConnService.terminated();
                //mConnService = null;
                stopSelf();
                //unregisterReceiver(mBtMsgReceiver);
            } else if (action.equals(SysControllerService.CONNECT_MESSAGE_ACTION)) {
                String displayMsg = intent.getExtras().getString("STR");
                Log.i(TAG, displayMsg + "has connect");
                //ConnectedDeviceAddr = displayMsg;
                TPMSParam.sConnectedBlueAddr = displayMsg;
                //if(DialogWait != null)
                //	DialogWait.dismiss();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        setupContectToThread();

                    }
                }).start();
            } else {
                Log.e(TAG, "another action: " + action);
            }
        }

    }

}
