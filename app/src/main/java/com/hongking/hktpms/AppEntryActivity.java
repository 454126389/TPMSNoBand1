package com.hongking.hktpms;
/*
 * 包说明
 * 软件启动画面Activity
 * */
import java.util.Locale;


import com.hongking.oemtpms.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
public class AppEntryActivity extends Activity {    
	private static final String TAG = "APPEntry";
    private SysParam TPMSParam ;
    private BluetoothAdapter mBluetoothAdapter = null;
    
    private NotificationReceiver mBtMsgReceiver = null;    
	/**     * Called when the activity is first created.     */    
    private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
//			Intent mainIntent = new Intent(AppEntryActivity.this, SetupActivity.class);
			Intent mainIntent = new Intent(AppEntryActivity.this, AppMainActivity.class);
			AppEntryActivity.this.startActivity(mainIntent);
			AppEntryActivity.this.finish();				
		}
		
	};
	protected void ssitchlanguage(int language){
		Resources resources = getResources();
		Configuration config = resources.getConfiguration();
		DisplayMetrics dm = resources.getDisplayMetrics();
		if(language == 0){	/*系统默认*/
			config.locale = config.locale;
		}
		else if(language == 1){	/*简体中文*/
			config.locale = Locale.SIMPLIFIED_CHINESE;
			resources.updateConfiguration(config, dm);
		}
		else if(language == 2){ /*繁体中文*/
			config.locale = Locale.ENGLISH;
			resources.updateConfiguration(config, dm);
		}

	}	
	@Override    
	public void onCreate(Bundle icicle) 
	{       
		super.onCreate(icicle);      
		TPMSParam = ((SysParam)SysParam.getInstance(this));
		int flag = getIntent().getIntExtra("flag", 0);  
		if(flag == ((SysParam)SysParam.getInstance(this)).EXIT_APPLICATION){  
			Log.e(TAG,"exit");
           finish();  
           System.exit(0);
           return;
		}  
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		setContentView(R.layout.app_entry_view);
		String info = getVersion();
		
		
		TextView  myTextview = (TextView)findViewById(R.id.versionNumber);
		myTextview.setText(info);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//TODO
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
		if (!mBluetoothAdapter.isEnabled()) {//蓝牙未开启
//			mBluetoothAdapter.enable();
			return;
			/*IntentFilter stateChangedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	        mBtMsgReceiver = new NotificationReceiver();
	        registerReceiver(mBtMsgReceiver, stateChangedFilter);*/
        }else{
        	new Thread(new Runnable(){
				@Override
					public void run() {
					/*------------------------------*/
						try { 
							Thread.currentThread();
							Thread.sleep(2000);//延时多久后自动超时退出
						} 
						catch (InterruptedException e)
						{ 
							e.printStackTrace();
						}
						ParamLoad();
						ssitchlanguage(TPMSParam.iLanguageType);
			}}).start();  
        }
		new Thread(new Runnable(){
			@Override
				public void run() {
				    ParamLoad();
				
		}}).start();  
		
	}
	
	private String getVersion() { 
		try { 
			PackageManager manager = this.getPackageManager(); 
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0); 
			String version = info.versionName; 
			return version + this.getString(R.string.version_name) ; 
		} catch (Exception e) { 
			e.printStackTrace(); 
			return null; 
		} 
	}
	@Override
	public void onDestroy() {
		Log.e(TAG, "[onDestroy]");
		super.onDestroy();
		if(mBtMsgReceiver != null)unregisterReceiver(mBtMsgReceiver);
		
	}
	private void ParamLoad(){
   		TPMSParam.ParamLoad(this);
   		TPMSParam.blueStatus.ConnectingSet(false);
   		TPMSParam.isOnExit = false;
		try { 
			Thread.currentThread();
			Thread.sleep(2);//延时多久后自动超时退出
		} 
		catch (InterruptedException e)
		{ 
			e.printStackTrace();
		}
		mHandler.sendEmptyMessage(0);
	}
	
	/**
	 * 监听蓝牙状态
	 * @author yuhg
	 */
	public class NotificationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action  = intent.getAction();
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
				Log.i(TAG, "[onReceive] ACTION_STATE_CHANGED");
				int currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				switch (currentState){
				case BluetoothAdapter.STATE_ON:
					Log.i(TAG, "[onReceive] current state = ON");
					new Thread(new Runnable(){
						@Override
							public void run() {
							    ParamLoad();
					}}).start();  
					
					break;
				case BluetoothAdapter.STATE_OFF:
					Log.i(TAG, "[onReceive] current state = OFF");
					
	        		break;
				case BluetoothAdapter.STATE_TURNING_ON:
					Log.i(TAG, "[onReceive] current state = TURNING_ON");
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					Log.i(TAG, "[onReceive] current state = TURNING_OFF");
					break;
				}			
			}else{
				Log.e(TAG, "another action: " + action);
			}
		}
    }
}