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

import java.io.UnsupportedEncodingException;

import com.hongking.hktpms.SysParam.SYS_MODE;
import com.hongking.hktpms.Utils.CH34xAndroidDriver;
import com.hongking.hktpms.Utils.SharePreferenceUtil;
import com.hongking.hktpms.views.EventBus;
import com.hongking.hktpms.views.EventBus_INITCONFIG;
import com.hongking.hktpms.views.EventBus_Media;
import com.hongking.hktpms.views.EventBus_bt_connect;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
//import android.widget.Toast;

public class UsbControllerService extends Service {
    private SysParam TPMSParam;
    SysApplication app;
    // Debugging
    private static final String TAG = "UsbControllerService";
    private static final boolean D = true;
    private boolean isRunning;
    // private boolean isConnecting = false;
    public static final String SEND_MSG_FROM_USB_ACTION = "TPMS_SEND_MSG_FROM_USB_ACTION";
    public static final String CONNECT_REQUEST_ACTION = "TPMS_CONNECT_REQUEST_ACTION";
    public static final String DISCONNECT_REQUEST_ACTION = "TPMS_DISCONNECT_REQUEST_ACTION";
    public static final String RECONNECT_ACTION = "TPMS_RECONNECT_REQUEST_ACTION";
    public static final String REQUEST_ECHO_ACTION = "TPMS_REQUEST_ECHO_ACTION";
    public static final String TOAST = "toast";

    public static final String CONNECT_MESSAGE_ACTION = "TPMS_CONNECT_MESSAGE_ACTION";
    public static final String DISCONNECT_MESSAGE_ACTION = "TPMS_DISCONNECT_MESSAGE_ACTION";
    public static final String CONNECT_LOST_MESSAGE_ACTION = "CONNECT_LOST_MESSAGE_ACTION";

    public static final String NOTIFY_UI = "TPMS_NOTIFY_UI";
    public static final String NOTIFY_TIRE_RESULT = "TPMS_NOTIFY_TIRE_RESULT";
    public static final String AUTO_SW_MAINPAGE = "AUTO_SW_MAINPAGE";

    public static final String INCOMING_MSG = "TPMS_INCOMING_MSG";
    public static final String OUTGOING_MSG = "TPMS_OUTGOING_MSG";
    public static final String KEY_ECHO_PREF = "TPMS_KEY_ECHO_PREF";
    public static final String STOP_SERVICE = "STOP_SELF";
    public static final String SERVICE_REPORT = "service is ok";

    public static final String DEVICE_ADDRESS = "device_address";
    public static final String DISCONNECT_DEVICE_ADDRESS = "disconnected_device_address";

    private MessageReceiver mBtMsgReceiver;
    private SerialPortConnModel mConnService = null;
    private MessageHandler msgHandler;

    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;

    Thread recPacketThread;

    Thread ActivityManagerThread;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_CONNECT = 5;
    public static final int MESSAGE_DISCONNECT = 6;
    public static final int MESSAGE_TOAST = 7;
    public static final int MESSAGE_CONNECT_LOST = 8;
    public static final int MESSAGE_RECONNECT = 9;
    public static final int MESSAGE_INIT_CONFIG = 10;
    public static final int MESSAGE_CONNECT_AGAIN = 11;
    public static final int MESSAGE_CONNECT_RESTART = 12;
    private DataMem dataMem;

