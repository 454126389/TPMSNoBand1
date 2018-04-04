package com.hongking.hktpms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.hongking.hktpms.Utils.SharePreferenceUtil;
import com.hongking.oemtpms.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Administrator on 2016/11/24.
 */

public class PowerBroadcastReceiver extends BroadcastReceiver {

    private SysParam TPMSParam;
    private SysApplication app;
    private Context mcontext;
    private BluetoothAdapter mBluetoothAdapter = null;
    private SharePreferenceUtil sp;
    private String deviceAddr;
    private int connectMode = 0x00;
    public static final String DEVICE_ADDRESS = "device_address";

    @Override
    public void onReceive(final Context context, Intent intent) {
        mcontext = context;
        sp = new SharePreferenceUtil(context);
        final String action = intent.getAction();
//        Toast.makeText(context,"监听到action"+action,Toast.LENGTH_LONG).show();
        Log.e("PowerBroadcastReceiver","action " + action);




        if (SharePreferenceUtil.CONNECT_TYPE_BLUETOOTH == sp.getConnectType()) {
//            if(Intent.ACTION_SCREEN_ON.equals(action)){
            if(Intent.ACTION_BOOT_COMPLETED.equals(action)||"com.android.action_acc_on".equals(action)){
//                context.startService(new Intent(context, SysControllerService.class));
//                context.startService(new Intent(context, SysManagerService.class));
//                initBluetooth();






             new CountDownTimer(5000,10000){

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        if(!sp.getAutostart().contains("noflag")&&BluetoothAdapter.getDefaultAdapter().isEnabled())
                        {

//                            context.startService(new Intent(context, SysControllerService.class));
//                            context.startService(new Intent(context, SysManagerService.class));
                            Log.e("2017kjx111212","1");
                            sp.setRebootflag("isflag");
                            Toast.makeText(context,"TPMS启动中",Toast.LENGTH_LONG).show();

                            Intent mainActivityIntent = new Intent(context, GuidanceActivity.class);  // 要启动的Activity
                            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(mainActivityIntent);
                        }else
                        {
                            Toast.makeText(context,"TPMS启动失败"+sp.getAutostart().contains("noflag")+"-"+BluetoothAdapter.getDefaultAdapter().isEnabled(),Toast.LENGTH_LONG).show();
                        }

                    }
                }.start();

            }else if(Intent.ACTION_SHUTDOWN.equals(action)){
                context.stopService(new Intent(context, SysControllerService.class));
                context.stopService(new Intent(context, SysManagerService.class));
//                Intent mainActivityIntent = new Intent(context, AboutMeAfterSaleSupportActivity.class);  // 要启动的Activity
//                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(mainActivityIntent);
            }else if("com.android.action_acc_off".equals(action))
            {
                BluetoothAdapter.getDefaultAdapter().disable();
                context.stopService(new Intent(context, SysControllerService.class));
                context.stopService(new Intent(context, SysManagerService.class));
            }

        } else if (SharePreferenceUtil.CONNECT_TYPE_USB == sp.getConnectType()) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)||"hongkingtest".equals(action)) {
                Toast.makeText(context, "监听到"+action, Toast.LENGTH_SHORT).show();
                boolean flag=false;
                UsbManager usbManager = (UsbManager) context.getSystemService("usb");
                HashMap<String,UsbDevice> test=usbManager.getDeviceList();
                Iterator ite = test.keySet().iterator();

                while(ite.hasNext()){

                    String   key   =   (String)ite.next();   //   key
                    String v=test.get(key).toString();
                          if(v.contains("29987")||v.contains("21795")){
                              flag=true;
                          }
                }
                if (flag) {
                    sp.setshutdowntime("USBin");
                    Toast.makeText(context, "TPMS启动中。"/*+"USB接口数量为"+String.valueOf(usbManager.getDeviceList().size())*/, Toast.LENGTH_SHORT).show();
                    new CountDownTimer(30000, 5000) {

                        @Override
                        public void onTick(long millisUntilFinished) {
//                            Toast.makeText(context, "TPMS启动中"+action+"时间是"+millisUntilFinished, Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFinish() {
//                            Toast.makeText(context, "TPMS正在连接"+String.valueOf(SysApplication.USBState), Toast.LENGTH_SHORT).show();
                            if (!SysApplication.USBState && SysApplication.conneceactno == 0) {
                                sp.setFirsttimeoncreate("yes");
//                                Toast.makeText(context, "开启ConnectChooseActivity", Toast.LENGTH_LONG).show();
                                 Intent mainActivityIntent = new Intent(context, ConnectChooseActivity.class);  // 要启动的Activity
                                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(mainActivityIntent);
                            }else{
//                                Toast.makeText(context, "已经存在选择界面，或者已连接" + SysApplication.USBState + String.valueOf(SysApplication.conneceactno), Toast.LENGTH_LONG).show();
                            }

                        }
                    }.start();
                } else {
                    Toast.makeText(context, "未检测到USB接收机", Toast.LENGTH_LONG).show();
                }

            } else if (Intent.ACTION_SHUTDOWN.equals(action)) {
                sp.setFirsttimeoncreate("test");
                context.stopService(new Intent(context, UsbControllerService.class));
                context.stopService(new Intent(context, SysManagerService.class));
            }
        }
    }
        private void simpleNotify(Context context,String service){
            //为了版本兼容  选择V7包下的NotificationCompat进行构造
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            //Ticker是状态栏显示的提示
            builder.setTicker("TPMS已连接");
            //第一行内容  通常作为通知栏标题
            builder.setContentTitle("TPMS");
            //第二行内容 通常是通知正文
            builder.setContentText("TPMS已连接"+service+"设备");
//            //第三行内容 通常是内容摘要什么的 在低版本机器上不一定显示
//            builder.setSubText("这里显示的是通知第三行内容！");
            //ContentInfo 在通知的右侧 时间的下面 用来展示一些其他信息
            builder.setContentInfo("2");
            //number设计用来显示同种通知的数量和ContentInfo的位置一样，如果设置了ContentInfo则number会被隐藏
            builder.setNumber(2);
            //可以点击通知栏的删除按钮删除
            builder.setAutoCancel(true);
            //系统状态栏显示的小图标
            builder.setSmallIcon(R.drawable.ic_launcher1);
            //下拉显示的大图标
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher1));
            Intent intent = new Intent(context,AppMainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(context,1,intent,0);
            //点击跳转的intent
            builder.setContentIntent(pIntent);
            //通知默认的声音 震动 呼吸灯
            builder.setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND);
            Notification notification = builder.build();
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(1, notification);
        }

