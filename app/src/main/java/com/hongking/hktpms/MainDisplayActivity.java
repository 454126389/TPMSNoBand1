package com.hongking.hktpms;

import android.annotation.SuppressLint;
/*
 * 包说明
 * 主页显示Activity
 * */
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hongking.hktpms.Utils.DownloadApp;
import com.hongking.hktpms.Utils.RspAppVersionFir;
import com.hongking.hktpms.Utils.SharePreferenceUtil;
import com.hongking.hktpms.views.AutoStartUnitSetPop;
import com.hongking.hktpms.views.PressureUnitSetPop;
import com.hongking.hktpms.views.ResetUnitSetPop;
import com.hongking.hktpms.views.TemperatureUnitSetPop;
import com.hongking.hktpms.views.ValueSetPop;
import com.hongking.oemtpms.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

import static com.hongking.hktpms.SysApplication.density;

public class MainDisplayActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 2;
    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;

    private SysApplication app;

    private TextView myViewLFTemp = null;
    private TextView myViewLFPress = null;
    private TextView myViewRFTemp = null;
    private TextView myViewRFPress = null;
    private TextView myViewLBTemp = null;
    private TextView myViewLBPress = null;
    private TextView myViewRBTemp = null;
    private TextView myViewRBPress = null;

    private TextView myViewLFTempUnit = null;
    private TextView myViewLFPressUnit = null;
    private TextView myViewRFTempUnit = null;
    private TextView myViewRFPressUnit = null;
    private TextView myViewLBTempUnit = null;
    private TextView myViewLBPressUnit = null;
    private TextView myViewRBTempUnit = null;
    private TextView myViewRBPressUnit = null;

    SysParam.DataInfo press_lf;
    SysParam.DataInfo press_rf;
    SysParam.DataInfo press_lb;
    SysParam.DataInfo press_rb;

    SysParam.DataInfo temp_lf;
    SysParam.DataInfo temp_rf;
    SysParam.DataInfo temp_lb;
    SysParam.DataInfo temp_rb;

    private SysParam TPMSParam;

    private View contentRB;
    private View contentRF;
    private View contentLB;
    private View contentLF;
    private View contentall;

    private ImageView WarnCenterImage;
    private BluetoothAdapter mBluetoothAdapter = null;
    static final String TAG = "MainDisplay";
    private NotificationReceiver mBtMsgReceiver;
    private RelativeLayout cjh_test;
    private Thread thread;
    private boolean heartflag = true;


    private ListView listView;
    private List<Map<String, Object>> mData;
    private int iUserSelect;
    private byte menuBase = 0;// 用来记录第一个有效菜单的起始值
    private Dialog curDialog;
    private Dialog DialogWait = null;
    private SharePreferenceUtil sp;
    private EditText input = null;
    private TextView infoView;


    void SelectLayout(Activity activity) {

        Display display = getWindowManager().getDefaultDisplay();
        int width, height;
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        float density = metric.density;
        density = density;
        width = display.getWidth();
        height = display.getHeight();
        if (width > 1000 && density == 1.5) {//large超大屏
            setContentView(R.layout.display_viewlargedpi15);
        } else if (width > 1000) {//large
            setContentView(R.layout.display_viewlargetwo);
//            setContentView(R.layout.display_viewlarge);
        } else if (height < 420) {
            setContentView(R.layout.display_viewsmallest);
        } else if (height > 460) {
            setContentView(R.layout.display_viewsby854480);//这里是给854*480
        } else {
            setContentView(R.layout.display_viewsmall);
        }







        contentall = findViewById(R.id.contentall);
        cjh_test = (RelativeLayout) findViewById(R.id.cjh_test);
        cjh_test.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("qqq", "qqqqq1");
                EventBus.getDefault().post(new com.hongking.hktpms.views.EventBus(0));
            }
        });
        findViewById(R.id.main_display_title).setVisibility(View.GONE);

        // Drawable dr =
        // this.getResources().getDrawable 888887 (TPMSParam.iThemeTable[TPMSParam.iThemeIndex]);
        // contentall.setBackgroundDrawable(dr);
        // View linearLayout = findViewById(R.id.CardView);
        // dr =
        // this.getResources().getDrawable(TPMSParam.iCardTable[TPMSParam.iCardIndex]);
        // linearLayout.setBackgroundDrawable(dr);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //setting

        TPMSParam = ((SysParam) SysParam.getInstance(this));
        app = (SysApplication) getApplicationContext();
        sp = new SharePreferenceUtil(this);
        SysParam.getInstance(this).addActivity(this);
        //


//        TPMSParam = ((SysParam) SysParam.getInstance(this));
//        SysParam.getInstance(this).addActivity(this);
        SelectLayout(this);

