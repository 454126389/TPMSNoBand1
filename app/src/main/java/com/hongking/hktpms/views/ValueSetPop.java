package com.hongking.hktpms.views;

import com.hongking.hktpms.SysApplication;
import com.hongking.hktpms.SysParam;
import com.hongking.oemtpms.R;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ValueSetPop extends PopupWindow {

	private final String TAG = "ValueSetPop";
	private Context myContext;

	private View rootView;
	private TextView title;
	private WheelView wheel1,wheel2,wheel3;
	int postion;

	public ValueSetPop(Context myContext) {
		this.myContext = myContext;
		LayoutInflater inflater = LayoutInflater.from(myContext);
		if(SysApplication.density==1.5){
			rootView = inflater.inflate(R.layout.pop_value_setden15, null);
		}else {
			rootView = inflater.inflate(R.layout.pop_value_set, null);
		}
		init();
	}
	
	private void init() {
		setContentView(rootView);
		setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setBackgroundDrawable(new BitmapDrawable());

		title = (TextView) rootView.findViewById(R.id.title);
		wheel1= (WheelView) rootView.findViewById(R.id.value_wheel1);
		wheel2= (WheelView) rootView.findViewById(R.id.value_wheel2);
		wheel3= (WheelView) rootView.findViewById(R.id.value_wheel3);
		
//		for (int i = 0; i < 10; i++) {
//			wheel3.addData(i + "");
//		}
//		wheel1.setCenterItem(4);
		rootView.findViewById(R.id.p_confirm).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				if(listener != null)listener.onValueSet(getValue());
			}
		});
		rootView.findViewById(R.id.p_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	private OnValueSetListener listener;
	public void setOnValueSetListenerr(OnValueSetListener listener){
		this.listener = listener;
	}
	
	public void maxValue(int max1,int max2,int max3){
		for (int i = 0; i < max1 + 1; i++) {
			wheel1.addData(i + "");
		}
		for (int i = 0; i < max2 + 1; i++) {
			wheel2.addData(i + "");
		}
		for (int i = 0; i < max3 + 1; i++) {
			wheel3.addData(i + "");
		}
	}
	
	public void setCurrentValue(int v1,int v2,int v3){
		wheel1.setCenterItem(v1 + "");
		wheel2.setCenterItem(v2 + "");
		wheel3.setCenterItem(v3 + "");
	}
	
	public void isShowPoint(boolean isShow1,boolean isShow2,boolean isShow3){
		wheel1.isShowPoint(isShow1);
		wheel2.isShowPoint(isShow2);
		wheel3.isShowPoint(isShow3);
	}
	
	public void setUnit(String unit1,String unit2,String unit3){
		wheel1.setUnit(unit1);
		wheel2.setUnit(unit2);
		wheel3.setUnit(unit3);
	}
	
	public int getValue(){
		String v1 = (String)wheel1.getCenterItem();
		String v2 = (String)wheel2.getCenterItem();
		String v3 = (String)wheel3.getCenterItem();
		
		int value = Integer.parseInt(v1)*100 + Integer.parseInt(v2)*10 + Integer.parseInt(v3);
		return value;
	}
	
	private int tempValue;
	private SysParam.DataInfo info;
	public void initData(SysParam.DataInfo info){
		this.info = info;
		if(info.baxMove >= 100){
			wheel1.isShowPoint(true);
		}else if(info.baxMove >= 10){
			wheel2.isShowPoint(true);
        }else{
		}
		
		wheel3.setUnit(info.unitName);
		
		tempValue = info.value;
		
		int temp = Math.abs(tempValue);
    	if(info.baxMax > 100 || info.baxMove >= 100){
    		int a = temp/100;
    		for (int i = 0; i < info.baxMax / 100 + 1; i++) {
				wheel1.addData(i + "");
			}
    		wheel1.setCenterItem(""+temp/100);
    	}else{
    		wheel1.addData("0");
    		wheel1.setCenterItem("0");
    	}
    	
    	for (int i = 0; i < 10; i++) {
    		 wheel2.addData(i + "");
    		 wheel3.addData(i + "");
		}
    	wheel2.setCenterItem(""+(temp/10)%10);
    	wheel3.setCenterItem(""+temp%10);
	}
	
	public void setTitle(String titleStr){
		if(titleStr != null){
			title.setText(titleStr);
		}
	}
	
	public interface OnValueSetListener{
		public void onValueSet(int value);
	}
	
	
}
