package com.hongking.hktpms;

/*
 * 包说明
 * 系统设置Activity
 * */
import com.hongking.hktpms.Utils.SharePreferenceUtil;
import com.hongking.oemtpms.R;
import com.hongking.hktpms.views.PressureUnitSetPop;
import com.hongking.hktpms.views.TemperatureUnitSetPop;
import com.hongking.hktpms.views.PressureUnitSetPop.OnCheckedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SetActivity extends BaseActivity {

	private int iUserSelect;
	private byte menuBase = 0;// 用来记录第一个有效菜单的起始值
	private List<Map<String, Object>> mData;
	private ListView listView;
	private TextView infoView;
	private TextView infoView1;
	private SysParam TPMSParam;
	private Dialog curDialog;
	private Dialog DialogWait = null;
	private EditText input = null;
	private ImageView bitmapView = null;
	private SysApplication app;
	private SharePreferenceUtil sp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TPMSParam = ((SysParam) SysParam.getInstance(this));
		app = (SysApplication) getApplicationContext();
		sp = new SharePreferenceUtil(this);
		SysParam.getInstance(this).addActivity(this);
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		float density = metric.density;
		if(density==1.5){
			setContentView(R.layout.settingden15);
		}else {
			setContentView(R.layout.setting);
		}
		// 标题栏
		// new
		// SysParam.Builder(this).setTitle(getResources().getString(R.string.StringSetting)).create();
		// View linearLayout = findViewById(R.id.content);
		// Drawable dr =
		// this.getResources().getDrawable(TPMSParam.iThemeTable[TPMSParam.iThemeIndex]);
		// linearLayout.setBackgroundDrawable(dr);

		new SysParam.Builder(this).setTitle(getResources().getString(R.string.StringSetting))
				.setBackButton(getResources().getString(R.string.Back), new View.OnClickListener() {
					public void onClick(View v) {
						// KeyEvent newEvent = new
						// KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK);
						// SetActivity.this.onKeyDown(KeyEvent.KEYCODE_BACK,newEvent);
						finish();
					}
				}).create();

		listView = (ListView) findViewById(R.id.ListView01);
		mData = getData();
		SysListviewbase myAdapter = new SysListviewbase(this, mData,density);
		listView.setAdapter(myAdapter);
		listView.setOnItemClickListener(new MyListViewListenerCommon());

		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				// OpenCopyMode();
				if (mData.get(position).get("title").equals(getResources().getString(R.string.RestoreFactorySetup)))
					onCreateDialog(0x02, getResources().getString(R.string.FactoryParameter));
				return false;
			}

		});
		
		receiver = new MsgReceiver();
	}

	private List<Map<String, Object>> getData() {

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();

		// int i = 0;
		// 空出一行
		// map.put("title", " ");
		// map.put("flag", "tag");
		// list.add(map);
		// menuBase++;
		// 设置分栏:功能设置
		// map = new HashMap<String, Object>();
		// map.put("title", getResources().getString(R.string.FeatureSetup));
		// //map.put("title", "Feature Setup");
		// map.put("flag","tag");
		// list.add(map);
		// menuBase++;

		// 压力单位设置
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_tyre_low);
		map.put("title", getResources().getString(R.string.PressureUnitSetup));
		map.put("info", TPMSParam.tyreData.sPressUnitSting[TPMSParam.tyreData.getPressUnit()]);
		map.put("flag", "middle");
		list.add(map);

		// 温度单位设置
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_warn_thigh);
		map.put("title", getResources().getString(R.string.TemperatureUnitSetup));
		map.put("info", TPMSParam.tyreData.sTempUnitSting[TPMSParam.tyreData.getTempUnit()]);
		map.put("flag", "middle");
		list.add(map);

		// 报警值设置
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_warn);
		map.put("title", getResources().getString(R.string.AlarmValueSetup));
		map.put("img", R.drawable.card_arrow);
		map.put("flag", "middle");
		list.add(map);

		// 传感器更换
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_tyre_change);
		map.put("title", getResources().getString(R.string.ReplaceTheTireSensor));
		map.put("img", R.drawable.card_arrow);
		map.put("flag", "middle");
		list.add(map);

		// 轮胎对调
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_tyre_sw);
		map.put("title", getResources().getString(R.string.TireSwap));
		map.put("img", R.drawable.card_arrow);
		map.put("flag", "bottom");
		list.add(map);

		// //顶出空行
		// map = new HashMap<String, Object>();
		// map.put("title", " ");
		// map.put("flag", "tag");
		// list.add(map);

		// //顶出空行
		// map = new HashMap<String, Object>();
		// map.put("title", " ");
		// map.put("flag", "tag");
		// list.add(map);

		// 系统设置
		// map = new HashMap<String, Object>();
		// map.put("title", getResources().getString(R.string.SystemSetup));
		// map.put("flag","tag");
		// list.add(map);

		// 界面风格
		// map = new HashMap<String, Object>();
		// map.put("imgTitle", R.drawable.icon_theme);
		// map.put("title",
		// getResources().getString(R.string.InterfaceStyleSetup));
		// map.put("flag","middle");
		// map.put("img", R.drawable.card_arrow);
		// list.add(map);

		// //屏幕长量
		// map = new HashMap<String, Object>();
		// map.put("imgTitle", R.drawable.icon_light);
		// map.put("title",
		// getResources().getString(R.string.ScreenAlwaysOnSetup));
		// map.put("flag","middle");
		// map.put("info",
		// getResources().getStringArray(R.array.SleepTimeSet)[TPMSParam.iSleepTime]);
		// list.add(map);

		// //系统语言设置
		// map = new HashMap<String, Object>();
		// map.put("imgTitle", R.drawable.icon_language);
		// map.put("title", getResources().getString(R.string.LanguageSetup));
		// map.put("flag","middle");
		// map.put("info",
		// getResources().getStringArray(R.array.LanguageSet)[TPMSParam.iLanguageType]);
		// list.add(map);

		// 恢复出厂设置
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_reset);
		map.put("title", getResources().getString(R.string.RestoreFactorySetup));
		map.put("flag", "middle");
		map.put("img", R.drawable.card_arrow);
		list.add(map);

		// 技术支持中兴屏蔽
		map = new HashMap<String, Object>();
		map.put("imgTitle", R.drawable.icon_support);
		map.put("title", getResources().getString(R.string.tech_support));
		map.put("flag", "middle");
		map.put("img", R.drawable.card_arrow);
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

		return list;
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// // 按下键盘上返回按钮
	//
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// //SysApplication.getInstance().exit(this);
	// return moveTaskToBack (true);
	// } else {
	// return super.onKeyDown(keyCode, event);
	// }
	// }
	@Override
	protected void onResume() {
		// View linearLayout = findViewById(R.id.content);
		// Drawable dr =
		// this.getResources().getDrawable(TPMSParam.iThemeTable[TPMSParam.iThemeIndex]);
		// linearLayout.setBackgroundDrawable(dr);
		IntentFilter filter = new IntentFilter(app.REBOOT_SUCC);
		registerReceiver(receiver, filter);
		super.onResume();
	}
	
	private MsgReceiver receiver;

	public void onPause() {
		super.onPause();
	}

	@Override
	public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
		return super.onWindowStartingActionMode(callback);
	}

	@Override
	public void onStart() {
		TPMSParam.activityStart();
			super.onStart();
	}

	@Override
	public void onStop() {
		Log.v("LOG ", "onStop is starting");
		TPMSParam.activityStop();
		if (curDialog != null)
			curDialog.dismiss();
		super.onStop();
	}

	public void onDestroy() {
		mHandler = null;
		super.onDestroy();
		if(receiver != null)unregisterReceiver(receiver);
	}

	// 当客户点击Menua按钮的时候，调用该方法
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
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

	public Dialog onCreateDialog(int dialogId, String msg) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = null;
		SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(this);
		Dialog dialog = null;
		boolean mode = true;
		if (dialogId == 0x01) {

			if(SysApplication.density==1.5){
				view = inflater.inflate(R.layout.set_simple_dialogden15, null);
			}else {
				view = inflater.inflate(R.layout.set_simple_dialog, null);
			}
			customBuilder.setMessage(getResources().getString(R.string.SetFailure) + "\n" + msg).setPositiveButton(
					getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					});
		} else if (dialogId == 0x02) {
			view = inflater.inflate(R.layout.set_password_dialog, null);
			input = (EditText) view.findViewById(R.id.inputcode);
			infoView = (TextView) view.findViewById(R.id.message);

			customBuilder.setMessage(msg).setNegativeButton(getResources().getString(R.string.Cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).setPositiveButton(getResources().getString(R.string.confirm),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									if (input.getText().toString().equals("27265937")) {
										dialog.dismiss();
										LayoutInflater inflater = LayoutInflater.from(SetActivity.this);
										View view = inflater.inflate(R.layout.set_datapicker_dialog, null);
										boolean dismiss = false;
										final SysDatapickerDialog.Builder customBuilder = new SysDatapickerDialog.Builder(
												SetActivity.this);
										;
										Dialog recordDialog = customBuilder.setMessage("RF射频调节")
												.setNegativeButton("退出", new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										}).setPositiveButton("修改", new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												// TODO Auto-generated method
												// stub
												SetRFCpValue((byte) customBuilder.TempValue);
											}
										}).create(view, TPMSParam.getRFCpInfo(), dismiss);
										recordDialog.show();
										curDialog = recordDialog;
									} else {

										infoView.setText("密码错误,请重新输入");
										input.setText("");
									}
								}
							});
			mode = false;
			// dismiss = false;//不自动消失

		} else if (dialogId == 0x03) {
			if(SysApplication.density==1.5){
				view = inflater.inflate(R.layout.set_custom_dialogden15, null);
			}else {
				view = inflater.inflate(R.layout.set_custom_dialog, null);
			}
			customBuilder.setMessage(this.getResources().getString(R.string.warnReset)).setPositiveButton(
					getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							SysApplication.BluetoothState=false;
							SysApplication.USBState=false;
							sp.setConnectType(0);
							TPMSParam.sConnectedBlueAddr="";
							app.changeMode(SysParam.SYS_MODE.MODE_REBOOT);
//							startActivity(new Intent(SetActivity.this,ConnectChooseActivity.class));
						}
					}).setNegativeButton(getResources().getString(R.string.Cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								}
							});
		} else if (dialogId == 0x04) {
			view = inflater.inflate(R.layout.set_wait_dialog1, null);
			mode = false;
			customBuilder.setMessage(null).setNegativeButton(getResources().getString(R.string.Cancel), null);
		} else if (dialogId == 0x05) {
			if(SysApplication.density==1.5){
				view = inflater.inflate(R.layout.set_simple_dialogden15, null);
			}else {
				view = inflater.inflate(R.layout.set_simple_dialog, null);
			}
			customBuilder.setMessage(getResources().getString(R.string.SetSuccess) + "\n" + msg).setPositiveButton(
					getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					});
		}
		dialog = customBuilder.create(view, mode);

		curDialog = dialog;
		dialog.show();
		// curDialog = dialog;
		return dialog;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			ListAdapter la = listView.getAdapter();
			if (mData.get(msg.arg1).get("title").equals(getResources().getString(R.string.PressureUnitSetup))) {
				if (msg.arg2 == 0x00) {
					HashMap<String, Object> map = (HashMap<String, Object>) mData.get(msg.arg1);
					map.put("info", TPMSParam.tyreData.sPressUnitSting[TPMSParam.tyreData.getPressUnit()]);
					((SysListviewbase) la).notifyDataSetChanged();// 通知系统，实时更新
				} else {
					onCreateDialog(0x01, String.format("%s",
							SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(SetActivity.this, msg.arg2)));
				}
			} else if (mData.get(msg.arg1).get("title")
					.equals(getResources().getString(R.string.TemperatureUnitSetup))) {
				if (msg.arg2 == 0x00) {
					HashMap<String, Object> map = (HashMap<String, Object>) mData.get(msg.arg1);
					map.put("info", TPMSParam.tyreData.sTempUnitSting[TPMSParam.tyreData.getTempUnit()]);
					((SysListviewbase) la).notifyDataSetChanged();// 通知系统，实时更新
				} else {
					onCreateDialog(0x01, String.format("%s",
							SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(SetActivity.this, msg.arg2)));
				}
			} else if (msg.obj.equals("RFCpset")) {
				if (DialogWait != null)
					DialogWait.dismiss();
				if (msg.arg2 == 0x00) {

				} else {
					onCreateDialog(0x01, String.format("%s",
							SysParam.TPMS_PROTOCOL_ERRCODE.errCodeExchange(SetActivity.this, msg.arg2)));
				}

			} else {
			}
		}

	};

	void SetParamCommand(final int which, final int value) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				int ret = 0x8b;
				Message msg = Message.obtain();
				if (mData.get(which + menuBase).get("title")
						.equals(getResources().getString(R.string.PressureUnitSetup))) {
					ret = TPMSParam.mBluethoothData.ProtocolSysParamSet(TPMSParam.mBluethoothData.SET_PRESS_UNIT,
							value);
				} else if (mData.get(which + menuBase).get("title")
						.equals(getResources().getString(R.string.TemperatureUnitSetup))) {
					ret = TPMSParam.mBluethoothData.ProtocolSysParamSet(TPMSParam.mBluethoothData.SET_TEMP_UNIT, value);
				} else {
				}
				msg.arg1 = which + menuBase;
				msg.arg2 = ret;
				if (mHandler != null)
					mHandler.sendMessage(msg);
				TPMSParam.mBluethoothData.ProtocolSysParamGet();// 获取参数
			}
		}).start();
	}

	void SetRFCpValue(final byte value) {
		DialogWait = onCreateDialog(0x04, "");
		DialogWait.setCancelable(false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				int ret = 0x8b;
				Message msg = Message.obtain();
				ret = TPMSParam.mBluethoothData.ProtocolRfCpSet(value);
				msg.arg2 = ret;
				msg.obj = "RFCpset";
				if (mHandler != null)
					mHandler.sendMessage(msg);
			}
		}).start();
	}

	protected void ssitchlanguage(int language) {
		Resources resources = getResources();
		Configuration config = resources.getConfiguration();
		DisplayMetrics dm = resources.getDisplayMetrics();
		if (language == 0) { /* 系统默认 */
			if (Locale.getDefault().getLanguage().equals("zh")) {
				config.locale = Locale.SIMPLIFIED_CHINESE;
			} else {
				config.locale = Locale.ENGLISH;
			}
		} else if (language == 1) { /* 简体中文 */
			config.locale = Locale.SIMPLIFIED_CHINESE;

		} else if (language == 2) { /* 繁体中文 */
			config.locale = Locale.ENGLISH;
		}
		resources.updateConfiguration(config, dm);
	}

	class MyListViewListenerCommon implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			final int iSelect;
			if (mData.get(arg2).get("title").equals(getResources().getString(R.string.PressureUnitSetup))) {


//				AlertDialog.Builder builder = new AlertDialog.Builder(SetActivity.this);
//				builder.setTitle(getResources().getString(R.string.PressureUnitSetup));
//				final String[] sex = {"男", "女", "未知性别"};
//				//    设置一个单项选择下拉框
//				/**
//				 * 第一个参数指定我们要显示的一组下拉单选框的数据集合
//				 * 第二个参数代表索引，指定默认哪一个单选框被勾选上，1表示默认'女' 会被勾选上
//				 * 第三个参数给每一个单选项绑定一个监听器
//				 */
//				builder.setSingleChoiceItems(sex, 1, new DialogInterface.OnClickListener()
//				{
//					@Override
//					public void onClick(DialogInterface dialog, int which)
//					{
//						Toast.makeText(SetActivity.this, "性别为：" + sex[which], Toast.LENGTH_SHORT).show();
//					}
//				});
//				builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
//				{
//					@Override
//					public void onClick(DialogInterface dialog, int which)
//					{
//						iUserSelect = which;
//						SetParamCommand(TPMSParam.mBluethoothData.SET_PRESS_UNIT, iUserSelect);
//					}
//				});
//				builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
//				{
//					@Override
//					public void onClick(DialogInterface dialog, int which)
//					{
//
//					}
//				});
//				builder.show();



				PressureUnitSetPop pop = new PressureUnitSetPop(SetActivity.this);
				pop.setOnCheckedListenerr(new OnCheckedListener() {
					@Override
					public void onChecked(int positon) {
						iUserSelect = positon;
						SetParamCommand(TPMSParam.mBluethoothData.SET_PRESS_UNIT, iUserSelect);
					}
				});

				pop.showAtLocation(getRootView(SetActivity.this), Gravity.CENTER, 300, 0);
//				pop.initPosition(TPMSParam.tyreData.getPressUnit());
				
			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.TemperatureUnitSetup))) {
				
				TemperatureUnitSetPop pop = new TemperatureUnitSetPop(SetActivity.this);
				pop.setOnCheckedListenerr(new OnCheckedListener() {
					@Override
					public void onChecked(int positon) {
						iUserSelect = positon;
						SetParamCommand(TPMSParam.mBluethoothData.SET_TEMP_UNIT, iUserSelect);
					}
				});
				pop.showAtLocation(getRootView(SetActivity.this), Gravity.CENTER, 300, 0);
				pop.initPosition(TPMSParam.tyreData.getTempUnit());
				
			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.LanguageSetup))) {
				iSelect = arg2;
				LayoutInflater inflater = LayoutInflater.from(SetActivity.this);
				View view = inflater.inflate(R.layout.set_item_dialog, null);
				boolean dismiss = true;
				final String languagetype[] = getResources().getStringArray(R.array.LanguageSet);
				Dialog SoundDialog = new SysRadioButtonDialog.Builder(SetActivity.this)
						.setMessage((String) mData.get(arg2).get("title")).setSingleChoiceItems(languagetype,
								TPMSParam.iLanguageType, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										iUserSelect = which;
									}
								})
						.setPositiveButton(getResources().getString(R.string.confirm),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										ListAdapter la = listView.getAdapter();

										// dialog.dismiss();
										// if(languagetype[iUserSelect].equals("English"))//暂不支持英文
										// {
										// onCreateDialog(0x01,String.format("暂不支持"));
										// }else{
										TPMSParam.iLanguageType = iUserSelect;
										// ssitchlanguage(iUserSelect);
										HashMap<String, Object> map = (HashMap<String, Object>) mData.get(iSelect);

										map.put("info", languagetype[TPMSParam.iLanguageType]);
										((SysListviewbase) la).notifyDataSetChanged();// 通知系统，实时更新
										/*
										 * View view = null; LayoutInflater
										 * inflater = LayoutInflater.from(this);
										 * view = inflater.inflate(R.layout.
										 * set_simple_dialog, null);
										 * customBuilder.setMessage("")
										 * .setPositiveButton(getResources().
										 * getString(R.string.confirm),this); //
										 */
										// ssitchlanguage(TPMSParam.iLanguageType);
										onCreateDialog(0x05, getResources().getString(R.string.PlaseRStartApp));
										// System.exit(0);
										// }
									}
								})
						.setNegativeButton(getResources().getString(R.string.Cancel), null).create(view, dismiss);
				SoundDialog.show();
				curDialog = SoundDialog;
			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.ScreenAlwaysOnSetup))) {
				iSelect = arg2;
				LayoutInflater inflater = LayoutInflater.from(SetActivity.this);
				View view = inflater.inflate(R.layout.set_item_dialog, null);
				boolean dismiss = true;
				final String temp[] = getResources().getStringArray(R.array.SleepTimeSet);
				Dialog DisplayTimeDialog = new SysRadioButtonDialog.Builder(SetActivity.this)
						.setMessage((String) mData.get(arg2).get("title"))
						.setSingleChoiceItems(temp, TPMSParam.iSleepTime, new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								iUserSelect = which;
							}
						}).setPositiveButton(getResources().getString(R.string.confirm),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										ListAdapter la = listView.getAdapter();
										TPMSParam.iSleepTime = iUserSelect;
										HashMap<String, Object> map = (HashMap<String, Object>) mData.get(iSelect);
										map.put("info", temp[TPMSParam.iSleepTime]);
										((SysListviewbase) la).notifyDataSetChanged();// 通知系统，实时更新
									}
								})
						.setNegativeButton(getResources().getString(R.string.Cancel), null).create(view, dismiss);
				DisplayTimeDialog.show();
				curDialog = DisplayTimeDialog;
			}
			else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.AlarmValueSetup))) {
				Intent newIntent = new Intent(SetActivity.this, SetWarnSubActivity.class);
				startActivityForResult(newIntent, 102);
			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.InterfaceStyleSetup))) {

			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.ReplaceTheTireSensor))) {
				Intent newIntent = new Intent(SetActivity.this, SetSensorChangeActivity.class);
				startActivityForResult(newIntent, 102);
			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.TireSwap))) {
				Intent newIntent = new Intent(SetActivity.this, SetSensorExchangeActivity.class);
				startActivityForResult(newIntent, 103);
			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.tech_support))) {
				Intent newIntent = new Intent(SetActivity.this, AboutMeAfterSaleSupportActivity.class);
				startActivity(newIntent);
			} else if (mData.get(arg2).get("title").equals(getResources().getString(R.string.RestoreFactorySetup))) {
				/*Dialog dialog = null;
				LayoutInflater inflater = LayoutInflater.from(SetActivity.this);
				SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(SetActivity.this);
				View view = inflater.inflate(R.layout.set_verify_dialog, null);
				input = (EditText) view.findViewById(R.id.inputcode);
				bitmapView = (ImageView) view.findViewById(R.id.Imageview);
				bitmapView.setImageBitmap(SetBpUtil.getInstance().createBitmap());
				infoView = (TextView) view.findViewById(R.id.message);
				infoView1 = (TextView) view.findViewById(R.id.infoview1);
				infoView1.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						bitmapView.setImageBitmap(SetBpUtil.getInstance().createBitmap());
					}
				});
				infoView1.setOnTouchListener(new View.OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							// 更改为按下时的背景图片
							infoView1.setTextColor(getResources().getColor(R.color.display_touch));
						} else if (event.getAction() == MotionEvent.ACTION_UP) {
							infoView1.setTextColor(getResources().getColor(R.color.display_text));
						}
						return false;
					}
				});
				// Log.v("seting","bitmap code is
				// "+SetBpUtil.getInstance().getCode());
				customBuilder.setMessage(SetActivity.this.getResources().getString(R.string.StringVerifyAlter))
						.setNegativeButton(getResources().getString(R.string.Cancel),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								})
						.setPositiveButton(getResources().getString(R.string.confirm),
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog, int which) {
										if (input.getText().toString()
												.equalsIgnoreCase(SetBpUtil.getInstance().getCode())) {
											dialog.dismiss();
											SetActivity.this.onCreateDialog(0x03, "");
										} else {

											infoView.setText(getResources().getString(R.string.InputErr));
											bitmapView.setImageBitmap(SetBpUtil.getInstance().createBitmap());
											input.setText("");
										}
									}
								});
				dialog = customBuilder.create(view, false);
				dialog.show();
				curDialog = dialog;*/
				SetActivity.this.onCreateDialog(0x03, "");
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 103:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				if (data.getExtras().getString("action").equals(SysControllerService.AUTO_SW_MAINPAGE)) {
					Intent i = new Intent(SysControllerService.AUTO_SW_MAINPAGE);
					sendBroadcast(i);
				}
			}
			break;
		case 102:
			if (resultCode == Activity.RESULT_OK) {

			}
			break;

		default:
			break;
		}
	}
	
	public View getRootView(Activity myContext){  
        return ((ViewGroup)myContext.findViewById(android.R.id.content)).getChildAt(0);  
    } 
	
	public class MsgReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(app.REBOOT_SUCC)){
				finishAllActivity();
				stopService(new Intent(SetActivity.this, UsbControllerService.class));
				stopService(new Intent(SetActivity.this, SysControllerService.class));
				stopService(new Intent(SetActivity.this, SysManagerService.class));
				startActivity(new Intent(SetActivity.this,ConnectChooseActivity.class));
			}
		}
	}
}
