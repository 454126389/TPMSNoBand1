package com.hongking.hktpms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class SysActivity extends Activity {
	public static final String AUTODESTORY = "AutoDestory";
	private SysBoardcast mMsgReceiver; 
	private IntentFilter DisconnectFilter;
	String TAG="SysActivity";
	@Override  
	protected void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);
	}
	@Override
	public void onDestroy() {
		Log.e(TAG, "[onDestroy]");
		super.onDestroy();
		if(mMsgReceiver != null)
			unregisterReceiver(mMsgReceiver);
	}
	void AutoDestroryListen(){
		mMsgReceiver = new SysBoardcast();
		DisconnectFilter = new IntentFilter(AUTODESTORY);
		registerReceiver(mMsgReceiver, DisconnectFilter);			
	}
	public class SysBoardcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action  = intent.getAction();
			if (action.equals(AUTODESTORY)){
				SysActivity.this.finish();		
			}
			else{
				Log.e(TAG, "another action: " + action);
			}
		}
	}
}

