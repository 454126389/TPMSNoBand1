package com.hongking.hktpms;
/*
 * 包说明
 * 轮胎对调Activity
 * */

import com.hongking.oemtpms.R;
import com.hongking.hktpms.SysCustomDialog;
import com.hongking.hktpms.SysParam;
import com.hongking.hktpms.views.TipPop2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
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


@SuppressLint("CutPasteId")
public class SetSensorExchangeActivity extends SysActivity implements DialogInterface.OnClickListener,
        OnTouchListener, OnClickListener {
    private final int CUSTOM_DIALOG_ITEM3 = 3;
    private final int CUSTOM_DIALOG_ITEM4 = 4;
    private final int CUSTOM_DIALOG_PROMPT = 5;
    //private final int CUSTOM_DIALOG_UNSELECT = 6;


    private ImageView contentRB;
    private ImageView contentRF;
    private ImageView contentLB;
    private ImageView contentLF;
    private ImageView ImageTyreSw;
    private ImageView ImageSelectFirst;
    private ImageView ImageSelectSecond;

    private TextView TitleText;
    SysParam.DataInfo press_lf;
    SysParam.DataInfo press_rf;
    SysParam.DataInfo press_lb;
    SysParam.DataInfo press_rb;

    SysParam.DataInfo temp_lf;
    SysParam.DataInfo temp_rf;
    SysParam.DataInfo temp_lb;
    SysParam.DataInfo temp_rb;

    private int selectSecond;

    private int step = 0;
    private Dialog DialogStep3;
    private Dialog DialogStep4;
    private Dialog DialogStep5;
    private Dialog DialogStep6;
    private TextView infoView;
    private TextView infoView1;
    private final int CUSTOM_DIALOG_ITEM0 = 0;
    private final int CUSTOM_DIALOG_ITEM1 = 1;
    private final int CUSTOM_DIALOG_ITEM2 = 2;
    private final int CUSTOM_DIALOG_WAIT = 4;
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

    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;
    static final String TAG = "SetSensorExchangeActivity";


    void SelectLayout(Activity activity) {
//        setContentView(R.layout.change_type_large);
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
//		linearLayout = findViewById(R.id.CardView);
//		dr = this.getResources().getDrawable(TPMSParam.iCardTable[TPMSParam.iCardIndex]); 
//		linearLayout.setBackgroundDrawable(dr);
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
    }

    void tyreFlash() {

        press_lf = TPMSParam.tyreData.getPressValue(TPMSParam.lf);
        press_rf = TPMSParam.tyreData.getPressValue(TPMSParam.rf);
        press_lb = TPMSParam.tyreData.getPressValue(TPMSParam.lb);
        press_rb = TPMSParam.tyreData.getPressValue(TPMSParam.rb);

        temp_lf = TPMSParam.tyreData.getTempValue(TPMSParam.lf);
        temp_rf = TPMSParam.tyreData.getTempValue(TPMSParam.rf);
        temp_lb = TPMSParam.tyreData.getTempValue(TPMSParam.lb);
        temp_rb = TPMSParam.tyreData.getTempValue(TPMSParam.rb);
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

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TPMSParam = ((SysParam) SysParam.getInstance(this));
        SysParam.getInstance(this).addActivity(this);
        SelectLayout(this);
        new SysParam.Builder(this).setTitle(getResources().getString(R.string.TireSwap)).setBackButton(getResources().getString(R.string.StringSetting), new View.OnClickListener() {
            public void onClick(View v) {
                KeyEvent newEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
                SetSensorExchangeActivity.this.onKeyDown(KeyEvent.KEYCODE_BACK, newEvent);
            }
        }).create();

        myImageMark[0] = (ImageView) findViewById(R.id.ImageLFMarkView);
        myImageMark[1] = (ImageView) findViewById(R.id.ImageRFMarkView);

        myImageMark[2] = (ImageView) findViewById(R.id.ImageLBMarkView);
        myImageMark[3] = (ImageView) findViewById(R.id.ImageRBMarkView);
        /*-----------------------------------------------------------------*/
        contentLF = (ImageView) findViewById(R.id.ImageLFTyreView);
        contentLB = (ImageView) findViewById(R.id.ImageLBTyreView);

        contentRF = (ImageView) findViewById(R.id.ImageRFTyreView);
        contentRB = (ImageView) findViewById(R.id.ImageRBTyreView);

        contentSet[0] = (ImageView) findViewById(R.id.ImageLFTyreView);
        contentSet[1] = (ImageView) findViewById(R.id.ImageRFTyreView);

        contentSet[2] = (ImageView) findViewById(R.id.ImageLBTyreView);
        contentSet[3] = (ImageView) findViewById(R.id.ImageRBTyreView);
        ImageTyreSw = (ImageView) findViewById(R.id.ImageTyreSw);

        TitleText = (TextView) findViewById(R.id.TitleText);
        contentSet[0].setVisibility(0x00);
        contentSet[1].setVisibility(0x00);
        contentSet[2].setVisibility(0x00);
        contentSet[3].setVisibility(0x00);
        contentRB.setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        contentRF.setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        contentLB.setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        contentLF.setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        ImageTyreSw.setOnClickListener(this);

        contentRB.setOnClickListener(this);
        contentRF.setOnClickListener(this);
        contentLB.setOnClickListener(this);
        contentLF.setOnClickListener(this);
        contentLF.setOnTouchListener(this);
        contentRF.setOnTouchListener(this);
        contentLB.setOnTouchListener(this);
        contentRB.setOnTouchListener(this);
        TitleText.setText(R.string.StringSelectAlter1);
        tyreFlash();
        this.AutoDestroryListen();
    }

    @Override
    public void onStart() {
        TPMSParam.activityStart();
        super.onStart();
    }

    @Override
    public void onStop() {

        TPMSParam.activityStop();
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

    private void SetExchangeCommand(int selectWhich2, int selectSecond2) {
        int ret;
        Message msg = Message.obtain();
        ret = TPMSParam.mBluethoothData.ProtocolTireExchange((byte) selectWhich2, (byte) selectSecond2);
        msg.arg1 = 0;
        msg.arg2 = ret;
        if (mHandler != null)
            mHandler.sendMessage(msg);
    }

    public Dialog onCreateErrDialog(int dialogId, String msg) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = null;
        SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(this);
        Dialog dialog = null;

        view = inflater.inflate(R.layout.set_simple_dialog, null);
        customBuilder.setMessage(getResources().getString(R.string.RequestFailure) + "\n" + msg)
                .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //SetSensorExchangeActivity.this.finish();
                    }
                });

        dialog = customBuilder.create(view, true);
        curDialog = dialog;
        dialog.show();
        return dialog;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            new CountDownTimer(1000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    DialogWait.dismiss();
                }
            }.start();

            if (msg.arg2 == 0x00) {

                SysParam.Tire_Inf info1 = TPMSParam.tyreData.getInfoValue(selectWhich);
                SysParam.Tire_Inf info2 = TPMSParam.tyreData.getInfoValue(selectSecond);

                int id1 = TPMSParam.tyreData.getId(selectWhich);
                int id2 = TPMSParam.tyreData.getId(selectSecond);
                info1.supdate = -1;//清空数据
                info2.supdate = -1;
                TPMSParam.tyreData.setInfoValue(selectWhich, info2);
                TPMSParam.tyreData.setInfoValue(selectSecond, info1);
                TPMSParam.tyreData.setID(selectWhich, id2);
                TPMSParam.tyreData.setID(selectSecond, id1);
//				DialogStep5 = onCreateDialog(CUSTOM_DIALOG_PROMPT);
                changesuccess();
            } else {
                onCreateErrDialog(0x01, String.format(SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(SetSensorExchangeActivity.this, msg.arg2)));
            }
        }
    };

    private void changesuccess() {
        TPMSParam.tyreData.setUpdateStatus(0,0);
        TPMSParam.tyreData.setUpdateStatus(1,1);
        TPMSParam.tyreData.setUpdateStatus(2,2);
        TPMSParam.tyreData.setUpdateStatus(3,3);
        tyreFlash();
        step=0;
        contentLF.setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        contentLB.setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        contentRF.setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        contentRB.setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
        ImageTyreSw.setVisibility(View.GONE);
        ImageSelectFirst=null;
        ImageSelectSecond=null;
        new CountDownTimer(1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                showResult(getResources().getStringArray(R.array.tyreItem)[selectWhich] +
                        SetSensorExchangeActivity.this.getResources().getString(R.string.And) +
                        getResources().getStringArray(R.array.tyreItem)[selectSecond] +
                        SetSensorExchangeActivity.this.getResources().getString(R.string.StringSuccessInfo2));
            }
        }.start();

    }

    @Override
    public void onClick(View v) {
        int temp = 0;
        if (v == contentLF) {
            temp = 0;
        } else if (v == contentRF) {
            temp = 1;
        } else if (v == contentLB) {
            temp = 2;
        } else if (v == contentRB) {
            temp = 3;
        } else {
        }

        if (v == ImageSelectFirst) {
            ImageTyreSw.setVisibility(0x04);
            ImageSelectFirst = ImageSelectSecond;
            ImageSelectSecond = null;
            selectWhich = selectSecond;

            ((ImageView) v).setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
            step--;
            if (step == 0) {
                TitleText.setText(R.string.StringSelectAlter1);
            } else if (step == 1) {
                TitleText.setText(R.string.StringSelectAlter2);
            }
        } else if (v == ImageSelectSecond) {
            ImageTyreSw.setVisibility(0x04);
            ImageSelectSecond = null;
            ((ImageView) v).setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
            step--;
            if (step == 0) {
                TitleText.setText(R.string.StringSelectAlter1);
            } else if (step == 1) {
                TitleText.setText(R.string.StringSelectAlter2);
            }

        } else if (step == 0) {
            selectWhich = temp;
            ImageSelectFirst = (ImageView) v;
            ((ImageView) v).setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre2));
            step = 1;//进入第二阶段
            TitleText.setText(R.string.StringSelectAlter2);
        } else if (step == 0x01) {
            selectSecond = temp;
            ImageSelectSecond = (ImageView) v;
            ((ImageView) v).setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre2));
            step = 2;
            TitleText.setText(R.string.StringSelectAlter3);
            ImageTyreSw.setVisibility(0x00);
        } else if (v == ImageTyreSw) {
//			showResult(getResources().getStringArray(R.array.tyreItem)[selectWhich]+
//					 this.getResources().getString(R.string.And)+
//                  getResources().getStringArray(R.array.tyreItem)[selectSecond]+
//                  this.getResources().getString(R.string.StringSuccessInfo2));
//        	DialogStep3 = onCreateDialog(CUSTOM_DIALOG_ITEM3);
//			DialogWait = onCreateDialog(CUSTOM_DIALOG_WAIT);
            cjhDialog();
        } else {
        }
    }

    private void cjhDialog() {
        Dialog dialog = null;
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = null;
        boolean dismiss = true;
        SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(this);
        view = inflater.inflate(R.layout.set_wait_dialog, null);
        customBuilder.setMessage(this.getResources().getString(R.string.StringExchangeInfo))
                .setNegativeButton(getResources().getString(R.string.Cancel), this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SetExchangeCommand(selectWhich, selectSecond);
            }
        }).start();
        DialogWait = customBuilder.create(view, dismiss);
        curDialog = DialogWait;
        DialogWait.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == DialogStep1) {
            if (which == -1) {

            } else {
                Log.v("keydebug", "which=" + which);
            }
        } else if (dialog == DialogStep2) {
            if (which == -1) {

            } else {
                Log.v("keydebug", "which=" + which);
            }
        } else if (dialog == DialogStep3) {//验证图片对话框
            if (which == -1) {

                if (input.getText().toString().equalsIgnoreCase(SetBpUtil.getInstance().getCode())) {
                    dialog.dismiss();
                    TitleText.setText(R.string.StringSelectAlter1);
                    ((ImageView) ImageSelectFirst).setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                    ((ImageView) ImageSelectSecond).setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                    ImageSelectFirst = null;
                    ImageSelectSecond = null;
                    step = 0;
                    ImageTyreSw.setVisibility(0x04);
                    DialogWait = onCreateDialog(CUSTOM_DIALOG_WAIT);
                } else {
                    infoView.setText(getResources().getString(R.string.InputErr));
                    bitmapView.setImageBitmap(SetBpUtil.getInstance().createBitmap());
                    input.setText("");
                }
            } else {
                dialog.dismiss();
            }
        } else if (dialog == DialogStep4) {
            if (which == -1) {

                DialogWait = onCreateDialog(CUSTOM_DIALOG_WAIT);
            } else {
                contentRB.setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                contentRF.setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                contentLB.setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
                contentLF.setImageDrawable(getResources().getDrawable(R.drawable.icon_tyre1));
            }
            step = 0;
        } else if (dialog == DialogStep5) {
            if (which == -1) {
                myImageMark[selectWhich].setBackgroundResource(R.drawable.icon_mark1);
                myImageMark[selectSecond].setBackgroundResource(R.drawable.icon_mark1);
                TPMSParam.tyreData.setStatus(selectWhich, true);
                TPMSParam.tyreData.setStatus(selectSecond, true);
                TPMSParam.tyreData.setUpdateStatus(selectWhich, selectWhich);
                TPMSParam.tyreData.setUpdateStatus(selectSecond, selectSecond);

            } else {

            }
        }    //第5步要做的事
        else if (dialog == DialogStep6) {

            myImageMark[selectWhich].setBackgroundResource(R.drawable.icon_mark1);
            myImageMark[selectSecond].setBackgroundResource(R.drawable.icon_mark1);
            TPMSParam.tyreData.setStatus(selectWhich, true);
            TPMSParam.tyreData.setStatus(selectSecond, true);
        } else if (dialog == DialogWait) {
            if (which == -1) {

            } else {
                myImageMark[selectWhich].setBackgroundResource(R.drawable.icon_mark1);
                myImageMark[selectSecond].setBackgroundResource(R.drawable.icon_mark1);
            }
        }
    }

    public Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = null;
        boolean dismiss = true;
        SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(this);
        switch (dialogId) {
            case CUSTOM_DIALOG_ITEM1:
                TitleText.setText(R.string.StringSelectAlter1);
                return null;

            case CUSTOM_DIALOG_ITEM2:
                TitleText.setText(R.string.StringSelectAlter2);
                return null;
            case CUSTOM_DIALOG_ITEM3:
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
                            //改为抬起时的图片
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
                view = inflater.inflate(R.layout.set_wait_dialog, null);
                customBuilder.setMessage(this.getResources().getString(R.string.StringExchangeInfo))
                        .setNegativeButton(getResources().getString(R.string.Cancel), this);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SetExchangeCommand(selectWhich, selectSecond);
                    }
                }).start();
                dialog = customBuilder.create(view, dismiss);
                curDialog = dialog;
                dialog.show();
                return dialog;
