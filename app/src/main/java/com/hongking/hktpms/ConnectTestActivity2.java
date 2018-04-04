package com.hongking.hktpms;

import com.google.gson.Gson;
import com.hongking.hktpms.Utils.CH34xAndroidDriver;
import com.hongking.hktpms.Utils.DownloadApp;
import com.hongking.hktpms.Utils.HomeWatcher;
import com.hongking.hktpms.Utils.RspAppVersionFir;
import com.hongking.hktpms.Utils.SharePreferenceUtil;
import com.hongking.hktpms.views.EventBus_bt_connect;
import com.hongking.oemtpms.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConnectTestActivity2 extends BaseActivity implements OnClickListener {

    private final String TAG = "ConnectTestActivity2";
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

    private boolean isRequest;
    private List<Map<String, Object>> mData;
    private Dialog DialogWait = null;
    private NotificationReceiver mBtMsgReceiver;
    private Dialog errDialog;
    private int connectMode = 0x00;
    private int requestTimes = 0;


    private RadioButton usbRadio, btRadio;
    private RadioGroup typeC;
    private RelativeLayout resultC;
    private ImageView img;
    private TextView title;
    private Button btnConfirm;

    private MsgReceiver receiver;
    private SysParam TPMSParam;
    private SysApplication app;
    private BluetoothAdapter mBluetoothAdapter = null;

    private SharePreferenceUtil sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("20161129", "onCreateConnectTestActivity2");
        TextView tv = new TextView(this);
        tv.setText(".");
        setContentView(tv);
        sp = new SharePreferenceUtil(this);
        de.greenrobot.event.EventBus.getDefault().register(this);
        app = (SysApplication) getApplicationContext();
        TPMSParam = app.TPMSParam;
//        init();
//        moveTaskToBack(true);
        receiver = new MsgReceiver();
//		WindowManager windowManager = getWindowManager();
//		Display display = windowManager.getDefaultDisplay();
//		int screenWidth = screenWidth = display.getWidth();
//		int screenHeight = screenHeight = display.getHeight();
//		Toast.makeText(ConnectTestActivity2.this,screenWidth+"宽...高"+screenHeight,Toast.LENGTH_SHORT).show();

        //	SysApplication.driver = new CH34xUARTDriver((UsbManager) getSystemService(Context.USB_SERVICE),getApplicationContext(), ACTION_USB_PERMISSION);
        /*new CountDownTimer(7000, 7000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                moveTaskToBack(true);
            }
        }.start();*/
    }

    private void init() {
        usbRadio = (RadioButton) findViewById(R.id.conn_usb);
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
        Log.e("20161129", "onStartConnectTestActivity2");
        if (true == TPMSParam.isOnExit) {
            this.finish();
        }
        requestTimes = 0;
        TPMSParam.activityStart();

        if (SysApplication.driver != null && SysApplication.driver.UsbFeatureSupported()) {
            try {
                if (2 == SysApplication.driver.ResumeUsbList()) {
//					Toast.makeText(this, "found usb device", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (SharePreferenceUtil.CONNECT_TYPE_BLUETOOTH == sp.getConnectType()) {
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
//            refreshView(ACTION_CONNECT);
            TPMSParam.device_type = TPMSParam.DEVICE_TYPE_USB;
            if (isServiceWork(this, "com.hongking.hktpms.UsbControllerService")) {
                stopService(new Intent(this, UsbControllerService.class));
                stopService(new Intent(this, SysManagerService.class));
            }
            initUsbDriver();

        }
//        String url = "http://api.fir.im/apps/latest/5837360b959d69558e0009e3?api_token=96087d1d51a3f574ac4656b478ebe870";
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.get(this, url, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int i, Header[] headers, byte[] bytes) {
//                onVersionRsp(new String(bytes));
//            }
//            @Override
//            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
//            }
//        });
    }

    @Override
    protected void onResume() {
        Log.e("20161129", "onResumeConnectTestActivity2");
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
        Log.e("20161129", "onPauseConnectTestActivity2");
//        unregisterReceiver(receiver);
//        if (mUsbReceiver != null) {
//            unregisterReceiver(mUsbReceiver);
//        }
        /*if (isServiceWork(this, "com.hongking.hktpms.UsbControllerService")) {
            stopService(new Intent(this, UsbControllerService.class));
            stopService(new Intent(this, SysManagerService.class));
        }*/
    }

    @Override
    protected void onStop() {
        Log.e("20161129", "onStopConnectTestActivity2");
        super.onStop();

        TPMSParam.activityStop();
    }

    @Override
    public void onDestroy() {
        Log.e("20161129", "onDestroyConnectTestActivity2");
        super.onDestroy();
        if (app != null) {
            app.mediaPlayerStop();
        }
        de.greenrobot.event.EventBus.getDefault().unregister(this);
        if (mBtMsgReceiver != null)
            unregisterReceiver(mBtMsgReceiver);
//        stopService(new Intent(this, UsbControllerService.class));
//        stopService(new Intent(this, SysManagerService.class));
//        stopService();
    }

    private final String ACTION_CONNECT = "connect_device";
    private final String ACTION_CONNECT_FAILED = "connect_device_failed";

    private void refreshView(String type) {
        if (type.contains(ACTION_CONNECT)) {
            typeC.setVisibility(View.GONE);
            resultC.setVisibility(View.VISIBLE);
            title.setVisibility(View.INVISIBLE);

            Animation animation = AnimationUtils.loadAnimation(this, R.anim.loading);
            animation.setInterpolator(new LinearInterpolator());
            img.startAnimation(animation);

            btnConfirm.setText(getResources().getString(R.string.connect_ing));
            btnConfirm.setClickable(false);
        } else if (type.equals(ACTION_CONNECT_FAILED)) {
            title.setText(getResources().getString(R.string.connect_fail));
            title.setVisibility(View.VISIBLE);
            btnConfirm.setText(getResources().getString(R.string.connect_to));
            btnConfirm.setClickable(true);
        }
    }


    public void confirm(View v) {
        if (usbRadio.isChecked()) {
            refreshView(ACTION_CONNECT);
            TPMSParam.device_type = TPMSParam.DEVICE_TYPE_USB;
            sp.setConnectType(SharePreferenceUtil.CONNECT_TYPE_USB);
            initUsbDriver();
        } else {
            sp.setConnectType(SharePreferenceUtil.CONNECT_TYPE_BLUETOOTH);
            TPMSParam.device_type = TPMSParam.DEVICE_TYPE_BT;
            initBluetooth();
        }
    }

    public void cancel(View v) {
        finish();
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
            sp.setConnectType(SharePreferenceUtil.CONNECT_TYPE_USB);
            Intent startIntent = new Intent(this, UsbControllerService.class);
            startService(startIntent);
            startService(new Intent(this, SysManagerService.class));
        }
    }

    private void initBluetooth() {

        boolean flag = false;
        setupService();
        TPMSParam.device_type = TPMSParam.DEVICE_TYPE_BT;
//		startActivity(new Intent(this, AppEntryActivity.class));
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(ConnectTestActivity2.this, "no bluetooth device found", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.enable();
            return;
        }
        sp.setConnectType(SharePreferenceUtil.CONNECT_TYPE_BLUETOOTH);
        /*String btAddr = TPMSParam.sConnectedBlueAddr;
        if(btAddr!=null) {
            sp.setBT_ADDRESS(btAddr);
        }*/
//        if (btAddr == null || "".equals(btAddr.trim())) {
//            if (deviceAddr == null || "".equals(deviceAddr.trim())) {
//                Intent serverIntent = new Intent(ConnectTestActivity2.this, BlueSetupActivity.class);
//                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
//            }
//        } else {
        setupConnect(0x01);
//        }


    }

    private void stopService() {
        Intent intent = new Intent(SysControllerService.STOP_SERVICE);
        Log.i(TAG, "request stop servie");
        sendBroadcast(intent);
    }

    private void setupService() {
        Log.d(TAG, "setupService()");
        Intent startIntent = new Intent(ConnectTestActivity2.this, SysControllerService.class);
        startService(startIntent);
        Intent startIntent1 = new Intent(ConnectTestActivity2.this, SysManagerService.class);
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
                if (device.getAddress().equals(sp.getBT_ADDRESS())) {
                    startConnect(sp.getBT_ADDRESS());
                    return;
                }
            }
        } else {
            Intent serverIntent = new Intent(ConnectTestActivity2.this, BlueSetupActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        }

    }


    boolean isSendSucc = false;

    void startConnect(final String address) {
        isSendSucc = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (SysApplication.BluetoothState) {
                    return;
                }
                while (!isSendSucc) {
                    Log.i(TAG, "connecttestact2.....start connect addr ..." + address);
                    Intent i = new Intent(SysControllerService.CONNECT_REQUEST_ACTION);
                    i.putExtra(DEVICE_ADDRESS, address);
                    sendBroadcast(i);
                    try {
                        Thread.sleep(2000);
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
                    SysApplication.BluetoothState = true;
                    Log.e("20161215", "ConnectTestAct2这里SysApplication.BluetoothState=" + SysApplication.BluetoothState);
                    Toast.makeText(ConnectTestActivity2.this, getResources().getString(R.string.BluetoothConnectionOK), Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(ConnectTestActivity2.this, AppMainActivity.class);
//                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mainIntent.putExtra("forwhere", "connecttestact2");
                    ConnectTestActivity2.this.startActivityForResult(mainIntent, REQUEST_RECONNECT_DEVICE);
                    finish();
//                    moveTaskToBack(true);
                }
            } else if (msg.arg1 == 111) {
                Intent intent = new Intent(ConnectTestActivity2.this, AppMainActivity.class);
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

    private void onVersionRsp(String rspData) {
        Gson gson = new Gson();

        final RspAppVersionFir rsp = (RspAppVersionFir) gson.fromJson(rspData, RspAppVersionFir.class);
        if (rsp != null) {
            try {
                PackageManager manager = this.getPackageManager();
                PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
                String buildId = rsp.getBuild();
                if (Integer.parseInt(buildId) > info.versionCode) {
                    new AlertDialog.Builder(this).setMessage(rsp.getChangelog())
                            .setTitle("更新").setPositiveButton("确认", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            DownloadApp download = new DownloadApp(ConnectTestActivity2.this, ConnectTestActivity2.this, rsp.getInstall_url());
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
                            Toast.makeText(ConnectTestActivity2.this, getResources().getString(R.string.TrialResetSystem),
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
//                            mBluetoothAdapter.enable();
                            return;
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
                Log.e("20161129", "ConncectActATTACHED");
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);//监听USB插拔，插入的时候使用此方法。
                device.getProductId();
                if (device != null && (device.getProductId() == 29987 || device.getProductId() == 221795)) {
                    if (sp.getConnectType() == SharePreferenceUtil.CONNECT_TYPE_USB) {
//                        initUsbDriver();
                    }
                }
            } else if (SysControllerService.CONNECT_REQUEST_ACTION_SEND.equals(action)) {
                isSendSucc = true;
                SysApplication.BluetoothState=true;
                mHandler.sendEmptyMessage(0x00);
            }
        }
    };

    public void onEvent(EventBus_bt_connect eventBus_bt_connect) {
        if (eventBus_bt_connect.getI() == 0) {
            //失败，重连。
            if (!TPMSParam.blueStatus.isConnecting()) {
                startConnect(sp.getBT_ADDRESS());
                Toast.makeText(ConnectTestActivity2.this, "连接失败，正在重连。", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


