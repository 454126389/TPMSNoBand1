package com.hongking.hktpms.views;

import com.hongking.hktpms.SysApplication;
import com.hongking.hktpms.views.PressureUnitSetPop.OnCheckedListener;
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
import android.widget.RadioGroup.OnCheckedChangeListener;

public class TemperatureUnitSetPop extends PopupWindow {

	private final String TAG = "TemperatureUnitSetPop";
	private Context myContext;

	private View rootView;

	int postion;

	public TemperatureUnitSetPop(Context myContext) {
		this.myContext = myContext;
		LayoutInflater inflater = LayoutInflater.from(myContext);



		if(SysApplication.density==1.5){
			rootView = inflater.inflate(R.layout.pop_temperatureden15, null);
		}else {
			rootView = inflater.inflate(R.layout.pop_temperature, null);
		}
		init();
	}
	
	RadioGroup group;

	private void init() {
		setContentView(rootView);
		setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setBackgroundDrawable(new BitmapDrawable());

		group = (RadioGroup) rootView.findViewById(R.id.p_group);

		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.t_c:
					postion = 0;
					break;
				case R.id.t_f:
					postion = 1;
					break;
				}
			}
		});
		
		rootView.findViewById(R.id.p_confirm).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(listener != null)listener.onChecked(postion);
				dismiss();
			}
		});
		rootView.findViewById(R.id.p_cancel).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	private OnCheckedListener listener;
	public void setOnCheckedListenerr(OnCheckedListener listener){
		this.listener = listener;
	}
	
	public void initPosition(int p){
		if(p < group.getChildCount()){
			this.postion = p;
			((RadioButton)group.getChildAt(p)).setChecked(true);
		}
	}

}