//			break;
                    /*----------以上线程用于超时用的---------------------------*/
            case CUSTOM_DIALOG_PROMPT:
                showResult(getResources().getStringArray(R.array.tyreItem)[selectWhich] +
                        this.getResources().getString(R.string.And) +
                        getResources().getStringArray(R.array.tyreItem)[selectSecond] +
                        this.getResources().getString(R.string.StringSuccessInfo2));
//	        	view = inflater.inflate(R.layout.set_simple_dialog, null); 
//	            customBuilder.setMessage(this.getResources().getString(R.string.StringCongratsInfo)+
//	            						 getResources().getStringArray(R.array.tyreItem)[selectWhich]+
//	            						 this.getResources().getString(R.string.And)+
//	        	                         getResources().getStringArray(R.array.tyreItem)[selectSecond]+
//	        	                         this.getResources().getString(R.string.StringSuccessInfo2))  
//	                .setPositiveButton(getResources().getString(R.string.confirm),this);  
                break;
            /*
            case CUSTOM_DIALOG_UNSELECT :
	            view = inflater.inflate(R.layout.set_custom_dialog, null); 
	            customBuilder.setMessage(this.getResources().getString(R.string.StringUnselectAlter))  
	                .setNegativeButton(getResources().getString(R.string.Cancel),this)  
	                .setPositiveButton(getResources().getString(R.string.confirm),this);   
	            break; 
	        */
        }
//		view = inflater.inflate(R.layout.set_wait_dialog, null);
//		customBuilder.setMessage(this.getResources().getString(R.string.StringExchangeInfo))
//				.setNegativeButton(getResources().getString(R.string.Cancel),this);


        dialog = customBuilder.create(view, dismiss);
        curDialog = dialog;
        dialog.show();
        return dialog;
    }

    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
        tyreFlash();

    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "[onDestroy]");
        mHandler = null;
        if (curDialog != null)
            curDialog.dismiss();
        super.onDestroy();
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

    public void showResult(String result) {
        TipPop2 tip = new TipPop2(this);
        tip.setContent(result);
        tip.showAtLocation(getRootView(this), Gravity.CENTER, 0, 0);
    }

    public View getRootView(Activity myContext) {
        return ((ViewGroup) myContext.findViewById(android.R.id.content)).getChildAt(0);
    }
}
