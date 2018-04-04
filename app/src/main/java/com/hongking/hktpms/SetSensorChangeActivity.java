package com.hongking.hktpms;
/*
 * 包说明
 * 传感器更换Activity
 * */

import com.hongking.oemtpms.R;
import com.hongking.hktpms.views.TipPop2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class SetSensorChangeActivity extends SysActivity implements DialogInterface.OnClickListener,
        OnTouchListener, OnClickListener {
    private TextView infoView;
    private TextView infoView1;
    private final int CUSTOM_DIALOG_ITEM0 = 0;
    private final int CUSTOM_DIALOG_ITEM1 = 1;
    private final int CUSTOM_DIALOG_ITEM2 = 2;
    private final int CUSTOM_DIALOG_SEARCH = 3;
    private final int CUSTOM_DIALOG_WAIT = 4;
    private final int CUSTOM_DIALOG_CONFIRM = 5;
    //private final int CUSTOM_DIALOG_PROMPT =5;

    private TextView myViewTemp[] = new TextView[4];
    private TextView myViewPress[] = new TextView[4];
    private TextView myViewTempUnit[] = new TextView[4];
    private TextView myViewPressUnit[] = new TextView[4];
    private ImageView myImageMark[] = new ImageView[4];

    private EditText input = null;
    private ImageView bitmapView = null;

    private View content[] = new View[4];

    private ImageView contentSet[] = new ImageView[4];

    SysParam.DataInfo pressValue[] = new SysParam.DataInfo[4];
    SysParam.DataInfo tempValue[] = new SysParam.DataInfo[4];

    private NotificationReceiver mBtMsgReceiver;

    private int selectWhich;
    private SysParam TPMSParam;
    //private SysApplication app ;
    private Dialog DialogWait;
    private Dialog DialogStep1;
    private Dialog DialogStep2;
    private Dialog DialogSearch;
    private Dialog DialogPrompt;

    private Dialog curDialog;

    private ImageView WarnCenterImage;
    static final String TAG = "SetSensorChangeActivity";

    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;

    void SelectLayout(Activity activity) {
        Display display = getWindowManager().getDefaultDisplay();
        int width, height;
        width = display.getWidth();
        height = display.getHeight();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        float density = metric.density;

        if(width>1000&&density==1.5) {//large
            setContentView(R.layout.changechuanganqi_largeden15);
        }else if(width>1000) {//large
            setContentView(R.layout.changechuanganqi_large);
        }else{
            setContentView(R.layout.changechuanganqi_small);
        }

        View linearLayout = findViewById(R.id.contentall);
//		Drawable dr = this.getResources().getDrawable(TPMSParam.iThemeTable[TPMSParam.iThemeIndex]); 
//		linearLayout.setBackgroundDrawable(dr);
        linearLayout = findViewById(R.id.CardView);
//		dr = this.getResources().getDrawable(TPMSParam.iCardTable[TPMSParam.iCardIndex]); 
//		linearLayout.setBackgroundDrawable(dr);
    }


    @SuppressLint("DefaultLocale")
    void tyreFlash(int which) {

        pressValue[which] = TPMSParam.tyreData.getPressValue(which);
        tempValue[which] = TPMSParam.tyreData.getTempValue(which);
        WarnCenterImage.setVisibility(0x04);
        myImageMark[0].setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        myImageMark[1].setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        myImageMark[2].setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        myImageMark[3].setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        myImageMark[which].setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_tyre2));