//        app = (SysApplication) getApplicationContext();
        // new SysParam.Builder(this).setTitle(
        // getResources().getString(R.string.app_name) + "-" +
        // getResources().getString(R.string.app_content)).create();
        new SysParam.Builder(this).setTitle(getResources().getString(R.string.app_name)).create();
        myViewLFTemp = (TextView) findViewById(R.id.LFTempText);
        myViewLFPress = (TextView) findViewById(R.id.LFPressText);

        myViewRFTemp = (TextView) findViewById(R.id.RFTempText);
        myViewRFPress = (TextView) findViewById(R.id.RFPressText);

        myViewLBTemp = (TextView) findViewById(R.id.LBTempText);
        myViewLBPress = (TextView) findViewById(R.id.LBPressText);

        myViewRBTemp = (TextView) findViewById(R.id.RBTempText);
        myViewRBPress = (TextView) findViewById(R.id.RBPressText);

		/*-----------------------------------------------------------------*/
        myViewLFTempUnit = (TextView) findViewById(R.id.LFTempTitle);
        myViewLFPressUnit = (TextView) findViewById(R.id.LFPressTitle);

        myViewRFTempUnit = (TextView) findViewById(R.id.RFTempTitle);
        myViewRFPressUnit = (TextView) findViewById(R.id.RFPressTitle);

        myViewLBTempUnit = (TextView) findViewById(R.id.LBTempTitle);
        myViewLBPressUnit = (TextView) findViewById(R.id.LBPressTitle);

        myViewRBTempUnit = (TextView) findViewById(R.id.RBTempTitle);
        myViewRBPressUnit = (TextView) findViewById(R.id.RBPressTitle);
        // IHelloService.Stub.asInterface
        contentRB = findViewById(R.id.contentRBValue);
        contentRF = findViewById(R.id.contentRFValue);
        contentLB = findViewById(R.id.contentLBValue);
        contentLF = findViewById(R.id.contentLFValue);

        contentRB.setVisibility(0x00);
        contentRF.setVisibility(0x00);
        contentLB.setVisibility(0x00);
        contentLF.setVisibility(0x00);


        //setting


        listView = (ListView) findViewById(R.id.ListView01);
        mData = getData();
        SysListviewbase myAdapter = new SysListviewbase(this, mData,density);
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(new MyListViewListenerCommon());

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                // OpenCopyMode();
                if (mData.get(position).get("title").equals(getResources().getString(R.string.RestoreFactorySetup)))
                    onCreateDialog(0x02, getResources().getString(R.string.FactoryParameter));
                return false;
            }

        });
        //



        WarnCenterImage = (ImageView) findViewById(R.id.WarnIconImage);

        AnimationUtils.loadAnimation(this, R.anim.main_value_view);
        if (TPMSParam.device_type.equals(TPMSParam.DEVICE_TYPE_USB)) {
            if (SysApplication.driver == null) {
                Toast.makeText(this, "usb device is not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        } else {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        IntentFilter stateDisconnectFilter = new IntentFilter(SysControllerService.NOTIFY_UI);
        mBtMsgReceiver = new NotificationReceiver();
        registerReceiver(mBtMsgReceiver, stateDisconnectFilter);

//        防止自动更新先遮蔽
//        update();



       /* thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (heartflag) {
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
        thread.start();*/
    }

    private void update() {
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
                            .setTitle("更新").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharePreferenceUtil sp = new SharePreferenceUtil(MainDisplayActivity.this);
                            sp.setConnectType(0);
                            TPMSParam.sConnectedBlueAddr = "";
                            dialog.dismiss();
                            DownloadApp download = new DownloadApp(MainDisplayActivity.this, MainDisplayActivity.this, rsp.getInstall_url());
                            download.showDownloadProgressDlg();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStart() {
        TPMSParam.activityStart();
        super.onStart();
        if (!TPMSParam.device_type.equals(TPMSParam.DEVICE_TYPE_USB)) {
            if (!mBluetoothAdapter.isEnabled()) {
                finish();
            } else {
                // setupChat();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按下键盘上返回按钮
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return moveTaskToBack(true);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    protected void onPause() {
        if (null != wakeLock) {
            Log.i(TAG, "release lock");
            wakeLock.release();
            wakeLock = null;
        }


        super.onPause();
    }

    protected void onStop() {
        TPMSParam.activityStop();
        Log.w(TAG, "[onStop]");
        super.onStop();
    }

    @Override
    protected void onResume() {




        if (TPMSParam.device_type.equals(TPMSParam.DEVICE_TYPE_USB)) {
            if (app.driver == null || !app.driver.isConnected()) {
                SysApplication.USBState = false;
                Toast.makeText(MainDisplayActivity.this, "TPMS" + getResources().getString(R.string.device_disconnect), Toast.LENGTH_SHORT).show();
                sendBroadcast(new Intent(UsbControllerService.DISCONNECT_REQUEST_ACTION));
                startActivity(new Intent(MainDisplayActivity.this, ConnectChooseActivity.class));
                stopService(new Intent(MainDisplayActivity.this, UsbControllerService.class));
                stopService(new Intent(MainDisplayActivity.this, SysManagerService.class));
                finish();
            }
        }
        // View linearLayout = findViewById(R.id.contentall);
        // Drawable dr =
        // this.getResources().getDrawable(TPMSParam.iThemeTable[TPMSParam.iThemeIndex]);
        // linearLayout.setBackgroundDrawable(dr);
        // linearLayout = findViewById(R.id.CardView);
        // dr =
        // this.getResources().getDrawable(TPMSParam.iCardTable[TPMSParam.iCardIndex]);
        // linearLayout.setBackgroundDrawable(dr);
        tyreFlash();
        EventBus.getDefault().post(new com.hongking.hktpms.views.EventBus(1));
        if (getResources().getStringArray(R.array.SleepTimeSet)[TPMSParam.iSleepTime].equals("开")) {
            Log.i(TAG, "request display keep wake");
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            ;
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "TPMS");
            wakeLock.acquire();
        }

        if (TPMSParam.device_type.equals(TPMSParam.DEVICE_TYPE_USB)) {
            IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
            IntentFilter filter2 = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            IntentFilter f2 = new IntentFilter(UsbManager.EXTRA_DEVICE);
            registerReceiver(mUsbReceiver, filter);
            registerReceiver(mUsbReceiver, filter2);
            registerReceiver(mUsbReceiver, f2);
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "[onDestroy]");
        super.onDestroy();
        if (TPMSParam.device_type.equals(TPMSParam.DEVICE_TYPE_USB)) {
            if (mUsbReceiver != null) {
                unregisterReceiver(mUsbReceiver);
            }
        }//201612211357上面这个判断本来在onpause。现在拿过来这边。
        if (mBtMsgReceiver != null)
            unregisterReceiver(mBtMsgReceiver);

    }

    // 当客户点击Menua按钮的时候，调用该方法
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        return super.onCreateOptionsMenu(menu);
    }
    // 当客户点击菜单中的某一个选项时，调用该方法，并将点击的选项通过参数传递进来

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == 1) {
            SysParam.getInstance(this).exit(this);
        }

        return super.onMenuItemSelected(featureId, item);
    }

    class MyViewListenerCommon implements OnClickListener {
        public void onClick(View v) {
            v.clearAnimation();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    // setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "蓝牙拒绝被开启", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(SysControllerService.NOTIFY_UI)) {
                Log.i(TAG, "** ON RECEIVE tire info **");
                tyreFlash();
            } else {
                Log.e(TAG, "another action: " + action);
            }
        }
    }

    void warnIconFlash(int which) {
        if (which == 0x01) {

        }
    }


    private TextView TextViewRequest(int which, byte index) {
        TextView myViewWarnText;

        if (which == TPMSParam.lf) {
            if (index == 0) {
                myViewWarnText = (TextView) findViewById(R.id.LFwarnArea1Text);
            } else if (index == 1) {
                myViewWarnText = (TextView) findViewById(R.id.LFwarnArea2Text);
            } else if (index == 2) {
                myViewWarnText = (TextView) findViewById(R.id.LFwarnArea3Text);
            } else {
                myViewWarnText = (TextView) findViewById(R.id.LFwarnArea4Text);
            }

        } else if (which == TPMSParam.rf) {
            if (index == 0) {
                myViewWarnText = (TextView) findViewById(R.id.RFwarnArea1Text);
            } else if (index == 1) {
                myViewWarnText = (TextView) findViewById(R.id.RFwarnArea2Text);
            } else if (index == 2) {
                myViewWarnText = (TextView) findViewById(R.id.RFwarnArea3Text);
            } else {
                myViewWarnText = (TextView) findViewById(R.id.RFwarnArea4Text);
            }

        } else if (which == TPMSParam.lb) {
            if (index == 0) {
                myViewWarnText = (TextView) findViewById(R.id.LBwarnArea1Text);
            } else if (index == 1) {
                myViewWarnText = (TextView) findViewById(R.id.LBwarnArea2Text);
            } else if (index == 2) {
                myViewWarnText = (TextView) findViewById(R.id.LBwarnArea3Text);
            } else {
                myViewWarnText = (TextView) findViewById(R.id.LBwarnArea4Text);
            }

        } else {
            if (index == 0) {
                myViewWarnText = (TextView) findViewById(R.id.RBwarnArea1Text);
            } else if (index == 1) {
                myViewWarnText = (TextView) findViewById(R.id.RBwarnArea2Text);
            } else if (index == 2) {
                myViewWarnText = (TextView) findViewById(R.id.RBwarnArea3Text);
            } else {
                myViewWarnText = (TextView) findViewById(R.id.RBwarnArea4Text);
            }

        }
        return myViewWarnText;

    }

    private ImageView ImageViewRequest(int lb, byte index) {
        ImageView myViewWarnImage;

        if (lb == TPMSParam.lf) {
            if (index == 0) {
                myViewWarnImage = (ImageView) findViewById(R.id.LFwarnArea1Icon);
            } else if (index == 1) {
                myViewWarnImage = (ImageView) findViewById(R.id.LFwarnArea2Icon);
            } else if (index == 2) {
                myViewWarnImage = (ImageView) findViewById(R.id.LFwarnArea3Icon);
            } else {
                myViewWarnImage = (ImageView) findViewById(R.id.LFwarnArea4Icon);
            }

        } else if (lb == TPMSParam.rf) {
            if (index == 0) {
                myViewWarnImage = (ImageView) findViewById(R.id.RFwarnArea1Icon);
            } else if (index == 1) {
                myViewWarnImage = (ImageView) findViewById(R.id.RFwarnArea2Icon);
            } else if (index == 2) {
                myViewWarnImage = (ImageView) findViewById(R.id.RFwarnArea3Icon);
            } else {
                myViewWarnImage = (ImageView) findViewById(R.id.RFwarnArea4Icon);
            }

        } else if (lb == TPMSParam.lb) {
            if (index == 0) {
                myViewWarnImage = (ImageView) findViewById(R.id.LBwarnArea1Icon);
            } else if (index == 1) {
                myViewWarnImage = (ImageView) findViewById(R.id.LBwarnArea2Icon);
            } else if (index == 2) {
                myViewWarnImage = (ImageView) findViewById(R.id.LBwarnArea3Icon);
            } else {
                myViewWarnImage = (ImageView) findViewById(R.id.LBwarnArea4Icon);
            }

        } else {
            if (index == 0) {
                myViewWarnImage = (ImageView) findViewById(R.id.RBwarnArea1Icon);
            } else if (index == 1) {
                myViewWarnImage = (ImageView) findViewById(R.id.RBwarnArea2Icon);
            } else if (index == 2) {
                myViewWarnImage = (ImageView) findViewById(R.id.RBwarnArea3Icon);
            } else {
                myViewWarnImage = (ImageView) findViewById(R.id.RBwarnArea4Icon);
            }

        }
        return myViewWarnImage;

    }

    private View ViewRequest(int which, byte index) {
        View myViewWarn;

        if (which == TPMSParam.lf) {
            if (index == 0) {
                myViewWarn = (View) findViewById(R.id.LFwarnArea1);
            } else if (index == 1) {
                myViewWarn = (View) findViewById(R.id.LFwarnArea2);
            } else if (index == 2) {
                myViewWarn = (View) findViewById(R.id.LFwarnArea3);
            } else {
                myViewWarn = (View) findViewById(R.id.LFwarnArea4);
            }

        } else if (which == TPMSParam.rf) {
            if (index == 0) {
                myViewWarn = (View) findViewById(R.id.RFwarnArea1);
            } else if (index == 1) {
                myViewWarn = (View) findViewById(R.id.RFwarnArea2);
            } else if (index == 2) {
                myViewWarn = (View) findViewById(R.id.RFwarnArea3);
            } else {
                myViewWarn = (View) findViewById(R.id.RFwarnArea4);
            }

        } else if (which == TPMSParam.lb) {
            if (index == 0) {
                myViewWarn = (View) findViewById(R.id.LBwarnArea1);
            } else if (index == 1) {
                myViewWarn = (View) findViewById(R.id.LBwarnArea2);
            } else if (index == 2) {
                myViewWarn = (View) findViewById(R.id.LBwarnArea3);
            } else {
                myViewWarn = (View) findViewById(R.id.LBwarnArea4);
            }

        } else {
            if (index == 0) {
                myViewWarn = (View) findViewById(R.id.RBwarnArea1);
            } else if (index == 1) {
                myViewWarn = (View) findViewById(R.id.RBwarnArea2);
            } else if (index == 2) {
                myViewWarn = (View) findViewById(R.id.RBwarnArea3);
            } else {
                myViewWarn = (View) findViewById(R.id.RBwarnArea4);
            }

        }
        return myViewWarn;

    }

    /**
     * 各种报警初始化
     *
     * @param which
     */
    private void ViewClr(int which) {
        View myViewWarn;
        ImageView myViewWarnImage;
        int ViewStatus = View.GONE;

        if (which == TPMSParam.lf) {

            myViewLFTemp.setTextColor(getResources().getColor(R.color.normalTempDisplay));
            myViewLFTempUnit.setTextColor(getResources().getColor(R.color.normalTempDisplay));

            myViewLFPress.setTextColor(getResources().getColor(R.color.normalPressDisplay));
            myViewLFPressUnit.setTextColor(getResources().getColor(R.color.normalPressDisplay));

            myViewWarnImage = (ImageView) findViewById(R.id.LFwarnAreaIcon);
            myViewWarnImage.setVisibility(ViewStatus);

            myViewWarn = findViewById(R.id.LFwarnArea1);
            myViewWarn.setVisibility(ViewStatus);
            myViewWarn = (View) findViewById(R.id.LFwarnArea2);
            myViewWarn.setVisibility(ViewStatus);
            myViewWarn = (View) findViewById(R.id.LFwarnArea3);
            myViewWarn.setVisibility(ViewStatus);
            myViewWarn = (View) findViewById(R.id.LFwarnArea4);
            myViewWarn.setVisibility(ViewStatus);
        } else if (which == TPMSParam.rf) {

            myViewRFTemp.setTextColor(getResources().getColor(R.color.normalTempDisplay));
            myViewRFTempUnit.setTextColor(getResources().getColor(R.color.normalTempDisplay));

            myViewRFPress.setTextColor(getResources().getColor(R.color.normalPressDisplay));
            myViewRFPressUnit.setTextColor(getResources().getColor(R.color.normalPressDisplay));

            myViewWarnImage = (ImageView) findViewById(R.id.RFwarnAreaIcon);
            myViewWarnImage.setVisibility(ViewStatus);

            myViewWarn = (View) findViewById(R.id.RFwarnArea1);
            myViewWarn.setVisibility(ViewStatus);
            myViewWarn = (View) findViewById(R.id.RFwarnArea2);
            myViewWarn.setVisibility(ViewStatus);
            myViewWarn = (View) findViewById(R.id.RFwarnArea3);
            myViewWarn.setVisibility(ViewStatus);
            myViewWarn = (View) findViewById(R.id.RFwarnArea4);
            myViewWarn.setVisibility(ViewStatus);

        } else if (which == TPMSParam.lb) {
            myViewLBTemp.setTextColor(getResources().getColor(R.color.normalTempDisplay));
            myViewLBTempUnit.setTextColor(getResources().getColor(R.color.normalTempDisplay));

            myViewLBPress.setTextColor(getResources().getColor(R.color.normalPressDisplay));
            myViewLBPressUnit.setTextColor(getResources().getColor(R.color.normalPressDisplay));

            myViewWarnImage = (ImageView) findViewById(R.id.LBwarnAreaIcon);
            myViewWarnImage.setVisibility(ViewStatus);

            myViewWarn = (View) findViewById(R.id.LBwarnArea1);
            myViewWarn.setVisibility(ViewStatus);
            myViewWarn = (View) findViewById(R.id.LBwarnArea2);
            myViewWarn.setVisibility(ViewStatus);
            myViewWarn = (View) findViewById(R.id.LBwarnArea3);
            myViewWarn.setVisibility(ViewStatus);
            myViewWarn = (View) findViewById(R.id.LBwarnArea4);
            myViewWarn.setVisibility(ViewStatus);

        } else {
            myViewRBTemp.setTextColor(getResources().getColor(R.color.normalTempDisplay));
            myViewRBTempUnit.setTextColor(getResources().getColor(R.color.normalTempDisplay));

            myViewRBPress.setTextColor(getResources().getColor(R.color.normalPressDisplay));
            myViewRBPressUnit.setTextColor(getResources().getColor(R.color.normalPressDisplay));

            myViewWarnImage = (ImageView) findViewById(R.id.RBwarnAreaIcon);
            myViewWarnImage.setVisibility(ViewStatus);

            myViewWarn = (View) findViewById(R.id.RBwarnArea1);
            myViewWarn.setVisibility(ViewStatus);
            myViewWarn = (View) findViewById(R.id.RBwarnArea2);
            myViewWarn.setVisibility(ViewStatus);
            myViewWarn = (View) findViewById(R.id.RBwarnArea3);
            myViewWarn.setVisibility(ViewStatus);
            myViewWarn = (View) findViewById(R.id.RBwarnArea4);
            myViewWarn.setVisibility(ViewStatus);
        }
    }

    void warnViewDisplay(int which) {
        ImageView myViewWarnImage;
        if (which == TPMSParam.lf) {
            myViewWarnImage = (ImageView) findViewById(R.id.LFwarnAreaIcon);
            myViewWarnImage.setVisibility(View.INVISIBLE);
        } else if (which == TPMSParam.rf) {
            myViewWarnImage = (ImageView) findViewById(R.id.RFwarnAreaIcon);
            myViewWarnImage.setVisibility(View.INVISIBLE);
        } else if (which == TPMSParam.lb) {
            myViewWarnImage = (ImageView) findViewById(R.id.LBwarnAreaIcon);
            myViewWarnImage.setVisibility(View.INVISIBLE);
        } else if (which == TPMSParam.rb) {
            myViewWarnImage = (ImageView) findViewById(R.id.RBwarnAreaIcon);
            myViewWarnImage.setVisibility(View.INVISIBLE);
        }
    }

    private void tyreFlash() {
        View myViewWarn;
        TextView myViewWarnText;
        ImageView myViewWarnIcon;
        byte i = 0;
        int tyre;

        press_lf = TPMSParam.tyreData.getPressValue(TPMSParam.lf);
        press_rf = TPMSParam.tyreData.getPressValue(TPMSParam.rf);
        press_lb = TPMSParam.tyreData.getPressValue(TPMSParam.lb);
        press_rb = TPMSParam.tyreData.getPressValue(TPMSParam.rb);

        temp_lf = TPMSParam.tyreData.getTempValue(TPMSParam.lf);
        temp_rf = TPMSParam.tyreData.getTempValue(TPMSParam.rf);
        temp_lb = TPMSParam.tyreData.getTempValue(TPMSParam.lb);
        temp_rb = TPMSParam.tyreData.getTempValue(TPMSParam.rb);

        WarnCenterImage.setVisibility(0x04);
        tyre = TPMSParam.lf;
        ViewClr(tyre);
        if (press_lf.HighAlertStatus || press_lf.LowAlertStatus || temp_lf.HighAlertStatus
                || press_lf.LeakageAlertStatus || press_lf.LBStatus || press_lf.NSStatus) {
            warnViewDisplay(tyre);
            if (temp_lf.HighAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_tp_high));
                myViewWarnText.setText(getResources().getString(R.string.highTemp));
                myViewLFTemp.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewLFTempUnit.setTextColor(getResources().getColor(R.color.warnDisplay));

            }
            if (press_lf.LowAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_plow));
                myViewWarnText.setText(getResources().getString(R.string.lowPress));
                myViewLFPress.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewLFPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
            } else if (press_lf.HighAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_phigh));
                myViewWarnText.setText(getResources().getString(R.string.highPress));

                myViewLFPress.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewLFPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
            }
            if (press_lf.LeakageAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_leakage));
                myViewWarnText.setText(getResources().getString(R.string.leakPress));

                myViewLFPress.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewLFPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));

            }
            if (press_lf.LBStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_elow));
                myViewWarnText.setText(getResources().getString(R.string.leakBattery));
            }
            if (press_lf.NSStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_sbleak));
                myViewWarnText.setText(getResources().getString(R.string.breakSenor));
            }

        } else {

            myViewWarn = ViewRequest(tyre, i);
            myViewWarnText = TextViewRequest(tyre, i);
            myViewWarnIcon = ImageViewRequest(tyre, i);
            myViewWarn.setVisibility(View.VISIBLE);
            if (press_lf.validFlag) {
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_select_button));
                myViewWarnText.setText(getResources().getString(R.string.StatusOk));
            } else {
                myViewWarnIcon.setImageDrawable(null);
                myViewWarnIcon.setVisibility(View.GONE);
                myViewWarnText.setText(getResources().getString(R.string.StatusUpdate));
            }
        }

        tyre = TPMSParam.rf;
        ViewClr(tyre);
        i = 0;
        if (press_rf.HighAlertStatus || press_rf.LowAlertStatus || temp_rf.HighAlertStatus
                || press_rf.LeakageAlertStatus || press_rf.LBStatus || press_rf.NSStatus) {
            warnViewDisplay(tyre);
            WarnCenterImage.setVisibility(0x00);
            if (temp_rf.HighAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_tp_high));
                myViewWarnText.setText(getResources().getString(R.string.highTemp));
                myViewRFTemp.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewRFTempUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
            }
            if (press_rf.LowAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_plow));
                myViewWarnText.setText(getResources().getString(R.string.lowPress));

                myViewRFPress.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewRFPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
            } else if (press_rf.HighAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_phigh));
                myViewWarnText.setText(getResources().getString(R.string.highPress));

                myViewRFPress.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewRFPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
            }
            if (press_rf.LeakageAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_leakage));
                myViewWarnText.setText(getResources().getString(R.string.leakPress));

                myViewRFPress.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewRFPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));

            }

            if (press_rf.LBStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_elow));
                myViewWarnText.setText(getResources().getString(R.string.leakBattery));
            }
            if (press_rf.NSStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_sbleak));
                myViewWarnText.setText(getResources().getString(R.string.breakSenor));
            }
        } else {
            myViewWarn = ViewRequest(tyre, i);
            myViewWarnText = TextViewRequest(tyre, i);
            myViewWarnIcon = ImageViewRequest(tyre, i);
            myViewWarn.setVisibility(View.VISIBLE);
            if (press_rf.validFlag) {
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_select_button));
                myViewWarnText.setText(getResources().getString(R.string.StatusOk));
            } else {
                myViewWarnIcon.setImageDrawable(null);
                myViewWarnIcon.setVisibility(View.GONE);
                myViewWarnText.setText(getResources().getString(R.string.StatusUpdate));
            }
        }

        tyre = TPMSParam.lb;
        ViewClr(tyre);
        i = 0;
        if (press_lb.HighAlertStatus || press_lb.LowAlertStatus || temp_lb.HighAlertStatus
                || press_lb.LeakageAlertStatus || press_lb.LBStatus || press_lb.NSStatus) {
            warnViewDisplay(tyre);
            WarnCenterImage.setVisibility(0x00);
            if (temp_lb.HighAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_tp_high));
                myViewWarnText.setText(getResources().getString(R.string.highTemp));
                myViewLBTemp.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewLBTempUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
            }
            if (press_lb.LowAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_plow));
                myViewWarnText.setText(getResources().getString(R.string.lowPress));

                myViewLBPress.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewLBPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
            } else if (press_lb.HighAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_phigh));
                myViewWarnText.setText(getResources().getString(R.string.highPress));
                myViewLBPress.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewLBPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
            }
            if (press_lb.LeakageAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_leakage));
                myViewWarnText.setText(getResources().getString(R.string.leakPress));
                myViewLBPress.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewLBPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));

            }
            if (press_lb.LBStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_elow));
                myViewWarnText.setText(getResources().getString(R.string.leakBattery));
            }
            if (press_lb.NSStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_sbleak));
                myViewWarnText.setText(getResources().getString(R.string.breakSenor));
            }
        } else {
            myViewWarn = ViewRequest(tyre, i);
            myViewWarnText = TextViewRequest(tyre, i);
            myViewWarnIcon = ImageViewRequest(tyre, i);
            myViewWarn.setVisibility(View.VISIBLE);
            if (press_lb.validFlag) {
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_select_button));
                myViewWarnText.setText(getResources().getString(R.string.StatusOk));
            } else {
//				myViewWarnIcon.setImageDrawable(null);
                myViewWarnIcon.setVisibility(View.GONE);
                myViewWarnText.setText(getResources().getString(R.string.StatusUpdate));
            }
        }

        tyre = TPMSParam.rb;
        ViewClr(tyre);
        i = 0;
        if (press_rb.HighAlertStatus || press_rb.LowAlertStatus || temp_rb.HighAlertStatus
                || press_rb.LeakageAlertStatus || press_rb.LBStatus || press_rb.NSStatus) {
            warnViewDisplay(tyre);
            WarnCenterImage.setVisibility(0x00);
            if (temp_rb.HighAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_tp_high));
                myViewWarnText.setText(getResources().getString(R.string.highTemp));
                myViewRBTemp.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewRBTempUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
            }
            if (press_rb.LowAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_plow));
                myViewWarnText.setText(getResources().getString(R.string.lowPress));
                myViewRBPress.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewRBPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
            } else if (press_rb.HighAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_phigh));
                myViewWarnText.setText(getResources().getString(R.string.highPress));
                myViewRBPress.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewRBPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
            }
            if (press_rb.LeakageAlertStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_leakage));
                myViewWarnText.setText(getResources().getString(R.string.leakPress));
                myViewRBPress.setTextColor(getResources().getColor(R.color.warnDisplay));
                myViewRBPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
            }
            if (press_rb.LBStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_elow));
                myViewWarnText.setText(getResources().getString(R.string.leakBattery));
            }
            if (press_rb.NSStatus) {
                myViewWarn = ViewRequest(tyre, i);
                myViewWarnText = TextViewRequest(tyre, i);
                myViewWarnIcon = ImageViewRequest(tyre, i);
                i++;

                myViewWarn.setVisibility(View.VISIBLE);
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_sbleak));
                myViewWarnText.setText(getResources().getString(R.string.breakSenor));
            }
        } else {

            myViewWarn = ViewRequest(tyre, i);
            myViewWarnText = TextViewRequest(tyre, i);
            myViewWarnIcon = ImageViewRequest(tyre, i);
            myViewWarn.setVisibility(View.VISIBLE);
            if (press_rb.validFlag) {
                myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_select_button));
                myViewWarnText.setText(getResources().getString(R.string.StatusOk));
            } else {
                myViewWarnIcon.setImageDrawable(null);
                myViewWarnIcon.setVisibility(View.GONE);
                myViewWarnText.setText(getResources().getString(R.string.StatusUpdate));
            }
        }

        myViewLFPress.setText(press_lf.Display);
        myViewLFTemp.setText(temp_lf.Display + temp_lf.unitName);

        myViewRFPress.setText(press_rf.Display);
        myViewRFTemp.setText(temp_rf.Display + temp_rf.unitName);

        myViewLBPress.setText(press_lb.Display);
        myViewLBTemp.setText(temp_lb.Display + temp_lb.unitName);

        myViewRBPress.setText(press_rb.Display);
        myViewRBTemp.setText(temp_rb.Display + temp_rb.unitName);

        myViewLFPressUnit.setText(press_lf.unitName);
        myViewLFTempUnit.setText(temp_lf.unitName);
        myViewRFPressUnit.setText(press_rf.unitName);
        myViewRFTempUnit.setText(temp_rf.unitName);
        myViewLBPressUnit.setText(press_lb.unitName);
        myViewLBTempUnit.setText(temp_lb.unitName);
        myViewRBPressUnit.setText(press_rb.unitName);
        myViewRBTempUnit.setText(temp_rb.unitName);
