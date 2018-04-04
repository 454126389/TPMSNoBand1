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

import com.hongking.hktpms.EventBus.Bluetoothlost;
import com.hongking.hktpms.SysParam.SYS_MODE;
import com.hongking.hktpms.views.EventBus_Media;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import de.greenrobot.event.EventBus;
//import android.widget.Toast;

public class SysControllerService extends Service {
    private SysParam TPMSParam;
    SysApplication app;
    // Debugging
    private static final String TAG = "SysControllerService";
    private static final boolean D = true;
    private boolean isRunning;
    // private boolean isConnecting = false;
    public static final String SEND_MSG_FROM_BT_ACTION = "TPMS_SEND_MSG_FROM_BT_ACTION";
    public static final String CONNECT_REQUEST_ACTION = "TPMS_CONNECT_REQUEST_ACTION";
    public static final String CONNECT_REQUEST_ACTION_SEND = "TPMS_CONNECT_REQUEST_ACTION_SEND";
    public static final String DISCONNECT_REQUEST_ACTION = "TPMS_DISCONNECT_REQUEST_ACTION";
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

    /**
     * 广播接收器
     */
    private MessageReceiver mBtMsgReceiver;
    // private BluetoothAdapter mBluetoothAdapter =
    // BluetoothAdapter.getDefaultAdapter();
    private BluetoothConnModel mConnService = null;
    /**
     * 蓝牙消息接收器
     */
    private MessageHandler msgHandler;
    private BluetoothDevice mDevice = null;

    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;

    /**
     * 数据解析线程
     */
    Thread recPacketThread;

    Thread ActivityManagerThread;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_CONNECT = 5;
    public static final int MESSAGE_DISCONNECT = 6;
    public static final int MESSAGE_TOAST = 7;
    public static final int MESSAGE_CONNECT_LOST = 8;
    public static final int MESSAGE_RECONNECT = 9;
    private DataMem dataMem;

    @Override
    public void onCreate() {
        if (D) Log.i(TAG, "[onCreate]");
        app = (SysApplication) getApplicationContext();
        //全局数据参数
        TPMSParam = app.TPMSParam;
        TPMSParam.blueStatus.ConnectingSet(false);
        //蓝牙动作接收器
        msgHandler = new MessageHandler();

        // 注册消息接收器
        IntentFilter mFilter01, mFilter02, mFilter03, mFilter04, stateChangedFilter;
        mFilter01 = new IntentFilter(SEND_MSG_FROM_BT_ACTION);
        mFilter02 = new IntentFilter(CONNECT_REQUEST_ACTION);
        mFilter03 = new IntentFilter(DISCONNECT_REQUEST_ACTION);
        mFilter04 = new IntentFilter(STOP_SERVICE);
        stateChangedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mBtMsgReceiver = new MessageReceiver();
        registerReceiver(mBtMsgReceiver, mFilter01);
        registerReceiver(mBtMsgReceiver, mFilter02);
        registerReceiver(mBtMsgReceiver, mFilter03);
        registerReceiver(mBtMsgReceiver, mFilter04);
        registerReceiver(mBtMsgReceiver, stateChangedFilter);

        dataMem = new DataMem();
        isRunning = true;
        //启动数据解析线程
        recPacketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    PacketAnalysis();
                }
            }
        });
        recPacketThread.start();
