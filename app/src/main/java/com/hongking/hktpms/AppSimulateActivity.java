package com.hongking.hktpms;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.hongking.hktpms.SysParam.DataInfo;
import com.hongking.oemtpms.R;

@SuppressLint("DefaultLocale")
public class AppSimulateActivity extends Activity {
	
    private PowerManager pm;
    private PowerManager.WakeLock wakeLock;
    
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
	
	private SysParam TPMSParam;
	
	private View contentRB;
	private View contentRF;
	private View contentLB;
	private View contentLF;
	private View contentall;
	private ImageView WarnCenterImage;
	static final String TAG = "DiplayDemo";
	private boolean isRunning;
	private SysApplication tpmsApp ;
	void SelectLayout(Activity activity) {
		Display display = getWindowManager().getDefaultDisplay();
		int width, height;
		width = display.getWidth();
		height = display.getHeight();
		Log.i(TAG, "starting display_view default ui");
		setContentView(R.layout.display_view);
//		contentall = findViewById(R.id.contentall);
//		Drawable dr = this.getResources().getDrawable(TPMSParam.iThemeTable[TPMSParam.iThemeIndex]); 
//		contentall.setBackgroundDrawable(dr);
//		View linearLayout = findViewById(R.id.CardView);
//		dr = this.getResources().getDrawable(TPMSParam.iCardTable[TPMSParam.iCardIndex]); 
//		linearLayout.setBackgroundDrawable(dr);
	}

