package com.hongking.hktpms;

/*
 * 包说明
 * 初始化页面Activity
 * */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hongking.oemtpms.R;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SetupActivity extends Activity implements OnClickListener {

	private static final String TAG = "BluetoohtSetupActivity";
	public static final String INCOMING_MSG = "INCOMING_MSG";
	public static final String OUTGOING_MSG = "OUTGOING_MSG";

	public static final String DEVICE_ADDRESS = "device_address";
	public static final String DISCONNECT_DEVICE_ADDRESS = "disconnected_device_address";
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_DISCONNECT_DEVICE = 4;
	private static final int REQUEST_RECONNECT_DEVICE = 5;

	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final String TOAST = "toast";
	private static final boolean D = true;

	private boolean isRequest;
	private List<Map<String, Object>> mData;
	private ListView listView;
	private TextView myTextView1 = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	private Dialog DialogWait = null;
	private NotificationReceiver mBtMsgReceiver;
	private SysParam TPMSParam;
	private SysApplication app;
	private Dialog errDialog;
	private int connectMode = 0x00;
	private int requestTimes = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "[onCreate]");
		super.onCreate(savedInstanceState);
		TPMSParam = ((SysParam) SysParam.getInstance(this));
		app = (SysApplication) getApplicationContext();
		SysParam.getInstance(this).addActivity(this);
		setContentView(R.layout.setting);
		new SysParam.Builder(this).setTitle(getResources().getString(R.string.Sysinit)).create();
		myTextView1 = (TextView) findViewById(R.id.DebugViewLine1);
		// 关闭主题 TODO
		// View linearLayout = findViewById(R.id.content);
		// Log.i(TAG,"theme id "+TPMSParam.iThemeIndex);
		// Drawable dr =
		// this.getResources().getDrawable(TPMSParam.iThemeTable[TPMSParam.iThemeIndex]);
		// linearLayout.setBackgroundDrawable(dr);
		myTextView1.setText(getResources().getString(R.string.Sysinit));
		listView = (ListView) findViewById(R.id.ListView01);
		mData = getData();
		SysListviewbase myAdapter = new SysListviewbase(this, mData,1);
		listView.setAdapter(myAdapter);
		listView.setOnItemClickListener(new MyListViewListenerCommon());
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		setupService();
		IntentFilter listenBluethoothOn = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		IntentFilter stateConnectFilter = new IntentFilter(SysControllerService.CONNECT_MESSAGE_ACTION);
		IntentFilter stateDisConnectFilter = new IntentFilter(SysControllerService.DISCONNECT_MESSAGE_ACTION);
		IntentFilter autoConnectFilter = new IntentFilter(SysControllerService.SERVICE_REPORT);
		mBtMsgReceiver = new NotificationReceiver();
		registerReceiver(mBtMsgReceiver, listenBluethoothOn);
		registerReceiver(mBtMsgReceiver, stateConnectFilter);
		registerReceiver(mBtMsgReceiver, stateDisConnectFilter);
		registerReceiver(mBtMsgReceiver, autoConnectFilter);
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		// 顶出空行
		map.put("title", " ");
		map.put("flag", "tag");
		list.add(map);
		// 蓝牙
		map = new HashMap<String, Object>();
		map.put("title", getResources().getString(R.string.Bluetooth));
		map.put("flag", "tag");
		list.add(map);
		// 蓝牙连接
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_blue);
		map.put("title", getResources().getString(R.string.BluetoothConnection));
		map.put("img", R.drawable.card_arrow);
		map.put("flag", "middle");
		list.add(map);
		// 蓝牙设置
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_setting);
		map.put("title", getResources().getString(R.string.BluetoothSettings));
		map.put("img", R.drawable.card_arrow);
		map.put("flag", "middle");
		list.add(map);
		// 顶出空行
		map = new HashMap<String, Object>();
		map.put("title", " ");
		map.put("flag", "tag");
		list.add(map);

		// 顶出空行
		map = new HashMap<String, Object>();
		map.put("title", " ");
		map.put("flag", "tag");
		list.add(map);
		// 模拟体验
		map = new HashMap<String, Object>();
		map.put("title", getResources().getString(R.string.SimulationExperience));
		map.put("flag", "tag");
		list.add(map);

		// 慢漏气
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_warn_leakage);
		map.put("title", getResources().getString(R.string.SolwLeakage));
		map.put("img", R.drawable.card_arrow);
		map.put("flag", "middle");
		list.add(map);
		// 气压高
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_tyre_high);
		map.put("title", getResources().getString(R.string.HighPressure));
		map.put("img", R.drawable.card_arrow);
		map.put("flag", "middle");
		list.add(map);
		// 气压低
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_tyre_low);
		map.put("title", getResources().getString(R.string.LowPressure));
		map.put("img", R.drawable.card_arrow);
		map.put("flag", "middle");
		list.add(map);
		// 温度高
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_warn_thigh);
		map.put("title", getResources().getString(R.string.HighTemp));
		map.put("img", R.drawable.card_arrow);
		map.put("flag", "middle");
		list.add(map);

		// 历史数据查看
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_record);
		map.put("title", getResources().getString(R.string.ViewHistoricaldata));
		map.put("img", R.drawable.card_arrow);
		map.put("flag", "middle");
		list.add(map);

		// map = new HashMap<String, Object>();
		// map.put("imgTitle", R.drawable.icon_record);
		// map.put("title", " ");
		// map.put("img", R.drawable.card_arrow);
		// map.put("info", TPMSParam.tyreData.getPressCapacityWarn().Display+"
		// "+TPMSParam.tyreData.getPressCapacityWarn().unitName);
		// map.put("flag","middle");
		// list.add(map);
		// 顶出空行
		map = new HashMap<String, Object>();
		map.put("title", " ");
		map.put("flag", "tag");
		list.add(map);
		return list;
	}

	@Override
	public void onStart() {
		Log.i(TAG, "[onStart]");
		if (true == TPMSParam.isOnExit) {
			this.finish();
		}
		requestTimes = 0;
		TPMSParam.activityStart();
		super.onStart();
	}

	@Override
	public void onPause() {
		Log.i(TAG, "[onPause]");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.i(TAG, "[onStop]");
		TPMSParam.activityStop();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "[onDestroy]");
		app.mediaPlayerStop();
		if (mBtMsgReceiver != null)
			unregisterReceiver(mBtMsgReceiver);
		stopService();
		if (mBluetoothAdapter.isEnabled()) {
		}
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		Log.e(TAG, "[onRestart]");
		if (true == TPMSParam.isOnExit) {
			this.finish();
		}
		// TODOAuto-generated method stub
		super.onRestart();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 按下键盘上返回按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return moveTaskToBack(true);
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	// 当客户点击菜单中的某一个选项时，调用该方法，并将点击的选项通过参数传递进来
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == 1) {
			SysParam.getInstance(this).exit(this);
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void stopService() {
		Intent intent = new Intent(SysControllerService.STOP_SERVICE);
		Log.i(TAG, "request stop servie");
		sendBroadcast(intent);
	}

	private void setupService() {
		Log.d(TAG, "setupService()");
		Intent startIntent = new Intent(SetupActivity.this, SysControllerService.class);
		startService(startIntent);
		Intent startIntent1 = new Intent(SetupActivity.this, SysManagerService.class);
		startService(startIntent1);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (DialogWait != null) {
				DialogWait.dismiss();
				DialogWait = null;
			}
			if (msg.arg1 == 0x00) {
				Toast.makeText(SetupActivity.this, getResources().getString(R.string.BluetoothConnectionOK),
						Toast.LENGTH_SHORT).show();
				Intent mainIntent = new Intent(SetupActivity.this, AppMainActivity.class);
				SetupActivity.this.startActivityForResult(mainIntent, REQUEST_RECONNECT_DEVICE);
			} else {
				if (errDialog != null) {
					errDialog.dismiss();
					errDialog = null;
				}
				stopConnect(TPMSParam.sConnectedBlueAddr);
			}
		}
	};

	public Dialog onCreateDialog(int dialogId, String msg) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = null;
		boolean mode = true;
		;
		SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(this);
		Dialog dialog = null;
		switch (dialogId) {
		case 0x00:
			view = inflater.inflate(R.layout.set_wait_dialog1, null);
			mode = false;
			customBuilder.setMessage(null).setNegativeButton(getResources().getString(R.string.Cancel), this);
			break;
		case 0x01:
			view = inflater.inflate(R.layout.set_simple_dialog, null);
			customBuilder.setMessage(getResources().getString(R.string.BluetoothConnectionFailue) + msg)
					.setPositiveButton(getResources().getString(R.string.confirm), this);
			break;
		}
		dialog = customBuilder.create(view, mode);
		dialog.show();
		return dialog;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == -1) {

		} else {
			Log.v("keydebug", "which=" + which);
			if (DialogWait != null) {
				DialogWait.dismiss();
				DialogWait = null;
			}
			connectMode = 0x00;// 恢复用户模式
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(BlueSetupActivity.EXTRA_DEVICE_ADDRESS);

				startConnect(address);
			}
			break;
		case REQUEST_DISCONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {

			}
			break;
		case REQUEST_RECONNECT_DEVICE: {

		}
			break;
		default:
			break;
		}
	}

	void setupConnect(int mode) {
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		connectMode = mode;
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getAddress().equals(TPMSParam.sConnectedBlueAddr)) {
					startConnect(TPMSParam.sConnectedBlueAddr);
					return;
				}
			}
		}
		if (mode == 0) {
			Intent serverIntent = new Intent(SetupActivity.this, BlueSetupActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		} else {

		}
	}

	void startConnect(String address) {
		Log.i(TAG, "start connect addr ..." + address);
		Intent i = new Intent(SysControllerService.CONNECT_REQUEST_ACTION);
		i.putExtra(DEVICE_ADDRESS, address);
		sendBroadcast(i);
		DialogWait = onCreateDialog(0x00, "");
	}

	void stopConnect(String address) {
		Intent i = new Intent(SysControllerService.DISCONNECT_REQUEST_ACTION);
		i.putExtra(DISCONNECT_DEVICE_ADDRESS, address);
		sendBroadcast(i);
	}

	// TODO
	class MyListViewListenerCommon implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			if (mData.get(arg2).get("title").equals(getResources().getString(R.string.BluetoothConnection))) {
				if (!mBluetoothAdapter.isEnabled()) {
//					mBluetoothAdapter.enable();
					isRequest = true;
					return;
				} else {
					setupConnect(0x01);
				}
			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.BluetoothSettings))) {
				Intent intent = new Intent(SetupActivity.this, BlueSetupActivity.class);
				startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.SolwLeakage))) {
				Intent mainIntent = new Intent(SetupActivity.this, AppSimulateActivity.class);
				mainIntent.putExtra("SimulateMode", getResources().getString(R.string.SolwLeakageSimulation));
				SetupActivity.this.startActivity(mainIntent);

			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.HighPressure))) {
				Intent mainIntent = new Intent(SetupActivity.this, AppSimulateActivity.class);
				mainIntent.putExtra("SimulateMode", getResources().getString(R.string.HighPressureSimulation));
				SetupActivity.this.startActivity(mainIntent);

			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.LowPressure))) {
				Intent mainIntent = new Intent(SetupActivity.this, AppSimulateActivity.class);
				mainIntent.putExtra("SimulateMode", getResources().getString(R.string.LowPressureSimulation));
				SetupActivity.this.startActivity(mainIntent);

			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.HighTemp))) {
				Intent mainIntent = new Intent(SetupActivity.this, AppSimulateActivity.class);
				mainIntent.putExtra("SimulateMode", getResources().getString(R.string.HighTempSimulation));
				SetupActivity.this.startActivity(mainIntent);

			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.ViewHistoricaldata))) {
				Intent mainIntent = new Intent(SetupActivity.this, AppSimulateActivity.class);
				mainIntent.putExtra("SimulateMode", getResources().getString(R.string.ViewHistoricaldataTitle));
				SetupActivity.this.startActivity(mainIntent);
			}
			/*
			 * else if(mData.get(arg2).get("title").equals(" ")){ Intent
			 * mainIntent1 = new Intent(SetupActivity.this,
			 * AppMainActivity.class);
			 * SetupActivity.this.startActivity(mainIntent1); }
			 */
			else {
			}
		}
	}

	public class NotificationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (D)
				Log.e(TAG, "** ON RECEIVE **");
			String action = intent.getAction();
			if (action.equals(INCOMING_MSG) || action.equals(OUTGOING_MSG)) {
				String displayMsg = intent.getExtras().getString("STR");
				Log.i(TAG, displayMsg);
			} else if (action.equals(SysControllerService.CONNECT_MESSAGE_ACTION)) {
				String displayMsg = intent.getExtras().getString("STR");
				Log.i(TAG, displayMsg + "has connect");
				Message msg = Message.obtain();
				msg.arg1 = intent.getExtras().getInt("ARG");
				mHandler.sendMessage(msg);
			} else if (action.equals(SysControllerService.DISCONNECT_MESSAGE_ACTION)) {
				String displayMsg = intent.getExtras().getString("STR");
				Log.i(TAG, displayMsg + " can't connect");

				if (DialogWait != null) {
					if (connectMode == 0x01) {
						if (requestTimes++ > 3) {
							requestTimes = 0;
							Toast.makeText(SetupActivity.this, getResources().getString(R.string.TrialResetSystem),
									Toast.LENGTH_SHORT).show();

							mBluetoothAdapter.disable();
							isRequest = true;
							DialogWait.dismiss();
							DialogWait = null;
						} else {
							Intent i = new Intent(SysControllerService.CONNECT_REQUEST_ACTION);
							i.putExtra(DEVICE_ADDRESS, TPMSParam.sConnectedBlueAddr);
							sendBroadcast(i);
						}
					}
				}
			} else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				Log.i(TAG, "[onReceive] ACTION_STATE_CHANGED");
				int currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				switch (currentState) {
				case BluetoothAdapter.STATE_ON:
					Log.i(TAG, "[onReceive] current state = ON");
					if (isRequest) {
						setupConnect(0x01);
						isRequest = false;
					}
					break;
				case BluetoothAdapter.STATE_OFF:
					if (isRequest) {
//						mBluetoothAdapter.enable();
						return;
					}
					Log.i(TAG, "[onReceive] current state = OFF");
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					Log.i(TAG, "[onReceive] current state = TURNING_ON");
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					Log.i(TAG, "[onReceive] current state = TURNING_OFF");
					break;
				}
			} else if (action.equals(SysControllerService.SERVICE_REPORT)) {

			} else {
				Log.e(TAG, "another action: " + action);
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.i(TAG, "onSaveInstanceState");
		TPMSParam.saveParam(this);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle outState) {
		Log.i(TAG, "onRestoreInstanceState");
		TPMSParam.ParamLoad(this);
		super.onRestoreInstanceState(outState);
	}
}