//        Toast.makeText(this,"开启PacketAnalysis",Toast.LENGTH_SHORT).show();

        //电源管理
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        if (D) Log.e(TAG, "[onDestroy]");
        isRunning = false;
        terminatedServiceSockets();
        unregisterReceiver(mBtMsgReceiver);
        // stopSelf();
        if (null != wakeLock) {
            Log.w(TAG, "release the partial lock due to server onDestroy");
            wakeLock.release();
        }
        // TPMSParam.blueStatus.ConngectingSet(false);
        super.onDestroy();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (D) Log.i(TAG, "[onStartCommand]");
        super.onStart(intent, startId);
        if (mConnService == null && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            startServiceSockets();
            Intent i = new Intent(SERVICE_REPORT);
            sendBroadcast(i);

        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        if (D)
            Log.e(TAG, "[onBind]");
        return null;
    }

    /**
     * 蓝牙模块消息接收器
     *
     * @author Administrator
     */
    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                case MESSAGE_DEVICE_NAME:
                    mDevice = (BluetoothDevice) msg.obj;
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    int readLen = msg.arg1;
                    Log.i("dataExchange", "msg read len" + readLen);
                    dataMem.write(readBuf, readLen);
                    break;
                case MESSAGE_CONNECT:
                    Log.v(TAG, "[handleMessage: " + "MESSAGE_CONNECT--------");
                    mDevice = (BluetoothDevice) msg.obj;
                    String deviceName = mDevice.getAddress();
                    TPMSParam.blueStatus.ConnectingSet(true);
                    if (null == wakeLock) {
                        Log.i(TAG, "request partial wake lock");
                        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TPMSService");
                        wakeLock.acquire();
                    }
                    TPMSParam.sConnectedBlueAddr = deviceName;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            setupContectToThread();
                        }
                    }).start();
                    break;
                case MESSAGE_DISCONNECT:
                    Log.v(TAG, "[handleMessage: " + "MESSAGE_DISCONNECT--------");
                    mDevice = (BluetoothDevice) msg.obj;
                {
                    String deviceName1 = mDevice.getName();
                    if (null != wakeLock) {
                        Log.i(TAG, "relase partial wake lock due to server disconnect");
                        wakeLock.release();
                        wakeLock = null;
                    }
                    sendBroadcast(deviceName1, SysControllerService.DISCONNECT_MESSAGE_ACTION);
                }
                break;
                case MESSAGE_CONNECT_LOST:
                    Log.v("20161215", "[handleMessage: " + "MESSAGE_CONNECT_LOST--------");
                    TPMSParam.blueStatus.ConnectingSet(false);
                    mDevice = (BluetoothDevice) msg.obj;
                    String deviceName2 = mDevice.getName();
                    EventBus.getDefault().post(new Bluetoothlost());
