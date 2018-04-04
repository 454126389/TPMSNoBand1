package com.hongking.hktpms;
/*
 * 包说明
 * 版权信息Activity
 * */
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import com.hongking.oemtpms.R;
import com.hongking.hktpms.SysParam;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class AboutMeRightInfoActivity extends SysActivity   {	
	private SysParam TPMSParam;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TPMSParam = ((SysParam)SysParam.getInstance(this));
		SysParam.getInstance(this).addActivity(this);
		setContentView(R.layout.aboutme_rightinfo);    
		View linearLayout = findViewById(R.id.content);
		Drawable dr = this.getResources().getDrawable(TPMSParam.iThemeTable[TPMSParam.iThemeIndex]); 
		linearLayout.setBackgroundDrawable(dr);
		
		String info = this.getResources().getString(R.string.rightInfo);
		info += "\n" + getResources().getString(R.string.AppVersion) + getVersion();
		info +="\n\n" + getResources().getString(R.string.ReceiverInformation);
		info += "\n" + getResources().getString(R.string.ModuleName) + TPMSParam.M_name;
		info += String.format("\n" + getResources().getString(R.string.MSVersion) + "V%x.%x", (TPMSParam.M_version.version >> 4) & 0x0f,TPMSParam.M_version.version & 0x0f);
		info += String.format("\n" + getResources().getString(R.string.BuildTimer) + "%02x:%02x", 
								
				(byte)(TPMSParam.M_version.ubuildtime>>8),
				(byte)(TPMSParam.M_version.ubuildtime>>0));
		info += String.format("\n" + getResources().getString(R.string.BuildDate) + "20%x-%x-%x", 
				(byte)(TPMSParam.M_version.builddate>>24),
				(byte)(TPMSParam.M_version.builddate>>16),
				(byte)(TPMSParam.M_version.builddate>>8));
				
		info +=  String.format("\n" + getResources().getString(R.string.MHVersion) + "20%x-%x-%x-V1.0",(byte)(TPMSParam.M_version.hard>>24),
				(byte)(TPMSParam.M_version.hard>>16),
				(byte)(TPMSParam.M_version.hard>>8));
		info += String.format("\n" + getResources().getString(R.string.ProVersion) + "V%d.%d", (byte)((TPMSParam.M_version.procotrol >> 4) & 0x0f),(byte)(TPMSParam.M_version.procotrol & 0x0f));
		TextView  myTextview = (TextView)findViewById(R.id.DebugViewLine1);
		myTextview.setText(info);
		new SysParam.Builder(this).setTitle(getResources().getString(R.string.CopyrightInformation)).setBackButton(getResources().getString(R.string.StringAboutMe), new View.OnClickListener(){
			public void onClick(View v){
				
				KeyEvent newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK);
				AboutMeRightInfoActivity.this.onKeyDown(KeyEvent.KEYCODE_BACK,newEvent);
			}
		}).create();
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
	public void onDestroy() {
		Log.e("rightInfo", "[onDestroy]");
		super.onDestroy();		
	}
	@Override  
	 public boolean onKeyDown(int keyCode, KeyEvent event) {          
		 if (keyCode == KeyEvent.KEYCODE_BACK) {   
			 finish();
			 return true;   
		  }  
		  return super.onKeyDown(keyCode, event);  
	 }
	
	private String getVersion() { 
		try { 
			PackageManager manager = this.getPackageManager(); 
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0); 
			String version = info.versionName; 
			return  version+this.getString(R.string.version_name); 
		} catch (Exception e) { 
			e.printStackTrace(); 
			return null; 
		} 
	}
	 String getUpdateTime() { 
		try { 
			PackageManager manager = this.getPackageManager(); 
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0); 
			//String version = info.versionName; 
			long time = info.lastUpdateTime;
			//String timeString = DateFormat.
			return ("" + time); 
		} catch (Exception e) { 
			e.printStackTrace(); 
			return null; 
		} 
	}
}
