package com.hongking.hktpms;

import android.app.ActivityManager;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.hongking.hktpms.Utils.SharePreferenceUtil;
import com.hongking.oemtpms.R;

import java.util.List;
import java.util.Set;


/**
 * Created by Administrator on 2016/11/24.
 */

public class ConncectBroadcastReceiver extends BroadcastReceiver {
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
        this.mcontext = context;
        SharePreferenceUtil sp = new SharePreferenceUtil(context);
        if (SharePreferenceUtil.CONNECT_TYPE_BLUETOOTH == sp.getConnectType()) {
//            Toast.makeText(context, "start startbt service", Toast.LENGTH_LONG).show();
       /*     context.startService(new Intent(context, SysControllerService.class));
            context.startService(new Intent(context, SysManagerService.class));
            initBluetooth();*/
//            simpleNotify(context, "蓝牙");
        } else if (SharePreferenceUtil.CONNECT_TYPE_USB == sp.getConnectType()) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
//                sp.setFirsttimeoncreate("no");
                Log.e("20161129", "ConncectBroadcastReceiverACTION_USB_DEVICE_DETACHED");
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                device.getProductId();
                if (device != null && (device.getProductId() == 29987 || device.getProductId() == 221795)) {
//                    context.sendBroadcast(new Intent(UsbControllerService.DISCONNECT_REQUEST_ACTION));
//                    context.stopService(new Intent(context,UsbControllerService.class));
//                    context.stopService(new Intent(context,SysManagerService.class));
//                    sp.setFirsttimeoncreate("no");
                    SysApplication.USBState=false;
                    Toast.makeText(context,"USB已被拔出" , Toast.LENGTH_SHORT).show();
                    sp.setshutdowntime("USBout");
                    context.sendBroadcast(new Intent(UsbControllerService.DISCONNECT_REQUEST_ACTION));
//                    context.startActivity(new Intent(context, ConnectChooseActivity.class));
                    context.stopService(new Intent(context, UsbControllerService.class));
                    context.stopService(new Intent(context, SysManagerService.class));
                } else {
                    context.sendBroadcast(new Intent(UsbControllerService.RECONNECT_ACTION));
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.e("20161129", "ConncectBroadcastReceiverACTION_USB_DEVICE_ATTACHED");
//                sp.setFirsttimeoncreate("no");
//                Toast.makeText(context,"监听到USB插入，该方法已屏蔽。",Toast.LENGTH_LONG).show();
//                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                device.getProductId();
//                if (device != null && (device.getProductId() == 29987 || device.getProductId() == 221795)) {
////                    if (isServiceWork(context, "com.hongking.hktpms.UsbControllerService")) {
////                        context.stopService(new Intent(context, UsbControllerService.class));
////                        context.stopService(new Intent(context, SysManagerService.class));
////                    }
////                    context.startService(new Intent(context, UsbControllerService.class));
////                    context.startService(new Intent(context, SysManagerService.class));201612221413屏蔽
//                } else {
//                }
            }
        }
    }

    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    private void simpleNotify(Context context, String service) {
        //为了版本兼容  选择V7包下的NotificationCompat进行构造
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        //Ticker是状态栏显示的提示
        builder.setTicker("TPMS已连接");
        //第一行内容  通常作为通知栏标题
        builder.setContentTitle("TPMS");
        //第二行内容 通常是通知正文
        builder.setContentText("TPMS已连接" + service + "设备");
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
        Intent intent = new Intent(context, AppMainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 1, intent, 0);
        //点击跳转的intent
        builder.setContentIntent(pIntent);
        //通知默认的声音 震动 呼吸灯
        builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
        Notification notification = builder.build();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1, notification);
    }

    private void initBluetooth() {
        sp = new SharePreferenceUtil(mcontext);
        app = (SysApplication) mcontext.getApplicationContext();
        TPMSParam = app.TPMSParam;
        boolean flag = false;
        TPMSParam.device_type = TPMSParam.DEVICE_TYPE_BT;
//		startActivity(new Intent(this, AppEntryActivity.class));
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(mcontext, "no bluetooth device found", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.enable();
            return;
        }
        sp.setConnectType(SharePreferenceUtil.CONNECT_TYPE_BLUETOOTH);
        String btAddr = TPMSParam.sConnectedBlueAddr;
        if (btAddr == null || "".equals(btAddr.trim())) {
            if (deviceAddr == null || "".equals(deviceAddr.trim())) {
//                Intent serverIntent = new Intent(mcontext, BlueSetupActivity.class);
//                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            }
        } else {
            setupConnect(0x01);
        }


    }
    void setupConnect(int mode) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        connectMode = mode;
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(TPMSParam.sConnectedBlueAddr)) {
                    startConnect(TPMSParam.sConnectedBlueAddr);
                    return;
                }
            }
        } else {
//            Intent serverIntent = new Intent(ConnectChooseActivity.this, BlueSetupActivity.class);
//            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        }

    }
    boolean isSendSucc = false;
    void startConnect(final String address) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isSendSucc) {
                    Log.i("20161130", "start connect addr ..." + address);
                    Intent i = new Intent(SysControllerService.CONNECT_REQUEST_ACTION);
                    i.putExtra(DEVICE_ADDRESS, address);
                    mcontext.sendBroadcast(i);
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

//        DialogWait = onCreateDialog(0x00, "");
    }
}


