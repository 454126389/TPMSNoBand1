package com.hongking.hktpms;

import com.google.gson.Gson;
import com.hongking.hktpms.Utils.CH34xAndroidDriver;
import com.hongking.hktpms.Utils.DownloadApp;
import com.hongking.hktpms.Utils.HomeWatcher;
import com.hongking.hktpms.Utils.RspAppVersionFir;
import com.hongking.hktpms.Utils.SharePreferenceUtil;
import com.hongking.hktpms.views.EventBus;
import com.hongking.hktpms.views.EventBus_bt_connect;
import com.hongking.hktpms.views.TipPop2;
import com.hongking.oemtpms.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConnectChooseActivity extends BaseActivity implements OnClickListener {

    private final String TAG = "ConnectChooseActivity";
    private HomeWatcher mHomeWatcher;
    private final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";


    public static final String INCOMING_MSG = "INCOMING_MSG";
    public static final String OUTGOING_MSG = "OUTGOING_MSG";

    public static final String DEVICE_ADDRESS = "device_address";
    public static final String DISCONNECT_DEVICE_ADDRESS = "disconnected_device_address";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_DISCONNECT_DEVICE = 4;
    private static final int REQUEST_RECONNECT_DEVICE = 5;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String TOAST = "toast";
    private static final boolean D = true;
    private Thread thread;
    private boolean isRequest;
    private List<Map<String, Object>> mData;
    private Dialog DialogWait = null;
    private NotificationReceiver mBtMsgReceiver;
    private Dialog errDialog;
    private int connectMode = 0x00;
    private int requestTimes = 0;
    CountDownTimer countDownTimer;

    private RadioButton usbRadio, btRadio;
    private RadioGroup typeC;
    private RelativeLayout resultC;
    private LinearLayout mainview_ll;
    private LinearLayout all_ll;
    private ImageView img;
    private TextView title;
    private Button btnConfirm;

    private MsgReceiver receiver;
    private SysParam TPMSParam;
    private SysApplication app;
    private BluetoothAdapter mBluetoothAdapter = null;
    private SharePreferenceUtil sp;
    private boolean flag = true;//用来判断是否停止服务。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("20161129", "onCreateConnectChooseActivity");
        Log.e("20161215", "oncreateconnect这里SysApplication.BluetoothState=" + SysApplication.BluetoothState);
        SysApplication.conneceactno++;
        if (SysApplication.driver == null) {
            SysApplication.driver = new CH34xAndroidDriver((UsbManager) getSystemService(Context.USB_SERVICE), getApplicationContext(), ACTION_USB_PERMISSION);
        }
        if (2 == SysApplication.driver.ResumeUsbList()) {
            Log.e("dd","aa");
        }
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
//            Toast.makeText(this,String.valueOf(SysApplication.BluetoothState)+"1"+String.valueOf(SysApplication.USBState)+String.valueOf(SysApplication.conneceactno),Toast.LENGTH_SHORT).show();
            flag = false;//这个地方是点击APP图标，不停止服务，不然会重连。而是直接进入APP。
            finish();
            return;
        }
       if (SysApplication.conneceactno > 1) {
//            Toast.makeText(this, "数量为：" + String.valueOf(SysApplication.conneceactno) + "finish掉", Toast.LENGTH_SHORT).show();
            finish();
           Log.e("dd","aa1");
            return;
        }
        if (SysApplication.USBState || SysApplication.BluetoothState) {
            Log.e("dd","aa2");
//            Toast.makeText(this,String.valueOf(SysApplication.BluetoothState)+"3"+String.valueOf(SysApplication.USBState),Toast.LENGTH_SHORT).show();
//            flag = false;
//            finish();

            Intent intent = new Intent(this, AppMainActivity.class);
//                intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                intent.setAction(Intent.ACTION_MAIN);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);

//            return;  //先试看看finish，如果不行的话，就打开appmainactivity。
        }