//                    sendBroadcast(deviceName2, SysControllerService.CONNECT_LOST_MESSAGE_ACTION);
                    break;
                case MESSAGE_TOAST:
                    break;
                case MESSAGE_RECONNECT:
                    Log.i(TAG, "MESSAGE_RECONNECT");
                    if (!TPMSParam.blueStatus.isConnecting()) {
                        terminatedServiceSockets();
                        startServiceSockets();
                        SysControllerService.this.connectTo(mDevice.getAddress());
                    } else {
                        Log.w(TAG, "bluethooth has already connected");
                    }
                    break;
            }
        }

    }

    /**
     * 发送广播STR
     *
     * @param str
     * @param action
     */
    private void sendBroadcast(String str, String action) {
        String displayString = null;
        displayString = str;
        Intent i = new Intent(action);
        i.putExtra("STR", displayString);
        SysControllerService.this.sendBroadcast(i);
    }

    /**
     * 发送广播ARG
     *
     * @param arg
     * @param action
     */
    private void sendBroadcast(int arg, String action) {
        Intent i = new Intent(action);
        i.putExtra("ARG", arg);
        SysControllerService.this.sendBroadcast(i);
    }

    /**
     * 监听数据进行解析
     */
    private void PacketAnalysis() {
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
        if (dataMem.read(rtemp, 1, offset, 10000) == 0) {
            if (rtemp[0] == 0x02) {
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
                        } else { /* 错误处理 */
                        }
                    }
                }
            } else {
                Log.w(TAG, String.format("abandon data %x ", recData[0]));
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
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send;
            try {
                send = message.getBytes("ISO-8859-1");
                if (mConnService != null)
                    mConnService.writeToAllSockets(send);
            } catch (UnsupportedEncodingException e) {
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
        try {
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
            if (mConnService != null)
                mConnService.connectTo(device);
            Log.e("20161202", "开始进行设备连接");
        } catch (IllegalArgumentException I) {
            Toast.makeText(this, "不是一个有效的蓝牙地址", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 断开设备
     *
     * @param address
     */
    private void disconnectTo(String address) {
        if (mConnService != null)
            mConnService.disconnectSocketFromAddress(address);
        Log.e(TAG, "disconnectTo!!!");
    }


    /**
     * 终止服务连接
     */
    private synchronized void terminatedServiceSockets() {
        // mConnService.terminated();
        if (mConnService != null)
            mConnService.terminated();
        mConnService = null;
        Log.e(TAG, "terminatedAllSockets!!!");
    }

    /**
     * 初始化，开始服务连接
     */
    private synchronized void startServiceSockets() {
        if (mConnService == null) {
            // mConnService.terminated();
            Log.e("20161212", "初始化，开始服务连接,这里应该要重新开启socket");
            mConnService = new BluetoothConnModel(this, msgHandler);
            mConnService.startSession();
        }
    }

    public class LocalBinder extends Binder {
        SysControllerService getService() {
            return SysControllerService.this;
        }
    }

    /////////////////////////////// 接收机握手///////////////////////////////////////////////////////

    /**
     * 接收机握手
     *
     * @return
     */
    private synchronized int setupConnectTo() {
        int ret = 0;
        if (D)
            Log.i(TAG, "setupConnectTo start--");
        // TPMSParam.mBluethoothData.adContext(this);
        for (int i = 0; i < 5; i++) {
            ret = TPMSParam.mBluethoothData.protocolHandshake();
            if (ret != 0x00)
                continue;
            ret = TPMSParam.mBluethoothData.ProtocolModeChange(SysParam.SYS_MODE.MODE_SETUP);
            if (ret != 0x00)
                continue;
            ret = TPMSParam.mBluethoothData.ProtocolSysParamGet();
            if (ret != 0x00)
                continue;
            ret = TPMSParam.mBluethoothData.ProtocolTireIDGet();
            if (ret != 0x00)
                continue;
            ret = TPMSParam.mBluethoothData.ProtocolAbout();
            if (ret != 0x00)
                continue;
            ret = TPMSParam.mBluethoothData.ProtocolRfCpGet();
            if (ret != 0x00)
                continue;
            TPMSParam.mBluethoothData.ProtocolModeChange(SysParam.SYS_MODE.MODE_NORM);
            TPMSParam.tyreData.resetUpdateStatus();// 清除所有数据
            break;
        }
        if (D)
            Log.i(TAG, "setupConnectTo ret" + ret);
        if (ret != 0x00) {
            disconnectTo(TPMSParam.sConnectedBlueAddr);
        }
        return ret;
    }

    /**
     * 连接协议
     */
    void setupContectToThread() {
        int ret = setupConnectTo();
        sendBroadcast(ret, SysControllerService.CONNECT_MESSAGE_ACTION);
        Log.e("20161205", "连接协议");
    }

    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * TODO
     * 消息处理器
     */
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // if(D) Log.e(TAG, "** ON RECEIVE **");
            String action = intent.getAction();

            if (action.equals(SEND_MSG_FROM_BT_ACTION)) {// 发送协议
                String state = intent.getExtras().getString("STATE");
                sendMessage(state);
            } else if (action.equals(CONNECT_REQUEST_ACTION)) {
                String deviceAddress = intent.getExtras().getString(DEVICE_ADDRESS);
                Log.i("20161202", "第一次接收到广播，准备开始连接，地址是 ：" + deviceAddress);
                if (!SysApplication.BluetoothState) {
                    Log.i("20161202", "已经连接成功，不在进行连接" + deviceAddress);
                    SysControllerService.this.connectTo(deviceAddress);
//                    sendBroadcast(new Intent(CONNECT_REQUEST_ACTION_SEND));
                }
            } else if (action.equals(DISCONNECT_REQUEST_ACTION)) {
                Log.i(TAG, "[onReceive] DISCONNECT_REQUEST_ACTION");
                String deviceAddress = intent.getExtras().getString(DISCONNECT_DEVICE_ADDRESS);
                Log.i(TAG, "[onReceive] disconnect device address = " + deviceAddress);
                SysControllerService.this.disconnectTo(deviceAddress);
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
                        // TPMSParam.blueStatus.ConnectingSet(false);
//					terminatedServiceSockets();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(TAG, "[onReceive] current state = TURNING_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(TAG, "[onReceive] current state = TURNING_OFF");
                        break;
                }
                // Log.i(TAG, "[onReceive] current state = "+currentState);
                // BluetoothConnController.this.disconnectTo(deviceAddress);
            } else if (action.equals(STOP_SERVICE)) {
                Log.i(TAG, "STOP service self");
                // if (mConnService != null) mConnService.terminated();
                // mConnService = null;
                stopSelf();
                // unregisterReceiver(mBtMsgReceiver);
            } else {
                Log.e(TAG, "another action: " + action);
            }
        }

    }

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

    public boolean isRunningForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (currentPackageName != null && currentPackageName.equals(getPackageName())) {
            return true;
        }
        return false;
    }
}
