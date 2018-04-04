package com.hongking.hktpms.views;

import com.hongking.hktpms.SysApplication;
import com.hongking.oemtpms.R;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class PressureUnitSetPop extends PopupWindow {

	private final String TAG = "PressureUnitSetPop";
	private Context myContext;

	private View rootView;

	int postion;

	public PressureUnitSetPop(Context myContext) {
		this.myContext = myContext;
		LayoutInflater inflater = LayoutInflater.from(myContext);
		if(SysApplication.density==1.5){
			rootView = inflater.inflate(R.layout.pop_pressureden15, null);
		}else {
			rootView = inflater.inflate(R.layout.pop_pressure, null);
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
				case R.id.p_bar:
					postion = 0;
					break;
				case R.id.p_psi:
					postion = 1;
					break;
				case R.id.p_kpa:
					postion = 2;
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
	
	public interface OnCheckedListener{
		public void onChecked(int positon);
	}
	
	public void initPosition(int p){
		if(p < group.getChildCount()){
			this.postion = p;
			((RadioButton)group.getChildAt(p)).setChecked(true);
		}
	}

}