//    private void initBluetooth() {
//        sp = new SharePreferenceUtil(mcontext);
//        app = (SysApplication) mcontext.getApplicationContext();
//        TPMSParam = app.TPMSParam;
//        boolean flag = false;
//        TPMSParam.device_type = TPMSParam.DEVICE_TYPE_BT;
////		startActivity(new Intent(this, AppEntryActivity.class));
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBluetoothAdapter == null) {
//            Toast.makeText(mcontext, "no bluetooth device found", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (!mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.enable();
//        }
//        sp.setConnectType(SharePreferenceUtil.CONNECT_TYPE_BLUETOOTH);
//        String btAddr = TPMSParam.sConnectedBlueAddr;
//        if (btAddr == null || "".equals(btAddr.trim())) {
//            if (deviceAddr == null || "".equals(deviceAddr.trim())) {
////                Intent serverIntent = new Intent(mcontext, BlueSetupActivity.class);
////                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
//            }
//        } else {
//            setupConnect(0x01);
//        }
//
//
//    }
//    void setupConnect(int mode) {
//        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//        connectMode = mode;
//        if (pairedDevices.size() > 0) {
//            for (BluetoothDevice device : pairedDevices) {
//                if (device.getAddress().equals(TPMSParam.sConnectedBlueAddr)) {
//                    startConnect(TPMSParam.sConnectedBlueAddr);
//                    return;
//                }
//            }
//        } else {
////            Intent serverIntent = new Intent(ConnectChooseActivity.this, BlueSetupActivity.class);
////            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
//        }
//
//    }
    boolean isSendSucc = false;
//    void startConnect(final String address) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (!isSendSucc) {
//                    Log.i("20161130", "start connect addr ..." + address);
//                    Intent i = new Intent(SysControllerService.CONNECT_REQUEST_ACTION);
//                    i.putExtra(DEVICE_ADDRESS, address);
//                    mcontext.sendBroadcast(i);
//                    try {
//                        Thread.sleep(2000);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
//
////        DialogWait = onCreateDialog(0x00, "");
//    }
    }
