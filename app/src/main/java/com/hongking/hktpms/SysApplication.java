package com.hongking.hktpms;
/*
 * 包说明
 * 系统应用类
 * */
import java.util.Locale;


import com.hongking.hktpms.Utils.CH34xAndroidDriver;
import com.hongking.hktpms.views.EventBus;
import com.hongking.hktpms.views.EventBus_Media;
import com.hongking.oemtpms.R;
import com.hongking.hktpms.SysParam.Tire_Inf;
import com.tencent.bugly.crashreport.CrashReport;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;


public class SysApplication extends Application  implements MediaPlayer.OnCompletionListener{
	public static CH34xAndroidDriver driver;
	public static float density;//屏幕密度1.0或者1.5。
	public static boolean usbhandflag=true;//USB连接，握手。
	public static boolean movetasktobackflag=true;//是否出现回退到后台的提示框
	public static boolean heartflag=true;//播放心跳
	public static boolean timeflag=true;//用于避免后台播报语音的时候，判断两次前后台。
	public static int timeflag1;//用于避免后台播报语音的时候，判断两次前后台。
	public static int timeflag2;//用于避免后台播报语音的时候，判断两次前后台。
	public static boolean BluetoothState=false;//用于判断蓝牙的连接状态。
	public static boolean USBState=false;//用于判断USB的连接状态。
	public static int conneceactno=0;//用于判断选择界面的act数量。
	public static boolean restartflag=true;//用于判断是否是开机启动。
	SysParam TPMSParam ;
	final String TAG = "application";
	private int length;
	private int index;
	private int soundArray[];
	private boolean isWakeLock = false;
	public static boolean USBTHREADRUN = true;
	public static boolean BTTHREADRUN = true;
	public static boolean isforground = true;
	final Object lockpm = new Object();
	MediaPlayer mPlayer;
	PowerManager.WakeLock wakeLock;
	public final String REBOOT_SUCC = "REBOOT_DEVICE_SUCC";
	
	@Override
	public void onCreate() {
		Log.i(TAG, "[onCreate]");
		TPMSParam = ((SysParam)SysParam.getInstance(this));
		CrashReport.initCrashReport(getApplicationContext(), "b01d02bf8a", false);//bugly
	}

