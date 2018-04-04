package com.hongking.hktpms;
/*
 * 包说明
 * 软件运行页面Activity
 * */

import java.util.List;


import com.hongking.hktpms.EventBus.Bluetoothlost;
import com.hongking.hktpms.Utils.SharePreferenceUtil;
import com.hongking.hktpms.views.EventBus_Media;
import com.hongking.hktpms.views.EventBus_bt_connect;
import com.hongking.oemtpms.R;
import com.hongking.hktpms.SysParam.SYS_MODE;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.app.Activity;
import android.app.ActivityGroup;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;


@SuppressWarnings("deprecation")
public class AppMainActivity extends ActivityGroup implements View.OnClickListener {
    private RadioGroup radioderGroup;
    private SysParam TPMSParam;
    private TabHost tabHost;
    //页卡内容   
    private ViewPager mPager;
    private NotificationReceiver mBtMsgReceiver;
    final String TAG = "TPMSAppMain";
    private SysApplication app;
    private boolean movebackflag = true;
    private LinearLayout menuView;
    private ImageView newmain_menu;
    private boolean isforground;
    private SharePreferenceUtil sp;
    private Thread thread;
    private CountDownTimer movebacktimer;
    private boolean heartflag = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.activityList.add(this);
        EventBus.getDefault().register(this);
        app = (SysApplication) getApplicationContext();
        TPMSParam = app.TPMSParam;
        sp = new SharePreferenceUtil(this);
        Log.e("20161215", "onCreateAPPmainact" + TPMSParam);
        //TODO
        if (TPMSParam.blueStatus.isConnecting() == false) {
            //Log.w(TAG,"bluethooth is no connecting");
            this.finish();
            return;
        }
        /*if (getIntent().getStringExtra("moveTaskToBack") != null) {
            moveTaskToBack(true);
        }*/
        if (SysApplication.movetasktobackflag) {
            SysApplication.movetasktobackflag = false;
            showCountDowntimer();
        }

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        float density = metric.density;
        SysParam.getInstance(this).addActivity(this);
        if (density == 1.5) {
            setContentView(R.layout.main_srcollden15);
        } else {
            setContentView(R.layout.main_srcoll);
        }
        menuView = (LinearLayout) findViewById(R.id.main_menu);
        newmain_menu = (ImageView) findViewById(R.id.newmain_menu);

        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(getLocalActivityManager());
        tabHost.addTab(tabHost.newTabSpec("1")
                .setIndicator(getResources().getString(R.string.StringMainView))
                .setContent(new Intent(AppMainActivity.this, MainDisplayActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("2")
                .setIndicator(getResources().getString(R.string.StringSetting))
                .setContent(new Intent(AppMainActivity.this, SetActivity.class)));
        tabHost.setBackgroundResource(R.drawable.tabhost_temp2);
//        tabHost.getTabWidget().getChildTabViewAt(1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e("qqq","qqqqq2");
//            }
//        });
        radioderGroup = (RadioGroup) findViewById(R.id.tab_radiogroup);
        radioderGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                updateTabBackground(checkedId);
            }
        });
        findViewById(R.id.mainview).setOnClickListener(this);
        findViewById(R.id.setview).setOnClickListener(this);
        findViewById(R.id.aboutmeview).setOnClickListener(this);
        updateTabBackground(R.id.mainview);

        IntentFilter stateDisconnectFilter = new IntentFilter(SysControllerService.CONNECT_LOST_MESSAGE_ACTION);
        IntentFilter stateDisconnectFilter1 = new IntentFilter(SysControllerService.AUTO_SW_MAINPAGE);
        SysApplication.heartflag = true;
        mBtMsgReceiver = new NotificationReceiver();
        registerReceiver(mBtMsgReceiver, stateDisconnectFilter);
        registerReceiver(mBtMsgReceiver, stateDisconnectFilter1);


        /*thread = new Thread(new Runnable() {
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
        thread.start();*/
       /* final IntentFilter filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        // 屏幕亮屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        // 屏幕解锁广播
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                Log.d(TAG, "onReceive");
                String action = intent.getAction();

                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    sp.setFirsttimeoncreate("test");
                    Log.d(TAG, "screen on");
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    sp.setFirsttimeoncreate("test");
                    Log.d(TAG, "screen off");
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    sp.setFirsttimeoncreate("test");
                    Log.d(TAG, "screen unlock");
                } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    sp.setFirsttimeoncreate("test");
                    Log.i(TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");
                }
            }
        };
        Log.d(TAG, "registerReceiver");
        registerReceiver(mBatInfoReceiver, filter);*/
    }

    private void showCountDowntimer() {
        final AlertDialog alertDialog = new AlertDialog.Builder(AppMainActivity.this).create();
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setContentView(R.layout.dialog_main_moveback);
        final TextView tv_title = (TextView) window.findViewById(R.id.message);
        tv_title.setText("5秒后回退到后台");
        Button cancel = (Button) window.findViewById(R.id.negativeButton);
        Button confirm = (Button) window.findViewById(R.id.positiveButton);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movebackflag = false;
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
        movebacktimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_title.setText((int) (millisUntilFinished / 1000) + "秒后回退到后台");
            }

            @Override
            public void onFinish() {
                alertDialog.dismiss();
                if (movebackflag) {
                    moveTaskToBack(true);
                }
            }
        }.start();

    }

    @Override
    public void onStart() {
        TPMSParam.activityStart();
        super.onStart();
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

    @Override
    protected void onResume() {
        super.onResume();
        app.changeMode(SysParam.SYS_MODE.MODE_NORM);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        //Log.i(TAG,"onStop");
        TPMSParam.activityStop();
        super.onStop();
    }

    /**
     * 更新Tab标签的背景图     * @param tabHost
     */
    private void updateTabBackground(int checkedId) {
        RadioButton home_radio;
        for (int i = 0; i < radioderGroup.getChildCount(); i++) {
            radioderGroup.getChildAt(i).setSelected(false);
        }
        switch (checkedId) {
            case R.id.mainview:
                tabHost.setCurrentTabByTag("1");
                //Log.i("Sysparam","sw to main----------\n");
                app.changeMode(SysParam.SYS_MODE.MODE_SETUP);
//                app.changeMode(SysParam.SYS_MODE.MODE_NORM);
                break;
            case R.id.setview:
                tabHost.setCurrentTabByTag("2");
                app.changeMode(SysParam.SYS_MODE.MODE_SETUP);
                break;
            case R.id.aboutmeview:
                app.changeMode(SysParam.SYS_MODE.MODE_SETUP);
                tabHost.setCurrentTabByTag("3");
                break;
            default:
                break;
        }
        home_radio = (RadioButton) findViewById(checkedId);
        home_radio.setSelected(true);
    }

    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }

    }

    @Override
    public void onDestroy() {
        Log.e("20161215", "[onDestroy]AppMainActivity");
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mBtMsgReceiver != null)
            unregisterReceiver(mBtMsgReceiver);
        if (movebacktimer != null) {
            movebacktimer.cancel();
        }
        BaseActivity.activityList.remove(this);
    }

    public class MyPagerAdapter extends PagerAdapter
    {
        public List<View> mListViews;

        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {

        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void startUpdate(ViewGroup arg0) {
            // TODO Auto-generated method stub
            super.startUpdate(arg0);
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO Auto-generated method stub
            return POSITION_NONE;
        }
    }

    public class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLowMemory() {
        Log.e(TAG, "System is onLowMeory");
        TPMSParam.saveParam(this);
        super.onLowMemory();
    }

    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(SysControllerService.CONNECT_LOST_MESSAGE_ACTION)) {
                SysApplication.BluetoothState = false;
                Log.e("20161215", "NotificationReceiverAppmainAct这里SysApplication.BluetoothState=" + SysApplication.BluetoothState);
                if (isBackground(AppMainActivity.this)) {

//                    Toast.makeText(AppMainActivity.this, "在后台", Toast.LENGTH_SHORT).show();
                    Intent mainActivityIntent = new Intent(context, ConnectTestActivity2.class);  // 要启动的Activity
                    mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainActivityIntent);
                } else {
                    Toast.makeText(AppMainActivity.this, getResources().getString(R.string.BluetoothConnectionBreak), Toast.LENGTH_SHORT).show();
//                    Toast.makeText(AppMainActivity.this, "在前台", Toast.LENGTH_SHORT).show();
                    Intent mainActivityIntent = new Intent(context, ConnectChooseActivity.class);  // 要启动的Activity
                    mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainActivityIntent);
                }
                String displayMsg = intent.getExtras().getString("STR");
                Log.i(TAG, displayMsg + "has lost");
                Intent i = new Intent(SysActivity.AUTODESTORY);
                sendBroadcast(i);
                AppMainActivity.this.setResult(Activity.RESULT_OK);
                AppMainActivity.this.finish();
                //20161207添加测试，蓝牙断开之后，进行重连

            } else if (action.equals(SysControllerService.AUTO_SW_MAINPAGE)) {
                radioderGroup.check(R.id.mainview);
            } else {
                Log.e(TAG, "another action: " + action);
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        TPMSParam.saveParam(this);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        Log.i(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(outState);
    }

    public void show(View v) {
        if (menuView.isShown()) {
            menuView.setVisibility(View.GONE);
            newmain_menu.setVisibility(View.VISIBLE);
        } else {
            menuView.setVisibility(View.VISIBLE);
            newmain_menu.setVisibility(View.GONE);
        }
    }

    public void Home(View v) {
        //TODO
    }

    public void setting(View v) {
        app.changeMode(SysParam.SYS_MODE.MODE_SETUP);
        menuView.setVisibility(View.GONE);
        newmain_menu.setVisibility(View.VISIBLE);
        Intent intent = new Intent(AppMainActivity.this, SetActivity.class);
        startActivity(intent);
    }

    public void about(View v) {
        app.changeMode(SysParam.SYS_MODE.MODE_SETUP);
        menuView.setVisibility(View.GONE);
        newmain_menu.setVisibility(View.VISIBLE);
        Intent intent = new Intent(AppMainActivity.this, AboutMeAfterSaleSupportActivity.class);
        startActivity(intent);
    }

    public void onEvent(com.hongking.hktpms.views.EventBus eventBus) {

        if (eventBus.getI() == 0) {
            sp.setFirsttimeoncreate("no");
            if (newmain_menu.isShown()) {
                newmain_menu.setVisibility(View.INVISIBLE);
            } else {
                newmain_menu.setVisibility(View.VISIBLE);
                new CountDownTimer(5000, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        newmain_menu.setVisibility(View.INVISIBLE);
                    }
                }.start();
            }
        } else {
            newmain_menu.setVisibility(View.INVISIBLE);
        }
    }

    public boolean timeflag = true;
    public int counttime = 0;

    public void onEvent(EventBus_Media EventBus_Media) {
        if (EventBus_Media.getI() == 0) {
            //从application发出，语音播报完毕
//            Toast.makeText(this,"语音播报完毕"+String.valueOf(isforground),Toast.LENGTH_SHORT).show();
            /*if(isforground){//在后台
                moveTaskToBack(true);
            }*/
//            Toast.makeText(this, "SysApplication.timeflag1"+String.valueOf(SysApplication.timeflag1)+"SysApplication.timeflag2"+String.valueOf(SysApplication.timeflag2),Toast.LENGTH_SHORT).show();
            Log.i("20161214", "SysApplication.timeflag1" + String.valueOf(SysApplication.timeflag1) + "SysApplication.timeflag2" + String.valueOf(SysApplication.timeflag2));
            if (SysApplication.timeflag1 == 1) {
                SysApplication.timeflag1 = 0;
                SysApplication.timeflag2 = 0;
                //返回后台
                moveTaskToBack(true);
            } else {
                SysApplication.timeflag1 = 0;
                SysApplication.timeflag2 = 0;
            }
        } else {
            //从syscontrollerservice发出。看看是否在前台。
            isforground = isBackground(this);
            Log.i("20161214", "判断前后台");
        }

    }

    /**
     * 判断当前应用程序处于前台还是后台
     */
    public static boolean isApplicationBroughtToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    Log.i("20161214后台", appProcess.processName + "SysApplication.timeflag=false");
                    SysApplication.timeflag = false;
                    SysApplication.timeflag1 = 1;