	private TextView TextViewRequest(int which,byte index)
	{
    	TextView myViewWarnText; 
		if(which == TPMSParam.lf){
			if(index == 0){
				myViewWarnText = (TextView)findViewById(R.id.LFwarnArea1Text);				
			}else if (index == 1){
				myViewWarnText = (TextView)findViewById(R.id.LFwarnArea2Text);
			}else if (index == 2){
				myViewWarnText = (TextView)findViewById(R.id.LFwarnArea3Text);
			}else {
				myViewWarnText = (TextView)findViewById(R.id.LFwarnArea4Text);
			}
		}else if(which == TPMSParam.rf){
			if(index == 0){
				myViewWarnText = (TextView)findViewById(R.id.RFwarnArea1Text);				
			}else if (index == 1){
				myViewWarnText = (TextView)findViewById(R.id.RFwarnArea2Text);
			}else if (index == 2){
				myViewWarnText = (TextView)findViewById(R.id.RFwarnArea3Text);
			}else {
				myViewWarnText = (TextView)findViewById(R.id.RFwarnArea4Text);
			}
			
		}else if(which == TPMSParam.lb){
			if(index == 0){
				myViewWarnText = (TextView)findViewById(R.id.LBwarnArea1Text);				
			}else if (index == 1){
				myViewWarnText = (TextView)findViewById(R.id.LBwarnArea2Text);
			}else if (index == 2){
				myViewWarnText = (TextView)findViewById(R.id.LBwarnArea3Text);
			}else {
				myViewWarnText = (TextView)findViewById(R.id.LBwarnArea4Text);
			}
			
		}else{
			if(index == 0){
				myViewWarnText = (TextView)findViewById(R.id.RBwarnArea1Text);				
			}else if (index == 1){
				myViewWarnText = (TextView)findViewById(R.id.RBwarnArea2Text);
			}else if (index == 2){
				myViewWarnText = (TextView)findViewById(R.id.RBwarnArea3Text);
			}else {
				myViewWarnText = (TextView)findViewById(R.id.RBwarnArea4Text);
			}
			
		}
		return myViewWarnText;
		
	}
	private ImageView ImageViewRequest(int lb,byte index)
	{
		ImageView myViewWarnImage; 
		if(lb == TPMSParam.lf){
			if(index == 0){
				myViewWarnImage = (ImageView)findViewById(R.id.LFwarnArea1Icon);				
			}else if (index == 1){
				myViewWarnImage = (ImageView)findViewById(R.id.LFwarnArea2Icon);
			}else if (index == 2){
				myViewWarnImage = (ImageView)findViewById(R.id.LFwarnArea3Icon);
			}else {
				myViewWarnImage = (ImageView)findViewById(R.id.LFwarnArea4Icon);
			}
			
		}else if(lb == TPMSParam.rf){
			if(index == 0){
				myViewWarnImage = (ImageView)findViewById(R.id.RFwarnArea1Icon);				
			}else if (index == 1){
				myViewWarnImage = (ImageView)findViewById(R.id.RFwarnArea2Icon);
			}else if (index == 2){
				myViewWarnImage = (ImageView)findViewById(R.id.RFwarnArea3Icon);
			}else {
				myViewWarnImage = (ImageView)findViewById(R.id.RFwarnArea4Icon);
			}
			
		}else if(lb == TPMSParam.lb){
			if(index == 0){
				myViewWarnImage = (ImageView)findViewById(R.id.LBwarnArea1Icon);				
			}else if (index == 1){
				myViewWarnImage = (ImageView)findViewById(R.id.LBwarnArea2Icon);
			}else if (index == 2){
				myViewWarnImage = (ImageView)findViewById(R.id.LBwarnArea3Icon);
			}else {
				myViewWarnImage = (ImageView)findViewById(R.id.LBwarnArea4Icon);
			}
			
		}else{
			if(index == 0){
				myViewWarnImage = (ImageView)findViewById(R.id.RBwarnArea1Icon);				
			}else if (index == 1){
				myViewWarnImage = (ImageView)findViewById(R.id.RBwarnArea2Icon);
			}else if (index == 2){
				myViewWarnImage = (ImageView)findViewById(R.id.RBwarnArea3Icon);
			}else {
				myViewWarnImage = (ImageView)findViewById(R.id.RBwarnArea4Icon);
			}
			
		}
		return myViewWarnImage;
		
	}
	private View ViewRequest(int which,byte index)
	{
    	View myViewWarn; 
    	
		if(which == TPMSParam.lf){
			if(index == 0){
				myViewWarn = (View)findViewById(R.id.LFwarnArea1);				
			}else if (index == 1){
				myViewWarn = (View)findViewById(R.id.LFwarnArea2);
			}else if (index == 2){
				myViewWarn = (View)findViewById(R.id.LFwarnArea3);
			}else {
				myViewWarn = (View)findViewById(R.id.LFwarnArea4);
			}
			
		}else if(which == TPMSParam.rf){
			if(index == 0){
				myViewWarn = (View)findViewById(R.id.RFwarnArea1);				
			}else if (index == 1){
				myViewWarn = (View)findViewById(R.id.RFwarnArea2);
			}else if (index == 2){
				myViewWarn = (View)findViewById(R.id.RFwarnArea3);
			}else {
				myViewWarn = (View)findViewById(R.id.RFwarnArea4);
			}
			
		}else if(which == TPMSParam.lb){
			if(index == 0){
				myViewWarn = (View)findViewById(R.id.LBwarnArea1);				
			}else if (index == 1){
				myViewWarn = (View)findViewById(R.id.LBwarnArea2);
			}else if (index == 2){
				myViewWarn = (View)findViewById(R.id.LBwarnArea3);
			}else {
				myViewWarn = (View)findViewById(R.id.LBwarnArea4);
			}
			
		}else{
			if(index == 0){
				myViewWarn = (View)findViewById(R.id.RBwarnArea1);				
			}else if (index == 1){
				myViewWarn = (View)findViewById(R.id.RBwarnArea2);
			}else if (index == 2){
				myViewWarn = (View)findViewById(R.id.RBwarnArea3);
			}else {
				myViewWarn = (View)findViewById(R.id.RBwarnArea4);
			}
			
		}
		return myViewWarn;
		
	}
	private void ViewClr(int which){
		View myViewWarn; 
		ImageView myViewWarnImage;
		int ViewStatus = View.GONE;
		if(which == TPMSParam.lf){
			
			myViewLFTemp.setTextColor(getResources().getColor(R.color.normalTempDisplay));
			myViewLFTempUnit.setTextColor(getResources().getColor(R.color.normalTempDisplay));
			
			myViewLFPress.setTextColor(getResources().getColor(R.color.normalPressDisplay));
			myViewLFPressUnit.setTextColor(getResources().getColor(R.color.normalPressDisplay));

			myViewWarnImage = (ImageView)findViewById(R.id.LFwarnAreaIcon);
			myViewWarnImage.setVisibility(ViewStatus);
			
			myViewWarn = findViewById(R.id.LFwarnArea1);				
			myViewWarn.setVisibility(ViewStatus);
			myViewWarn = (View)findViewById(R.id.LFwarnArea2);
			myViewWarn.setVisibility(ViewStatus);
			myViewWarn = (View)findViewById(R.id.LFwarnArea3);
			myViewWarn.setVisibility(ViewStatus);
			myViewWarn = (View)findViewById(R.id.LFwarnArea4);
			myViewWarn.setVisibility(ViewStatus);
			
			
		}else if(which == TPMSParam.rf){
			
			myViewRFTemp.setTextColor(getResources().getColor(R.color.normalTempDisplay));
			myViewRFTempUnit.setTextColor(getResources().getColor(R.color.normalTempDisplay));
			
			myViewRFPress.setTextColor(getResources().getColor(R.color.normalPressDisplay));
			myViewRFPressUnit.setTextColor(getResources().getColor(R.color.normalPressDisplay));
			
			myViewWarnImage = (ImageView)findViewById(R.id.RFwarnAreaIcon);
			myViewWarnImage.setVisibility(ViewStatus);
			
			myViewWarn = (View)findViewById(R.id.RFwarnArea1);				
			myViewWarn.setVisibility(ViewStatus);
			myViewWarn = (View)findViewById(R.id.RFwarnArea2);
			myViewWarn.setVisibility(ViewStatus);
			myViewWarn = (View)findViewById(R.id.RFwarnArea3);
			myViewWarn.setVisibility(ViewStatus);
			myViewWarn = (View)findViewById(R.id.RFwarnArea4);
			myViewWarn.setVisibility(ViewStatus);
			
		}else if(which == TPMSParam.lb){
			myViewLBTemp.setTextColor(getResources().getColor(R.color.normalTempDisplay));
			myViewLBTempUnit.setTextColor(getResources().getColor(R.color.normalTempDisplay));
			
			myViewLBPress.setTextColor(getResources().getColor(R.color.normalPressDisplay));
			myViewLBPressUnit.setTextColor(getResources().getColor(R.color.normalPressDisplay));
			
			myViewWarnImage = (ImageView)findViewById(R.id.LBwarnAreaIcon);
			myViewWarnImage.setVisibility(ViewStatus);
			
			myViewWarn = (View)findViewById(R.id.LBwarnArea1);				
			myViewWarn.setVisibility(ViewStatus);
			myViewWarn = (View)findViewById(R.id.LBwarnArea2);
			myViewWarn.setVisibility(ViewStatus);
			myViewWarn = (View)findViewById(R.id.LBwarnArea3);
			myViewWarn.setVisibility(ViewStatus);
			myViewWarn = (View)findViewById(R.id.LBwarnArea4);
			myViewWarn.setVisibility(ViewStatus);
			
		}else{
			myViewRBTemp.setTextColor(getResources().getColor(R.color.normalTempDisplay));
			myViewRBTempUnit.setTextColor(getResources().getColor(R.color.normalTempDisplay));
			
			myViewRBPress.setTextColor(getResources().getColor(R.color.normalPressDisplay));
			myViewRBPressUnit.setTextColor(getResources().getColor(R.color.normalPressDisplay));
			
			myViewWarnImage = (ImageView)findViewById(R.id.RBwarnAreaIcon);
			myViewWarnImage.setVisibility(ViewStatus);
			
			myViewWarn = (View)findViewById(R.id.RBwarnArea1);				
			myViewWarn.setVisibility(ViewStatus);
			myViewWarn = (View)findViewById(R.id.RBwarnArea2);
			myViewWarn.setVisibility(ViewStatus);
			myViewWarn = (View)findViewById(R.id.RBwarnArea3);
			myViewWarn.setVisibility(ViewStatus);
			myViewWarn = (View)findViewById(R.id.RBwarnArea4);
			myViewWarn.setVisibility(ViewStatus);
			
		}
		
	}
	void warnViewDisplay(int which){
		ImageView myViewWarnImage;
		if(which == TPMSParam.lf){
			myViewWarnImage = (ImageView)findViewById(R.id.LFwarnAreaIcon);
			myViewWarnImage.setVisibility(0x00);
		}else if(which == TPMSParam.rf){
			myViewWarnImage = (ImageView)findViewById(R.id.RFwarnAreaIcon);
			myViewWarnImage.setVisibility(0x00);
		}else if(which == TPMSParam.lb){
			myViewWarnImage = (ImageView)findViewById(R.id.LBwarnAreaIcon);
			myViewWarnImage.setVisibility(0x00);
		}else if(which == TPMSParam.rb){
			myViewWarnImage = (ImageView)findViewById(R.id.RBwarnAreaIcon);
			myViewWarnImage.setVisibility(0x00);
		}
	}
	void tyreFlash(Message msg){
		View  myViewWarn;
    	TextView myViewWarnText; 
    	ImageView myViewWarnIcon ;
    	byte i = 0;
    	int tyre;
		
		SysParam.DataInfo infoValue[][] = (DataInfo[][]) msg.obj;
		Log.i(TAG,"msg add is "+msg);
		SysParam.DataInfo press_lf;
	    SysParam.DataInfo press_rf;
	    SysParam.DataInfo press_lb;
	    SysParam.DataInfo press_rb;
	    
	    SysParam.DataInfo temp_lf;
	    SysParam.DataInfo temp_rf;
	    SysParam.DataInfo temp_lb;
	    SysParam.DataInfo temp_rb;
		press_lf = infoValue[TPMSParam.lf][0];
	    press_rf = infoValue[TPMSParam.rf][0];
	    press_lb = infoValue[TPMSParam.lb][0];
	    press_rb = infoValue[TPMSParam.rb][0];
	    
	    temp_lf = infoValue[TPMSParam.lf][1];
	    temp_rf = infoValue[TPMSParam.rf][1];
	    temp_lb = infoValue[TPMSParam.lb][1];
	    temp_rb = infoValue[TPMSParam.rb][1];
	    
	    WarnCenterImage.setVisibility(0x04);
	    tyre = TPMSParam.lf;
	    ViewClr(tyre);
	    if(press_lf.HighAlertStatus || press_lf.LowAlertStatus || 
	    	temp_lf.HighAlertStatus || press_lf.LeakageAlertStatus){
	    	warnViewDisplay(tyre);
	    	if(temp_lf.HighAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
		    	
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_thigh));
	    		myViewWarnText.setText(getResources().getString(R.string.highTemp));
	    		myViewLFTemp.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewLFTempUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		
	    	}
	    	if(press_lf.LowAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_plow));
	    		myViewWarnText.setText(getResources().getString(R.string.lowPress));
	    		myViewLFPress.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewLFPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	}
	    	else if(press_lf.HighAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_phigh));
	    		myViewWarnText.setText(getResources().getString(R.string.highPress));

	    		myViewLFPress.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewLFPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	}
	    	if(press_lf.LeakageAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_leakage));
	    		myViewWarnText.setText(getResources().getString(R.string.leakPress));

	    		myViewLFPress.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewLFPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	
	    	}
	    	if(press_lf.LBStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_elow));
	    		myViewWarnText.setText(getResources().getString(R.string.leakBattery));
	    	}
			if(press_lf.NSStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_sbleak));
	    		myViewWarnText.setText(getResources().getString(R.string.breakSenor));
	    	}
	    	
	    }
	    else{
	    	
	    	myViewWarn = ViewRequest(tyre,i);
	    	myViewWarnText = TextViewRequest(tyre,i);
	    	myViewWarnIcon = ImageViewRequest(tyre,i);
	    	myViewWarn.setVisibility(View.VISIBLE);
	    	if(press_lf.validFlag){
    			myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_select_button));
        		myViewWarnText.setText(getResources().getString(R.string.StatusOk));
    		}
    		else{
    			myViewWarnIcon.setImageDrawable(null);
        		myViewWarnText.setText(getResources().getString(R.string.StatusUpdate));
    		}
	    }

	    tyre = TPMSParam.rf;
	    ViewClr(tyre);
	    i = 0;
	    if(press_rf.HighAlertStatus || press_rf.LowAlertStatus || 
	        temp_rf.HighAlertStatus || press_rf.LeakageAlertStatus){
	    	warnViewDisplay(tyre);
	    	WarnCenterImage.setVisibility(0x00);
	    	if(temp_rf.HighAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_thigh));
	    		myViewWarnText.setText(getResources().getString(R.string.highTemp));
	    		myViewRFTemp.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewRFTempUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	}
	    	if(press_rf.LowAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_plow));
	    		myViewWarnText.setText(getResources().getString(R.string.lowPress));

	    		myViewRFPress.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewRFPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		
	    		
	    	}
	    	else if(press_rf.HighAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_phigh));
	    		myViewWarnText.setText(getResources().getString(R.string.highPress));
	    		
	    		myViewRFPress.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewRFPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	}
	    	if(press_rf.LeakageAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_leakage));
	    		myViewWarnText.setText(getResources().getString(R.string.leakPress));

	    		myViewRFPress.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewRFPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	
	    	}
	    	if(press_rf.LBStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_elow));
	    		myViewWarnText.setText(getResources().getString(R.string.leakBattery));
	    	}
			if(press_rf.NSStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_sbleak));
	    		myViewWarnText.setText(getResources().getString(R.string.breakSenor));
	    	}
	    }
	    else{
	    	myViewWarn = ViewRequest(tyre,i);
	    	myViewWarnText = TextViewRequest(tyre,i);
	    	myViewWarnIcon = ImageViewRequest(tyre,i);
	    	myViewWarn.setVisibility(View.VISIBLE);
	    	if(press_rf.validFlag){
    			myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_select_button));
        		myViewWarnText.setText(getResources().getString(R.string.StatusOk));
    		}
    		else{
    			myViewWarnIcon.setImageDrawable(null);
        		myViewWarnText.setText(getResources().getString(R.string.StatusUpdate));
    		}
	    }
	    
	    tyre = TPMSParam.lb;
	    ViewClr(tyre);
	    i = 0;
	    if(press_lb.HighAlertStatus || press_lb.LowAlertStatus || 
	    	temp_lb.HighAlertStatus || press_lb.LeakageAlertStatus){
	    	warnViewDisplay(tyre);
	    	WarnCenterImage.setVisibility(0x00);
	    	if(temp_lb.HighAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_thigh));
	    		myViewWarnText.setText(getResources().getString(R.string.highTemp));
	    		myViewLBTemp.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewLBTempUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	}
	    	if(press_lb.LowAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_plow));
	    		myViewWarnText.setText(getResources().getString(R.string.lowPress));
	    		
	    		myViewLBPress.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewLBPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		
	    		
	    	}
	    	else if(press_lb.HighAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_phigh));
	    		myViewWarnText.setText(getResources().getString(R.string.highPress));
	    		myViewLBPress.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewLBPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	}
	    	if(press_lb.LeakageAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_leakage));
	    		myViewWarnText.setText(getResources().getString(R.string.leakPress));
	    		myViewLBPress.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewLBPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	
	    	}
	    	if(press_lb.LBStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_elow));
	    		myViewWarnText.setText(getResources().getString(R.string.leakBattery));
	    	}
			if(press_lb.NSStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_sbleak));
	    		myViewWarnText.setText(getResources().getString(R.string.breakSenor));
	    	}
	    }
	    else{
	    	myViewWarn = ViewRequest(tyre,i);
	    	myViewWarnText = TextViewRequest(tyre,i);
	    	myViewWarnIcon = ImageViewRequest(tyre,i);
	    	myViewWarn.setVisibility(View.VISIBLE);
	    	if(press_lb.validFlag){
    			myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_select_button));
        		myViewWarnText.setText(getResources().getString(R.string.StatusOk));
    		}
    		else{
    			myViewWarnIcon.setImageDrawable(null);
        		myViewWarnText.setText(getResources().getString(R.string.StatusUpdate));
    		}
	    }

	    tyre = TPMSParam.rb;
	    ViewClr(tyre);
	    i = 0;
		if(press_rb.HighAlertStatus || press_rb.LowAlertStatus || 
			temp_rb.HighAlertStatus || press_rb.LeakageAlertStatus){
	    	warnViewDisplay(tyre);
	    	WarnCenterImage.setVisibility(0x00);
	    	if(temp_rb.HighAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_thigh));
	    		myViewWarnText.setText(getResources().getString(R.string.highTemp));
	    		myViewRBTemp.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewRBTempUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	}
	    	if(press_rb.LowAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_plow));
	    		myViewWarnText.setText(getResources().getString(R.string.lowPress));
	    		myViewRBPress.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewRBPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	}
	    	else if(press_rb.HighAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_phigh));
	    		myViewWarnText.setText(getResources().getString(R.string.highPress));
	    		myViewRBPress.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewRBPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	}
	    	if(press_rb.LeakageAlertStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_leakage));
	    		myViewWarnText.setText(getResources().getString(R.string.leakPress));
	    		myViewRBPress.setTextColor(getResources().getColor(R.color.warnDisplay));
	    		myViewRBPressUnit.setTextColor(getResources().getColor(R.color.warnDisplay));
	    	}
	    	if(press_rb.LBStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_elow));
	    		myViewWarnText.setText(getResources().getString(R.string.leakBattery));
	    	}
			if(press_rb.NSStatus){
	    		myViewWarn = ViewRequest(tyre,i);
		    	myViewWarnText = TextViewRequest(tyre,i);
		    	myViewWarnIcon = ImageViewRequest(tyre,i);
		    	i++;
	    		
	    		myViewWarn.setVisibility(View.VISIBLE);
	    		myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_warn_sbleak));
	    		myViewWarnText.setText(getResources().getString(R.string.breakSenor));
	    	}
	    }
	    else{
	    	
	    	myViewWarn = ViewRequest(tyre,i);
	    	myViewWarnText = TextViewRequest(tyre,i);
	    	myViewWarnIcon = ImageViewRequest(tyre,i);
	    	myViewWarn.setVisibility(View.VISIBLE);
	    	if(press_rb.validFlag){
    			myViewWarnIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_select_button));
        		myViewWarnText.setText(getResources().getString(R.string.StatusOk));
    		}
    		else{
    			myViewWarnIcon.setImageDrawable(null);
        		myViewWarnText.setText(getResources().getString(R.string.StatusUpdate));
    		}
	    }
	    myViewLFPress.setText(press_lf.Display);
	    myViewLFTemp.setText(temp_lf.Display+temp_lf.unitName);
	    myViewRFPress.setText(press_rf.Display);
	    myViewRFTemp.setText(temp_rf.Display+temp_rf.unitName);
	    myViewLBPress.setText(press_lb.Display);
	    myViewLBTemp.setText(temp_lb.Display+temp_lb.unitName);
	    myViewRBPress.setText(press_rb.Display);
	    myViewRBTemp.setText(temp_rb.Display+temp_rb.unitName);
		myViewLFPressUnit.setText(press_lf.unitName);
		myViewLFTempUnit.setText(temp_lf.unitName);
		myViewRFPressUnit.setText(press_rf.unitName);
		myViewRFTempUnit.setText(temp_rf.unitName);
		myViewLBPressUnit.setText(press_lb.unitName);
		myViewLBTempUnit.setText(temp_lb.unitName);
		myViewRBPressUnit.setText(press_rb.unitName);
		myViewRBTempUnit.setText(temp_rb.unitName);
	}
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			tyreFlash(msg);			
		}
	};
	@SuppressLint("DefaultLocale")
	String getDisplay(int value,int punit){
		String strText;
        if(punit == 0)/*Bar*/
        {
            strText = String.format("%d.%02d",value / 100,value % 100);
        }else if(punit == 0x01)/*PSI*/
        {
            strText = String.format("%02d.%d",value / 10,value % 10);
        }
        else /*Kpa*/
        {
            strText = String.format("%03d",value);
        }

        return strText;
	}
	private void ParamLoadDemonstrate(int mode,int tyre){
		
		SysParam.DataInfo infoValue[][] = new SysParam.DataInfo[4][2];
		Log.w(TAG,"infovalue add is"+infoValue);
		infoValue[TPMSParam.lf][0] = TPMSParam.tyreData.getPressValue(TPMSParam.lf);
		infoValue[TPMSParam.rf][0] = TPMSParam.tyreData.getPressValue(TPMSParam.rf);
		infoValue[TPMSParam.lb][0] = TPMSParam.tyreData.getPressValue(TPMSParam.lb);
		infoValue[TPMSParam.rb][0] = TPMSParam.tyreData.getPressValue(TPMSParam.rb);
		
		infoValue[TPMSParam.lf][1] = TPMSParam.tyreData.getTempValue(TPMSParam.lf);
		infoValue[TPMSParam.rf][1] = TPMSParam.tyreData.getTempValue(TPMSParam.rf);
		infoValue[TPMSParam.lb][1] = TPMSParam.tyreData.getTempValue(TPMSParam.lb);
		infoValue[TPMSParam.rb][1] = TPMSParam.tyreData.getTempValue(TPMSParam.rb);
		if(mode == 0x04){
			mHandler.obtainMessage(0, infoValue).sendToTarget();
			return ;
			
		}
		for(int i=0;i<4;i++){
			infoValue[i][0].Display =""+getDisplay(240,0);
			infoValue[i][0].unitName=TPMSParam.tyreData.sPressUnitSting[0];
			infoValue[i][0].LeakageAlertStatus = false;
			infoValue[i][0].LowAlertStatus = false;
			infoValue[i][0].HighAlertStatus = false;
			infoValue[i][0].validFlag = true;
			
			infoValue[i][1].Display = ""+35;				
			infoValue[i][1].unitName=TPMSParam.tyreData.sTempUnitSting[0];
			infoValue[i][1].LeakageAlertStatus = false;
			infoValue[i][1].LowAlertStatus = false;
			infoValue[i][1].HighAlertStatus = false;
			
			
		}
		switch (mode){
		case 0://低压报警演示
			if(tyre < 0x04){                
				infoValue[tyre][0].Display = ""+getDisplay(170,0);	
				infoValue[tyre][0].LowAlertStatus=true;
			}	
			break;
		case 1://高压报警显示
			if(tyre < 0x04){
				infoValue[tyre][0].Display = ""+getDisplay(310,0);
				infoValue[tyre][0].HighAlertStatus=true;
			}
			break;
		case 2://高温报警显示
			if(tyre < 0x04){
				infoValue[tyre][1].Display = ""+75;			
				infoValue[tyre][1].HighAlertStatus = true;
			}
			
			break;
		case 3://漏气报警演示
			if(tyre < 0x04){
				
				for(int i=0;i < 7 && isRunning ;i++){
					infoValue[tyre][0].Display  = getDisplay(240-i*10,0);				
					if(i >= 3){
						if(i == 0x03 && isRunning){
							Resources resources = getResources();
							Configuration config = resources.getConfiguration();
							DisplayMetrics dm = resources.getDisplayMetrics();
							if(TPMSParam.iLanguageType == 1){
								tpmsApp.playMusic(tyre,3);
							}
							else if(TPMSParam.iLanguageType == 0){
								Log.i(TAG, "SysLanguage:"+Locale.getDefault().getLanguage());
								if(Locale.getDefault().getLanguage().equals("zh")){
									Log.i(TAG, "SysLanguage:"+Locale.getDefault().getLanguage());
									tpmsApp.playMusic(tyre,3);
								}
								else{
									//return;
									tpmsApp.playMusicDuDuDu();
									//delay(2000);
								}
							}
							else {
								//return;
								tpmsApp.playMusicDuDuDu();
								//delay(2000);
							}
						}
						infoValue[tyre][0].LeakageAlertStatus = true;
					}	
					mHandler.obtainMessage(0, infoValue).sendToTarget();					
					delay(2000);
				}
				return;
			}
			break;
		default:
			break;
		}
		mHandler.obtainMessage(0, infoValue).sendToTarget();
		if(tyre <= 3){
			Resources resources = getResources();
			Configuration config = resources.getConfiguration();
			DisplayMetrics dm = resources.getDisplayMetrics();
			if(TPMSParam.iLanguageType == 1){
				tpmsApp.playMusicPend(tyre,mode);
			}
			else if(TPMSParam.iLanguageType == 0){
				if(Locale.getDefault().getLanguage().equals("zh")){
					tpmsApp.playMusicPend(tyre,mode);
				}
				else{
					//return;
					tpmsApp.playMusicDuDuDuPend();
				}
			}
			else {
				//return;
				tpmsApp.playMusicDuDuDuPend();
			}			
			
		}else
			delay(500);
	}
	
	private void delay(int times){
		try { 
			Thread.currentThread();
			Thread.sleep(times);//延时多久后自动超时退出
		} 
		catch (InterruptedException e)
		{ 
			e.printStackTrace();
		}
	}
	private void valueLoad(String intent){
		isRunning = true;
		int i = 0;
		if(intent.equals(getResources().getString(R.string.LowPressureSimulation)))/*气压偏低演示*/
		{
			while(isRunning && i < 4){
				ParamLoadDemonstrate(0,4);
				ParamLoadDemonstrate(0,i++);
			}
		}
		else if(intent.equals(getResources().getString(R.string.HighPressureSimulation)))	/*气压偏高演示*/
		{
			while(isRunning && i < 4){
				ParamLoadDemonstrate(1,4);
				ParamLoadDemonstrate(1,i++);
			}
		}
		else if(intent.equals(getResources().getString(R.string.SolwLeakageSimulation)))	/*慢漏气演示*/
		{
			while(isRunning && i < 4){
				ParamLoadDemonstrate(3,i++);
			}
		}
		else if(intent.equals(getResources().getString(R.string.HighTempSimulation)))	/*温度偏高演示*/
		{
			while(isRunning && i < 4){
				ParamLoadDemonstrate(2,4);
				ParamLoadDemonstrate(2,i++);
			}
		}else if(intent.equals(getResources().getString(R.string.ViewHistoricaldataTitle)))	/*历史数据查看*/
		{
			ParamLoadDemonstrate(4,4);
			return;
			
		}
		this.finish();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tpmsApp = (SysApplication)getApplicationContext();
		TPMSParam = ((SysParam)SysParam.getInstance(this));
		SysParam.getInstance(this).addActivity(this);
		SelectLayout(this); 
		Intent intent = getIntent();
	    final String value = intent.getStringExtra("SimulateMode");
		new SysParam.Builder(this).setTitle(value).setBackButton(getResources().getString(R.string.Back), new View.OnClickListener(){
			public void onClick(View v){
				
				KeyEvent newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK);
				AppSimulateActivity.this.onKeyDown(KeyEvent.KEYCODE_BACK,newEvent);
			}
		}).create();
		myViewLFTemp = (TextView)findViewById(R.id.LFTempText);
		myViewLFPress = (TextView)findViewById(R.id.LFPressText);
		
		myViewRFTemp = (TextView)findViewById(R.id.RFTempText);
		myViewRFPress = (TextView)findViewById(R.id.RFPressText);
		
		myViewLBTemp = (TextView)findViewById(R.id.LBTempText);
		myViewLBPress = (TextView)findViewById(R.id.LBPressText);
		
		myViewRBTemp = (TextView)findViewById(R.id.RBTempText);
		myViewRBPress = (TextView)findViewById(R.id.RBPressText); 

		/*-----------------------------------------------------------------*/	
		myViewLFTempUnit = (TextView)findViewById(R.id.LFTempTitle);
		myViewLFPressUnit = (TextView)findViewById(R.id.LFPressTitle);
			
		myViewRFTempUnit = (TextView)findViewById(R.id.RFTempTitle);
		myViewRFPressUnit = (TextView)findViewById(R.id.RFPressTitle);
		
		myViewLBTempUnit = (TextView)findViewById(R.id.LBTempTitle);
		myViewLBPressUnit = (TextView)findViewById(R.id.LBPressTitle);
		
		myViewRBTempUnit = (TextView)findViewById(R.id.RBTempTitle);
		myViewRBPressUnit = (TextView)findViewById(R.id.RBPressTitle);
		
		WarnCenterImage = (ImageView)findViewById(R.id.WarnIconImage);
		
		contentRB = findViewById(R.id.contentRBValue);
		contentRF = findViewById(R.id.contentRFValue);
		contentLB = findViewById(R.id.contentLBValue);
		contentLF = findViewById(R.id.contentLFValue);
		
		contentRB.setVisibility(0x00);
		contentRF.setVisibility(0x00);
		contentLB.setVisibility(0x00);
		contentLF.setVisibility(0x00);
        new Thread(new Runnable(){
			@Override
				public void run() {
				valueLoad(value);
		}}).start(); 
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
    public void onStart() {
		TPMSParam.activityStart();	
		pm =(PowerManager)getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "TPMS");
		wakeLock.acquire();	
        super.onStart();
    }

	protected void onDestroy(){
		Log.i(TAG,"onDestroy");
		tpmsApp.mediaPlayerStop();
		TPMSParam.activityStop();	
		super.onDestroy();
	}
	protected void onStop(){
		
		isRunning = false;
		if(null != wakeLock){  
			Log.i(TAG,"release lock");
			wakeLock.release();  
			wakeLock = null;
		}  
		super.onStop();
	}
	@Override   
	 protected void onResume() {  
//		View linearLayout = findViewById(R.id.contentall);
//		Drawable dr = this.getResources().getDrawable(TPMSParam.iThemeTable[TPMSParam.iThemeIndex]); 
//		linearLayout.setBackgroundDrawable(dr);
//		linearLayout = findViewById(R.id.CardView);
//		dr = this.getResources().getDrawable(TPMSParam.iCardTable[TPMSParam.iCardIndex]); 
//		linearLayout.setBackgroundDrawable(dr);
		super.onResume();
	 } 
    // 当客户点击菜单中的某一个选项时，调用该方法，并将点击的选项通过参数传递进来
	class MyViewListenerCommon implements OnClickListener{
		public void onClick(View v){
			v.clearAnimation();
		}
	}
}
