package com.hongking.hktpms.views;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hongking.hktpms.SysApplication;
import com.hongking.oemtpms.R;

public class TipPop2 extends PopupWindow {

	private final String TAG = "TipPop";
	private Context myContext;

	private View rootView;
	private TextView content;
	
	public TipPop2(Context myContext) {
		this.myContext = myContext;
		LayoutInflater inflater = LayoutInflater.from(myContext);

		if(SysApplication.density==1.5){
			rootView = inflater.inflate(R.layout.pop_tip2den15,
					null);
		}else {
			rootView = inflater.inflate(R.layout.pop_tip2, null);
		}

		init();
	}
	

	private void init() {
		setContentView(rootView);
		setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setBackgroundDrawable(new BitmapDrawable());
		
		content = (TextView) rootView.findViewById(R.id.content);

		ImageView close = (ImageView)rootView.findViewById(R.id.close);
		close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
	}
	
	
	public void setContent(String string){
		if(string != null) content.setText(string);
	}
	
}
