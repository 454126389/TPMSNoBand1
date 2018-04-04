package com.hongking.hktpms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hongking.hktpms.Utils.SharePreferenceUtil;
import com.hongking.oemtpms.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * 指导页
 */
public class GuidanceActivity extends Activity {
    private SharePreferenceUtil sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guidance_activity);
//        setSystemBarTint(false);
        Resources resources = getResources();//获得res资源对象
        Configuration config = resources.getConfiguration();//获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();
        config.locale = Locale.CHINESE;
        SysApplication.movetasktobackflag=false;
        resources.updateConfiguration(config, dm);

        sp = new SharePreferenceUtil(this);

//       sp.setRebootflag("isflag");





       if(sp.getRebootflag().contains("isflag"))
            {
                 moveTaskToBack(true);
            }

        Log.e("dd","SysApplication.USBState="+SysApplication.USBState +"-"+SysApplication.BluetoothState);
        sp.setFirsttimeoncreate("no");
        if (SysApplication.USBState || SysApplication.BluetoothState) {
            handler.sendEmptyMessage(1);
        } else {
            handler.sendEmptyMessageDelayed(1, 2000);
        }
    }

    private View v;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if(BluetoothAdapter.getDefaultAdapter().isEnabled()){
                    Intent intent = new Intent(GuidanceActivity.this, ConnectChooseActivity.class);
                    intent.putExtra("forguidAct", "yes");
                    startActivity(intent);
                    finish();
                    }else
                    {
                        Toast.makeText(GuidanceActivity.this,"蓝牙不可用",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