//        findViewById(R.id.ImageLBMarkView).setBackgroundDrawable(null);
//        content[which].setVisibility(0x00);
        if (pressValue[which].HighAlertStatus || pressValue[which].LowAlertStatus ||
                pressValue[which].LeakageAlertStatus) {
            myViewPress[which].setTextColor(getResources().getColor(R.color.warnDisplay));

        } else {
            myViewPress[which].setTextColor(getResources().getColor(R.color.normalTempDisplay));
        }

        myViewPress[which].setText(pressValue[which].Display);
        myViewTemp[which].setText(String.format("ID:%08x", pressValue[which].id).toUpperCase());
        myViewPressUnit[which].setText(pressValue[which].unitName);
        myViewTempUnit[which].setText(tempValue[which].unitName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TPMSParam = ((SysParam) SysParam.getInstance(this));
        //app = (SysApplication)getApplicationContext();
        SysParam.getInstance(this).addActivity(this);
        SelectLayout(this);
        new SysParam.Builder(this).setTitle(getResources().getString(R.string.ReplaceTheTireSensor)).setBackButton(getResources().getString(R.string.StringSetting), new View.OnClickListener() {
            public void onClick(View v) {
                KeyEvent newEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
                SetSensorChangeActivity.this.onKeyDown(KeyEvent.KEYCODE_BACK, newEvent);
            }
        }).create();

        myImageMark[0] = (ImageView) findViewById(R.id.ImageLFTyreView);
        myImageMark[1] = (ImageView) findViewById(R.id.ImageRFTyreView);
        myImageMark[2] = (ImageView) findViewById(R.id.ImageLBTyreView);
        myImageMark[3] = (ImageView) findViewById(R.id.ImageRBTyreView);
        /*-----------------------------------------------------------------*/
        myViewTemp[0] = (TextView) findViewById(R.id.LFTempText);
        myViewPress[0] = (TextView) findViewById(R.id.LFPressText);
        pressValue[0] = TPMSParam.tyreData.getPressValue(0);
        myViewTemp[0].setText(String.format("ID:%08x", pressValue[0].id).toUpperCase());

        myViewTemp[1] = (TextView) findViewById(R.id.RFTempText);
        myViewPress[1] = (TextView) findViewById(R.id.RFPressText);
        pressValue[1] = TPMSParam.tyreData.getPressValue(1);
        myViewTemp[1].setText(String.format("ID:%08x", pressValue[1].id).toUpperCase());

        myViewTemp[2] = (TextView) findViewById(R.id.LBTempText);
        myViewPress[2] = (TextView) findViewById(R.id.LBPressText);
        pressValue[2] = TPMSParam.tyreData.getPressValue(2);
        myViewTemp[2].setText(String.format("ID:%08x", pressValue[2].id).toUpperCase());

        myViewTemp[3] = (TextView) findViewById(R.id.RBTempText);
        myViewPress[3] = (TextView) findViewById(R.id.RBPressText);
        pressValue[3] = TPMSParam.tyreData.getPressValue(3);
        myViewTemp[3].setText(String.format("ID:%08x", pressValue[3].id).toUpperCase());

		/*-----------------------------------------------------------------*/
        myViewTempUnit[0] = (TextView) findViewById(R.id.LFTempTitle);
        myViewPressUnit[0] = (TextView) findViewById(R.id.LFPressTitle);

        myViewTempUnit[1] = (TextView) findViewById(R.id.RFTempTitle);
        myViewPressUnit[1] = (TextView) findViewById(R.id.RFPressTitle);

        myViewTempUnit[2] = (TextView) findViewById(R.id.LBTempTitle);
        myViewPressUnit[2] = (TextView) findViewById(R.id.LBPressTitle);

        myViewTempUnit[3] = (TextView) findViewById(R.id.RBTempTitle);
        myViewPressUnit[3] = (TextView) findViewById(R.id.RBPressTitle);

        content[0] = findViewById(R.id.contentLFValue);
        content[1] = findViewById(R.id.contentRFValue);
        content[2] = findViewById(R.id.contentLBValue);
        content[3] = findViewById(R.id.contentRBValue);


        contentSet[0] = (ImageView) findViewById(R.id.ImageLFTyreView);
        contentSet[1] = (ImageView) findViewById(R.id.ImageRFTyreView);

        contentSet[2] = (ImageView) findViewById(R.id.ImageLBTyreView);
        contentSet[3] = (ImageView) findViewById(R.id.ImageRBTyreView);

        contentSet[0].setVisibility(0x00);
        contentSet[1].setVisibility(0x00);
        contentSet[2].setVisibility(0x00);
        contentSet[3].setVisibility(0x00);

        contentSet[0].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        contentSet[1].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        contentSet[2].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        contentSet[3].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));

        contentSet[0].setOnClickListener(this);
        contentSet[1].setOnClickListener(this);
        contentSet[2].setOnClickListener(this);
        contentSet[3].setOnClickListener(this);
        WarnCenterImage = (ImageView) findViewById(R.id.WarnIconImage);

        contentSet[0].setOnTouchListener(this);
        contentSet[1].setOnTouchListener(this);
        contentSet[2].setOnTouchListener(this);
        contentSet[3].setOnTouchListener(this);
        IntentFilter stateDisconnectFilter = new IntentFilter(SysControllerService.NOTIFY_TIRE_RESULT);
        mBtMsgReceiver = new NotificationReceiver();
        registerReceiver(mBtMsgReceiver, stateDisconnectFilter);
        this.AutoDestroryListen();
        onCreateDialog(CUSTOM_DIALOG_ITEM0);
        newtyreFlash();
    }

    private void newtyreFlash() {
        for(int which=0;which<=3;which++) {
            pressValue[which] = TPMSParam.tyreData.getPressValue(which);
            tempValue[which] = TPMSParam.tyreData.getTempValue(which);
            if (pressValue[which].HighAlertStatus || pressValue[which].LowAlertStatus ||
                    pressValue[which].LeakageAlertStatus) {
                myViewPress[which].setTextColor(getResources().getColor(R.color.warnDisplay));

            } else {
                myViewPress[which].setTextColor(getResources().getColor(R.color.normalTempDisplay));
            }

            myViewPress[which].setText(pressValue[which].Display);
            myViewTemp[which].setText(String.format("ID:%08x", pressValue[which].id).toUpperCase());
            myViewPressUnit[which].setText(pressValue[which].unitName);
            myViewTempUnit[which].setText(tempValue[which].unitName);
        }
    }

    @Override
    public void onStart() {
        TPMSParam.activityStart();
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        ;
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "TPMS");
        wakeLock.acquire();
        super.onStart();
    }

    @Override
    public void onStop() {
        cancelSearch(DialogWait);
        TPMSParam.activityStop();
        if (null != wakeLock) {
            Log.i(TAG, "release lock");
            wakeLock.release();
            wakeLock = null;
        }

        super.onStop();
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
    public void onClick(View v) {
        // TODO Auto-generated method stub
        contentSet[0].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        contentSet[1].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        contentSet[2].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        contentSet[3].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        if (v == contentSet[0]) {
            selectWhich = 0;
            contentSet[0].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre2));
        } else if (v == contentSet[1]) {
            selectWhich = 1;
            contentSet[1].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre2));
        } else if (v == contentSet[2]) {
            selectWhich = 2;
            contentSet[2].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre2));
        } else if (v == contentSet[3]) {
            selectWhich = 3;
            contentSet[3].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre2));
        } else return;
        //myImageMark[selectWhich].setBackgroundResource(R.drawable.icon_mark);