    @Override
    public void onCreate() {
        if (D)
            Log.i(TAG, "[onCreate]");

        app = (SysApplication) getApplicationContext();
        TPMSParam = app.TPMSParam;
        TPMSParam.device_type = TPMSParam.DEVICE_TYPE_USB;
        TPMSParam.blueStatus.ConnectingSet(false);
        msgHandler = new MessageHandler();

        // 注册消息接收器
        IntentFilter mFilter01, mFilter02, mFilter03, mFilter04, mFilter05, stateChangedFilter;
        mFilter01 = new IntentFilter(SEND_MSG_FROM_USB_ACTION);
        mFilter02 = new IntentFilter(CONNECT_REQUEST_ACTION);
        mFilter03 = new IntentFilter(DISCONNECT_REQUEST_ACTION);
        mFilter04 = new IntentFilter(STOP_SERVICE);
        mFilter05 = new IntentFilter(RECONNECT_ACTION);
        stateChangedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mBtMsgReceiver = new MessageReceiver();
        registerReceiver(mBtMsgReceiver, mFilter01);
        registerReceiver(mBtMsgReceiver, mFilter02);
        registerReceiver(mBtMsgReceiver, mFilter03);
        registerReceiver(mBtMsgReceiver, mFilter04);
        registerReceiver(mBtMsgReceiver, mFilter05);
        registerReceiver(mBtMsgReceiver, stateChangedFilter);
        // TPMSParam.showNotification(this,0x00);

        dataMem = new DataMem();
        isRunning = true;
        recPacketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    PacketAnalysis();
                }
            }
        });
        recPacketThread.start();

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        de.greenrobot.event.EventBus.getDefault().register(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (D)
            Log.i(TAG, "[onStartCommand]");
        super.onStart(intent, startId);
//        if (mConnService != null)
//            mConnService.disconnectDevice();
        if (mConnService != null)
            mConnService.closeDevice();
        mConnService = null;
        if (mConnService == null) {
            startServiceSockets();
            Intent i = new Intent(SERVICE_REPORT);
            sendBroadcast(i);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (D)
            Log.e(TAG, "[onDestroy]");
        isRunning = false;
        terminatedServiceSockets();
        de.greenrobot.event.EventBus.getDefault().unregister(this);
        unregisterReceiver(mBtMsgReceiver);
        dataMem = null;
        TPMSParam.blueStatus.ConnectingSet(false);
        // stopSelf();
        if (null != wakeLock) {
            Log.w(TAG, "release the partial lock due to server onDestroy");
            wakeLock.release();
        }
        super.onDestroy();
    }
    public void onEvent(EventBus_INITCONFIG eventBus) {
        if(eventBus.getTest()==0) {
            Log.e("20161130","USBControllServiceEVENTBUSMESSAGE_INIT_CONFIG");
            if (mConnService != null) mConnService.startReadThread();
        }
    }
    @Override
    public IBinder onBind(Intent arg0) {
        if (D)
            Log.e(TAG, "[onBind]");
        return null;
    }

    /**
     * USB连接消息处理
     *
     * @author Administrator
     */
    private class MessageHandler extends Handler {
        @SuppressLint("NewApi")
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                case MESSAGE_DEVICE_NAME:
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    int readLen = msg.arg1;
                    Log.i("dataExchange", "msg read len" + readLen);
                    if (dataMem != null) dataMem.write(readBuf, readLen);
                    break;
                case MESSAGE_INIT_CONFIG:
                    Log.e("20161130","USBControllServiceMESSAGE_INIT_CONFIG");
                    if (mConnService != null) mConnService.startReadThread();
                    break;
                case MESSAGE_CONNECT_AGAIN:
                    if (mConnService != null) mConnService.startSession();
                case MESSAGE_CONNECT_RESTART:
                    restartConn();
                case MESSAGE_CONNECT:
                    Log.v("20161205", "收到消息 " + "MESSAGE_CONNECT--------");

                    TPMSParam.blueStatus.ConnectingSet(true);
                    if (null == wakeLock) {
                        Log.i(TAG, "request partial wake lock");
                        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TPMSService");
                        wakeLock.acquire();
                    }
                    TPMSParam.sConnectedBlueAddr = "USB";
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v("20161205", "开启线程，准备握手");
                            setupContectToThread();
                        }
                    }).start();
                    break;
                case MESSAGE_DISCONNECT:
                    Log.v(TAG, "[handleMessage: " + "MESSAGE_DISCONNECT--------");
                    if (null != wakeLock) {
                        Log.i(TAG, "relase partial wake lock due to server disconnect");
                        wakeLock.release();
                        wakeLock = null;
                    }
                    sendBroadcast("usb device", UsbControllerService.DISCONNECT_MESSAGE_ACTION);

                    break;
                case MESSAGE_CONNECT_LOST:
                    Log.v(TAG, "[handleMessage: " + "MESSAGE_CONNECT_LOST--------");
                    TPMSParam.blueStatus.ConnectingSet(false);
                    sendBroadcast("usb device", UsbControllerService.CONNECT_LOST_MESSAGE_ACTION);
                    break;
                case MESSAGE_TOAST:
                    break;
                case MESSAGE_RECONNECT:
                    Log.i(TAG, "MESSAGE_RECONNECT");
                    if (!TPMSParam.blueStatus.isConnecting()) {
                        terminatedServiceSockets();
                        startServiceSockets();
                        // TODO
                        // UsbControllerService.this.connectTo(mDevice.getAddress());
                    } else {
                        Log.w(TAG, "bluethooth has already connected");
                    }
                    break;
            }
        }

    }


    private void restartConn() {
        if (mConnService != null) {
            dataMem = null;
            dataMem = new DataMem();
            isRunning = true;
            try {
                if (recPacketThread != null) recPacketThread.interrupt();
                recPacketThread = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            recPacketThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        PacketAnalysis();
                    }
                }
            });
            recPacketThread.start();
            TPMSParam.blueStatus.ConnectingSet(false);
            mConnService.reconnectDevice();


        }
    }

    private void sendBroadcast(String str, String action) {
        String displayString = null;
        displayString = str;
        Intent i = new Intent(action);
        i.putExtra("STR", displayString);
        UsbControllerService.this.sendBroadcast(i);
    }

    private void sendBroadcast(int arg, String action) {
        Intent i = new Intent(action);
        i.putExtra("ARG", arg);
        UsbControllerService.this.sendBroadcast(i);
    }

    private int abandonTimes = 0;

    /**
     * 接收数据解析
     */
    private void PacketAnalysis() {
        Log.e("20170104","调用一次PacketAnalysis");
        byte recData[] = new byte[256];
        byte recRData[] = new byte[256];
        byte rtemp[] = new byte[4];
        int result[] = new int[1];
        int recLen;
        int offset;
        offset = 0x00;
        if (TPMSParam.blueStatus.connectingWait() == false) {// 没有连接时不进行数据监听
            return;
        }

        if (dataMem != null && dataMem.read(rtemp, 1, offset, 10000) == 0) {
            if (rtemp[0] == 0x02) {
                abandonTimes = 0;
                recData[offset++] = rtemp[0];
                // recLen++; /*长度低字节*/
                if (dataMem.read(rtemp, 1, offset, 1000) == 0) {
                    recLen = rtemp[0];
                    recData[offset++] = rtemp[0];
                    // recv_prt++; /*长度高字节*/
                    if (dataMem.read(rtemp, 1, offset, 1000) == 0) {
                        recLen |= (rtemp[0] << 8);
                        recData[offset++] = rtemp[0];
                        // Log.i(TAG,"Packet len "+recLen);
                        if (recLen >= 256) {
                            recLen = 256;
                        }
                        if (dataMem.read(recRData, recLen - 3, offset, 1000) == 0) {
                            for (int i = 0; i < recLen - 3; i++) {
                                recData[i + offset] = recRData[i];
                            }
                            int update = TPMSParam.mBluethoothData.onMsg(recData, recLen, result);
                            if (update == 0x01) {// 判断是否要更新ui
                                String displaymsg = new String("update tyre info");
                                int alarmflag = 0;
                                for (int i = 0; i < 4; i++) {
                                    if ((TPMSParam.tyreData.getInfoValue(i).alarm_flag & 0x40) == 0x40) {
                                        alarmflag = 1;
                                    }
                                }
                                if (alarmflag == 1) {
                                    de.greenrobot.event.EventBus.getDefault().post(new EventBus_Media(1));
                                    Intent intent = new Intent(this, AppMainActivity.class);
                                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                    intent.setAction(Intent.ACTION_MAIN);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                    startActivity(intent);
                                }
                                // Log.i(TAG,"rec the tire info !");
                                app.WarnNotifation(this);
                                sendBroadcast(displaymsg, NOTIFY_UI);
                            } else if (update == 0x02) {

                                Log.i(TAG, "rec the change result!");
                                sendBroadcast(result[0], NOTIFY_TIRE_RESULT);
                            }
                        } else {/* 错误处理 */
                        }
                    }
                }
            } else {
                abandonTimes++;
                Log.w(TAG, String.format("abandon data %x ", recData[0]));
            }
            if (abandonTimes == 20) {
//				msgHandler.obtainMessage(UsbControllerService.MESSAGE_CONNECT_RESTART).sendToTarget();
            }
        } else {
            if (TPMSParam.mBluethoothData.mStatus == SYS_MODE.MODE_SETUP) {
                app.changeMode(SysParam.SYS_MODE.MODE_HEARTBEAT);
            }
        }

    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send;
            try {
                send = message.getBytes("ISO-8859-1");
                if (mConnService != null)
                    mConnService.write(send);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    /**
     * 连接到设备
     *
     * @param deviceAddress
     */
    private void connectTo(String deviceAddress) {
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        if (mConnService != null)
            // TODO
            // mConnService.connectTo(device);
            Log.e(TAG, "connectTo!!!");
    }

    /**
     * 断开设备
     *
     * @param address
     */
    private void disconnectTo() {
        if (mConnService != null)
            mConnService.disconnectDevice();
        Log.e(TAG, "disconnectTo!!!");
    }

    private void reconnectTo() {
        if (mConnService != null) {
            mConnService.reconnectDevice();
        }
    }

    /**
     * 终止连接
     */
    private synchronized void terminatedServiceSockets() {
        // mConnService.terminated();
        if (mConnService != null)
            mConnService.closeDevice();
        mConnService = null;
        Log.e(TAG, "terminatedAllSockets!!!");
    }

    /**
     * 初始化并连接
     */
    private synchronized void startServiceSockets() {
        if (mConnService == null) {
            // mConnService.terminated();
            mConnService = new SerialPortConnModel(this, msgHandler);
            mConnService.startSession();
            Log.e("20161205", "初始化并连接");
        }
    }

    public class LocalBinder extends Binder {
        UsbControllerService getService() {
            return UsbControllerService.this;
        }
    }

    /////////////////////////////// 接收机握手///////////////////////////////////////////////////////

    private synchronized int setupConnectTo() {
        int ret = 0;
        if (D)
            Log.i(TAG, "setupConnectTo start--");
        // TPMSParam.mBluethoothData.adContext(this);
        for (int i = 0; i < 2; i++) {
            try {
                super.wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ret = TPMSParam.mBluethoothData.protocolHandshake();
            if (ret != 0x00) {
                Log.e("20161205", "================> protocolHandshake"+ret);
                continue;
            }
            ret = TPMSParam.mBluethoothData.ProtocolModeChange(SysParam.SYS_MODE.MODE_SETUP);
            if (ret != 0x00) {
                Log.e("20161205", "================> ProtocolModeChange"+ret);
                continue;
            }
            ret = TPMSParam.mBluethoothData.ProtocolSysParamGet();
            if (ret != 0x00) {
                Log.e("20161205", "================> ProtocolSysParamGet"+ret);
                continue;
            }
            ret = TPMSParam.mBluethoothData.ProtocolTireIDGet();
            if (ret != 0x00) {
                Log.e("20161205", "================> ProtocolTireIDGet"+ret);
                continue;
            }
            ret = TPMSParam.mBluethoothData.ProtocolAbout();
            if (ret != 0x00) {
                Log.e("20161205", "================> ProtocolAbout"+ret);
                continue;
            }
            ret = TPMSParam.mBluethoothData.ProtocolRfCpGet();
            if (ret != 0x00) {
                Log.e("20161205", "================> ProtocolRfCpGet"+ret);
                continue;
            }
            TPMSParam.mBluethoothData.ProtocolModeChange(SysParam.SYS_MODE.MODE_NORM);
            TPMSParam.tyreData.resetUpdateStatus();// 清除所有数据
            break;
        }
        SysApplication.usbhandflag=true;
        Log.e("20161205", "--------------------setupConnectTo ret " + ret);
        if (ret != 0x00) {
            Log.e("20161205", "ret的值是：" + ret+"发送eventbus重新连接");
//            de.greenrobot.event.EventBus.getDefault().post(new EventBus_bt_connect(0));
//            msgHandler.obtainMessage(MESSAGE_CONNECT_RESTART).sendToTarget();
//			reconnectTo();
//			disconnectTo();
        } else {

        }
        return ret;
    }

    /**
     * 接收器握手
     */
    void setupContectToThread() {
        Log.e("20161205","接收器握手1");
        int ret = setupConnectTo();
        Log.e("20161205","接收器握手2");
        sendBroadcast(ret, UsbControllerService.CONNECT_MESSAGE_ACTION);
        Log.e("20161205", "接收器握手3");
    }

    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * 消息处理器
     */
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // if(D) Log.e(TAG, "** ON RECEIVE **");
            String action = intent.getAction();

            if (action.equals(SEND_MSG_FROM_USB_ACTION)) {// 发送协议
                String state = intent.getExtras().getString("STATE");
                sendMessage(state);
            } else if (action.equals(RECONNECT_ACTION)) {
                if (mConnService != null) mConnService.reconnectDevice();
            } else if (action.equals(CONNECT_REQUEST_ACTION)) {
                // TODO
                // String deviceAddress =
                // intent.getExtras().getString(DEVICE_ADDRESS);
                // Log.i(TAG, "[onReceive] deviceAddress = " + deviceAddress);
                // UsbControllerService.this.connectTo(deviceAddress);
            } else if (action.equals(DISCONNECT_REQUEST_ACTION)) {
                Log.i(TAG, "[onReceive] DISCONNECT_REQUEST_ACTION");
//				String deviceAddress = intent.getExtras().getString(DISCONNECT_DEVICE_ADDRESS);
                Log.i(TAG, "[onReceive] disconnect device  ");
                disconnectTo();
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Log.i(TAG, "[onReceive] ACTION_STATE_CHANGED");
                int currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (currentState) {
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "[onReceive] current state = ON");
                        startServiceSockets();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG, "[onReceive] current state = OFF");
                        terminatedServiceSockets();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(TAG, "[onReceive] current state = TURNING_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(TAG, "[onReceive] current state = TURNING_OFF");
                        break;
                }
            } else if (action.equals(STOP_SERVICE)) {
                Log.i(TAG, "STOP service self");
                stopSelf();
            }if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.e("20161129","UsbControllerServiceATTACHED");
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);//监听USB插拔，插入的时候使用此方法。
                SharePreferenceUtil sp = new SharePreferenceUtil(context);
                if (device != null && (device.getProductId() == 29987 || device.getProductId() == 221795)) {
                    if (sp.getConnectType() == SharePreferenceUtil.CONNECT_TYPE_USB) {
                        if(SysApplication.driver == null){
                            SysApplication.driver = new CH34xAndroidDriver((UsbManager) getSystemService(Context.USB_SERVICE), getApplicationContext(), "cn.wch.wchusbdriver.USB_PERMISSION");
                        }

                    }
                }
            } else {
                Log.e(TAG, "another action: " + action);
            }
        }

    }

    /*
        class DataMem {
            final int Maxlen = 1024;
            byte buf[] = new byte[Maxlen];
            volatile int inIndex;
            volatile int count;
            volatile int outIndex;

            DataMem() {
                Log.i(TAG, "data mem create");
                inIndex = 0x00;
                outIndex = 0x00;
                count = 0x00;
            }

            synchronized int read(byte buf[], int len, int offset, int time) {
                // Log.i(TAG,"datamem count="+count);
                Log.i("dataExchange", "read len=" + len + " mem len=" + count);
                if (count < len) {
                    try {
                        super.wait(time);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (count < len)
                    return 0x8a;
                for (int i = 0; i < len; i++) {
                    buf[i + offset] = this.buf[outIndex++];

                    if (outIndex >= Maxlen)
                        outIndex = 0x00;
                }
                count -= len;
                return 0;
            }

            synchronized int write(byte buf[], int len) {

                for (int i = 0; i < len; i++) {
                    this.buf[inIndex++] = buf[i];
                    if (inIndex >= this.Maxlen)
                        inIndex = 0x00;
                }

                count += len;
                Log.i("dataExchange", "write len" + count);
                super.notify();
                return 0;
            }

            synchronized void clean() {
                inIndex = 0x00;
                outIndex = 0x00;
                count = 0x00;

            }

        }*/
    class DataMem {
        final int Maxlen = 500;
        volatile byte r_buf[] = new byte[Maxlen];
        volatile int inpos, outpos;

        DataMem() {
            Log.i(TAG, "data mem create");
            inpos = 0x00;
            outpos = 0x00;
        }

        synchronized int read(byte buf[], int len, int offset, int time) {
            //Log.i("dataExchange", "read len=" + len + "mem len=" + count);
            int get_count = 0;
            long systime = System.currentTimeMillis();
            while (true) {

                if (this.inpos != this.outpos) {
                    systime = System.currentTimeMillis();
                    buf[get_count++] = this.r_buf[outpos++];
                    if (this.outpos >= this.Maxlen) {
                        this.outpos = 0;
                    }
                    if (get_count >= len) {
                        break;
                    }
                } else {
                    try {
                        super.wait(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (System.currentTimeMillis() - systime > time) {
                    return 0x8a;
                }
            }
            return 0;
        }

        synchronized int write(byte buf[], int len) {

            for (int i = 0; i < len; i++) {
                this.r_buf[this.inpos++] = buf[i];
                if (this.inpos >= this.Maxlen) {
                    this.inpos = 0x00;
                }
            }
            //Log.i("dataExchange", "write len" + count);
            super.notify();
            return 0;
        }

        synchronized void clean() {
            byte c_buf[] = new byte[1024];
            int get_count = 0;
            while (true) {
                if (this.inpos != this.outpos) {
                    c_buf[get_count++] = this.r_buf[this.outpos++];
                    if (this.outpos >= this.Maxlen) {
                        this.outpos = 0;
                    }
                } else {
                    break;
                }
            }
        }
    }
}
