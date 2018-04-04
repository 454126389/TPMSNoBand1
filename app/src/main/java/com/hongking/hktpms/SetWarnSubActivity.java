package com.hongking.hktpms;
/*
 * 包说明
 * 报警值设置Activity
 * */

import com.hongking.oemtpms.R;
import com.hongking.hktpms.SysDatapickerDialog;
import com.hongking.hktpms.SysParam;
import com.hongking.hktpms.views.ValueSetPop;
import com.hongking.hktpms.views.ValueSetPop.OnValueSetListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class SetWarnSubActivity extends SysActivity implements OnClickListener {
    private int iUserSelect;
    private byte menuBase = 0;//用来记录第一个有效菜单的起始值
    //private TextView myTextView1 = null;
    private List<Map<String, Object>> mData;
    private ListView listView;
    private SysDatapickerDialog.Builder customBuilder;
    private Dialog DialogArray[] = new Dialog[6];
    private SysParam TPMSParam = ((SysParam) SysParam.getInstance(this));
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SysParam.getInstance(this).addActivity(this);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        float density = metric.density;
        if(density==1.5){
            setContentView(R.layout.act_setting_secondden15);
        }else {
            setContentView(R.layout.act_setting_second);
        }
        new SysParam.Builder(this).setTitle(getResources().getString(R.string.AlarmValueSetup)).setBackButton(getResources().getString(R.string.StringSetting), new View.OnClickListener() {
            public void onClick(View v) {
                KeyEvent newEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
                SetWarnSubActivity.this.onKeyDown(KeyEvent.KEYCODE_BACK, newEvent);
            }
        }).create();
        settings = getSharedPreferences("myConfig", Activity.MODE_PRIVATE);
        //myTextView1 = (TextView)findViewById(R.id.DebugViewLine1);
        View linearLayout = findViewById(R.id.content);
//		Drawable dr = this.getResources().getDrawable(TPMSParam.iThemeTable[TPMSParam.iThemeIndex]); 
//		linearLayout.setBackgroundDrawable(dr);
        //myTextView1.setText("报警设置栏\n");

        listView = (ListView) findViewById(R.id.ListView01);
        mData = getData();
        SysListviewbase myAdapter = new SysListviewbase(this, mData,density);
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(new MyListViewListenerCommon());
        this.AutoDestroryListen();
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();

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
        //顶出空行
        map = new HashMap<String, Object>();
        map.put("title", " ");
        map.put("flag", "tag");
        list.add(map);
        return list;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
//		View linearLayout = findViewById(R.id.content);
//		Drawable dr = this.getResources().getDrawable(TPMSParam.iThemeTable[TPMSParam.iThemeIndex]); 
//		linearLayout.setBackgroundDrawable(dr);
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        TPMSParam.activityStart();
        super.onStart();
    }

    public void onStop() {
        Editor mEditor;
        TPMSParam.activityStop();
        mEditor = settings.edit();
        mEditor.putInt("WarnCapacityValue", TPMSParam.tyreData.getPressCapacityWarn().value);
        mEditor.putInt("WarnPressLowValue", TPMSParam.tyreData.getPressLowWarn().value);
        mEditor.putInt("WarnPressHighValue", TPMSParam.tyreData.getPressHighWarn().value);
        mEditor.putInt("WarnTempHighValue", TPMSParam.tyreData.getTempHighWarn().value);
        mEditor.commit();
        super.onStop();
    }

    public void onDestroy() {
        //Log.v("LOG ","OnDestory is starting");
        mHandler = null;
        if (DialogArray[iUserSelect] != null)
            DialogArray[iUserSelect].dismiss();
        super.onDestroy();
    }

    // 当客户点击Menua按钮的时候，调用该方法
    class MyListViewListenerCommon implements OnItemClickListener {
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            iUserSelect = arg2;
            ValueSetPop pop = new ValueSetPop(SetWarnSubActivity.this);
            pop.setTitle((String) mData.get(iUserSelect).get("title"));
            pop.setOnValueSetListenerr(new OnValueSetListener() {
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
            if (arg2 == menuBase) {
                pop.initData(TPMSParam.tyreData.getPressCapacityWarn());
                pop.showAtLocation(getRootView(SetWarnSubActivity.this), Gravity.CENTER, 300, 0);
            } else if (arg2 == (menuBase + 1)) {
                pop.initData(TPMSParam.tyreData.getPressHighWarn());
                pop.showAtLocation(getRootView(SetWarnSubActivity.this), Gravity.CENTER, 300, 0);
            } else if (arg2 == (menuBase + 2)) {
                pop.initData(TPMSParam.tyreData.getPressLowWarn());
                pop.showAtLocation(getRootView(SetWarnSubActivity.this), Gravity.CENTER, 300, 0);
            } else if (arg2 == (menuBase + 3)) {
                pop.initData(TPMSParam.tyreData.getTempHighWarn());
                pop.showAtLocation(getRootView(SetWarnSubActivity.this), Gravity.CENTER, 300, 0);
            } else {
                DialogArray[arg2] = onCreateDialog(arg2);
            }
        }
    }

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

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            ListAdapter la = listView.getAdapter();
            HashMap<String, Object> map = (HashMap<String, Object>) mData.get(iUserSelect);

            if (iUserSelect == menuBase) {
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

                } else {
                    onCreateErrDialog(0x01, String.format(SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(SetWarnSubActivity.this, msg.arg2)));
                }
            } else if (iUserSelect == (menuBase + 1)) {
                if (msg.arg2 == 0x00) {
                    String test2 = "";
                    if (TPMSParam.tyreData.getPressHighWarn().Display.length() == 3) {
                        test2 = "0";
                    }
                    map.put("info", TPMSParam.tyreData.getPressHighWarn().Display +test2+ " " + TPMSParam.tyreData.getPressHighWarn().unitName);
                } else {
                    onCreateErrDialog(0x01, String.format(SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(SetWarnSubActivity.this, msg.arg2)));
                }
            } else if (iUserSelect == (menuBase + 2)) {
                if (msg.arg2 == 0x00) {
                    String test3 = "";
                    if (TPMSParam.tyreData.getPressLowWarn().Display.length() == 3) {
                        test3 = "0";
                    }
                    map.put("info", TPMSParam.tyreData.getPressLowWarn().Display+test3 + " " + TPMSParam.tyreData.getPressLowWarn().unitName);
                } else {
                    onCreateErrDialog(0x01, String.format(SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(SetWarnSubActivity.this, msg.arg2)));
                }
            } else if (iUserSelect == (menuBase + 3)) {
                if (msg.arg2 == 0x00) {
                    map.put("info", TPMSParam.tyreData.getTempHighWarn().Display + " " + TPMSParam.tyreData.getTempHighWarn().unitName);
                } else {
                    onCreateErrDialog(0x01, String.format(SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(SetWarnSubActivity.this, msg.arg2)));
                }
            } else {
            }
            ((SysListviewbase) la).notifyDataSetChanged();//通知系统，实时更新
        }
    };

    void SetParamCommand(int which, int value) {
        int ret = 0x8b;
        Message msg = new Message();
        if (iUserSelect == menuBase) {
            ret = TPMSParam.mBluethoothData.ProtocolSysParamSet(TPMSParam.mBluethoothData.SET_CAPACITY_WARN, value);
        } else if (iUserSelect == (menuBase + 1)) {
            ret = TPMSParam.mBluethoothData.ProtocolSysParamSet(TPMSParam.mBluethoothData.SET_PRESSHIGH_WARN, value);
        } else if (iUserSelect == (menuBase + 2)) {
            ret = TPMSParam.mBluethoothData.ProtocolSysParamSet(TPMSParam.mBluethoothData.SET_PRESSLOW_WARN, value);
        } else if (iUserSelect == (menuBase + 3)) {
            ret = TPMSParam.mBluethoothData.ProtocolSysParamSet(TPMSParam.mBluethoothData.SET_TEMPHIGH_WARN, value);
        } else {
        }
        TPMSParam.mBluethoothData.ProtocolSysParamGet();//获取参数
        msg.arg1 = which;
        msg.arg2 = ret;
        if (mHandler != null)
            mHandler.sendMessage(msg);
    }

    public Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = null;
        boolean dismiss = true;
        customBuilder = new SysDatapickerDialog.Builder(this);
        view = inflater.inflate(R.layout.set_datapicker_dialog, null);
        if (dialogId == menuBase) {
            customBuilder.setMessage((String) mData.get(dialogId).get("title"))
                    .setNegativeButton(getResources().getString(R.string.Cancel), this)
                    .setPositiveButton(getResources().getString(R.string.confirm), this);
            dialog = customBuilder.create(view, TPMSParam.tyreData.getPressCapacityWarn(), dismiss);
        } else if (dialogId == (menuBase + 1)) {
            customBuilder.setMessage((String) mData.get(dialogId).get("title"))
                    .setNegativeButton(getResources().getString(R.string.Cancel), this)
                    .setPositiveButton(getResources().getString(R.string.confirm), this);
            dialog = customBuilder.create(view, TPMSParam.tyreData.getPressHighWarn(), dismiss);
        } else if (dialogId == (menuBase + 2)) {
            customBuilder.setMessage((String) mData.get(dialogId).get("title"))
                    .setNegativeButton(getResources().getString(R.string.Cancel), this)
                    .setPositiveButton(getResources().getString(R.string.confirm), this);
            dialog = customBuilder.create(view, TPMSParam.tyreData.getPressLowWarn(), dismiss);
        } else if (dialogId == (menuBase + 3)) {
            customBuilder.setMessage((String) mData.get(dialogId).get("title"))
                    .setNegativeButton(getResources().getString(R.string.Cancel), this)
                    .setPositiveButton(getResources().getString(R.string.confirm), this);
            dialog = customBuilder.create(view, TPMSParam.tyreData.getTempHighWarn(), dismiss);
        } else {
        }
        dialog.show();
        return dialog;
    }

    public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub
        if (which == -1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SetParamCommand(iUserSelect, customBuilder.TempValue);
                }
            }).start();
        } else {
            Log.v("keydebug", "which=" + which);
        }
    }

    public View getRootView(Activity myContext) {
        return ((ViewGroup) myContext.findViewById(android.R.id.content)).getChildAt(0);
    }
}