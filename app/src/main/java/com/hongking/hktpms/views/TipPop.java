package com.hongking.hktpms.views;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hongking.oemtpms.R;

public class TipPop extends PopupWindow {

	private final String TAG = "TipPop";
	private Context myContext;

	private View rootView;
	private TextView title,content;
	private Button confirm,cancel;
	
	public TipPop(Context myContext) {
		this.myContext = myContext;
		LayoutInflater inflater = LayoutInflater.from(myContext);
		rootView = inflater.inflate(R.layout.pop_tip, null);
		init();
	}
	

	private void init() {
		setContentView(rootView);
		setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		setFocusable(true);
		setBackgroundDrawable(new BitmapDrawable());
		
		title = (TextView) rootView.findViewById(R.id.title);
		content = (TextView) rootView.findViewById(R.id.content);

		confirm = (Button)rootView.findViewById(R.id.p_confirm);
		confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(listener != null) listener.onConfirmClicked();
				dismiss();
			}
		});
		
		cancel = (Button)rootView.findViewById(R.id.p_cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(listener != null) listener.onCancelClicked();
				dismiss();
			}
		});
	}
	
	public void setTile(String string){
		if(string != null) title.setText(string);
	}
	
	public void setContent(String string){
		if(string != null) content.setText(string);
	}
	
	private OnTipBtnListener listener;
	public void setOnTipBtnListener(OnTipBtnListener listener){
		this.listener = listener;
	}
	
	public void setBtnVisibility(boolean isShowConfirm,boolean isShowCancel){
		if(!isShowConfirm){
			confirm.setVisibility(View.GONE);
		}
		if(!isShowCancel){
			cancel.setVisibility(View.GONE);
		}
	}
	
	public interface OnTipBtnListener{
		public void onConfirmClicked();
		public void onCancelClicked();
	}
	

}