//                    Toast.makeText(this,"20161214后台",Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    Log.i("20161214前台", appProcess.processName + "SysApplication.timeflag=true");
                    SysApplication.timeflag = true;
                    if (SysApplication.timeflag2 == 1) {
                        SysApplication.timeflag1 = 1;//个别机型从后台进入，要判断两次。而且两次都是在前台。
                    }
                    SysApplication.timeflag2 = 1;
//                    Toast.makeText(this,"20161214前台",Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return false;
    }

    public void onEvent(Bluetoothlost bluetoothlost) {

        SysApplication.BluetoothState = false;
        Log.e("20161215", "onEventAppmainAct这里SysApplication.BluetoothState=" + SysApplication.BluetoothState);
        if (isBackground(AppMainActivity.this)) {
            Toast.makeText(AppMainActivity.this, "在后台", Toast.LENGTH_SHORT).show();
            Intent mainActivityIntent = new Intent(this, ConnectTestActivity2.class);  // 要启动的Activity
            startActivity(mainActivityIntent);
        } else {
            Toast.makeText(AppMainActivity.this, getResources().getString(R.string.BluetoothConnectionBreak), Toast.LENGTH_SHORT).show();
                    Toast.makeText(AppMainActivity.this, "在前台", Toast.LENGTH_SHORT).show();
            if(BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Intent mainActivityIntent = new Intent(this, ConnectChooseActivity.class);  // 要启动的Activity
            startActivity(mainActivityIntent);
            }else
            {
                Toast.makeText(AppMainActivity.this,"蓝牙不可用,code=0x02",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        Intent i = new Intent(SysActivity.AUTODESTORY);
        sendBroadcast(i);
        AppMainActivity.this.setResult(Activity.RESULT_OK);
        AppMainActivity.this.finish();
        //20161207添加测试，蓝牙断开之后，进行重连

    }
}
