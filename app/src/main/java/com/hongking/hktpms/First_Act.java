package com.hongking.hktpms;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.hongking.hktpms.Utils.SharePreferenceUtil;
import com.hongking.oemtpms.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/12/27.
 */
public class First_Act extends Activity {
    private long time;
    private long waittime = 30000;
    private Timer timer;
    private SharePreferenceUtil sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText(".");
        setContentView(tv);
        time = SystemClock.uptimeMillis();
        sp = new SharePreferenceUtil(this);
        SysApplication.movetasktobackflag=true;
//        sp.setFirsttimeoncreate("yes");//USBin
        Toast.makeText(this, "正在启动TPMS，请稍候"/* + sp.getshutdowntime()*/, Toast.LENGTH_SHORT).show();
        if (time >= waittime && !sp.getshutdowntime().contains("USBin")) {
            sp.setshutdowntime("USBin");
            //如果有USBOUT，就是插拔，直接启动，不需要发送广播。

            Intent intent = new Intent(First_Act.this, ConnectChooseActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            First_Act.this.startActivity(intent);
            new CountDownTimer(3000, 3000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    finish();
                }
            }.start();
        } else {
            sp.setshutdowntime("USBin");
            /*Toast.makeText(this,"发送广播",Toast.LENGTH_SHORT).show();*/
            Intent intent = new Intent("hongkingtest");
            sendBroadcast(intent);
            finish();
           /* new CountDownTimer(10000, 10000) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public voooid onFinish() {
                    Toast.makeText(First_Act.this, "跳转到ConnectChooseActivity", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(First_Act.this, ConnectChooseActivity.class);
                    First_Act.this.startActivity(intent);
                     new CountDownTimer(3000, 3000) {

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        finish();
                    }
                }.start();
                }
            }.start();*/
        }
    }
}