//        Intent intent = new Intent("hongkingtpms");
//        intent.putExtra("press_lf",press_lf.toString());
//        intent.putExtra("press_rf",press_rf.toString());
//        intent.putExtra("press_lb",press_lb.toString());
//        intent.putExtra("press_rb",press_rb.toString());
//        intent.putExtra("temp_lf",temp_lf.toString());
//        intent.putExtra("temp_rf",temp_rf.toString());
//        intent.putExtra("temp_lb",temp_lb.toString());
//        intent.putExtra("temp_rb",temp_rb.toString());
//        sendBroadcast(intent);
    }

    @SuppressLint("NewApi")
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                Log.e("20161129", "MainDisplayActATTACHED");
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//	            <usb-device product-id="29987" vendor-id="6790" />
//	            <usb-device product-id="21795" vendor-id="6790" />
                device.getProductId();
                if (device != null && (device.getProductId() == 29987 || device.getProductId() == 221795)) {
                    SysApplication.USBState = false;
//                    Toast.makeText(MainDisplayActivity.this, getResources().getString(R.string.device_disconnect), Toast.LENGTH_SHORT).show();
//                    sendBroadcast(new Intent(UsbControllerService.DISCONNECT_REQUEST_ACTION));
//                    startActivity(new Intent(MainDisplayActivity.this, ConnectChooseActivity.class));
//                    stopService(new Intent(MainDisplayActivity.this, UsbControllerService.class));
//                    stopService(new Intent(MainDisplayActivity.this, SysManagerService.class));
                    heartflag = false;
                    SysApplication.heartflag = false;
                    finish();
                } else {
//	            	sendBroadcast(new Intent(UsbControllerService.RECONNECT_ACTION));
                }
            } else {

            }
        }
    };





    //settting

    private List<Map<String, Object>> getData() {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

        // int i = 0;
        // 空出一行
        // map.put("title", " ");
        // map.put("flag", "tag");
        // list.add(map);
        // menuBase++;
        // 设置分栏:功能设置
        // map = new HashMap<String, Object>();
        // map.put("title", getResources().getString(R.string.FeatureSetup));
        // //map.put("title", "Feature Setup");
        // map.put("flag","tag");
        // list.add(map);
        // menuBase++;




        // 压力单位设置
        map = new HashMap<String, Object>();
        map.put("imgTitle", R.drawable.icon_tyre_low);
        map.put("title", getResources().getString(R.string.PressureUnitSetup));
        map.put("info", TPMSParam.tyreData.sPressUnitSting[TPMSParam.tyreData.getPressUnit()]);
        map.put("flag", "middle");
        list.add(map);

        // 温度单位设置
        map = new HashMap<String, Object>();
        map.put("imgTitle", R.drawable.icon_warn_thigh);
        map.put("title", getResources().getString(R.string.TemperatureUnitSetup));
        map.put("info", TPMSParam.tyreData.sTempUnitSting[TPMSParam.tyreData.getTempUnit()]);
        map.put("flag", "middle");
        list.add(map);

        // 报警值设置
/*        map = new HashMap<String, Object>();
        map.put("imgTitle", R.drawable.icon_warn);
        map.put("title", getResources().getString(R.string.AlarmValueSetup));
        map.put("img", R.drawable.card_arrow);
        map.put("flag", "middle");
        list.add(map);*/


        //标准充气压力设置
        String test1 = "";
        if (TPMSParam.tyreData.getPressCapacityWarn().Display.length() == 3) {
            test1 = "0";
        }
        map = new HashMap<String, Object>();
        map.put("imgTitle", R.drawable.icon_tyre_norm);
        map.put("title", getResources().getString(R.string.StringWarnNormalPre));
        map.put("info", TPMSParam.tyreData.getPressCapacityWarn().Display + test1 + " " + TPMSParam.tyreData.getPressCapacityWarn().unitName);
        map.put("flag", "middle");
        list.add(map);

        //高压报警值
        String test2 = "";
        if (TPMSParam.tyreData.getPressHighWarn().Display.length() == 3) {
            test2 = "0";
        }
        map = new HashMap<String, Object>();
        map.put("imgTitle", R.drawable.icon_tyre_high);
        map.put("title", getResources().getString(R.string.StringWarnHighPre));
        map.put("info", TPMSParam.tyreData.getPressHighWarn().Display + test2 + " " + TPMSParam.tyreData.getPressHighWarn().unitName);
        map.put("flag", "middle");
        list.add(map);

        //低压报警值
        String test3 = "";
        if (TPMSParam.tyreData.getPressLowWarn().Display.length() == 3) {
            test3 = "0";
        }
        map = new HashMap<String, Object>();
        map.put("imgTitle", R.drawable.icon_tyre_low);
        map.put("title", getResources().getString(R.string.StringWarnLowPre));
        map.put("info", TPMSParam.tyreData.getPressLowWarn().Display + test3 + " " + TPMSParam.tyreData.getPressLowWarn().unitName);
        map.put("flag", "middle");
        list.add(map);
        //高温报警值
        map = new HashMap<String, Object>();
        map.put("imgTitle", R.drawable.icon_warn_thigh);
        map.put("title", getResources().getString(R.string.StringWarnHighTemp));
        map.put("info", TPMSParam.tyreData.getTempHighWarn().Display + " " + TPMSParam.tyreData.getTempHighWarn().unitName);
        map.put("flag", "bottom");
        list.add(map);


        // 传感器更换
        map = new HashMap<String, Object>();
        map.put("imgTitle", R.drawable.icon_tyre_change);
        map.put("title", getResources().getString(R.string.ReplaceTheTireSensor));
        map.put("img", R.drawable.card_arrow);
        map.put("flag", "middle");
        list.add(map);

        // 轮胎对调
        map = new HashMap<String, Object>();
        map.put("imgTitle", R.drawable.icon_tyre_sw);
        map.put("title", getResources().getString(R.string.TireSwap));
        map.put("img", R.drawable.card_arrow);
        map.put("flag", "bottom");
        list.add(map);

        // //顶出空行
        // map = new HashMap<String, Object>();
        // map.put("title", " ");
        // map.put("flag", "tag");
        // list.add(map);

        // //顶出空行
        // map = new HashMap<String, Object>();
        // map.put("title", " ");
        // map.put("flag", "tag");
        // list.add(map);

        // 系统设置
        // map = new HashMap<String, Object>();
        // map.put("title", getResources().getString(R.string.SystemSetup));
        // map.put("flag","tag");
        // list.add(map);

        // 界面风格
        // map = new HashMap<String, Object>();
        // map.put("imgTitle", R.drawable.icon_theme);
        // map.put("title",
        // getResources().getString(R.string.InterfaceStyleSetup));
        // map.put("flag","middle");
        // map.put("img", R.drawable.card_arrow);
        // list.add(map);

        // //屏幕长量
        // map = new HashMap<String, Object>();
        // map.put("imgTitle", R.drawable.icon_light);
        // map.put("title",
        // getResources().getString(R.string.ScreenAlwaysOnSetup));
        // map.put("flag","middle");
        // map.put("info",
        // getResources().getStringArray(R.array.SleepTimeSet)[TPMSParam.iSleepTime]);
        // list.add(map);

        // //系统语言设置
        // map = new HashMap<String, Object>();
        // map.put("imgTitle", R.drawable.icon_language);
        // map.put("title", getResources().getString(R.string.LanguageSetup));
        // map.put("flag","middle");
        // map.put("info",
        // getResources().getStringArray(R.array.LanguageSet)[TPMSParam.iLanguageType]);
        // list.add(map);

        // 恢复出厂设置
        map = new HashMap<String, Object>();
        map.put("imgTitle", R.drawable.icon_reset);
        map.put("title", getResources().getString(R.string.RestoreFactorySetup));
        map.put("flag", "middle");
        map.put("img", R.drawable.card_arrow);
        list.add(map);




        // 开机启动设置
        map = new HashMap<String, Object>();
        map.put("imgTitle", R.drawable.icon_reset);
        map.put("title", getResources().getString(R.string.AutoStartSetup));

        map.put("flag", "middle");
        list.add(map);


        // 技术支持中兴屏蔽
        map = new HashMap<String, Object>();
        map.put("imgTitle", R.drawable.icon_support);
        map.put("title", getResources().getString(R.string.tech_support));
        map.put("flag", "middle");
        map.put("img", R.drawable.card_arrow);
        list.add(map);

        // 顶出空行
        map = new HashMap<String, Object>();
        map.put("title", " ");
        map.put("flag", "tag");
        list.add(map);
        // 顶出空行
        map = new HashMap<String, Object>();
        map.put("title", " ");
        map.put("flag", "tag");
        list.add(map);

        return list;
    }

    class MyListViewListenerCommon implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            final int iSelect;

            app.changeMode(SysParam.SYS_MODE.MODE_SETUP);



            if (mData.get(arg2).get("title").equals(getResources().getString(R.string.PressureUnitSetup))) {


//				AlertDialog.Builder builder = new AlertDialog.Builder(SetActivity.this);
//				builder.setTitle(getResources().getString(R.string.PressureUnitSetup));
//				final String[] sex = {"男", "女", "未知性别"};
//				//    设置一个单项选择下拉框
//				/**
//				 * 第一个参数指定我们要显示的一组下拉单选框的数据集合
//				 * 第二个参数代表索引，指定默认哪一个单选框被勾选上，1表示默认'女' 会被勾选上
//				 * 第三个参数给每一个单选项绑定一个监听器
//				 */
//				builder.setSingleChoiceItems(sex, 1, new DialogInterface.OnClickListener()
//				{
//					@Override
//					public void onClick(DialogInterface dialog, int which)
//					{
//						Toast.makeText(SetActivity.this, "性别为：" + sex[which], Toast.LENGTH_SHORT).show();
//					}
//				});
//				builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
//				{
//					@Override
//					public void onClick(DialogInterface dialog, int which)
//					{
//						iUserSelect = which;
//						SetParamCommand(TPMSParam.mBluethoothData.SET_PRESS_UNIT, iUserSelect);
//					}
//				});
//				builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
//				{
//					@Override
//					public void onClick(DialogInterface dialog, int which)
//					{
//
//					}
//				});
//				builder.show();



                PressureUnitSetPop pop = new PressureUnitSetPop(MainDisplayActivity.this);
                pop.setOnCheckedListenerr(new PressureUnitSetPop.OnCheckedListener() {
                    @Override
                    public void onChecked(int positon) {
                        iUserSelect = positon;
                        SetParamCommand(TPMSParam.mBluethoothData.SET_PRESS_UNIT, iUserSelect);
                    }
                });

                pop.showAtLocation(getRootView(MainDisplayActivity.this), Gravity.CENTER, 0, 0);
//				pop.initPosition(TPMSParam.tyreData.getPressUnit());

            }else if(mData.get(arg2).get("title").equals(getResources().getString(R.string.AutoStartSetup))){
                // 开机启动设置
      /*          map = new HashMap<String, Object>();
                map.put("imgTitle", R.drawable.icon_reset);
                map.put("title", getResources().getString(R.string.AutoStartSetup));
                map.put("info", sp.getAutostart());
                map.put("flag", "middle");
                list.add(map);*/

                AutoStartUnitSetPop pop = new AutoStartUnitSetPop(MainDisplayActivity.this);
                pop.setOnCheckedListenerr(new PressureUnitSetPop.OnCheckedListener() {
                    @Override
                    public void onChecked(int positon) {
                   if(positon==1)
                       sp.setAutostart("noflag");
                   else
                       sp.setAutostart("isflag");

                    }
                });
                pop.showAtLocation(getRootView(MainDisplayActivity.this), Gravity.CENTER, 0, 0);

                if(sp.getAutostart().contains("noflag"))
                    pop.initPosition(1);
                else
                    pop.initPosition(0);

            }


            else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.TemperatureUnitSetup))) {

                TemperatureUnitSetPop pop = new TemperatureUnitSetPop(MainDisplayActivity.this);
                pop.setOnCheckedListenerr(new PressureUnitSetPop.OnCheckedListener() {
                    @Override
                    public void onChecked(int positon) {
                        iUserSelect = positon;
                        SetParamCommand(TPMSParam.mBluethoothData.SET_TEMP_UNIT, iUserSelect);
                    }
                });
                pop.showAtLocation(getRootView(MainDisplayActivity.this), Gravity.CENTER, 0, 0);
                pop.initPosition(TPMSParam.tyreData.getTempUnit());

            } else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.LanguageSetup))) {
                iSelect = arg2;
                LayoutInflater inflater = LayoutInflater.from(MainDisplayActivity.this);
                View view = inflater.inflate(R.layout.set_item_dialog, null);
                boolean dismiss = true;
                final String languagetype[] = getResources().getStringArray(R.array.LanguageSet);
                Dialog SoundDialog = new SysRadioButtonDialog.Builder(MainDisplayActivity.this)
                        .setMessage((String) mData.get(arg2).get("title")).setSingleChoiceItems(languagetype,
                                TPMSParam.iLanguageType, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        iUserSelect = which;
                                    }
                                })
                        .setPositiveButton(getResources().getString(R.string.confirm),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        ListAdapter la = listView.getAdapter();

                                        // dialog.dismiss();
                                        // if(languagetype[iUserSelect].equals("English"))//暂不支持英文
                                        // {
                                        // onCreateDialog(0x01,String.format("暂不支持"));
                                        // }else{
                                        TPMSParam.iLanguageType = iUserSelect;
                                        // ssitchlanguage(iUserSelect);
                                        HashMap<String, Object> map = (HashMap<String, Object>) mData.get(iSelect);

                                        map.put("info", languagetype[TPMSParam.iLanguageType]);
                                        ((SysListviewbase) la).notifyDataSetChanged();// 通知系统，实时更新
										/*
										 * View view = null; LayoutInflater
										 * inflater = LayoutInflater.from(this);
										 * view = inflater.inflate(R.layout.
										 * set_simple_dialog, null);
										 * customBuilder.setMessage("")
										 * .setPositiveButton(getResources().
										 * getString(R.string.confirm),this); //
										 */
                                        // ssitchlanguage(TPMSParam.iLanguageType);
                                        onCreateDialog(0x05, getResources().getString(R.string.PlaseRStartApp));
                                        // System.exit(0);
                                        // }
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.Cancel), null).create(view, dismiss);
                SoundDialog.show();
                curDialog = SoundDialog;
            } else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.ScreenAlwaysOnSetup))) {
                iSelect = arg2;
                LayoutInflater inflater = LayoutInflater.from(MainDisplayActivity.this);
                View view = inflater.inflate(R.layout.set_item_dialog, null);
                boolean dismiss = true;
                final String temp[] = getResources().getStringArray(R.array.SleepTimeSet);
                Dialog DisplayTimeDialog = new SysRadioButtonDialog.Builder(MainDisplayActivity.this)
                        .setMessage((String) mData.get(arg2).get("title"))
                        .setSingleChoiceItems(temp, TPMSParam.iSleepTime, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                iUserSelect = which;
                            }
                        }).setPositiveButton(getResources().getString(R.string.confirm),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ListAdapter la = listView.getAdapter();
                                        TPMSParam.iSleepTime = iUserSelect;
                                        HashMap<String, Object> map = (HashMap<String, Object>) mData.get(iSelect);
                                        map.put("info", temp[TPMSParam.iSleepTime]);
                                        ((SysListviewbase) la).notifyDataSetChanged();// 通知系统，实时更新
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.Cancel), null).create(view, dismiss);
                DisplayTimeDialog.show();
                curDialog = DisplayTimeDialog;
            }
            else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.AlarmValueSetup))) {
                Intent newIntent = new Intent(MainDisplayActivity.this, SetWarnSubActivity.class);
                startActivityForResult(newIntent, 102);
            }else if(mData.get(arg2).get("title").equals(getResources().getString(R.string.StringWarnNormalPre)))
            {

                iUserSelect = arg2;
                ValueSetPop pop = new ValueSetPop(MainDisplayActivity.this);
                pop.setTitle((String) mData.get(iUserSelect).get("title"));
                pop.setOnValueSetListenerr(new ValueSetPop.OnValueSetListener() {
                    @Override
                    public void onValueSet(final int value) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SetParamCommand(iUserSelect, value);
                            }
                        }).start();
                    }
                });

                pop.initData(TPMSParam.tyreData.getPressCapacityWarn());
                pop.showAtLocation(getRootView(MainDisplayActivity.this), Gravity.CENTER, 0, 0);


            }else if(mData.get(arg2).get("title").equals(getResources().getString(R.string.StringWarnHighPre)))
            {

                iUserSelect = arg2;
                ValueSetPop pop = new ValueSetPop(MainDisplayActivity.this);
                pop.setTitle((String) mData.get(iUserSelect).get("title"));
                pop.setOnValueSetListenerr(new ValueSetPop.OnValueSetListener() {
                    @Override
                    public void onValueSet(final int value) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SetParamCommand(iUserSelect, value);
                            }
                        }).start();
                    }
                });

                pop.initData(TPMSParam.tyreData.getPressHighWarn());
                pop.showAtLocation(getRootView(MainDisplayActivity.this), Gravity.CENTER, 0, 0);

            }else if(mData.get(arg2).get("title").equals(getResources().getString(R.string.StringWarnLowPre)))
            {
                iUserSelect = arg2;
                ValueSetPop pop = new ValueSetPop(MainDisplayActivity.this);
                pop.setTitle((String) mData.get(iUserSelect).get("title"));
                pop.setOnValueSetListenerr(new ValueSetPop.OnValueSetListener() {
                    @Override
                    public void onValueSet(final int value) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SetParamCommand(iUserSelect, value);
                            }
                        }).start();
                    }
                });

                pop.initData(TPMSParam.tyreData.getPressLowWarn());
                pop.showAtLocation(getRootView(MainDisplayActivity.this), Gravity.CENTER, 0, 0);

            }else if(mData.get(arg2).get("title").equals(getResources().getString(R.string.StringWarnHighTemp)))
            {
                iUserSelect = arg2;
                ValueSetPop pop = new ValueSetPop(MainDisplayActivity.this);
                pop.setTitle((String) mData.get(iUserSelect).get("title"));
                pop.setOnValueSetListenerr(new ValueSetPop.OnValueSetListener() {
                    @Override
                    public void onValueSet(final int value) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SetParamCommand(iUserSelect, value);
                            }
                        }).start();
                    }
                });

                pop.initData(TPMSParam.tyreData.getTempHighWarn());
                pop.showAtLocation(getRootView(MainDisplayActivity.this), Gravity.CENTER, 0, 0);

            }

            else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.InterfaceStyleSetup))) {

            } else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.ReplaceTheTireSensor))) {
                Intent newIntent = new Intent(MainDisplayActivity.this, SetSensorChangeActivity.class);
                startActivityForResult(newIntent, 102);
            } else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.TireSwap))) {
                Intent newIntent = new Intent(MainDisplayActivity.this, SetSensorExchangeActivity.class);
                startActivityForResult(newIntent, 103);
            } else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.tech_support))) {
                Intent newIntent = new Intent(MainDisplayActivity.this, AboutMeAfterSaleSupportActivity.class);
                startActivity(newIntent);
            } else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.RestoreFactorySetup))) {
				/*Dialog dialog = null;
				LayoutInflater inflater = LayoutInflater.from(SetActivity.this);
				SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(SetActivity.this);
				View view = inflater.inflate(R.layout.set_verify_dialog, null);
				input = (EditText) view.findViewById(R.id.inputcode);
				bitmapView = (ImageView) view.findViewById(R.id.Imageview);
				bitmapView.setImageBitmap(SetBpUtil.getInstance().createBitmap());
				infoView = (TextView) view.findViewById(R.id.message);
				infoView1 = (TextView) view.findViewById(R.id.infoview1);
				infoView1.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						bitmapView.setImageBitmap(SetBpUtil.getInstance().createBitmap());
					}
				});
				infoView1.setOnTouchListener(new View.OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							// 更改为按下时的背景图片
							infoView1.setTextColor(getResources().getColor(R.color.display_touch));
						} else if (event.getAction() == MotionEvent.ACTION_UP) {
							infoView1.setTextColor(getResources().getColor(R.color.display_text));
						}
						return false;
					}
				});
				// Log.v("seting","bitmap code is
				// "+SetBpUtil.getInstance().getCode());
				customBuilder.setMessage(SetActivity.this.getResources().getString(R.string.StringVerifyAlter))
						.setNegativeButton(getResources().getString(R.string.Cancel),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								})
						.setPositiveButton(getResources().getString(R.string.confirm),
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog, int which) {
										if (input.getText().toString()
												.equalsIgnoreCase(SetBpUtil.getInstance().getCode())) {
											dialog.dismiss();
											SetActivity.this.onCreateDialog(0x03, "");
										} else {

											infoView.setText(getResources().getString(R.string.InputErr));
											bitmapView.setImageBitmap(SetBpUtil.getInstance().createBitmap());
											input.setText("");
										}
									}
								});
				dialog = customBuilder.create(view, false);
				dialog.show();
				curDialog = dialog;*/
//                MainDisplayActivity.this.onCreateDialog(0x03, "");


                ResetUnitSetPop pop = new ResetUnitSetPop(MainDisplayActivity.this);
                pop.setOnCheckedListenerr(new PressureUnitSetPop.OnCheckedListener() {
                    @Override
                    public void onChecked(int positon) {
                        if(positon==0)
                        {
                        SysApplication.BluetoothState=false;
                        SysApplication.USBState=false;
                        sp.setConnectType(0);
                        TPMSParam.sConnectedBlueAddr="";
                        app.changeMode(SysParam.SYS_MODE.MODE_REBOOT);
                        }
//                        iUserSelect = positon;
//                        SetParamCommand(TPMSParam.mBluethoothData.SET_TEMP_UNIT, iUserSelect);
                    }
                });
                pop.showAtLocation(getRootView(MainDisplayActivity.this), Gravity.CENTER, 0, 0);
                pop.initPosition(TPMSParam.tyreData.getTempUnit());

            }
        }
    }


    void SetParamCommand(final int which, final int value) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = 0x8b;
                Message msg = Message.obtain();
                if (mData.get(which + menuBase).get("title")
                        .equals(getResources().getString(R.string.PressureUnitSetup))) {
                    ret = TPMSParam.mBluethoothData.ProtocolSysParamSet(TPMSParam.mBluethoothData.SET_PRESS_UNIT,
                            value);
                } else if (mData.get(which + menuBase).get("title")
                        .equals(getResources().getString(R.string.TemperatureUnitSetup))) {
                    ret = TPMSParam.mBluethoothData.ProtocolSysParamSet(TPMSParam.mBluethoothData.SET_TEMP_UNIT, value);
                } else if (mData.get(which + menuBase).get("title")
                        .equals(getResources().getString(R.string.StringWarnNormalPre))) {
                    ret = TPMSParam.mBluethoothData.ProtocolSysParamSet(TPMSParam.mBluethoothData.SET_CAPACITY_WARN, value);
                } else if (mData.get(which + menuBase).get("title")
                        .equals(getResources().getString(R.string.StringWarnHighPre))) {
                    ret = TPMSParam.mBluethoothData.ProtocolSysParamSet(TPMSParam.mBluethoothData.SET_PRESSHIGH_WARN, value);
                } else if (mData.get(which + menuBase).get("title")
                        .equals(getResources().getString(R.string.StringWarnLowPre))) {
                    ret = TPMSParam.mBluethoothData.ProtocolSysParamSet(TPMSParam.mBluethoothData.SET_PRESSLOW_WARN, value);
                } else if (mData.get(which + menuBase).get("title")
                        .equals(getResources().getString(R.string.StringWarnHighTemp))) {
                    ret = TPMSParam.mBluethoothData.ProtocolSysParamSet(TPMSParam.mBluethoothData.SET_TEMPHIGH_WARN, value);
                }

                else {
                }
                msg.arg1 = which + menuBase;
                msg.arg2 = ret;
                if (mHandler != null)
                    mHandler.sendMessage(msg);
                TPMSParam.mBluethoothData.ProtocolSysParamGet();// 获取参数
            }
        }).start();
    }



    public Dialog onCreateDialog(int dialogId, String msg) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = null;
        SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(this);
        Dialog dialog = null;
        boolean mode = true;
        if (dialogId == 0x01) {

            if(density==1.5){
                view = inflater.inflate(R.layout.set_simple_dialogden15, null);
            }else {
                view = inflater.inflate(R.layout.set_simple_dialog, null);
            }
            customBuilder.setMessage(getResources().getString(R.string.SetFailure) + "\n" + msg).setPositiveButton(
                    getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        } else if (dialogId == 0x02) {
            view = inflater.inflate(R.layout.set_password_dialog, null);
            input = (EditText) view.findViewById(R.id.inputcode);
            infoView = (TextView) view.findViewById(R.id.message);

            customBuilder.setMessage(msg).setNegativeButton(getResources().getString(R.string.Cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton(getResources().getString(R.string.confirm),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (input.getText().toString().equals("27265937")) {
                                dialog.dismiss();
                                LayoutInflater inflater = LayoutInflater.from(MainDisplayActivity.this);
                                View view = inflater.inflate(R.layout.set_datapicker_dialog, null);
                                boolean dismiss = false;
                                final SysDatapickerDialog.Builder customBuilder = new SysDatapickerDialog.Builder(
                                        MainDisplayActivity.this);
                                ;
                                Dialog recordDialog = customBuilder.setMessage("RF射频调节")
                                        .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).setPositiveButton("修改", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // TODO Auto-generated method
                                                // stub
                                                SetRFCpValue((byte) customBuilder.TempValue);
                                            }
                                        }).create(view, TPMSParam.getRFCpInfo(), dismiss);
                                recordDialog.show();
                                curDialog = recordDialog;
                            } else {

                                infoView.setText("密码错误,请重新输入");
                                input.setText("");
                            }
                        }
                    });
            mode = false;
            // dismiss = false;//不自动消失

        } else if (dialogId == 0x03) {
            if(density==1.5){
                view = inflater.inflate(R.layout.set_custom_dialogden15, null);
            }else {
                view = inflater.inflate(R.layout.set_custom_dialog, null);
            }
            customBuilder.setMessage(this.getResources().getString(R.string.warnReset)).setPositiveButton(
                    getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SysApplication.BluetoothState=false;
                            SysApplication.USBState=false;
                            sp.setConnectType(0);
                            TPMSParam.sConnectedBlueAddr="";
                            app.changeMode(SysParam.SYS_MODE.MODE_REBOOT);
//							startActivity(new Intent(SetActivity.this,ConnectChooseActivity.class));
                        }
                    }).setNegativeButton(getResources().getString(R.string.Cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        } else if (dialogId == 0x04) {
            view = inflater.inflate(R.layout.set_wait_dialog1, null);
            mode = false;
            customBuilder.setMessage(null).setNegativeButton(getResources().getString(R.string.Cancel), null);
        } else if (dialogId == 0x05) {
            if(density==1.5){
                view = inflater.inflate(R.layout.set_simple_dialogden15, null);
            }else {
                view = inflater.inflate(R.layout.set_simple_dialog, null);
            }
            customBuilder.setMessage(getResources().getString(R.string.SetSuccess) + "\n" + msg).setPositiveButton(
                    getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
        }
        dialog = customBuilder.create(view, mode);

        curDialog = dialog;
        dialog.show();
        // curDialog = dialog;
        return dialog;
    }


    public View getRootView(Activity myContext){
        return ((ViewGroup)myContext.findViewById(android.R.id.content)).getChildAt(0);
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            ListAdapter la = listView.getAdapter();
            if (mData.get(msg.arg1).get("title").equals(getResources().getString(R.string.PressureUnitSetup))) {
                if (msg.arg2 == 0x00) {
                    HashMap<String, Object> map = (HashMap<String, Object>) mData.get(msg.arg1);
                    map.put("info", TPMSParam.tyreData.sPressUnitSting[TPMSParam.tyreData.getPressUnit()]);


                     map = (HashMap<String, Object>) mData.get(2);
                    String test1 = "";
                    if (TPMSParam.tyreData.getPressCapacityWarn().Display.length() == 3) {
                        test1 = "0";
                    }
                    String test2 = "";
                    if (TPMSParam.tyreData.getPressHighWarn().Display.length() == 3) {
                        test2 = "0";
                    }
                    String test3 = "";
                    if (TPMSParam.tyreData.getPressLowWarn().Display.length() == 3) {
                        test3 = "0";
                    }
                    map.put("info", TPMSParam.tyreData.getPressCapacityWarn().Display + test1 + " " + TPMSParam.tyreData.getPressCapacityWarn().unitName);
                    map = (HashMap<String, Object>) mData.get(3);
                    map.put("info", TPMSParam.tyreData.getPressHighWarn().Display + test2 + " " + TPMSParam.tyreData.getPressHighWarn().unitName);
                    map = (HashMap<String, Object>) mData.get(4);
                    map.put("info", TPMSParam.tyreData.getPressLowWarn().Display + test3 + " " + TPMSParam.tyreData.getPressLowWarn().unitName);




                    ((SysListviewbase) la).notifyDataSetChanged();// 通知系统，实时更新

                } else {
                    onCreateDialog(0x01, String.format("%s",
                            SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(MainDisplayActivity.this, msg.arg2)));
                }
            } else if (mData.get(msg.arg1).get("title")
                    .equals(getResources().getString(R.string.TemperatureUnitSetup))) {
                if (msg.arg2 == 0x00) {
                    HashMap<String, Object> map = (HashMap<String, Object>) mData.get(msg.arg1);
                    map.put("info", TPMSParam.tyreData.sTempUnitSting[TPMSParam.tyreData.getTempUnit()]);
                    map = (HashMap<String, Object>) mData.get(5);
                    map.put("info", TPMSParam.tyreData.getTempHighWarn().Display + " " + TPMSParam.tyreData.getTempHighWarn().unitName);

                    ((SysListviewbase) la).notifyDataSetChanged();// 通知系统，实时更新
                } else {
                    onCreateDialog(0x01, String.format("%s",
                            SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(MainDisplayActivity.this, msg.arg2)));
                }
            }else if (mData.get(msg.arg1).get("title")
                    .equals(getResources().getString(R.string.StringWarnNormalPre)))
            {

                HashMap<String, Object> map = (HashMap<String, Object>) mData.get(iUserSelect);

                if (msg.arg2 == 0x00) {
                    String test1 = "";
                    if (TPMSParam.tyreData.getPressCapacityWarn().Display.length() == 3) {
                        test1 = "0";
                    }
                    String test2 = "";
                    if (TPMSParam.tyreData.getPressHighWarn().Display.length() == 3) {
                        test2 = "0";
                    }
                    String test3 = "";
                    if (TPMSParam.tyreData.getPressLowWarn().Display.length() == 3) {
                        test3 = "0";
                    }
                    map.put("info", TPMSParam.tyreData.getPressCapacityWarn().Display + test1 + " " + TPMSParam.tyreData.getPressCapacityWarn().unitName);
                    map = (HashMap<String, Object>) mData.get(iUserSelect + 1);
                    map.put("info", TPMSParam.tyreData.getPressHighWarn().Display + test2 + " " + TPMSParam.tyreData.getPressHighWarn().unitName);
                    map = (HashMap<String, Object>) mData.get(iUserSelect + 2);
                    map.put("info", TPMSParam.tyreData.getPressLowWarn().Display + test3 + " " + TPMSParam.tyreData.getPressLowWarn().unitName);

                    ((SysListviewbase) la).notifyDataSetChanged();// 通知系统，实时更新

                } else {
                    onCreateErrDialog(0x01, String.format(SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(MainDisplayActivity.this, msg.arg2)));
                }

            }
            else if (mData.get(msg.arg1).get("title")
                    .equals(getResources().getString(R.string.StringWarnHighPre)))
            {
                HashMap<String, Object> map = (HashMap<String, Object>) mData.get(iUserSelect);
                if (msg.arg2 == 0x00) {
                    String test2 = "";
                    if (TPMSParam.tyreData.getPressHighWarn().Display.length() == 3) {
                        test2 = "0";
                    }
                    map.put("info", TPMSParam.tyreData.getPressHighWarn().Display +test2+ " " + TPMSParam.tyreData.getPressHighWarn().unitName);

                    ((SysListviewbase) la).notifyDataSetChanged();// 通知系统，实时更新

                } else {
                    onCreateErrDialog(0x01, String.format(SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(MainDisplayActivity.this, msg.arg2)));
                }
            }
            else if (mData.get(msg.arg1).get("title")
                    .equals(getResources().getString(R.string.StringWarnLowPre)))
            {
                HashMap<String, Object> map = (HashMap<String, Object>) mData.get(iUserSelect);
                if (msg.arg2 == 0x00) {
                    String test3 = "";
                    if (TPMSParam.tyreData.getPressLowWarn().Display.length() == 3) {
                        test3 = "0";
                    }
                    map.put("info", TPMSParam.tyreData.getPressLowWarn().Display+test3 + " " + TPMSParam.tyreData.getPressLowWarn().unitName);

                    ((SysListviewbase) la).notifyDataSetChanged();// 通知系统，实时更新

                } else {
                    onCreateErrDialog(0x01, String.format(SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(MainDisplayActivity.this, msg.arg2)));
                }
            }
            else if (mData.get(msg.arg1).get("title")
                    .equals(getResources().getString(R.string.StringWarnHighTemp)))
            {
                HashMap<String, Object> map = (HashMap<String, Object>) mData.get(iUserSelect);
                if (msg.arg2 == 0x00) {
                    map.put("info", TPMSParam.tyreData.getTempHighWarn().Display + " " + TPMSParam.tyreData.getTempHighWarn().unitName);

                    ((SysListviewbase) la).notifyDataSetChanged();// 通知系统，实时更新

                } else {
                    onCreateErrDialog(0x01, String.format(SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(MainDisplayActivity.this, msg.arg2)));
                }
            }
            else if (msg.obj.equals("RFCpset")) {
                if (DialogWait != null)
                    DialogWait.dismiss();
                if (msg.arg2 == 0x00) {

                } else {
                    onCreateDialog(0x01, String.format("%s",
                            SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(MainDisplayActivity.this, msg.arg2)));
                }

            } else {
            }
        }

    };


    public Dialog onCreateErrDialog(int dialogId, String msg) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = null;
        SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(this);
        Dialog dialog = null;
        view = inflater.inflate(R.layout.set_simple_dialog, null);
        customBuilder.setMessage(getResources().getString(R.string.SetFailure) + "\n" + msg)
                .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        dialog = customBuilder.create(view, true);
        dialog.show();
        return dialog;
    }


    void SetRFCpValue(final byte value) {
        DialogWait = onCreateDialog(0x04, "");
        DialogWait.setCancelable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int ret = 0x8b;
                Message msg = Message.obtain();
                ret = TPMSParam.mBluethoothData.ProtocolRfCpSet(value);
                msg.arg2 = ret;
                msg.obj = "RFCpset";
                if (mHandler != null)
                    mHandler.sendMessage(msg);
            }
        }).start();
    }


    //
}