	@Override
	public void onTerminate(){
		mediaPlayerRelase();
	}
	void playMusic(int which ,int mode){
		Log.i(TAG,"play");
		int soundArray[] = new int[8];
		int index = 0;
		if(which == 0)/*第一轮胎开始报警时提示*/
		{
			soundArray[index++] = TPMSParam.iSoundTable[0];
		}
		soundArray[index++] = TPMSParam.iSoundTireType[0][which];
		soundArray[index++] = TPMSParam.iSoundWarnType[0][mode];
		soundArray[index++] = TPMSParam.iSoundPromptType[0][0];
		playSoundArray(soundArray,index);
	}
	void playMusicDuDuDu(){
		Log.i(TAG,"play");
		int soundArray[] = new int[8];
		int index = 0;
		soundArray[index++] = TPMSParam.iSoundTable[0];
		playSoundArray(soundArray,index);
	}
	synchronized void playMusicPend(int which ,int mode){

		Log.i(TAG,"play");
		int soundArray[] = new int[8];
		int index = 0;
		if(which == 0)/*第一轮胎开始报警时提示*/
		{
			soundArray[index++] = TPMSParam.iSoundTable[0];
		}
		soundArray[index++] = TPMSParam.iSoundTireType[0][which];
		soundArray[index++] = TPMSParam.iSoundWarnType[0][mode];
		soundArray[index++] = TPMSParam.iSoundPromptType[0][0];
		playSoundArray(soundArray,index);
		try {
			super.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	synchronized void playMusicDuDuDuPend(){

		Log.i(TAG,"play");
		int soundArray[] = new int[8];
		int index = 0;
		soundArray[index++] = TPMSParam.iSoundTable[0];

		playSoundArray(soundArray,index);
		try {
			super.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	synchronized void mediaPlayerStop(){
		mediaPlayerRelase();
		super.notify();
	}
	synchronized private void mediaPlayerRelase(){
		if(mPlayer != null){
			MediaPlayer temp = mPlayer;
			mPlayer = null;
			temp.stop();
			temp.release();
		}
	}
	@Override
	synchronized public void onCompletion(MediaPlayer mp) {
		mediaPlayerRelase();
		if(index < length){
			Log.i(TAG,"play completion");
			mPlayer = MediaPlayer.create(this,soundArray[index++]);//
			mPlayer.setOnCompletionListener(this);
			mPlayer.start();

		}else{
			de.greenrobot.event.EventBus.getDefault().post(new EventBus_Media(0));
			super.notify();
		}
	}
	private void playSoundArray(int soundArray[],int length){
		/*
		Resources resources = getResources();
		Configuration config = resources.getConfiguration();
		DisplayMetrics dm = resources.getDisplayMetrics();
		if(TPMSParam.iLanguageType == 1){
			
		}
		else if(TPMSParam.iLanguageType == 0){
			if(config.locale == Locale.SIMPLIFIED_CHINESE){
				
			}
			else{
				//return;
			}
		}
		else {
			return;
		}
		//*/
		mediaPlayerRelase();
		this.length = length;
		this.soundArray = soundArray;
		this.index = 0x00;
		mPlayer = MediaPlayer.create(this,soundArray[index++]);//
		mPlayer.setOnCompletionListener(this);
		mPlayer.start();
	}
	public void showNotification(int mode) {
		Notification notification ;
		CharSequence contentText;
		// 创建一个NotificationManager的引用
		if(TPMSParam.isOnExit )//销毁模式不显示
			return;
		NotificationManager notificationManager = (NotificationManager)
				this.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		// 定义Notification的各种属性
		if(mode == 0x01){
			notification = new Notification(R.drawable.ic_launcher2,
					getResources().getString(R.string.BackgroundAlarm), System.currentTimeMillis());
			notification.sound = Uri.parse("android.resource://" + getPackageName() + "/" +R.raw.warn);
			contentText  = getResources().getString(R.string.BackgroundAlarm);// 通知栏内容
			acquireWakeLock();
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		}else{
			notification = new Notification(R.drawable.ic_launcher1,
					null, System.currentTimeMillis());
			contentText  = this.getResources().getString(R.string.app_name);// 通知栏内容
			notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
			notification.flags |= Notification.FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		}
		// 设置通知的事件消息
		CharSequence contentTitle = this.getResources().getString(R.string.app_name); // 通知栏标题
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClass(this, AppEntryActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent contextIntent = PendingIntent.getActivity(this, 0, intent, 0);
		notification.setLatestEventInfo(this, contentTitle, contentText,contextIntent);
		// 把Notification传递给NotificationManager
		notificationManager.notify(0, notification);
	}
	private void acquireWakeLock(){
		synchronized(lockpm){
			if(!isWakeLock){
				PowerManager  pm = (PowerManager)getSystemService(POWER_SERVICE);
				wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.SCREEN_DIM_WAKE_LOCK,"SimpleTimer");
				wakeLock.acquire();
				isWakeLock = true;
				Log.i(TAG,"wakelock acquire");
			}else{}//already lock
		}
	}

	private void releaseWakeLock(){
		synchronized(lockpm){
			if(isWakeLock){
				wakeLock.release();
				isWakeLock = false;
				Log.i(TAG,"wakelock release");
			}else{}//already release
		}
	}
	public void cancelNotification(){
		//if(!isNotificationClear)
		{
			NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(0);
			releaseWakeLock();
		}
		//else
		//	Log.w(TAG,"notificationStatus alreadly cancel");
	}
	void WarnNotifation(Context context){
		int soundArray[] = new int[32];
		int index =0;
		int beepflage = 0;
		int voic_ok;
		Tire_Inf info;

		Resources resources = getResources();
		Configuration config = resources.getConfiguration();
		DisplayMetrics dm = resources.getDisplayMetrics();
		if(TPMSParam.iLanguageType == 1){
			voic_ok = 1;
		}
		else if(TPMSParam.iLanguageType == 0){
			if(Locale.getDefault().getLanguage().equals("zh")){
				voic_ok = 1;
			}
			else{
				voic_ok = 0;
			}
		}
		else {
			voic_ok = 0;
		}
		if(voic_ok == 1){
			for(int i = 0;i < 4;i++){
				info = TPMSParam.tyreData.getInfoValue(i);
				//Log.i(TAG,"info.alarm_flag="+info.alarm_flag );
				if(!((info.alarm_flag & (1 << 6)) == (1 << 6)) )//获取报警标志
					continue;
				if(beepflage == 0)
				{
					beepflage = 1;
					soundArray[index++] = TPMSParam.iSoundTable[0];
				}
				if((info.supdate >= 0x00) &&  (info.alarm_flag > 0x00)){
					soundArray[index++] = TPMSParam.iSoundTireType[0][i];
					if((info.alarm_flag & 0x01)== 0x01)
						soundArray[index++] = TPMSParam.iSoundWarnType[0][0];
					else if((info.alarm_flag & 0x02) == 0x02)
						soundArray[index++] = TPMSParam.iSoundWarnType[0][1];
					if((info.alarm_flag & 0x10) == 0x10)
						soundArray[index++] = TPMSParam.iSoundWarnType[0][3];

					if((info.alarm_flag & 0x04) == 0x04)
						soundArray[index++] = TPMSParam.iSoundWarnType[0][2];
					if((info.alarm_flag & 0x08) == 0x08)
						soundArray[index++] = TPMSParam.iSoundWarnType[0][4];
					if((info.alarm_flag & 0x20) == 0x20)
						soundArray[index++] = TPMSParam.iSoundWarnType[0][5];

					soundArray[index++] = TPMSParam.iSoundPromptType[0][0];/*添加澹:请及时处理*/
				}
			}
		}else{
			for(int i = 0;i < 4;i++){
				info = TPMSParam.tyreData.getInfoValue(i);
				//Log.i(TAG,"info.alarm_flag="+info.alarm_flag );
				if(!((info.alarm_flag & (1 << 6)) == (1 << 6)) )//获取报警标志
					continue;
				if(beepflage == 0)
				{
					beepflage = 1;
					soundArray[index++] = TPMSParam.iSoundTable[0];
				}
			}
		}
		if(index > 0x00){
			if(TPMSParam.activityStatusGet() == 0x00){
				cancelNotification();
//				showNotification(0x01);

			}else{
				for(int i = 0;i < 4;i++){
					TPMSParam.tyreData.setArlamFlagMask(i,6);
				}
				playSoundArray(soundArray,index);
			}
		}
	}
	synchronized public void changeMode(byte mode){
		final  byte _mode = mode;

		Log.e("mode", "mode="+mode);

		new Thread(new Runnable(){
			@Override
				public void run() {
				int ret;
				byte times = 1;
				while(times-- >0x00){
				ret = TPMSParam.mBluethoothData.ProtocolModeChange(_mode);//切换系统模式
				if(ret == 0x00){
					if(_mode == SysParam.SYS_MODE.MODE_REBOOT){
						sendBroadcast(new Intent(REBOOT_SUCC));
					}
					break;
				}
			}
		}}).start();   
	}
}