//        Toast.makeText(this, String.valueOf(SysApplication.BluetoothState) + "4" + String.valueOf(SysApplication.USBState) + String.valueOf(SysApplication.conneceactno), Toast.LENGTH_SHORT).show();

        Log.e("dd","aa3");

        SysApplication.USBTHREADRUN = true;
        SysApplication.BTTHREADRUN = true;
        setContentView(R.layout.activity_connect);
        de.greenrobot.event.EventBus.getDefault().register(this);
        sp = new SharePreferenceUtil(this);


        if(sp.getRebootflag().contains("isflag"))
        {
            moveTaskToBack(true);
            sp.setRebootflag("reflag");
        }

        app = (SysApplication) getApplicationContext();
        TPMSParam = app.TPMSParam;
        init();
//        moveTaskToBack(true);
        receiver = new MsgReceiver();
//        sp.setFirsttimeoncreate("yes");
//		WindowManager windowManager = getWindowManager();
//		Display display = windowManager.getDefaultDisplay();
//		int screenWidth = screenWidth = display.getWidth();
//		int screenHeight = screenHeight = display.getHeight();
//		Toast.makeText(ConnectChooseActivity.this,screenWidth+"宽...高"+screenHeight,Toast.LENGTH_SHORT).show();

        //	SysApplication.driver = new CH34xUARTDriver((UsbManager) getSystemService(Context.USB_SERVICE),getApplicationContext(), ACTION_USB_PERMISSION);
        if (sp.getFirsttimeoncreate().contains("yes")) {
            new CountDownTimer(10000, 10000) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    if(sp.getFirsttimeoncreate().contains("yes")){
                        finish();
                    }

                }
            }.start();
        }
    }

    private void init() {
        usbRadio = (RadioButton) findViewById(R.id.conn_usb);
        all_ll = (LinearLayout) findViewById(R.id.all_ll);
        btRadio = (RadioButton) findViewById(R.id.conn_bluetooth);
        usbRadio.setChecked(true);
        title = (TextView) findViewById(R.id.conn_title);
        typeC = (RadioGroup) findViewById(R.id.conn_typeContent);
        img = (ImageView) findViewById(R.id.conn_img);
        resultC = (RelativeLayout) findViewById(R.id.conn_resultC);
        btnConfirm = (Button) findViewById(R.id.conn_confirm);
    }


    @Override
    protected void onStart() {
        super.onStart();
//        Toast.makeText(this, "ConnectAct_onStart", Toast.LENGTH_SHORT).show();
        if (SysApplication.USBState || SysApplication.BluetoothState) {
//            Toast.makeText(this,String.valueOf(SysApplication.BluetoothState)+"6"+String.valueOf(SysApplication.USBState),Toast.LENGTH_SHORT).show();
            flag = false;
//            startActivity(new Intent(this,AppMainActivity.class));
            finish();
            return;  //先试看看finish，如果不行的话，就打开appmainactivity。
        }
//        sp.setFirsttimeoncreate("yes");
        Log.e("20161129", "onStartConnectChooseActivity");
        if (true == TPMSParam.isOnExit) {
            this.finish();
        }
        if (SysApplication.BluetoothState && sp.getConnectType() == 2) {//如果蓝牙已经连接，就直接退出。
//            Toast.makeText(this, "蓝牙已连接，finish", Toast.LENGTH_SHORT).show();
            flag = false;
            finish();
            return;
        }
        requestTimes = 0;
        TPMSParam.activityStart();
      /*  if (SysApplication.driver == null) {
            SysApplication.driver = new CH34xAndroidDriver((UsbManager) getSystemService(Context.USB_SERVICE), getApplicationContext(), ACTION_USB_PERMISSION);
        }
        if (SysApplication.driver != null && SysApplication.driver.UsbFeatureSupported()) {
            try {
                if (2 == SysApplication.driver.ResumeUsbList()) {
//					Toast.makeText(this, "found usb device", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        if (SharePreferenceUtil.CONNECT_TYPE_BLUETOOTH == sp.getConnectType() && !SysApplication.BluetoothState) {
            btRadio.setChecked(true);
            sp.setConnectType(SharePreferenceUtil.CONNECT_TYPE_BLUETOOTH);
            TPMSParam.device_type = TPMSParam.DEVICE_TYPE_BT;
            if (isServiceWork(this, "com.hongking.hktpms.SysControllerService")) {
//                stopService(new Intent(this, SysControllerService.class));
//                stopService(new Intent(this, SysManagerService.class));
            } else {
                setupService();
            }
            initBluetooth();
        } else if (SharePreferenceUtil.CONNECT_TYPE_USB == sp.getConnectType()) {
            usbRadio.setChecked(true);
            refreshView(ACTION_CONNECT);
            TPMSParam.device_type = TPMSParam.DEVICE_TYPE_USB;
            if (isServiceWork(this, "com.hongking.hktpms.UsbControllerService")) {
                stopService(new Intent(this, UsbControllerService.class));
                stopService(new Intent(this, SysManagerService.class));
            }
            initUsbDriver();

        }
        String url = "http://api.fir.im/apps/latest/58414c35ca87a87de1001981?api_token=96087d1d51a3f574ac4656b478ebe870";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(this, url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                onVersionRsp(new String(bytes));
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            }
        });
    }

    @Override
    protected void onResume() {
        Log.e("20161129", "onResumeConnectChooseActivity");
        if (receiver != null) {
            IntentFilter filter = new IntentFilter(UsbControllerService.CONNECT_MESSAGE_ACTION);
            registerReceiver(receiver, filter);
        }


        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        IntentFilter f2 = new IntentFilter(UsbManager.EXTRA_DEVICE);
        IntentFilter f3 = new IntentFilter(SysControllerService.CONNECT_REQUEST_ACTION_SEND);
        registerReceiver(mUsbReceiver, filter);
        registerReceiver(mUsbReceiver, f2);
        registerReceiver(mUsbReceiver, f3);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("20161129", "onPauseConnectChooseActivity");
        unregisterReceiver(receiver);
        if (mUsbReceiver != null) {
            unregisterReceiver(mUsbReceiver);
        }
        /*if (isServiceWork(this, "com.hongking.hktpms.UsbControllerService")) {
            stopService(new Intent(this, UsbControllerService.class));
            stopService(new Intent(this, SysManagerService.class));
        }*/
    }

    @Override
    protected void onStop() {
        Log.e("20161129", "onStopConnectChooseActivity");
        super.onStop();

        TPMSParam.activityStop();
    }

    @Override
    public void onDestroy() {
        Log.e("20161129", "onDestroyConnectChooseActivity");
        super.onDestroy();
        SysApplication.conneceactno--;
        de.greenrobot.event.EventBus.getDefault().unregister(this);
        if (app != null) {
            app.mediaPlayerStop();
        }
        if (mBtMsgReceiver != null)
            unregisterReceiver(mBtMsgReceiver);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
      /*  if (flag) {
            stopService(new Intent(this, UsbControllerService.class));
            stopService(new Intent(this, SysManagerService.class));
            stopService();
            flag = true;
        }*/
    }

    private final String ACTION_CONNECT = "connect_device";
    private final String ACTION_CONNECT_FAILED = "connect_device_failed";
    private final String ACTION_CONNECT_CANCEL = "connect_cancel";
    Animation animation;

    private void refreshView(String type) {
        if (type.contains(ACTION_CONNECT)) {
            typeC.setVisibility(View.GONE);
            resultC.setVisibility(View.VISIBLE);
            title.setVisibility(View.INVISIBLE);

            animation = AnimationUtils.loadAnimation(this, R.anim.loading);
            animation.setInterpolator(new LinearInterpolator());
            img.startAnimation(animation);
            img.setVisibility(View.VISIBLE);
            btnConfirm.setText(getResources().getString(R.string.connect_ing));
            btnConfirm.setClickable(false);
        } else if (type.equals(ACTION_CONNECT_FAILED)) {
            title.setText(getResources().getString(R.string.connect_fail));
            title.setVisibility(View.VISIBLE);
            btnConfirm.setText(getResources().getString(R.string.connect_to));
            btnConfirm.setClickable(true);
        } else if (type.equals(ACTION_CONNECT_CANCEL)) {
//            Toast.makeText(ConnectChooseActivity.this, "这里应该要屏蔽3", Toast.LENGTH_LONG).show();
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            title.setVisibility(View.VISIBLE);
            title.setText(getResources().getString(R.string.connect_title));
            typeC.setVisibility(View.VISIBLE);
            resultC.setVisibility(View.GONE);
            btnConfirm.setText(getResources().getString(R.string.confirm));
            animation.cancel();
            img.setVisibility(View.GONE);
            btnConfirm.setClickable(true);
            if (DialogWait != null) {
                DialogWait.dismiss();
            }
            sp.setConnectType(0);
            if (isServiceWork(this, "com.hongking.hktpms.UsbControllerService")) {
                stopService(new Intent(this, UsbControllerService.class));
                stopService(new Intent(this, SysManagerService.class));
            }
        }
    }


    public void confirm(View v) {
        sp.setFirsttimeoncreate("no");
        if (usbRadio.isChecked()) {
            refreshView(ACTION_CONNECT);
            if (SysApplication.driver == null) {
                SysApplication.driver = new CH34xAndroidDriver((UsbManager) getSystemService(Context.USB_SERVICE), getApplicationContext(), ACTION_USB_PERMISSION);
            }
            if (SysApplication.driver != null && SysApplication.driver.UsbFeatureSupported()) {
                try {
                    if (2 == SysApplication.driver.ResumeUsbList()) {
                        Toast.makeText(this, "found no usb device", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!SysApplication.driver.UsbFeatureSupported()) {
                return;
            }
            SysApplication.USBTHREADRUN = true;
            SysApplication.BTTHREADRUN = false;
            TPMSParam.device_type = TPMSParam.DEVICE_TYPE_USB;
            sp.setConnectType(SharePreferenceUtil.CONNECT_TYPE_USB);
            initUsbDriver();
        } else {
            SysApplication.USBTHREADRUN = false;
            SysApplication.BTTHREADRUN = true;
            if (isServiceWork(this, "com.hongking.hktpms.UsbControllerService")) {
                stopService(new Intent(this, UsbControllerService.class));
                stopService(new Intent(this, SysManagerService.class));
            }
            sp.setConnectType(0);
            TPMSParam.device_type = TPMSParam.DEVICE_TYPE_BT;
            initBluetooth();
        }
    }

    public void cancel(View v) {
        refreshView(ACTION_CONNECT_CANCEL);
//        finish();
    }

    public void support(View v) {

    }

    private void initUsbDriver() {
        if (SysApplication.driver == null) {
            SysApplication.driver = new CH34xAndroidDriver((UsbManager) getSystemService(Context.USB_SERVICE), getApplicationContext(), ACTION_USB_PERMISSION);
        }
        //ÊÇ·ñÖ§³ÖÉè±¸
        if (SysApplication.driver == null || !SysApplication.driver.UsbFeatureSupported()) {
            Toast.makeText(this, "No Support USB host API", Toast.LENGTH_SHORT).show();
            SysApplication.driver = null;

            title.setText(getResources().getString(R.string.connect_fail));
            title.setVisibility(View.VISIBLE);
            btnConfirm.setText(getResources().getString(R.string.connect_to));
            btnConfirm.setClickable(true);
            //TODO
//			app.changeMode(SysParam.SYS_MODE.MODE_SETUP);
//			Intent intent = new Intent(this, AboutMeActivity.class);
//			startActivity(intent);
//			finish();
        } else {
            try {
                if (SysApplication.driver != null) {
                    SysApplication.driver.CloseDevice();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isServiceWork(this, "com.hongking.hktpms.UsbControllerService")) {
                stopService(new Intent(this, UsbControllerService.class));
                stopService(new Intent(this, SysManagerService.class));
            }
            final UsbManager usbManager = (UsbManager) ConnectChooseActivity.this.getSystemService("usb");
            sp.setConnectType(SharePreferenceUtil.CONNECT_TYPE_USB);
            countDownTimer = new CountDownTimer(50000, 5000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    if (usbManager.getDeviceList().isEmpty()) {
                        //在这里在放个计时器？然后把connectbroadcast里面的开启服务屏蔽掉。
                        Toast.makeText(ConnectChooseActivity.this, "请插入USB接收机", Toast.LENGTH_SHORT).show();
                    } else {
                        if(SysApplication.usbhandflag) {
                            SysApplication.usbhandflag = false;
                            Intent startIntent = new Intent(ConnectChooseActivity.this, UsbControllerService.class);
                            startService(startIntent);
                            startService(new Intent(ConnectChooseActivity.this, SysManagerService.class));
                        }
                    }
                }

                @Override
                public void onFinish() {

                }
            }.start();

//            DialogWait = onCreateDialog(0x00, "");
        }
    }

    private void initBluetooth() {
        Log.e("20161206", "startServiceScreenService");
        boolean flag = false;
        setupService();
        TPMSParam.device_type = TPMSParam.DEVICE_TYPE_BT;
//		startActivity(new Intent(this, AppEntryActivity.class));
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(ConnectChooseActivity.this, "no bluetooth device found", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        String btAddr = "";
        if (sp.getConnectType() == 2) {//如果等于2，就说明上次是蓝牙，这次可以自动连接
            btAddr = TPMSParam.sConnectedBlueAddr;
        } else {
            sp.setConnectType(SharePreferenceUtil.CONNECT_TYPE_BLUETOOTH);
        }

        sp.setBT_ADDRESS(btAddr);
        if (btAddr == null || "".equals(btAddr.trim())) {
            if (deviceAddr == null || "".equals(deviceAddr.trim())) {
                Intent serverIntent = new Intent(ConnectChooseActivity.this, BlueSetupActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            }
        } else {
            setupConnect(0x01);
        }


    }

    private void stopService() {
        Intent intent = new Intent(SysControllerService.STOP_SERVICE);
        Log.i(TAG, "request stop servie");
        sendBroadcast(intent);
    }

    private void setupService() {
        Log.d(TAG, "setupService()");
        Intent startIntent = new Intent(ConnectChooseActivity.this, SysControllerService.class);
        startService(startIntent);
        Intent startIntent1 = new Intent(ConnectChooseActivity.this, SysManagerService.class);
        startService(startIntent1);
    }

    public Dialog onCreateDialog(int dialogId, String msg) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = null;
        boolean mode = true;
        SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(this);
        Dialog dialog = null;
        switch (dialogId) {
            case 0x00:
                view = inflater.inflate(R.layout.set_wait_dialog1, null);
                mode = false;
                customBuilder.setMessage(null).setNegativeButton(getResources().getString(R.string.Cancel), this);
                break;
            case 0x01:
                view = inflater.inflate(R.layout.set_simple_dialog, null);
                customBuilder.setMessage(getResources().getString(R.string.BluetoothConnectionFailue) + msg)
                        .setPositiveButton(getResources().getString(R.string.confirm), this);
                break;
        }
        dialog = customBuilder.create(view, mode);
        dialog.show();
        return dialog;
    }

    private String deviceAddr;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    deviceAddr = data.getExtras().getString(BlueSetupActivity.EXTRA_DEVICE_ADDRESS);
                    sp.setBT_ADDRESS(deviceAddr);
                    startConnect(deviceAddr);
                }
                break;
        }
    }

    void setupConnect(int mode) {
        if (SysApplication.BluetoothState) {
            return;
        }
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
            Intent serverIntent = new Intent(ConnectChooseActivity.this, BlueSetupActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        }

    }


    boolean isSendSucc = false;

    void startConnect(final String address) {
        if (SysApplication.BluetoothState) {
            return;
        }
        Log.i("20161202", "SysApplication.BluetoothState..." + String.valueOf(SysApplication.BluetoothState));
        isSendSucc = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (SysApplication.BluetoothState) {
                    return;
                }
                while (!SysApplication.BluetoothState) {
                    Log.i("20161202", "start connect addr ..." + address);
                    Intent i = new Intent(SysControllerService.CONNECT_REQUEST_ACTION);
                    i.putExtra(DEVICE_ADDRESS, address);
                    sendBroadcast(i);
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        DialogWait = onCreateDialog(0x00, "");
    }

    void stopConnect(String address) {
        Intent i = new Intent(SysControllerService.DISCONNECT_REQUEST_ACTION);
        i.putExtra(DISCONNECT_DEVICE_ADDRESS, address);
        sendBroadcast(i);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (DialogWait != null) {
                DialogWait.dismiss();
                DialogWait = null;
            }
            if (msg.arg1 == 0x00) {
                if (sp.getConnectType() != 0) {

                    Log.e("20161215", "ConnectAct这里SysApplication.BluetoothState=" + SysApplication.BluetoothState);
                    Toast.makeText(ConnectChooseActivity.this, getResources().getString(R.string.BluetoothConnectionOK), Toast.LENGTH_SHORT).show();

                    SysApplication.heartflag=true;
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (SysApplication.heartflag) {
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (TPMSParam.mBluethoothData.mStatus == SysParam.SYS_MODE.MODE_NORM && TPMSParam.M_version.procotrol >= 0x13) {
                                    Log.e("20170104", "发送一次心跳");
                                    app.changeMode(SysParam.SYS_MODE.MODE_RUNHEARTBEAT);
                                }
                            }
                        }
                    });
                    thread.start();
                    if (sp.getFirsttimeoncreate().contains("yes")) {
                        all_ll.setVisibility(View.GONE);
                        sp.setFirsttimeoncreate("no");
                        showResult("连接成功");
                        new CountDownTimer(5000, 5000) {

                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {

                                Intent mainIntent = new Intent(ConnectChooseActivity.this, AppMainActivity.class);
                                mainIntent.putExtra("moveTaskToBack","yes");//有可能卡顿，造成无法回退后台，加这个以防意外。
                                ConnectChooseActivity.this.startActivityForResult(mainIntent, REQUEST_RECONNECT_DEVICE);
                                if (sp.getConnectType() == 2) {
                                    SysApplication.BluetoothState = true;
                                    SysApplication.USBState = false;
                                } else {
                                    SysApplication.USBState = true;
                                    SysApplication.BluetoothState = false;
                                }
//                                Toast.makeText(ConnectChooseActivity.this, "TPMS已进入后台监测", Toast.LENGTH_LONG).show();
//                                moveTaskToBack(true);
                                finish();
                            }
                        }.start();

                    } else {

                        if(sp.getRebootflag().contains("reflag"))
                        {


                            moveTaskToBack(true);
                            sp.setRebootflag("noflag");
//
//
//
                        }else {
                            Intent mainIntent = new Intent(ConnectChooseActivity.this, AppMainActivity.class);
                            ConnectChooseActivity.this.startActivityForResult(mainIntent, REQUEST_RECONNECT_DEVICE);
                        }

                      /*  Intent mainIntent = new Intent(ConnectChooseActivity.this, AppMainActivity.class);
                        ConnectChooseActivity.this.startActivityForResult(mainIntent, REQUEST_RECONNECT_DEVICE);*/
                        if (sp.getConnectType() == 2) {
                            SysApplication.BluetoothState = true;
                            SysApplication.USBState = false;
                        } else {
                            SysApplication.USBState = true;
                            SysApplication.BluetoothState = false;
                        }
                        finish();
                    }
                }
            } else if (msg.arg1 == 111) {
                Intent intent = new Intent(ConnectChooseActivity.this, AppMainActivity.class);
                startActivity(intent);
                finish();
            } else if (msg.what == 123) {

            } else {
                if (errDialog != null) {
                    errDialog.dismiss();
                    errDialog = null;
                }
                stopConnect(TPMSParam.sConnectedBlueAddr);
            }


        }
    };

    public void showResult(String result) {
        TipPop2 tip = new TipPop2(this);
        tip.setContent(result);
        tip.showAtLocation(getRootView(this), Gravity.CENTER, 0, 50);
    }

    public View getRootView(Activity myContext) {
        return ((ViewGroup) myContext.findViewById(android.R.id.content)).getChildAt(0);
    }

    private void onVersionRsp(String rspData) {
        Gson gson = new Gson();

        final RspAppVersionFir rsp = (RspAppVersionFir) gson.fromJson(rspData, RspAppVersionFir.class);
        if (rsp != null) {
            try {
                PackageManager manager = this.getPackageManager();
                PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
                String buildId = rsp.getBuild();
                String versionShort = rsp.getVersionShort();
                if (!versionShort.contains(info.versionName)) {
                    new AlertDialog.Builder(this).setMessage(rsp.getChangelog())
                            .setTitle("更新").setPositiveButton("确认", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sp.setConnectType(0);
                            TPMSParam.sConnectedBlueAddr = "";
                            dialog.dismiss();
                            DownloadApp download = new DownloadApp(ConnectChooseActivity.this, ConnectChooseActivity.this, rsp.getInstall_url());
                            download.showDownloadProgressDlg();
                        }
                    }).setNegativeButton("取消", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                    ;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbControllerService.CONNECT_MESSAGE_ACTION)) {
                mHandler.sendEmptyMessage(111);
            }
        }
    }

    public class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "** ON RECEIVE **");
            String action = intent.getAction();
            if (action.equals(INCOMING_MSG) || action.equals(OUTGOING_MSG)) {
                String displayMsg = intent.getExtras().getString("STR");
                Log.i(TAG, displayMsg);
            } else if (action.equals(SysControllerService.CONNECT_MESSAGE_ACTION)) {
                String displayMsg = intent.getExtras().getString("STR");
                Log.i(TAG, displayMsg + "has connect");
                Message msg = Message.obtain();
                msg.arg1 = intent.getExtras().getInt("ARG");
                mHandler.sendMessage(msg);
            } else if (action.equals(SysControllerService.DISCONNECT_MESSAGE_ACTION)) {
                String displayMsg = intent.getExtras().getString("STR");
                Log.i(TAG, displayMsg + " can't connect");

                if (DialogWait != null) {
                    if (connectMode == 0x01) {
                        if (requestTimes++ > 3) {
                            requestTimes = 0;
                            Toast.makeText(ConnectChooseActivity.this, getResources().getString(R.string.TrialResetSystem),
                                    Toast.LENGTH_SHORT).show();
                            mBluetoothAdapter.disable();
                            isRequest = true;
                            DialogWait.dismiss();
                            DialogWait = null;
                        } else {
                            Intent i = new Intent(SysControllerService.CONNECT_REQUEST_ACTION);
                            i.putExtra(DEVICE_ADDRESS, TPMSParam.sConnectedBlueAddr);
                            sendBroadcast(i);
                        }
                    }
                }
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Log.i(TAG, "[onReceive] ACTION_STATE_CHANGED");
                int currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (currentState) {
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "[onReceive] current state = ON");
                        if (isRequest) {
                            setupConnect(0x01);
                            isRequest = false;
                        }
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        if (isRequest) {
                            mBluetoothAdapter.enable();
                        }
                        Log.i(TAG, "[onReceive] current state = OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(TAG, "[onReceive] current state = TURNING_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(TAG, "[onReceive] current state = TURNING_OFF");
                        break;
                }
            } else if (action.equals(SysControllerService.SERVICE_REPORT)) {

            } else {
                Log.e(TAG, "another action: " + action);
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
        } else {
            Log.v("keydebug", "which=" + which);
            if (DialogWait != null) {
                DialogWait.dismiss();
                DialogWait = null;
            }
            connectMode = 0x00;// 恢复用户模式
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


    @SuppressLint("NewApi")
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
//                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);//监听USB插拔，插入的时候使用此方法。
//                device.getProductId();
//                if (device != null && (device.getProductId() == 29987 || device.getProductId() == 221795)) {
//                    if (sp.getConnectType() == SharePreferenceUtil.CONNECT_TYPE_USB) {
////                        initUsbDriver();
//                    }
//                }
            } else if (SysControllerService.CONNECT_REQUEST_ACTION_SEND.equals(action)) {
                Log.i("20161202", "接收到回来的广播，已连接成功？不再进行连接？");
                isSendSucc = true;
            }
        }
    };

    public void onEvent(EventBus_bt_connect eventBus_bt_connect) {
        if (eventBus_bt_connect.getI() == 0) {
            //失败，重连。
            Log.e("20161202", "这里是eventbus收到失败连接的通知，准备下一次连接");
            if (!TPMSParam.blueStatus.isConnecting()) {
                Log.e("20161202", "连接失败，正在重连。");
                startConnect(sp.getBT_ADDRESS());
                Toast.makeText(ConnectChooseActivity.this, "连接失败，正在重连。", Toast.LENGTH_SHORT).show();
            }
        }else if(eventBus_bt_connect.getI() == 99)
        {
            Log.e("20161202", "连接成功");
            mHandler.sendEmptyMessage(0x00);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            if (DialogWait == null) {
                finish();
            }
            if (DialogWait != null) {
                DialogWait.dismiss();
                DialogWait = null;
            }
            SysApplication.USBTHREADRUN = false;
            SysApplication.BTTHREADRUN = false;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
