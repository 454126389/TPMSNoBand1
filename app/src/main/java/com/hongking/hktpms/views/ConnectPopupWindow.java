package com.hongking.hktpms.views;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

public class ConnectPopupWindow extends PopupWindow {

	private Context context;
    private View rootView;


    public ConnectPopupWindow(Context context,int layoutId){
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        rootView = inflater.inflate(layoutId, null);
        init();
    }

    public void init(){
        setContentView(rootView);
        setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new BitmapDrawable());
        setOutsideTouchable(true);
    }
}