//		DialogStep1 = onCreateDialog(CUSTOM_DIALOG_ITEM1);
        Dialog dialog = null;
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = null;
        boolean dismiss = true;
        SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(this);
        if(SysApplication.density==1.5){
            view = inflater.inflate(R.layout.set_custom_dialogden15, null);
        }else {
            view = inflater.inflate(R.layout.set_custom_dialog, null);
        }
                customBuilder.setMessage("进行"+this.getResources().getStringArray(R.array.tyreItem)[selectWhich]+"传感器更换？")
                        .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SetChangeCommandStart(selectWhich);
                                        //在此线程里面进行搜索数据如果搜到数据则通知主线程
                                    }
                                }).start();
                            }
                        }).setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        contentSet[0].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                        contentSet[1].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                        contentSet[2].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                        contentSet[3].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                    }
                });
        dialog = customBuilder.create(view, dismiss);
        dialog.show();
    }

    private void SetChangeCommandStart(int selectWhich) {
        int ret;
        Message msg = Message.obtain();
        ret = 0;
        if (ret == 0) {
            SysParam.Tire_Inf info1 = TPMSParam.tyreData.getInfoValue(selectWhich);
            info1.supdate = -1;//清空数据
            TPMSParam.tyreData.setInfoValue(selectWhich, info1);
            ret = TPMSParam.mBluethoothData.ProtocolTPMSTyreChange(selectWhich, (byte) 0x02, TPMSParam.mBluethoothData.MAX_WAIT_TIME);
        }
        msg.arg1 = 0;
        msg.arg2 = ret;
        msg.obj = "changecommandStart";
        if (mHandler != null)
            mHandler.sendMessage(msg);
    }

    private void SetChangeCommandStop(int selectWhich) {
        int ret;
        Message msg = Message.obtain();
        ret = TPMSParam.mBluethoothData.ProtocolTPMSTyreChange(selectWhich, (byte) 0x00, 0);

        msg.arg1 = 0;
        msg.arg2 = ret;
        msg.obj = "changecommandstop";
        if (mHandler != null)
            mHandler.sendMessage(msg);
    }

    private void SetChangeCommandConfirm(int value) {
        int ret;
        Message msg = Message.obtain();
        TPMSParam.mBluethoothData.ProtocolTPMSTyreChangeConfirm(value);
        ret = TPMSParam.mBluethoothData.ProtocolTireIDGet();
        msg.arg1 = 0;
        msg.arg2 = ret;
        msg.obj = "updateUi";
        if (mHandler != null)
            mHandler.sendMessage(msg);
    }

    public Dialog onCreateErrDialog(int dialogId, String msg) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = null;
        SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(this);
        Dialog dialog = null;

        view = inflater.inflate(R.layout.set_simple_dialog, null);

        customBuilder.setMessage(msg)
                .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SetSensorChangeActivity.this.finish();
                    }
                });

        dialog = customBuilder.create(view, true);
        dialog.show();
        curDialog = dialog;
        return dialog;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.obj.equals("changecommandStart")) {
                if (msg.arg2 == 0x00) {
                    //DialogPrompt = onCreateDialog(CUSTOM_DIALOG_PROMPT);
                    DialogWait = onCreateDialog(CUSTOM_DIALOG_WAIT);

                } else {
                    onCreateErrDialog(0x01, "清除胎压信息失败\n" + SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(SetSensorChangeActivity.this, msg.arg2));
                }
            } else if (msg.obj.equals("changecommandstop")) {
                if (msg.arg2 == 0x00) {

                } else {
                    onCreateErrDialog(0x01, getResources().getString(R.string.StopSearching) + "\n " + SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(SetSensorChangeActivity.this, msg.arg2));
                }
            } else if (msg.obj.equals("updateUi")) {
                tyreFlash(selectWhich);
                TPMSParam.tyreData.setStatus(selectWhich, true);
            } else if (msg.obj.equals("timeOut")) {
                onCreateErrDialog(0x01, (getResources().getString(R.string.WaitTimerOut)));
            }
            if (msg.arg2 == 0x00) {

            }
        }
    };

    public void onClick(DialogInterface dialog, int which) {
        if (dialog == DialogStep1) {//选择轮胎
            if (which == -1) {
                DialogStep2 = onCreateDialog(CUSTOM_DIALOG_ITEM2);
            } else {
                Log.v("keydebug", "which=" + which);
                myImageMark[selectWhich].setBackgroundResource(R.drawable.icon_mark1);
            }
        } else if (dialog == DialogStep2) {//验证图片对话框
            if (which == -1) {
                if (input.getText().toString().equalsIgnoreCase(SetBpUtil.getInstance().getCode())) {
                    dialog.dismiss();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SetChangeCommandStart(selectWhich);
                            //在此线程里面进行搜索数据如果搜到数据则通知主线程
                        }
                    }).start();
                } else {

                    infoView.setText(getResources().getString(R.string.InputErr));
                    bitmapView.setImageBitmap(SetBpUtil.getInstance().createBitmap());
                    input.setText("");
                }
            } else {
                Log.w(TAG, "user cancel");
                dialog.dismiss();
                myImageMark[selectWhich].setBackgroundResource(R.drawable.icon_mark1);
            }
        } else if (dialog == DialogWait) {//等待收索
            if (which == -1) {

            } else {
                Log.w(TAG, "user cancel");
                dialog.dismiss();
                myImageMark[selectWhich].setBackgroundResource(R.drawable.icon_mark1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        SetChangeCommandStop(selectWhich);
                        //在此线程里面进行搜索数据如果搜到数据则通知主线程
                    }
                }).start();
                dialog = null;
            }
        } else if (dialog == DialogSearch) {

            if (which == -1) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SetChangeCommandConfirm(0x00);
                        //在此线程里面进行搜索数据如果搜到数据则通知主线程
                    }
                }).start();
            } else {
                myImageMark[selectWhich].setBackgroundResource(R.drawable.icon_mark1);
            }

        } else if (dialog == DialogPrompt) {
            tyreFlash(selectWhich);
            TPMSParam.tyreData.setStatus(selectWhich, true);
        }

    }

    private synchronized void cancelSearch(Dialog dialog) {
        if (dialog == null || !dialog.isShowing())
            return;
        dialog.dismiss();
        myImageMark[selectWhich].setBackgroundResource(R.drawable.icon_mark1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SetChangeCommandStop(selectWhich);
                //在此线程里面进行搜索数据如果搜到数据则通知主线程
            }
        }).start();
        dialog = null;
    }

    public Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = null;
        boolean dismiss = true;
        SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(this);
        switch (dialogId) {
            case CUSTOM_DIALOG_ITEM0:
                if(SysApplication.density==1.5){
                    view = inflater.inflate(R.layout.set_simple_dialogden15, null);
                }else {
                    view = inflater.inflate(R.layout.set_simple_dialog, null);
                }
                customBuilder.setMessage(this.getResources().getString(R.string.StringChangeAlter1))
                        .setPositiveButton(getResources().getString(R.string.confirm), this);
                break;
            case CUSTOM_DIALOG_CONFIRM:
                if(SysApplication.density==1.5){
                    view = inflater.inflate(R.layout.set_simple_dialogden15, null);
                }else {
                    view = inflater.inflate(R.layout.set_simple_dialog, null);
                }
                customBuilder.setMessage(this.getResources().getString(R.string.StringChangeAlter1))
                        .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SetChangeCommandStart(selectWhich);
                                        //在此线程里面进行搜索数据如果搜到数据则通知主线程
                                    }
                                }).start();
                            }
                        }).setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(SetSensorChangeActivity.this,"已取消，请重新选择轮胎",Toast.LENGTH_SHORT).show();
                        contentSet[0].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                        contentSet[1].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                        contentSet[2].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                        contentSet[3].setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                    }
                });
                break;
            case CUSTOM_DIALOG_ITEM1:
                view = inflater.inflate(R.layout.set_custom_dialog, null);
                customBuilder.setMessage(this.getResources().getString(R.string.StringChangeTyreInfoWarn1))
                        .setNegativeButton(getResources().getString(R.string.Cancel), this)
                        .setPositiveButton(getResources().getString(R.string.confirm), this);
                break;
            case CUSTOM_DIALOG_ITEM2:
                view = inflater.inflate(R.layout.set_verify_dialog, null);
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
                            //更改为按下时的背景图片
                            infoView1.setTextColor(getResources().getColor(R.color.display_touch));

                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            infoView1.setTextColor(getResources().getColor(R.color.display_text));
                        }
                        return false;
                    }
                });
                Log.v("seting", "bitmap code is " + SetBpUtil.getInstance().getCode());
                customBuilder.setMessage(this.getResources().getString(R.string.StringVerifyAlter))
                        .setNegativeButton(getResources().getString(R.string.Cancel), this)
                        .setPositiveButton(getResources().getString(R.string.confirm), this);

                dismiss = false;//不自动消失
                break;

            case CUSTOM_DIALOG_WAIT:
                if(SysApplication.density==1.5){
                    view = inflater.inflate(R.layout.set_wait_dialogden15, null);
                }else {
                    view = inflater.inflate(R.layout.set_wait_dialog, null);
                }
                customBuilder.setMessage(this.getResources().getString(R.string.StringSearchInfo))
                        .setNegativeButton(getResources().getString(R.string.StopSearching), this);
                dismiss = false;//不自动消失
                break;
            case CUSTOM_DIALOG_SEARCH:
                if(SysApplication.density==1.5){
                    view = inflater.inflate(R.layout.set_simple_dialogden15, null);
                }else {
                    view = inflater.inflate(R.layout.set_simple_dialog, null);
                }
                customBuilder.setMessage(getResources().getStringArray(R.array.tyreItem)[selectWhich]+this.getResources().getString(R.string.StringChangeAlter2))
                        .setPositiveButton(getResources().getString(R.string.confirm), this);
                break;
			/*
	        case CUSTOM_DIALOG_PROMPT:
	        	view = inflater.inflate(R.layout.set_simple_dialog, null); 
	            customBuilder.setMessage(this.getResources().getString(R.string.StringSuccessInfo))  
	            .setPositiveButton(getResources().getString(R.string.confirm),this); 
	            break; 
	        */
        }
        dialog = customBuilder.create(view, dismiss);
        dialog.show();
        curDialog = dialog;
        return dialog;
    }

    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "[onDestroy]");
        mHandler = null;
        if (curDialog != null)
            curDialog.dismiss();
        super.onDestroy();
        if (mBtMsgReceiver != null)
            unregisterReceiver(mBtMsgReceiver);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //更改为按下时的背景图片
            ((ImageView) v).getDrawable().setAlpha(150);//
            ((ImageView) v).invalidate();

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            //改为抬起时的图片
            ((ImageView) v).getDrawable().setAlpha(255);//还原图片
            ((ImageView) v).invalidate();

        }
        return false;
    }

    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(SysControllerService.NOTIFY_TIRE_RESULT)) {
                Log.i(TAG, "** ON RECEIVE tire info **");
                int result = intent.getExtras().getInt("ARG");

                if (DialogWait != null) {
                    Log.w(TAG, "dismiss wait dialog");
                    DialogWait.dismiss();
                    DialogWait = null;
                }
                if (result == 0x00) {
                    DialogSearch = onCreateDialog(CUSTOM_DIALOG_SEARCH);
                } else {
//					showResult(getResources().getString(R.string.SearchTireErr) + "\n" + SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(SetSensorChangeActivity.this,result));
                    onCreateErrDialog(0x01, getResources().getString(R.string.SearchTireErr) + "\n" + SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(SetSensorChangeActivity.this, result));
                }

            } else {
                Log.e(TAG, "another action: " + action);
            }
        }
    }

    public void showResult(String result) {
        TipPop2 tip = new TipPop2(this);
        tip.setContent(result);
        tip.showAtLocation(getRootView(this), Gravity.CENTER, 0, 0);
    }

    public View getRootView(Activity myContext) {
        return ((ViewGroup) myContext.findViewById(android.R.id.content)).getChildAt(0);
    }
}

