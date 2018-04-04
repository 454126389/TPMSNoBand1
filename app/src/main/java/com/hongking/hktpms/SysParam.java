package com.hongking.hktpms;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.hongking.oemtpms.R;

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SysParam {
	private List<Activity> mList = new LinkedList<Activity>();
	String TAG = "Sysparam";
	private static SysParam instance;
	private boolean Debug = true;
	final int EXIT_APPLICATION = 0x01;
	final boolean PRO_DATA_DECRYPT = false;
	final int lf = 0;// 左前
	final int rf = 1;// 右前
	final int lb = 2;// 左后
	final int rb = 3;// 右后

	/** 协议起始标识 */
	final byte APP_PROTOCOL_STX = (0x02);
	/** 协议结束标识 */
	final byte APP_PROTOCOL_ETX = (0x03);

	final int iThemeTable[] = { R.color.display_bg };
	final int iSoundTable[] = { R.raw.sound1, R.raw.sound2, R.raw.sound3, R.raw.sound4, R.raw.sound4 };
	final int iSoundTireType[][] = { { R.raw.lf, R.raw.rf, R.raw.lb, R.raw.rb, R.raw.sound4 } };/* 轮胎位置 */
	final int iSoundWarnType[][] = {
			{ R.raw.presslow, R.raw.presshigh, R.raw.temphigh, R.raw.pressleak, R.raw.lowb, R.raw.ns } };/* 报警信息 */
	final int iSoundPromptType[][] = { { R.raw.promptprocessnow } };/* 提示音 */
	final int iWarnIconTable[] = { R.drawable.icon_warn_thigh, R.drawable.icon_warn_plow, R.drawable.icon_warn_phigh,
			R.drawable.icon_warn_leakage };

	final class TPMS_CMD {
		// TPMS_CMD.CMD_TPMS_MIN_NUM=0x80,
		static final byte CMD_TPMS_BASE = (byte) 0x80;
		static final byte CMD_TPMS_HANDS = (byte) CMD_TPMS_BASE; /* 双机握手 */
		static final byte CMD_TPMS_SYSP_SET = (byte) CMD_TPMS_BASE + 1; /* 系统参数设置 */
		static final byte CMD_TPMS_SYSP_GET = (byte) CMD_TPMS_BASE + 2; /* 系统参数获取 */
		static final byte CMD_TPMS_TIRE_EXCHANGE = (byte) CMD_TPMS_BASE + 3; /* TPMS轮胎对调 */
		static final byte CMD_TPMS_TPMSINF = (byte) (byte) CMD_TPMS_BASE + 4; /* TPMS信息上报 */
		static final byte CMD_TPMS_TIRE_ID_GET = (byte) CMD_TPMS_BASE + 5; /* TPMS轮胎ID获取 */
		static final byte CMD_TPMS_MODE_CHANGE = (byte) CMD_TPMS_BASE + 6; /* TPMS主机模式切换 */
		static final byte CMD_TPMS_VER = (byte) CMD_TPMS_BASE + 7; /* 版本号查看 */
		static final byte CMD_TPMS_TIRE_AUTO_PAIR = (byte) CMD_TPMS_BASE + 8; /* TPMS自动搜索配对轮 */
		static final byte CMD_TPMS_TIRE_AUTO_PAIR_RESULT = (byte) CMD_TPMS_BASE + 9; /* TPMS轮胎自动配对 */
		static final byte CMD_TPMS_COMM_FAIL = (byte) CMD_TPMS_BASE + 0x0a; /* 通信失败 */
		static final byte CMD_TPMS_TIRE_RSET = (byte) CMD_TPMS_BASE
				+ 0x0b; /* TPMS更换轮胎-直接写ID:正式程序不会调用 */
		static final byte CMD_TPMS_ALL = (byte) CMD_TPMS_BASE + 0x0c;
		static final byte CMD_TPMS_HIDE_MIN = (byte) 0xF0;
		static final byte CMD_TPMS_RF_CP = CMD_TPMS_HIDE_MIN;
	} /* 协议命令码 */

	static class TPMS_PROTOCOL_ERRCODE {
		static final byte TPMS_ERR_NONE = (byte) 0x00; /* 成功 */
		static final byte TPMS_ERR_PARAM = (byte) 0x01; /* 传入的参数错误 */
		static final byte TPMS_ERR_TIMEOUT = (byte) 0x02; /* 通信超时 */
		static final byte TPMS_ERR_XOR = (byte) 0x03; /* 协议包XOR错误 */
		static final byte TPMS_ERR_NULLFUN = (byte) 0x04; /* 空函数出错 */
		static final byte TPMS_ERR_STX = (byte) 0x05; /* 协议头出错 */
		static final byte TPMS_ERR_ETX = (byte) 0x06; /* 协议尾出错 */
		static final byte TPMS_ERR_LENG = (byte) 0x07; /* 协议长度出错 */
		static final byte TPMS_ERR_DEC = (byte) 0x08; /* 解密出错 */
		static final byte TPMS_ERR_HANDS = (byte) 0x09; /* 握手失败 */
		static final byte TPMS_ERR_FUN_TIMEOUT = (byte) 0x0a;/* 函数调用超时 */
		static final byte TPMS_ERR_MODE = (byte) 0x0b; /* 模式错误 */
		static final byte TPMS_ERR_ALL = (byte) 0x0c;

		static String errCodeExchange(Context context, int errcode) {
			int a;
			String Errcode[] = context.getResources().getStringArray(R.array.SystemErrCode);

			a = Math.abs(errcode);
			if (a < Errcode.length)
				return Errcode[a];
			else
				return "未知错误";
		}

	}

	class PRESSER_UNIT_TYPE {
		static final byte BAR_UNIT = (byte) 0x00; /* 压力单位Bar */
		static final byte PSI_UNIT = (byte) 0x00; /* 压力单位PSI */
		static final byte KPA_UNIT = (byte) 0x00; /* 压力单位Kpa */
	}

	class TMPER_UNIT_TYPE {
		static final byte OC_UNIT = 0x00; /* 温度单位℃ */
		static final byte OF_UNIT = 0x01; /* 温度单位℉ */
	};

	class TIRE_PLACE {
		static final byte TIRE_LF = 0x00; /* 左前轮 */
		static final byte TIRE_RF = 0x01; /* 右前轮 */
		static final byte TIRE_LB = 0x02; /* 左后轮 */
		static final byte TIRE_RB = 0x03; /* 右后轮 */
	};

	class SYS_MODE {
		static final byte MODE_NORM = 0x00; /* 正常模式 */
		static final byte MODE_SETUP = 0x01; /* 系统设置模式 */
		static final byte MODE_REBOOT = 0x02; /* 系统复位操作模式 */
		static final byte MODE_HEARTBEAT = 0x03;
		static final byte MODE_RUNHEARTBEAT = 0x04;/*正常模式下的心跳命令*/
	};

	class RF_CP_MODE {
		static final byte RF_CP_READ = 0x00; /* 读配置数据 */
		static final byte RF_CP_WRITE = 0x01; /* 写配置数据 */
	}

	class Tire_Inf {
		int supdate; /* 更新标识位，>=0表示正常数据更新，又标识轮胎位置，<0表示该轮胎暂无有效数据 */
		int alarm_flag; /* 报警标志位 */
		int pressure_value; /* 压力值 */
		int tmper_value; /* 温度值 */
		// int id; /*轮胎id*/
	}

	class Sys_Version {
		byte version; /* 版本号如0x10表示1.0 */
		short ubuildtime; /* 编译时间:0x1412,表示14:12 */
		int builddate;/* 编译日期:0x12082400,表示12年08月24日,最后如果为0表示正式版本，其余未测试版本 */
		int hard; /* 硬件版本:0x12082400,表示12年08月24日,最后表示硬件版本 */
		byte procotrol;
	}

	class APP_PRO_PARAMSET_ST {
		byte cmd; /* 系统设置命令 */
		byte press_unit; /* 压力单位 */
		byte tmper_unit; /* 温度单位 */
		short auto_press_value; /* 标准充气压力值 */
		short high_press_value; /* 高压报警值 */
		short low_press_value; /* 低压报警值 */
		short high_tmper_value; /* 高温报警值 */
	};

	// 系统参数获取结构体
	class APP_PRO_PARAMGET_ST {
		byte cmd; /* 系统设置获取命令 */
		int ret; /* 返回值 > 0成功，表示后续的数据长度 */
		byte press_unit; /* 压力单位 */
		byte tmper_unit; /* 温度单位 */
		short auto_press_value; /* 标准充气压力值 */
		short high_press_value; /* 高压报警值 */
		short low_press_value; /* 低压报警值 */
		short high_tmper_value; /* 高温报警值 */
	};

	class SysInit {
		public SysInit() {
		}

	}

	class warnPressPara {
		warnPressPara() {
		}

		int unit;

	}

	final String sDefautTempValue[] = { "--", "--" };
	final String sDefautPressValue[] = { "-.--","--.-","---"};
	final String sDefautTempUnit = "℃";
	final String sDefautPressUnit = "PSI";
	String M_name = "";// 接收机名称

	/**
	 * 接收器类型
	 */
	public String device_type = DEVICE_TYPE_BT;
	public final static String DEVICE_TYPE_USB = "usb";
	public final static String DEVICE_TYPE_BT = "bluetooth";
	Sys_Version M_version = new Sys_Version();// 接收机版本号

	/* 压力报警设置返回的基本单元 */
	public class DataInfo {
		DataInfo() {
		}

		@Override
		public String toString() {
			return "{" +
					"flag='" + flag + '\'' +
					", id=" + id +
					", value=" + value +
					", baxMin=" + baxMin +
					", baxMax=" + baxMax +
					", baxMove=" + baxMove +
					", Display='" + Display + '\'' +
					", unitName='" + unitName + '\'' +
					", HighAlertStatus=" + HighAlertStatus +
					", LowAlertStatus=" + LowAlertStatus +
					", LeakageAlertStatus=" + LeakageAlertStatus +
					", LBStatus=" + LBStatus +
					", NSStatus=" + NSStatus +
					", NewStatus=" + NewStatus +
					", validFlag=" + validFlag +
					'}';
		}

		public String flag;// 相关数据，压力or温度
		public int id;// id值
		public int value;// 压力值
		public int baxMin;// 最小值
		public int baxMax;// 压力最大值
		public int baxMove;// 换算单位值
		public String Display;// 屏幕输出的格式字符串
		public String unitName;// 单位名称
		public boolean HighAlertStatus;// 高压/高温报警状态
		public boolean LowAlertStatus;// 低压报警状态
		public boolean LeakageAlertStatus;// 漏气报警状态
		public boolean LBStatus;// 电池电量低
		public boolean NSStatus;// 传感器故障
		public boolean NewStatus;// 最新数值
		public boolean validFlag;
	}

	/*----------------------轮胎压力相关信息----------------------------------*/
	class TyreData {
		final int ityreNum = 20;// 轮胎个数

		final String sPressUnitSting[] = { "Bar", "PSI","Kpa" };
		final String sTempUnitSting[] = { "℃", "℉" };
		final int iWarnBarPressMax[] = { 450,653,450 };
		final int iWarnBarPressMove[] = { 100, 10,1 };
		final int iWarnBarTempMax[] = { 125, 257 };

		private int iPressUnit;// 压力单位
		private int iTempUnit;// 温度单位
		private int id[] = new int[ityreNum];// 各个轮胎的id号

		private int tempTable[][] = new int[ityreNum][sTempUnitSting.length];// 存放各个轮胎的温度值
		private int pressureTable[][] = new int[ityreNum][sPressUnitSting.length];// 存放各个轮胎的压力值
		private boolean Status[] = new boolean[ityreNum];// 是否是最新数据
		/*
		 * 其中alarm_flag各个bit的含义如下 31 30 ... 12 11 10 9 8 7 6 5 4 3 2 1 0 NC NC
		 * NC NC NC NC NC NC NC NC NS LK LB HT HP LP LP：该位被置1表示轮胎气压偏低
		 * HP：该位被置1表示轮胎气压偏高 HT：该位被置1表示轮胎温度过高 LB：该位被置1表示传感器电池电量过低 LK：该位被置1表示该轮胎漏气
		 * NS：该位被置1表示该轮胎故障 NC：表示目前没有使用 pressure_value：
		 */
		private int alarm_flag[] = new int[ityreNum];// 报警状态
		private int update_status[] = new int[ityreNum];// 更新状态

		private int pressurePreTable[][] = new int[ityreNum][sPressUnitSting.length];// 存放各个轮胎上一次的压力值
		private int leakageAvg[] = new int[ityreNum];// 气压的平均值用来记录气压变化的过程
		// int BarMax;
		// int BarMove;//

		/* 默认情况下压力单位Kpa为单位存储 */
		private int iPressCapacity[] = new int[sPressUnitSting.length];// 智能设置的值
		final private int iPressCapacityDefault = 240;// 智能设置的值默认值

		private int iPressWarnLow[] = new int[sPressUnitSting.length];// 最低压力值
		final private int iPressWarnLowDefault = 180;// 最低压力值默认值

		private int iPressWarnHigh[] = new int[sPressUnitSting.length];// 最高压力值
		final private int iPressWarnHighDefault = 300;// 最高压力值默认值
		/* 默认情况下温度单位摄氏度为单位存储 */
		private int iTempWarnHigh[] = new int[sTempUnitSting.length];// 最高温度报警
		final private int iTempWarnHighDefault = 70;// 最高温度报警默认值

		// private boolean isSimulateMode = false;

		TyreData() {
			// for(int i = 0;i < ityreNum;i++)
			// update_status[i] = -1;
		}

		void setStatus(int which, boolean status) {
			Status[which] = status;
		}

		void setUpdateStatus(int which, int status) {
			update_status[which] = status;
		}

		void resetUpdateStatus() {
			for (int i = 0; i < 4; i++)
				update_status[i] = -1;
		}

		void setArlamFlagMask(int which, int location) {
			alarm_flag[which] &= ~(1 << location);
		}

		void restoreFactorySet() {
			iPressUnit = 0x00;
			iTempUnit = 0x00;
			iPressCapacity[iPressUnit] = iPressCapacityDefault;
			iPressWarnLow[iPressUnit] = iPressWarnLowDefault;
			iPressWarnHigh[iPressUnit] = iPressWarnHighDefault;
			iTempWarnHigh[iPressUnit] = iTempWarnHighDefault;

		}

		void setID(int which, int value) {
			if (which < ityreNum)
				id[which] = value;
		}

		void setRFCp(int value) {
			iRFCpValue = value & 0xff;
		}

		int getId(int which) {
			if (which < ityreNum)
				return id[which];
			else
				return 0x00;
		}

		void setInfoValue(int which, Tire_Inf info) {
			Log.i(TAG, "setinfo " + which);
			if (which < ityreNum) {
				Log.i(TAG, String.format("rec %d tyre info %d,%d,%d,%d", which, info.pressure_value, info.tmper_value,
						info.alarm_flag, info.supdate));
				pressureTable[which][iPressUnit] = info.pressure_value;
				tempTable[which][iTempUnit] = info.tmper_value;
				alarm_flag[which] = info.alarm_flag;
				update_status[which] = info.supdate;
				// id[which] = info.id;
			}
		}

		Tire_Inf getInfoValue(int which) {
			Tire_Inf info = new Tire_Inf();
			if (which < ityreNum) {
				// Log.i(TAG,String.format("rec %d tyre info %d,%d,%d,%d",
				// which,info.pressure_value,info.tmper_valude,info.alarm_flag,info.supdate));
				info.pressure_value = pressureTable[which][iPressUnit];
				info.tmper_value = tempTable[which][iTempUnit];
				info.alarm_flag = alarm_flag[which];
				info.supdate = update_status[which];
				// info.id = id[which];
			}
			return info;
		}

		int getPressUnit() {
			return iPressUnit;

		}

		void SetPressUnit(int id) {
			Log.i(TAG, String.format("SetPressUnit =%d", id));
			if (id > sPressUnitSting.length - 1)
				return;
			if (id == iPressUnit)
				return;
			if (id == 0x01) {
				iPressCapacity[id] = (int) ((float) ((float) iPressCapacity[iPressUnit] * 1.45));// 为PSI单位的时候
				iPressWarnLow[id] = (int) ((float) ((float) iPressWarnLow[iPressUnit] * 1.45));// 为PSI单位的时候
				iPressWarnHigh[id] = (int) ((float) ((float) iPressWarnHigh[iPressUnit] * 1.45));// 为PSI单位的时候
				for (int i = 0; i < ityreNum; i++) {
					pressureTable[i][id] = (int) ((float) ((float) pressureTable[i][iPressUnit] * 1.45));

				}

				// iTempWarnHigh[id] =
				// (int)((float)((float)iTempWarnHigh[iPressUnit]/1.45));//为PSI单位的时候

			} else if (iPressUnit == 0x01) {

				iPressCapacity[id] = (int) ((float) ((float) iPressCapacity[iPressUnit] / 1.45));// 为PSI单位的时候
				iPressWarnLow[id] = (int) ((float) ((float) iPressWarnLow[iPressUnit] / 1.45));// 为PSI单位的时候
				iPressWarnHigh[id] = (int) ((float) ((float) iPressWarnHigh[iPressUnit] / 1.45));// 为PSI单位的时候
				for (int i = 0; i < ityreNum; i++) {
					pressureTable[i][id] = (int) ((float) ((float) pressureTable[i][iPressUnit] / 1.45));

				}

			} else {
				iPressCapacity[id] = iPressCapacity[iPressUnit];
				iPressWarnLow[id] = iPressWarnLow[iPressUnit];
				iPressWarnHigh[id] = iPressWarnHigh[iPressUnit];
				for (int i = 0; i < ityreNum; i++) {
					pressureTable[i][id] = pressureTable[i][iPressUnit];
				}

			}
			iPressUnit = id;
		}

		/* 设置智能报警值 */
		void setPressCapaticy(int value) {
			Log.i(TAG, String.format("setPressCapaticy =%d", value));
			iPressCapacity[iPressUnit] = value;

		}

		void setPressLowWarn(int value) {
			Log.i(TAG, String.format("setPressLowWarn =%d", value));
			iPressWarnLow[iPressUnit] = value;

		}

		void setPressHighWarn(int value) {
			Log.i(TAG, String.format("setPressHighWarn =%d", value));
			iPressWarnHigh[iPressUnit] = value;

		}

		void setPressValue(int which, int value) {
			if (which < ityreNum)
				pressureTable[which][iPressUnit] = value;

		}

		String getPressDisplay(int value) {
			String Display = "";
			if (iPressUnit == 0) {
				Display = "" + (float) ((float) value / 100);
				// Capacity.unitName = "Bar";

			} else if (iPressUnit == 0x01) {
				Display = "" + (float) ((float) value / 10);
			} else {
				Display = "" + (value);
				// Capacity.unitName = "Kpa";
			}
			Display += sPressUnitSting[iPressUnit];
			return Display;
		}

		/* 获取相应轮胎的压力基本信息 */
		DataInfo getPressValue(int which) {
			DataInfo info = new DataInfo();
			// 用来演示的
			// Random random = new Random();
			//
			info.flag = "pressure";
			info.value = pressureTable[which][iPressUnit];// +random.nextInt(20);//debug
			info.id = id[which];
			if (update_status[which] >= 0x00) {

				if (iPressUnit == 0)/* Bar */
				{
					info.Display = String.format("%d.%02d",(int)info.value / 100,(int)info.value % 100);
				} else if (iPressUnit == 0x01)/* PSI */
				{
					info.Display = String.format("%02d.%d",(int)info.value / 10,(int)info.value % 10);
				} else /* Kpa */
				{
					info.Display = String.format("%03d",info.value);
				}
				if (pressurePreTable[which][iPressUnit] > pressureTable[which][iPressUnit]) {
					leakageAvg[which]++;
					if (leakageAvg[which] > 2) {
						info.LeakageAlertStatus = true;
					}
				} else {
					if (leakageAvg[which] > 0)
						leakageAvg[which]--;
				}
				pressurePreTable[which][iPressUnit] = pressureTable[which][iPressUnit];
				info.baxMax = iWarnBarPressMax[iPressUnit];
				info.baxMove = iWarnBarPressMove[iPressUnit];
				info.unitName = sPressUnitSting[iPressUnit];
				info.HighAlertStatus = ((alarm_flag[which] & 0x02) == 0x02);// info.value
																			// >
																			// iPressWarnHigh[iPressUnit];
				info.LowAlertStatus = ((alarm_flag[which] & 0x01) == 0x01);// info.value
																			// <
																			// iPressWarnLow[iPressUnit];
				info.LBStatus = ((alarm_flag[which] & 0x08) == 0x08);
				info.LeakageAlertStatus = ((alarm_flag[which] & 0x10) == 0x10);//
				info.NSStatus = ((alarm_flag[which] & 0x20) == 0x20);//

				info.NewStatus = Status[which];
				info.validFlag = ((alarm_flag[which] & (1 << 31)) != (1 << 31));
				if (!info.validFlag) {
					info.Display = sDefautPressValue[iPressUnit];
				}

			} else {
				// Log.i(TAG,"get pressure null");
				info.Display = " ";
				info.unitName = " ";
				info.HighAlertStatus = false;
				info.LowAlertStatus = false;
				info.LeakageAlertStatus = false;
				info.validFlag = false;
			}
			return info;

		}

		/* 1psi=6.895KPa，1bar≈14.5psi 1bar≈100Kpa */
		/* 获取智能报警值 */
		DataInfo getPressCapacityWarn() {
			DataInfo Capacity = new DataInfo();
			Capacity.flag = "pressure";
			Capacity.baxMin = 0;// 最少压力值多少？
			Capacity.baxMax = iWarnBarPressMax[iPressUnit];
			Capacity.baxMove = iWarnBarPressMove[iPressUnit];
			Capacity.unitName = sPressUnitSting[iPressUnit];

			iPressCapacity[iPressUnit] = (iPressCapacity[iPressUnit] <= Capacity.baxMin) ? Capacity.baxMin
					: iPressCapacity[iPressUnit];
			iPressCapacity[iPressUnit] = (iPressCapacity[iPressUnit] >= Capacity.baxMax) ? Capacity.baxMax
					: iPressCapacity[iPressUnit];
			Capacity.value = iPressCapacity[iPressUnit];
			if (iPressUnit == 0) {
				// Capacity.baxMax = 450;
				// Capacity.baxMove = 100;

				Capacity.Display = "" + (float) ((float) Capacity.value / 100);

			} else if (iPressUnit == 0x01) {
				// Capacity.baxMax = 630;
				// Capacity.baxMove = 10;
				// Capacity.value =
				// (int)((float)((float)iPressCapacity*1.45));//为PSI单位的时候
				Capacity.baxMin = (int) ((float) ((float) Capacity.baxMin * 1.45));
				Capacity.Display = "" + (float) ((float) Capacity.value / 10);
			} else {
				// Capacity.baxMax = 450;
				// Capacity.baxMove = 1;
				// Capacity.value = iPressCapacity;
				Capacity.Display = "" + Capacity.value;
			}

			return Capacity;
		}

		/* 获取低压报警值 */
		DataInfo getPressLowWarn() {
			DataInfo Capacity = new DataInfo();
			Capacity.flag = "pressure";
			Capacity.baxMin = 0;// 最少压力值多少？
			Capacity.baxMax = iPressWarnHigh[iPressUnit];
			Capacity.baxMove = iWarnBarPressMove[iPressUnit];
			Capacity.unitName = sPressUnitSting[iPressUnit];

			iPressWarnLow[iPressUnit] = (iPressWarnLow[iPressUnit] <= Capacity.baxMin) ? Capacity.baxMin
					: iPressWarnLow[iPressUnit];
			iPressWarnLow[iPressUnit] = (iPressWarnLow[iPressUnit] >= Capacity.baxMax) ? Capacity.baxMax
					: iPressWarnLow[iPressUnit];
			Capacity.value = iPressWarnLow[iPressUnit];
			if (iPressUnit == 0) {
				// Capacity.baxMax = 450;
				// Capacity.baxMove = 100;

				Capacity.Display = "" + (float) ((float) Capacity.value / 100);

			} else if (iPressUnit == 0x01) {
				// Capacity.baxMax = 630;
				// Capacity.baxMove = 10;
				// Capacity.value =
				// (int)((float)((float)iPressWarnLow*1.45));//为PSI单位的时候
				Capacity.baxMin = (int) ((float) ((float) Capacity.baxMin * 1.45));
				Capacity.Display = "" + (float) ((float) Capacity.value / 10);
			} else {
				// Capacity.baxMax = 450;
				// Capacity.baxMove = 1;
				// Capacity.value = iPressWarnLow;
				Capacity.Display = "" + Capacity.value;
			}

			return Capacity;
		}

		/* 获取高压报警值 */
		DataInfo getPressHighWarn() {
			DataInfo Capacity = new DataInfo();
			Capacity.flag = "pressure";
			Capacity.baxMin = iPressWarnLow[iPressUnit];// 最少压力值多少？
			Capacity.baxMax = iWarnBarPressMax[iPressUnit];
			Capacity.baxMove = iWarnBarPressMove[iPressUnit];
			Capacity.unitName = sPressUnitSting[iPressUnit];

			iPressWarnHigh[iPressUnit] = (iPressWarnHigh[iPressUnit] <= Capacity.baxMin) ? Capacity.baxMin
					: iPressWarnHigh[iPressUnit];
			iPressWarnHigh[iPressUnit] = (iPressWarnHigh[iPressUnit] >= Capacity.baxMax) ? Capacity.baxMax
					: iPressWarnHigh[iPressUnit];
			Capacity.value = iPressWarnHigh[iPressUnit];
			if (iPressUnit == 0) {
				// Capacity.baxMax = 450;
				// Capacity.baxMove = 100;

				Capacity.Display = "" + (float) ((float) Capacity.value / 100);

			} else if (iPressUnit == 0x01) {
				// Capacity.baxMax = 653;
				// Capacity.baxMove = 10;
				// Capacity.value =
				// (int)((float)((float)iPressWarnHigh*1.45));//为PSI单位的时候
				Capacity.baxMin = (int) ((float) ((float) Capacity.baxMin * 1.45));
				Capacity.Display = "" + (float) ((float) Capacity.value / 10);
			} else {
				// Capacity.baxMax = 450;
				// Capacity.baxMove = 1;
				// Capacity.value = iPressWarnHigh;
				Capacity.Display = "" + Capacity.value;
			}

			return Capacity;
		}

		/*------------温度相关---------------*/
		int getTempUnit() {
			return iTempUnit;
		}

		void setTempUnit(int id) {
			Log.i(TAG, String.format("setTempUnit =%d", id));
			if (id > sTempUnitSting.length - 1)
				return;
			if (id == iTempUnit)
				return;
			if (id == 0x01) {
				iTempWarnHigh[id] = (iTempWarnHigh[iTempUnit] * 9 / 5 + 32);// 为PSI单位的时候
				for (int i = 0; i < ityreNum; i++) {
					tempTable[i][id] = (tempTable[i][iTempUnit] * 9 / 5 + 32);

				}

				// iTempWarnHigh[id] =
				// (int)((float)((float)iTempWarnHigh[iPressUnit]/1.45));//为PSI单位的时候

			} else {

				iTempWarnHigh[id] = ((iTempWarnHigh[iTempUnit] - 32) * 5) / 9;// 为PSI单位的时候
				for (int i = 0; i < ityreNum; i++) {
					tempTable[i][id] = (tempTable[i][iTempUnit] - 32) * 5 / 9;

				}

			}

			iTempUnit = id;
		}

		void setTempHighWarn(int value) {
			Log.i(TAG, String.format("setTempHighWarn =%d", value));
			iTempWarnHigh[iTempUnit] = value;
		}

		void setTempValue(int which, int value) {
			if (which < ityreNum)
				tempTable[which][iTempUnit] = value;
		}

		String getTempDisplay(int value) {
			String Display = "";
			if (iTempUnit == 0) {
				Display = "" + (value);
				// Capacity.unitName = "Bar";
			} else {
				Display = "" + (value);
				// Capacity.unitName = "Kpa";
			}
			Display += sTempUnitSting[iTempUnit];
			return Display;
		}

		DataInfo getTempValue(int which) {
			// 用来演示的
			// Random random = new Random();
			//
			DataInfo info = new DataInfo();
			info.flag = "tempareture";
			info.id = id[which];
			if (update_status[which] >= 0x00) {

				info.baxMin = -40;// 最少温度值多少？
				info.baxMax = 120;// 最少温度值多少？
				info.baxMove = 1;
				info.value = tempTable[which][iTempUnit];// +random.nextInt(20);;

				info.Display = "" + (info.value);

				info.baxMax = iWarnBarTempMax[iTempUnit];
				info.unitName = sTempUnitSting[iTempUnit];
				info.HighAlertStatus = ((alarm_flag[which] & 0x04) == 0x04);// info.value
																			// >
																			// iTempWarnHigh[iTempUnit];
				info.NewStatus = Status[which];
				info.validFlag = ((alarm_flag[which] & (1 << 31)) != (1 << 31));

				if (!info.validFlag) {
					info.Display = sDefautTempValue[iTempUnit];
				}
			} else {
				// Log.i(TAG,"get temp null");
				info.Display = " ";
				info.unitName = " ";
				info.validFlag = false;
				info.HighAlertStatus = false;
			}
			// Log.i(TAG,"get temp value is "+info.value+"update state is
			// "+update_status[which]);
			return info;
		}

		/* 用于获取最高温度报警的基本单元 */
		DataInfo getTempHighWarn() {
			DataInfo Capacity = new DataInfo();
			Capacity.flag = "tempareture";
			Capacity.baxMin = -40;// 最少温度值多少？
			Capacity.baxMove = 1;
			Capacity.baxMax = iWarnBarTempMax[iTempUnit];
			Capacity.unitName = sTempUnitSting[iTempUnit];
			Capacity.value = iTempWarnHigh[iTempUnit];
			iTempWarnHigh[iTempUnit] = (iTempWarnHigh[iTempUnit] <= Capacity.baxMin) ? Capacity.baxMin
					: iTempWarnHigh[iTempUnit];
			iTempWarnHigh[iTempUnit] = (iTempWarnHigh[iTempUnit] >= Capacity.baxMax) ? Capacity.baxMax
					: iTempWarnHigh[iTempUnit];
			Capacity.Display = "" + (Capacity.value);
			return Capacity;
		}

	}

	TyreData tyreData;
	/*----------------------------------------------------------------------*/

	class bluethoothData {
		private final int TableCrypto[] = { 0x10, 0x9A, 0x33, 0xCB, 0x6F, 0xA9, 0x75, 0xBA, 0x5C, 0xC5, 0x31, 0xE9,
				0xCA, 0x52, 0x4E, 0xE8, 0xC1, 0x3A, 0xB3, 0x8A, 0x0F, 0xC3, 0xD7, 0x60, 0x3F, 0xB6, 0x96, 0x72, 0x47,
				0x2A, 0xAC, 0x69, 0x97, 0x80, 0x0F, 0xC5, 0x6C, 0xC0, 0x18, 0x58, 0xD2, 0x2B, 0x40, 0xF8, 0xDE, 0xE0,
				0x48, 0x61, 0xEE, 0xA5, 0xEA, 0xF8, 0x5B, 0x6C, 0xFC, 0xD8, 0x3B, 0x14, 0x85, 0xA4, 0x83, 0xB5, 0x88,
				0xD8, 0x80, 0xB7, 0x49, 0xA2, 0xC8, 0x5E, 0x9F, 0x12, 0x8F, 0x37, 0x2E, 0x60, 0x96, 0x5F, 0xE3, 0x4F,
				0x9B, 0x07, 0xFA, 0xE2, 0x5D, 0x53, 0x70, 0x8B, 0x6D, 0x29, 0x37, 0x67, 0x1E, 0xD0, 0x27, 0x4D, 0x89,
				0x2C, 0xB6, 0x8A, 0x9C, 0x83, 0xE7, 0x99, 0x01, 0x6D, 0x28, 0x23, 0x7B, 0x64, 0xC8, 0xF0, 0xFC, 0x5C,
				0x84, 0xF9, 0x1E, 0xA8, 0x91, 0xAF, 0x14, 0xBF, 0x79, 0x58, 0xBD, 0xAB, 0xB5, 0xB3, 0x31, 0x1D, 0xDA,
				0x70, 0x56, 0x63, 0xD3, 0x73, 0xDC, 0x6B, 0x82, 0xE1, 0x2F, 0xA5, 0x3E, 0x5C, 0x15, 0x3F, 0x5B, 0xD6,
				0xC3, 0x35, 0xB9, 0x2C, 0x7A, 0x73, 0x77, 0x50, 0x71, 0xD4, 0x00, 0xA0, 0x60, 0x8B, 0x07, 0xEB, 0x8C,
				0xB1, 0x13, 0xC2, 0x65, 0x4B, 0xAD, 0x22, 0xBC, 0xA0, 0x16, 0xCF, 0x79, 0x1D, 0xC5, 0x39, 0xBF, 0x0F,
				0xCD, 0x0B, 0x33, 0x14, 0xEF, 0xCB, 0x68, 0xEE, 0x3B, 0xE5, 0xD9, 0x7F, 0xC7, 0x15, 0x99, 0x60, 0xEC,
				0x26, 0xED, 0xB5, 0x70, 0x62, 0x2D, 0xA6, 0x56, 0xBE, 0x0A, 0x6D, 0x91, 0x70, 0xB9, 0x84, 0x32, 0x46,
				0x11, 0xD4, 0xF9, 0xFF, 0x6D, 0x1B, 0x97, 0x6D, 0x75, 0xFE, 0xDB, 0xBB, 0x10, 0x80, 0x13, 0xB2, 0xA7,
				0x30, 0x1A, 0x18, 0xBD, 0x45, 0x60, 0x98, 0x93, 0x3B, 0x3B, 0x39, 0x91, 0x57, 0xB2, 0xDC, 0xD4, 0xF7,
				0xAC, 0x04, 0x22, 0xF7, 0xBC, 0x41 };
		Context context;
		private byte recBuf[];
		private int recLen;
		// private String recMsg;
		byte mStatus;
		final Object lock1 = new Object();

		final byte SET_PRESS_UNIT = 0x00;
		final byte SET_TEMP_UNIT = 0x01;
		final byte SET_CAPACITY_WARN = 0x02;
		final byte SET_PRESSHIGH_WARN = 0x03;
		final byte SET_PRESSLOW_WARN = 0x04;
		final byte SET_TEMPHIGH_WARN = 0x05;

		final int MAX_WAIT_TIME = 5 * 60 * 1000;// 接收等待时间

		public bluethoothData(Context context) {
			this.context = context;
		}

		public bluethoothData() {
		}

		// public void adContext(Activity context){
		// this.context = context;
		// }
		public void dataAnalysis(byte dataBuf[]) {

		}

		void vDisplay(String msg) {
			Log.i("TPMSAPP", msg);
		}

		synchronized int onMsg(byte[] msg, int len, int[] result) {

			if (Debug) {
				String displaymsg = "";
				for (int i = 0; i < len; i++) {
					String dismsg = String.format("%x,", msg[i]);
					displaymsg += dismsg;
				}
				debugOut("onMsg: " + displaymsg);
			}

			if (ProtocolTPMSInfoUpload(msg, len) == 0)
				return 0x01;
			if (ProtocolTPMSTyreChangeResultReport(msg, len, result) == 0)
				return 0x02;
			this.recLen = len;
			recBuf = msg;
			super.notify();// 通知调用线程收到数据
			return 0x00;
		}

		private void DataEnDecrypt(byte rnd, int datalen, byte[] datainout) {
			int i;
			for (i = 0; i < datalen; i++) {
				datainout[i] = (byte) (datainout[i] ^ TableCrypto[(rnd + i) & 0xff]);
			}
		}

		/**
		 * 协议编码
		 * 
		 * @param rnd
		 * @param src
		 * @param srcLenth
		 * @param dst
		 * @param dstLen
		 * @return
		 */
		private int Protocol_Encode(byte rnd, byte[] src, int srcLenth, byte[] dst, int[] dstLen) {
			int i = 0;
			byte prt = 0, CHK = 0;
			int packlen = 6 + srcLenth;

			dst[prt++] = APP_PROTOCOL_STX; /* STX */
			dst[prt++] = (byte) (packlen & 0xff); /* LEN */
			dst[prt++] = (byte) ((packlen & 0xff00) >> 8);
			dst[prt++] = rnd; /* 随机数 */

			CHK = rnd;
			for (i = 0; i < srcLenth; i++) /* DATA */
			{
				CHK ^= src[i];
				dst[prt++] = src[i];
			}
			dst[prt++] = CHK; /* CHK */
			dst[prt++] = APP_PROTOCOL_ETX; /* ETX */
			dstLen[0] = packlen;
			return 0x00;

		}

		private int Protocol_Decode(byte[] rnd, byte[] src, int srcLenth, byte[] dst, int[] dstLen) {
			int i = 0;
			int prt = 0;
			int datalen = 0;
			byte CHK = 0;

			if ((src == null) || (dst == null) || (srcLenth < 7)) {
				return -TPMS_PROTOCOL_ERRCODE.TPMS_ERR_LENG; /* 传入参数错误 */
			}

			if ((src[0] != APP_PROTOCOL_STX) || (src[srcLenth - 1] != APP_PROTOCOL_ETX)) {
				return -TPMS_PROTOCOL_ERRCODE.TPMS_ERR_STX; /* 包起始或者结束标识错误 */
			}

			datalen = (int) ((src[2] << 8) | src[1]);
			datalen -= 6;
			if (datalen > 94)
				return -TPMS_PROTOCOL_ERRCODE.TPMS_ERR_LENG;
			rnd[0] = src[3]; /* 获取随机数 */
			CHK = rnd[0];
			prt = 4;
			for (i = 0; i < datalen; i++) {
				CHK ^= src[prt++];
			}

			if (CHK != src[prt]) {
				return -TPMS_PROTOCOL_ERRCODE.TPMS_ERR_XOR; /* 校验出错 */
			}
			if (PRO_DATA_DECRYPT)
				DataEnDecrypt(rnd[0], datalen, src); /* 数据解密处理 */

			prt = 4;
			for (i = 0; i < datalen; i++) /* 解出真实有效数据 */
			{
				dst[i] = src[prt++];
			}
			dstLen[0] = i;
			return 0;

		}

		void debugOut(String msg) {
			if (Debug)
				Log.i("dataExchange", msg);

		}

		void debugOut(byte[] msg, int len) {
			if (Debug) {
				String displaymsg = "";
				for (int i = 0; i < len; i++) {
					String dismsg = String.format("%x,", msg[i]);
					displaymsg += dismsg;
				}
				Log.i("dataExchange", displaymsg);
			}
		}

		void diaryInsert(byte[] msg, byte len) {
			String displaymsg = "";
			for (int i = 0; i < len; i++) {
				String dismsg = String.format("%x,", msg[i]);
				displaymsg += dismsg;
			}
			if (Debug)
				debugOut(displaymsg);
			if (context != null) {
				StorageMyDB dba = new StorageMyDB(context);
				dba.open();

			}

		}

		void diaryInsertExchange(byte[] msg1, int len1, byte[] msg2, int len2) {
			String displaymsg = "发送:\n";
			if (msg1 != null) {
				for (int i = 0; i < len1; i++) {
					String dismsg = String.format("%x,", msg1[i]);
					displaymsg += dismsg;
				}
			}
			displaymsg += "\n接收:\n";
			if (msg2 != null) {
				for (int i = 0; i < len2; i++) {
					String dismsg = String.format("%x,", msg2[i]);
					displaymsg += dismsg;
				}
			}
			displaymsg += "\n";
			if (Debug)
				debugOut(displaymsg);
			if (context != null) {
				StorageMyDB dba = new StorageMyDB(context);

				byte index = (byte) (msg2[0] - 0x80);
				String title;
				if (index < (context.getResources().getStringArray(R.array.ComunicationCommand).length)) {
					title = context.getResources().getStringArray(R.array.ComunicationCommand)[index];
				} else {
					title = "未知命令";
				}
				for (int i = 0; i < context.getResources().getStringArray(R.array.diaryLogCommand).length; i++) {
					if (context.getResources().getStringArray(R.array.diaryLogCommand)[i].equals(title)) {
						dba.open();
						dba.inserdiary(title, displaymsg);
						dba.close();
						break;
					}
				}
			}

		}

		void diaryInsertExchange(String msg1, String msg2) {

			if (context != null) {
				StorageMyDB dba = new StorageMyDB(context);
				dba.open();

				dba.inserdiary(msg1, msg2);
				dba.close();

			}

		}

		/**
		 * 发送协议
		 * 
		 * @param sendBuf
		 * @param sendLen
		 * @return
		 */
		private synchronized int sendData(byte[] sendBuf, int sendLen) {
			String message;
			debugOut(sendBuf, sendLen);
			try {
				message = new String(sendBuf, 0, sendLen, "ISO-8859-1");
				Intent intent = null;
				if (device_type.equals(DEVICE_TYPE_USB)) {
					intent = new Intent(UsbControllerService.SEND_MSG_FROM_USB_ACTION);
				} else {
					intent = new Intent(SysControllerService.SEND_MSG_FROM_BT_ACTION);
				}
				intent.putExtra("STATE", message);
				if (context != null) {
					context.sendBroadcast(intent);
					return 0;
				} else {
					return 0x8b;
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}

			/*
			 * Intent intent = new
			 * Intent(BluetoothConnController.SEND_MSG_FROM_BT_ACTION);
			 * intent.putExtra("STATE", sendBuf); intent.putExtra("LENGTH",
			 * sendLen); if(context != null){ context.sendBroadcast(intent);
			 * return 0; }else{ return 0x8b; }
			 */
		}

		private synchronized int recData(byte[] dataRec, int[] len) {
			try {
				super.wait(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -TPMS_PROTOCOL_ERRCODE.TPMS_ERR_TIMEOUT;
			}
			if (recBuf == null)
				return -TPMS_PROTOCOL_ERRCODE.TPMS_ERR_TIMEOUT;
			// len[0] = this.recLen[0];
			// byte []dataRec1 = recMsg.getBytes();
			// len[0] = recMsg.length();
			// Log.i("TPMSAPP555:",new String(dataRec1));
			// dataRec = Arrays.copyOf(dataRec1, newLength);
			len[0] = this.recLen;
			for (int i = 0; i < len[0]; i++)
				dataRec[i] = recBuf[i];
			recBuf = null;
			return 0x00;
		}

		private byte getRandom() {

			return (byte) new Random().nextInt();

		}

		private int protocolSendData(byte[] sendBuf, int sendLen) {
			byte[] protocol_data = new byte[100];
			int[] recLen = new int[1];
			int ret = Protocol_Encode(getRandom(), sendBuf, sendLen, protocol_data, recLen);
			if (ret == 0x00)
				return sendData(protocol_data, recLen[0]);
			else
				return ret;
		}

		private int protocolRecData(byte cmd, byte[] recData, int[] dataLen) {
			byte[] recBuf = new byte[100];
			int[] recLen = new int[1];
			byte[] rnd = new byte[1];
			int ret;
			ret = recData(recBuf, recLen);
			if (ret == 0x00) {
				ret = Protocol_Decode(rnd, recBuf, recLen[0], recData, dataLen);
				// Log.i(TAG,"recLen="+recLen[0]+"dataLen="+dataLen[0]);
				if (ret == 0) {
					if (cmd == recData[0]) {
						return ret;
					} else {
						debugOut(String.format("func err %x expect %x", recData[0], cmd));
						return -TPMS_PROTOCOL_ERRCODE.TPMS_ERR_NULLFUN;
					}

				} else {/* 解码出错 */
					debugOut("de_code err =" + ret);
				}

			}
			return ret;
		}

		private int protocolExchange(byte cmd, byte[] sendbuf, int datalen, byte[] recbuf, int[] recLen,
				int timeout_cnt) {
			int err = 0;
			int ret = 0x8a;
			debugOut("exchange start-----");
			synchronized (lock1) {
				Log.i(TAG, "exchange1");
				while (err++ < timeout_cnt) {
					ret = protocolSendData(sendbuf, datalen);
					debugOut("send data ret=" + ret);
					if (ret != 0x00)
						continue;
					// 等待回应
					ret = protocolRecData(cmd, recbuf, recLen);
					debugOut("rec data ret=" + ret);
					// 协议数据接收成功
					if (ret == 0) {
						diaryInsertExchange(sendbuf, datalen, recbuf, recLen[0]);
						break;
					}
				}
				debugOut(String.format("exchange end-----ret=%x\n", ret));
				debugOut(" ");
				return ret;
			}
		}

		private int protocolExchange(byte cmd, byte[] sendbuf, int datalen) {
			// int err = 0;
			int ret = 0x8a;
			Log.i(TAG, "exchange");
			synchronized (lock1) {
				Log.i(TAG, "exchange2");
				ret = protocolSendData(sendbuf, datalen);

				Log.i(TAG, "retun exchange ret" + ret);
				return ret;
			}
		}

		/**
		 * 接收机握手
		 */
		public int protocolHandshake() {
			byte sendbuff[] = new byte[100];
			byte recbuff[] = new byte[100];
			// APP_PRO_PARAMGET_ST pro_paramget_st;
			int send_len = 0, ret;
			byte[] rnd = new byte[1];
			int[] recLen = new int[1];
			sendbuff[0] = TPMS_CMD.CMD_TPMS_HANDS;
			sendbuff[1] = getRandom();
			// buff[1] = 0x55;
			rnd[0] = sendbuff[1];
			send_len = 2;
			// mStatus = SYS_MODE.MODE_SETUP;
			ret = protocolExchange(TPMS_CMD.CMD_TPMS_HANDS, sendbuff, send_len, recbuff, recLen, 1);
			if (ret == 0) {
				if (recbuff[5] == (rnd[0] ^ 0x55) || recbuff[1] == (rnd[0] ^ 0x55)) {
					return 0;
				} else {
					return TPMS_PROTOCOL_ERRCODE.TPMS_ERR_HANDS;
				}
			}
			return ret;
		}

		private int toUnsigned(int s) {
			Log.i(TAG, String.format("s = 0x%x\n", s));
			return s >= 0 ? s : 256 + s;
		}

		private int byteArrayToInt(byte[] b, int offset) {
			int value = 0;
			for (int i = 0; i < 4; i++) {
				// int shift= (4 - 1 - i) * 8;
				int shift = i * 8;
				value += (b[i + offset] & 0x000000FF) << shift;
			}
			return value;
		}

		private int byteArrayToShort(byte[] b, int offset) {
			int value = 0;
			for (int i = 0; i < 2; i++) {
				// int shift= (4 - 1 - i) * 8;
				int shift = i * 8;
				value += (b[i + offset] & 0x000000FF) << shift;
			}
			return value;
		}

		int ProtocolSysParamGet() {
			byte sendbuff[] = new byte[100];
			byte recbuff[] = new byte[100];
			// APP_PRO_PARAMGET_ST pro_paramget_st;
			int send_len = 0, ret;
			int[] recLen = new int[1];
			sendbuff[0] = TPMS_CMD.CMD_TPMS_SYSP_GET;

			send_len = 1;

			ret = protocolExchange(TPMS_CMD.CMD_TPMS_SYSP_GET, sendbuff, send_len, recbuff, recLen, 1);
			debugOut(sendbuff, (byte) ret);
			if (ret == 0) {

				ret = byteArrayToInt(recbuff, 1);
				if (ret >= 0x00) {
					// memcpy((byte *)&pro_paramget_st, send_buff,
					// sizeof(pro_paramget_st));
					int i = 5;
					tyreData.SetPressUnit(toUnsigned(recbuff[i++] | (recbuff[i++] << 8)));
					tyreData.setTempUnit(toUnsigned(recbuff[i++] | (recbuff[i++] << 8)));

					tyreData.setPressCapaticy(byteArrayToShort(recbuff, i));
					i += 2;
					tyreData.setPressHighWarn(byteArrayToShort(recbuff, i));
					i += 2;
					// Log.i(TAG,String.format("low
					// =%x,high=%x\n",recbuff[i-2],(recbuff[i-1] <<8)));
					// short value = (short) (recbuff[i-2]+(short)(recbuff[i-1]
					// <<8));

					// Log.i(TAG,String.format("value=%x,%x\n",value,byteArrayToShort(recbuff,i-2)));
					tyreData.setPressLowWarn(byteArrayToShort(recbuff, i));
					i += 2;
					tyreData.setTempHighWarn(toUnsigned(recbuff[i++]) | (recbuff[i++] << 8));
					// App_Parameter_Set.press_unit =
					// pro_paramget_st.press_unit;
					// App_Parameter_Set.tmper_unit =
					// pro_paramget_st.tmper_unit;
					// App_Parameter_Set.press_norm =
					// pro_paramget_st.auto_press_value;
					// App_Parameter_Set.press_up =
					// pro_paramget_st.high_press_value;
					// App_Parameter_Set.press_dn =
					// pro_paramget_st.low_press_value;
					// App_Parameter_Set.tmper_up =
					// pro_paramget_st.high_tmper_value;
					return 0;
				} else {
					return ret;
				}
			}

			return ret;

		}

		int ProtocolSysParamSetOld(byte setWhich, int value) {
			byte sendbuff[] = new byte[100];
			byte recbuff[] = new byte[100];
			APP_PRO_PARAMSET_ST pro_paramset_st = new APP_PRO_PARAMSET_ST();
			int send_len = 0, ret;
			int[] recLen = new int[1];
			pro_paramset_st.cmd = TPMS_CMD.CMD_TPMS_SYSP_SET;
			pro_paramset_st.press_unit = (byte) tyreData.getPressUnit();// App_Parameter_Set.press_unit;
			pro_paramset_st.tmper_unit = (byte) tyreData.getTempUnit();
			pro_paramset_st.auto_press_value = (short) tyreData.getPressCapacityWarn().value;
			pro_paramset_st.high_press_value = (short) tyreData.getPressHighWarn().value;
			pro_paramset_st.low_press_value = (short) tyreData.getPressLowWarn().value;
			pro_paramset_st.high_tmper_value = (short) tyreData.getTempHighWarn().value;

			debugOut(String.format("pressunit=%d,tempunit=%d,capacitywarn=%d,presshighwarn=%d,presslow=%d,temphigh=%d",
					pro_paramset_st.press_unit, pro_paramset_st.tmper_unit, pro_paramset_st.auto_press_value,
					pro_paramset_st.high_press_value, pro_paramset_st.low_press_value,
					pro_paramset_st.high_tmper_value));
			if (setWhich == SET_PRESS_UNIT)
				pro_paramset_st.press_unit = (byte) value;
			else if (setWhich == SET_TEMP_UNIT)
				pro_paramset_st.tmper_unit = (byte) value;
			else if (setWhich == SET_CAPACITY_WARN)
				pro_paramset_st.auto_press_value = (short) value;
			else if (setWhich == SET_PRESSHIGH_WARN)
				pro_paramset_st.high_press_value = (short) value;
			else if (setWhich == SET_PRESSLOW_WARN)
				pro_paramset_st.low_press_value = (short) value;
			else if (setWhich == SET_TEMPHIGH_WARN)
				pro_paramset_st.high_tmper_value = (short) value;
			int i = 0;
			sendbuff[i++] = pro_paramset_st.cmd;
			sendbuff[i++] = pro_paramset_st.press_unit;
			sendbuff[i++] = pro_paramset_st.tmper_unit;
			sendbuff[i++] = (byte) (pro_paramset_st.auto_press_value);
			sendbuff[i++] = (byte) (pro_paramset_st.auto_press_value >> 8);

			sendbuff[i++] = (byte) (pro_paramset_st.high_press_value);
			sendbuff[i++] = (byte) (pro_paramset_st.high_press_value >> 8);

			sendbuff[i++] = (byte) (pro_paramset_st.low_press_value);
			sendbuff[i++] = (byte) (pro_paramset_st.low_press_value >> 8);

			sendbuff[i++] = (byte) (pro_paramset_st.high_tmper_value);
			sendbuff[i++] = (byte) (pro_paramset_st.high_tmper_value >> 8);
			// send_len = sizeof(pro_paramset_st);
			// memcpy(send_buff, (byte *)&pro_paramset_st, send_len);
			send_len = i;
			ret = protocolExchange(TPMS_CMD.CMD_TPMS_SYSP_SET, sendbuff, send_len, recbuff, recLen, 1);

			if (ret == 0x00) {
				if (setWhich == SET_PRESS_UNIT)
					tyreData.SetPressUnit(value);
				else if (setWhich == SET_TEMP_UNIT)
					tyreData.setTempUnit(value);
				else if (setWhich == SET_CAPACITY_WARN)
					tyreData.setPressCapaticy(value);

				else if (setWhich == SET_PRESSHIGH_WARN)
					tyreData.setPressHighWarn(value);
				else if (setWhich == SET_PRESSLOW_WARN)
					tyreData.setPressLowWarn(value);
				else if (setWhich == SET_TEMPHIGH_WARN)
					tyreData.setTempHighWarn(value);
			}
			return ret;
		}

		int ProtocolSysParamSet(byte setWhich, int value) {
			byte sendbuff[] = new byte[100];
			byte recbuff[] = new byte[100];
			// APP_PRO_PARAMSET_ST pro_paramset_st = new APP_PRO_PARAMSET_ST();
			// short value;
			int send_len = 0, ret;
			int[] recLen = new int[1];
			// pro_paramset_st.cmd = TPMS_CMD.CMD_TPMS_SYSP_SET;

			int i = 0;
			sendbuff[i++] = TPMS_CMD.CMD_TPMS_SYSP_SET;
			sendbuff[i++] = setWhich;
			sendbuff[i++] = (byte) (value);
			sendbuff[i++] = (byte) (value >> 8);
			// send_len = sizeof(pro_paramset_st);
			// memcpy(send_buff, (byte *)&pro_paramset_st, send_len);
			send_len = i;
			ret = protocolExchange(TPMS_CMD.CMD_TPMS_SYSP_SET, sendbuff, send_len, recbuff, recLen, 1);

			if (ret == 0x00) {

				ret = byteArrayToInt(recbuff, 1);
				if (ret >= 0x00) {

					if (setWhich == SET_PRESS_UNIT)
						tyreData.SetPressUnit(value);
					else if (setWhich == SET_TEMP_UNIT)
						tyreData.setTempUnit(value);
					else if (setWhich == SET_CAPACITY_WARN)
						tyreData.setPressCapaticy(value);

					else if (setWhich == SET_PRESSHIGH_WARN)
						tyreData.setPressHighWarn(value);
					else if (setWhich == SET_PRESSLOW_WARN)
						tyreData.setPressLowWarn(value);
					else if (setWhich == SET_TEMPHIGH_WARN)
						tyreData.setTempHighWarn(value);
					else {
					}
					return 0;
				}

			}
			return ret;
		}

		int ProtocolTireExchange(byte tire1, byte tire2) {
			byte sendbuff[] = new byte[100];
			byte recbuff[] = new byte[100];
			// APP_PRO_TIRE_EXCHANGE_ST pro_tire_exchange_st;
			int send_len = 0, ret;
			int i = 0;
			int[] recLen = new int[1];
			sendbuff[i++] = TPMS_CMD.CMD_TPMS_TIRE_EXCHANGE;
			sendbuff[i++] = tire1;
			sendbuff[i++] = tire2;

			send_len = i;

			ret = protocolExchange(TPMS_CMD.CMD_TPMS_TIRE_EXCHANGE, sendbuff, send_len, recbuff, recLen, 1);
			if (ret == 0) {
				ret = byteArrayToInt(recbuff, 1);
				if (ret >= 0x00) {
					ret = 0;
				}
			}
			return ret;
		}

		int ProtocolTireRset(byte tire, int id) {
			// APP_PRO_TIRE_RSET_ST pro_tire_rset_st;
			byte sendbuff[] = new byte[100];
			byte recbuff[] = new byte[100];
			int send_len = 0, ret;
			int i = 0;
			int[] recLen = new int[1];
			sendbuff[i++] = TPMS_CMD.CMD_TPMS_TIRE_RSET;
			sendbuff[i++] = tire;
			sendbuff[i++] = (byte) (id >> 24);
			sendbuff[i++] = (byte) (id >> 16);
			sendbuff[i++] = (byte) (id >> 8);
			sendbuff[i++] = (byte) (id >> 0);
			send_len = i;
			ret = protocolExchange(TPMS_CMD.CMD_TPMS_TIRE_RSET, sendbuff, send_len, recbuff, recLen, 1);
			if (ret == 0) {
				ret = byteArrayToInt(recbuff, 1);
				if (ret >= 0x00) {
					ret = 0;
				}
			}
			return ret;
		}

		int ProtocolTireIDGet(byte tire, int[] id) {
			// APP_PRO_TIREIDGET_ST pro_tireidget_st;
			byte sendbuff[] = new byte[100];
			byte recbuff[] = new byte[100];
			int send_len = 0, ret;
			int i = 0;
			int[] recLen = new int[1];

			sendbuff[i++] = TPMS_CMD.CMD_TPMS_TIRE_ID_GET;
			sendbuff[i++] = tire;
			send_len = i;

			ret = protocolExchange(TPMS_CMD.CMD_TPMS_TIRE_ID_GET, sendbuff, send_len, recbuff, recLen, 1);

			if (ret == 0) {

				ret = byteArrayToInt(recbuff, 1);
				if (ret >= 0x00) {
					i = 6;
					// memcpy((byte *)&pro_tireidget_st, send_buff,
					// sizeof(pro_tireidget_st));
					// *id = pro_tireidget_st.id;
					id[0] = byteArrayToInt(recbuff, i);// (recbuff[i++]|recbuff[i++]<<8|recbuff[i++]<<16|recbuff[i++]<<24);
					tyreData.setID(tire, id[0]);
					return 0;
				}

			}

			return ret;
		}

		int ProtocolTireIDGet() {
			int[] id = new int[10];
			int ret = 0;
			// APP_PRO_TIREIDGET_ST pro_tireidget_st;
			for (byte i = 0; i < 4; i++) {
				ret = ProtocolTireIDGet(i, id);
				if (ret != 0x00)
					break;
			}
			return ret;
		}

		int ProtocolModeChange(byte sys_mode) {
			byte sendbuff[] = new byte[100];
			byte recbuff[] = new byte[100];
			int send_len = 0, ret;
			int i = 0;
			int[] recLen = new int[1];
			byte mStatusbackup = mStatus;// 在系统切换时要记录当前状态，并强制切到系统模式

			// if(mStatus == sys_mode){
			// return 0;
			// }
			mStatus = SYS_MODE.MODE_SETUP;
			Log.i(TAG, "set mode to " + sys_mode);
			sendbuff[i++] = TPMS_CMD.CMD_TPMS_MODE_CHANGE;
			sendbuff[i++] = sys_mode;

			send_len = i;
			if (sys_mode == SYS_MODE.MODE_HEARTBEAT){
				ret = protocolExchange(TPMS_CMD.CMD_TPMS_MODE_CHANGE, sendbuff, send_len);
			}else
				ret = protocolExchange(TPMS_CMD.CMD_TPMS_MODE_CHANGE, sendbuff, send_len, recbuff, recLen, 1);
			if (ret == 0) {
				ret = byteArrayToInt(recbuff, 1);
				if (ret >= 0x00) {
					if (sys_mode != SYS_MODE.MODE_HEARTBEAT)
						mStatus = sys_mode;
					return 0;
				}

			} else {
				mStatus = mStatusbackup;
			}
			return ret;
		}

		int ProtocolRf_cp(byte mode, int[] cp_value) {
			byte sendbuff[] = new byte[100];
			byte recbuff[] = new byte[100];
			int send_len = 0, ret;
			int i = 0;
			int[] recLen = new int[1];
			sendbuff[i++] = TPMS_CMD.CMD_TPMS_RF_CP;
			sendbuff[i++] = mode;
			sendbuff[i++] = (byte) cp_value[0];

			send_len = i;

			ret = protocolExchange(TPMS_CMD.CMD_TPMS_RF_CP, sendbuff, send_len, recbuff, recLen, 1);
			if (ret == 0) {
				ret = byteArrayToInt(recbuff, 1);
				if (ret >= 0) {
					if (mode == RF_CP_MODE.RF_CP_READ) {
						cp_value[0] = recbuff[5];
						cp_value[0] &= 0xff;// 转成无符号
					}
					return 0;
				}
			}

			return ret;
		}

		int ProtocolRfCpGet() {
			int[] RFCpValue = new int[1];
			int ret;
			ret = ProtocolRf_cp((byte) 0x00, RFCpValue);
			if (ret == 0x00) {
				tyreData.setRFCp(RFCpValue[0]);
			}
			return ret;
		}

		int ProtocolRfCpSet(int value) {
			int[] RFCpValue = new int[1];
			int ret;
			RFCpValue[0] = value;
			ret = ProtocolRf_cp((byte) 0x01, RFCpValue);
			if (ret == 0x00) {
				tyreData.setRFCp(RFCpValue[0]);
			}
			return ret;
		}

		int ProtocolAbout() {
			byte sendbuff[] = new byte[100];
			byte recbuff[] = new byte[100];
			int send_len = 0, ret;
			int i = 0;
			int[] recLen = new int[1];

			sendbuff[i++] = TPMS_CMD.CMD_TPMS_VER;

			send_len = i;

			ret = protocolExchange(TPMS_CMD.CMD_TPMS_VER, sendbuff, send_len, recbuff, recLen, 1);
			if (ret == 0) {
				// memcpy((int8t *)&ret,&send_buff[1],sizeof(ret));
				i = 1;
				ret = byteArrayToInt(recbuff, 1);
				// int len = recbuff[i++];
				if (ret > 0) /* API调用成功 */
				{
					// memcpy(nane,&send_buff[1 + sizeof(ret)],11);

					// name = _name.getBytes();

					// String _ver;
					try {
						String _name = new String(recbuff, 1 + 4, 11, "UTF-8");
						M_name = _name;
						// _name = new String(recbuff,16,11);
						Log.i(TAG, "版本名称" + _name);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					i = 16;
					M_version.version = recbuff[i++];
					M_version.ubuildtime = (short) (recbuff[i++] | recbuff[i++] << 8);
					M_version.builddate = byteArrayToInt(recbuff, i);
					M_version.hard = byteArrayToInt(recbuff, i + 4);
					M_version.procotrol = recbuff[i + 4 + 4];
					return 0;
					// ver = _ver.getBytes();
					// memcpy((byte *)ver,&send_buff[1 + sizeof(ret) +
					// 11],sizeof(Sys_Version));
				}
			}
			return ret;
		}

		int ProtocolTPMSInfoUpload(byte[] recBuf, int len) {

			byte update_tire = 0, i = 0;
			Tire_Inf info = new Tire_Inf();

			byte[] recData = new byte[100];
			int[] recLen = new int[1];
			byte[] rnd = new byte[1];

			int ret = Protocol_Decode(rnd, recBuf, len, recData, recLen);// 进行解码

			if (ret == 0) {

				// debugOut("rec tyre command "+recData[0]);
				if (TPMS_CMD.CMD_TPMS_TPMSINF == recData[0]) {
					diaryInsertExchange(null, 0x00, recData, recLen[0]);
					update_tire = recData[1];
					update_tire = recData[1];
					debugOut("rec tyre num" + update_tire);
					if (recLen[0] == update_tire * 16 + 2) {
						for (i = 0; i < update_tire; i++) {
							// buff_prt = recData[2] + i * 16;
							// memcpy((int8u *)&tire_inf_st, buff_prt,
							// sizeof(tire_inf_st));
							// tire_index = tire_inf_st.update;
							info.supdate = byteArrayToInt(recData, 2 + i * 16);
							info.alarm_flag = byteArrayToInt(recData, 2 + i * 16 + 4);
							info.pressure_value = byteArrayToInt(recData, 2 + i * 16 + 8);
							info.tmper_value = byteArrayToInt(recData, 2 + i * 16 + 12);
							debugOut("update flag=" + info.supdate);
							if (info.supdate >= 0) {
								// tyreData.setPressValue(i,info.pressure_value);
								// tyreData.setTempValue (i,info.tmper_valude);
								tyreData.setInfoValue(info.supdate, info);
							}
							// HKAPPSensor_Tab[tire_index].recv_ok = 1;
							// HKAPPSensor_Tab[tire_index].presser =
							// tire_inf_st.pressure_value;
							// HKAPPSensor_Tab[tire_index].tepmer =
							// tire_inf_st.tmper_valude;
							// HKAPPSensor_Tab[tire_index].recv_timer = 0; //
							// /清除无信号计数器

							// App_TPMS_Menu_Status.update[tire_index] = 1;

							// 报警标志位
							// HKAPP_Alarm.alarm_st[tire_index] =
							// tire_inf_st.alarm_flag;
						}
					}
					return 0x00;
				} else {
					// Log.e(TAG,"cmd err");
					return -TPMS_PROTOCOL_ERRCODE.TPMS_ERR_NULLFUN;
				}

			} else /* 解码出错 */
			{
				Log.e(TAG, "de_code err =" + ret);
				// dst_buff[0] = CMD_TPMS_COMM_FAIL;
				// memcpy((int8t *)&dst_buff[1], (int8t *)&pro_decode_ret, 4);
				// pro_decode_ret = 5;
			}

			return ret;
		}

		int ProtocolTPMSTyreChange(int which, byte mode, int time) {
			byte sendbuff[] = new byte[100];
			byte recbuff[] = new byte[100];
			int send_len = 0, ret;
			int i = 0;
			int[] recLen = new int[1];

			// byte mStatusbackup = mStatus;//在系统切换时要记录当前状态，并强制切到系统模式

			// mStatus = SYS_MODE.MODE_SETUP;
			sendbuff[i++] = TPMS_CMD.CMD_TPMS_TIRE_AUTO_PAIR;
			// sendbuff[i++] = sys_mode;
			sendbuff[i++] = (byte) which;
			sendbuff[i++] = (byte) mode;

			sendbuff[i++] = (byte) (time >> 0);
			sendbuff[i++] = (byte) (time >> 8);
			sendbuff[i++] = (byte) (time >> 16);
			sendbuff[i++] = (byte) (time >> 24);

			send_len = i;

			ret = protocolExchange(TPMS_CMD.CMD_TPMS_TIRE_AUTO_PAIR, sendbuff, send_len, recbuff, recLen, 1);
			if (ret == 0) {
				ret = byteArrayToInt(recbuff, 1);
				if (ret != 0x00) {
					Log.i(TAG, "ProtocolTPMSTyreChange ret=" + ret);
					return ret;
				}
				// mStatus = sys_mode;
			} else {
				// mStatus = mStatusbackup;
			}
			return ret;
		}

		int ProtocolTPMSTyreChangeResultReport(byte[] recBuf, int len, int[] result) {

			byte[] recData = new byte[100];
			int[] recLen = new int[1];
			byte[] rnd = new byte[1];
			Tire_Inf info = new Tire_Inf();
			int ret = Protocol_Decode(rnd, recBuf, len, recData, recLen);// 进行解码

			if (ret == 0) {

				// debugOut("rec tyre command "+recData[0]);
				if (TPMS_CMD.CMD_TPMS_TIRE_AUTO_PAIR_RESULT == recData[0]) {
					diaryInsertExchange(null, 0x00, recData, recLen[0]);
					ret = byteArrayToInt(recData, 1);
					if (ret >= 0x00) {
						info.supdate = byteArrayToInt(recData, 5 + 0);
						info.alarm_flag = byteArrayToInt(recData, 5 + 4);
						info.pressure_value = byteArrayToInt(recData, 5 + 8);
						info.tmper_value = byteArrayToInt(recData, 5 + 12);
						debugOut("update flag=" + info.supdate);
						if (info.supdate >= 0) {
							// tyreData.setPressValue(i,info.pressure_value);
							// tyreData.setTempValue (i,info.tmper_valude);
							tyreData.setInfoValue(info.supdate, info);
						}
						result[0] = 0x00;
					} else {
						result[0] = ret;
					}
					return 0x00;
				} else {
					// Log.e(TAG,"cmd err");
					return -TPMS_PROTOCOL_ERRCODE.TPMS_ERR_NULLFUN;
				}

			} else /* 解码出错 */
			{
				Log.e(TAG, "de_code err =" + ret);
				// dst_buff[0] = CMD_TPMS_COMM_FAIL;
				// memcpy((int8t *)&dst_buff[1], (int8t *)&pro_decode_ret, 4);
				// pro_decode_ret = 5;
			}

			return ret;
		}

		int ProtocolTPMSTyreChangeConfirm(int value) {
			byte sendbuff[] = new byte[100];
			int send_len = 0, ret;
			int i = 0;
			// byte mStatusbackup = mStatus;//在系统切换时要记录当前状态，并强制切到系统模式

			// mStatus = SYS_MODE.MODE_SETUP;
			sendbuff[i++] = TPMS_CMD.CMD_TPMS_TIRE_AUTO_PAIR_RESULT;
			sendbuff[i++] = (byte) (value >> 24);
			sendbuff[i++] = (byte) (value >> 16);
			sendbuff[i++] = (byte) (value >> 8);
			sendbuff[i++] = (byte) (value >> 0);

			send_len = i;

			ret = protocolSendData(sendbuff, send_len);
			return ret;
		}

	}

	bluethoothData mBluethoothData;
	/*----------------------------------------------------------------------*/
	// warnPressPara WarnParaArray;

	int iCarSeriesIndex;
	int iCarTypeIndex;

	String sCarSeries;
	String sYear;
	String sPressure;
	String sName;
	String sNickName;
	String sCode;
	String sCodeAffirm;
	String sEmail;
	String sProductSN;

	String sConnectedBlueAddr;
	boolean isConnected;

	int iSleepTime;
	int iLanguageType = 0x00;

	int iThemeIndex;
	int iThemeIndexPre;
	int iCardIndex;// 车型标号
	int iCardIndexPre;
	int iRecordValue;
	int iRFCpValue;
	Context context;

	int activityStatus;
	int activityEventNum = 0;
	boolean isOnExit = false;

	blueConnectStatus blueStatus = new blueConnectStatus();

	class blueConnectStatus {
		private volatile boolean isConnecting = false;

		synchronized boolean isConnecting() {
			return isConnecting;

		}

		synchronized boolean connectingWait() {
			if (isConnecting == false) {
				try {
					super.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return isConnecting;

		}

		synchronized void ConnectingSet(boolean status) {
			isConnecting = status;
			if (isConnecting) {
				super.notify();
			}
		}
	}

	DataInfo getRecordInfo() {
		DataInfo recordInfo = new DataInfo();
		recordInfo.baxMax = 150;
		recordInfo.baxMin = 50;
		recordInfo.baxMove = 1;
		recordInfo.value = iRecordValue;
		recordInfo.unitName = "条";
		return recordInfo;

	}

	DataInfo getRFCpInfo() {
		DataInfo recordInfo = new DataInfo();
		recordInfo.baxMax = 255;
		recordInfo.baxMin = 0;
		recordInfo.baxMove = 1;
		recordInfo.value = iRFCpValue;
		recordInfo.unitName = "PF";
		return recordInfo;

	}

	SysParam(Context context) {
		Log.w(TAG, "create application");
		tyreData = new TyreData();
		mBluethoothData = new bluethoothData(context);
		// WarnParaArray = new warnPressPara();
		activityStatus = 0;
		ParamLoad(context);
		Log.w("getInstance", "add is " + this);

	}

	public synchronized static SysParam getInstance(Context context) {
		if (null == instance) {
			instance = new SysParam(context);

		}
		// Log.w("getInstance","add is "+instance);
		return instance;
	}

	// add Activity
	public void addActivity(Activity activity) {
		// isOnExit = false;
		// mList.add(activity);
		mList.add(0, activity);
		// mList.
	}

	public static class Builder implements View.OnClickListener, View.OnTouchListener {
		private Activity context;
		// private Activity acivity;
		private String title;
		// private View layout;
		private String BackButtonText;
		private OnClickListener BackButtonClickListener;
		Button BackButton;

		public Builder(Activity context) {
			this.context = context;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */

		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setView(View view) {
			// this.layout = view;
			return this;
		}

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setBackButton(String BackButtonText, OnClickListener listener) {
			this.BackButtonText = BackButtonText;
			this.BackButtonClickListener = listener;
			return this;
		}

		public void create() {
			// context.findViewById(R.id.titleText);
			((TextView) context.findViewById(R.id.titleText)).setText(title);
			if (BackButtonText != null) {
				// ((Button) layout.findViewById(R.id.positiveButton))
				// .setText(positiveButtonText);
				BackButton = ((Button) context.findViewById(R.id.BackButton));
				// BackButton.setText(BackButtonText);
				if (BackButtonClickListener != null) {

					BackButton.setVisibility(View.VISIBLE);
					BackButton.setOnClickListener(this);
					// BackButton.setOnTouchListener(this);
				} else {
					BackButton.setVisibility(View.GONE);
				}
			}
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				// 更改为按下时的背景图片
				v.getBackground().setAlpha(150);//
				v.invalidate();

			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				// 改为抬起时的图片
				v.getBackground().setAlpha(255);// 还原图片
				v.invalidate();

			}
			return false;
		}

		@Override
		public void onClick(View v) {
			if (v == BackButton) {
				BackButtonClickListener.onClick(v);
			}
		}
	}

	synchronized void activityStart() {

		activityStatus++;
		activityEventNum++;
		super.notify();
	}

	synchronized void activityStop() {

		activityStatus--;
		activityEventNum++;
		super.notify();
	}

	synchronized int activityStatusListen() {

		if (--activityEventNum > 0x00) {
			return activityStatus;
		}
		try {
			super.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return activityStatus;
	}

	synchronized int activityStatusGet() {
		return activityStatus;
	}

	void stopService(Context context) {
		Intent intent = new Intent(SysControllerService.STOP_SERVICE);
		// intent.putExtra("STATE", message);
		Log.i(TAG, "request stop servie");
		// if(context != null)
		context.sendBroadcast(intent);
	}

	private Resources getResources() {
		// TODO Auto-generated method stub
		Resources mResources = null;
		mResources = getResources();
		return mResources;
	}

	public void exit(Activity context) {
		final Activity curContext = context;
		LayoutInflater inflater = LayoutInflater.from(curContext);
		View view = null;
		boolean dismiss = true;
		SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(curContext);
		view = inflater.inflate(R.layout.set_custom_dialog, null);

		// customBuilder.setIcon(R.drawable.app_small_icon)
		// .setTitle(context.getResources().getString(R.string.app_name))
		customBuilder.setMessage("确认要退出" + context.getResources().getString(R.string.app_name) + "吗?")
				.setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				})

				.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						isOnExit = true;
						stopService(curContext);
						saveParam(curContext);
						/*
						 * Intent mIntent = new Intent();
						 * mIntent.setClass(curContext,
						 * TPMSAppEntryActivity.class); //这里设置flag还是比较 重要的
						 * mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						 * //发出退出程序指示 mIntent.putExtra("flag",
						 * EXIT_APPLICATION); curContext.startActivity(mIntent);
						 */

						try {
							for (Activity activity : mList) {
								Log.w(TAG, "finish the activity " + activity);
								if (activity != null)
									// mList.remove(activity);
									activity.finish();

							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).create(view, dismiss).show();

	}

	synchronized void ParamLoad(Context context) {
		// StorageMyDB dba = new StorageMyDB(this);
		SharedPreferences settings = context.getSharedPreferences("myConfig", Activity.MODE_PRIVATE);
		// dba.open();

		// Log.v(TAG,"record count="+dba.getDiaresCount());

		// dba.close();
		Log.v(TAG, "ParamLoad");
		tyreData.SetPressUnit(settings.getInt("PressureUnit", 0x00)); /* 压力单位获取 */
		tyreData.setTempUnit(settings.getInt("TempUnit", 0x00)); /* 温度单位获取 */

		iSleepTime = settings.getInt("SleepValue", 0x00);
		iLanguageType = settings.getInt("LanguageSet", 0x00);
		iRecordValue = 500;/* 日志记录条数 */
		iThemeIndex = settings.getInt("ThemeIndex", 0x00);
		Log.i(TAG, "theme index is " + iThemeIndex);
		iCardIndex = settings.getInt("CardIndex", 0x00);
		// notificationStatus = settings.getBoolean("notification status",
		// false);
		tyreData.setPressCapaticy(settings.getInt("WarnCapacityValue", 240));
		tyreData.setPressLowWarn(settings.getInt("WarnPressLowValue", 180));
		tyreData.setPressHighWarn(settings.getInt("WarnPressHighValue", 300));
		tyreData.setTempHighWarn(settings.getInt("WarnTempHighValue", 70));

		Tire_Inf info = tyreData.getInfoValue(lf);
		info.pressure_value = settings.getInt("leftFrontPressure", 0x00);
		info.tmper_value = settings.getInt("leftFrontTemp", 0x00);
		info.supdate = settings.getInt("leftFontStatus", -0x01);
		info.alarm_flag = settings.getInt("leftFontAlarm", 0x00);
		tyreData.setInfoValue(lf, info);

		info = tyreData.getInfoValue(rf);
		info.pressure_value = settings.getInt("rightFrontPressure", 0x00);
		info.tmper_value = settings.getInt("rightFrontTemp", 0x00);
		info.supdate = settings.getInt("rightFontStatus", -0x01);
		info.alarm_flag = settings.getInt("rightFontAlarm", 0x00);
		tyreData.setInfoValue(rf, info);

		info = tyreData.getInfoValue(lb);
		info.pressure_value = settings.getInt("leftBackPressure", 0x00);
		info.tmper_value = settings.getInt("leftBackTemp", 0x00);
		info.supdate = settings.getInt("leftBackStatus", -0x01);
		info.alarm_flag = settings.getInt("leftBackAlarm", 0x00);

		tyreData.setInfoValue(lb, info);

		info = tyreData.getInfoValue(rb);
		info.pressure_value = settings.getInt("rightBackPressure", 0x00);
		info.tmper_value = settings.getInt("rightBackTemp", 0x00);
		info.supdate = settings.getInt("rightBackStatus", -0x01);
		info.alarm_flag = settings.getInt("rightBackAlarm", 0x00);
		tyreData.setInfoValue(rb, info);

		// isConnecting = settings.getBoolean("blue connectStatus", false);
		sConnectedBlueAddr = settings.getString("connected addr of latest times", "");
		iCarSeriesIndex = settings.getInt("Series", 0);
		iCarTypeIndex = settings.getInt("Type", 0);
		sCarSeries = settings.getString("CarSeries", "");
		sYear = settings.getString("Year", "");
		sPressure = settings.getString("Pressure", "");
		sName = settings.getString("Name", "");
		sNickName = settings.getString("NickName", "");
		sCode = settings.getString("Code", "");
		sCodeAffirm = settings.getString("CodeAffirm", "");
		sEmail = settings.getString("Email", "");
		sProductSN = settings.getString("ProductSN", "");

	}

	synchronized void saveParam(Context context) {
		Log.v("TPMS Sysapplication", "save the param");

		SharedPreferences settings = context.getSharedPreferences("myConfig", Activity.MODE_PRIVATE);
		Editor mEditor;
		// Log.v("LOG ","onStop is starting");
		mEditor = settings.edit();

		// mEditor.commit();
		if (tyreData.getPressCapacityWarn().value != 0) {
			mEditor.putInt("WarnCapacityValue", tyreData.getPressCapacityWarn().value);
			mEditor.putInt("WarnPressLowValue", tyreData.getPressLowWarn().value);
			mEditor.putInt("WarnPressHighValue", tyreData.getPressHighWarn().value);
			mEditor.putInt("WarnTempHighValue", tyreData.getTempHighWarn().value);

			mEditor.putInt("RecordValue", iRecordValue);
			// mEditor.commit();

			// mEditor = settings.edit();
			mEditor.putInt("SleepValue", iSleepTime);
			// mEditor.commit();

			// mEditor = settings.edit();
			mEditor.putInt("LanguageSet", iLanguageType);
			// mEditor.commit();
			mEditor.putInt("PressureUnit", tyreData.getPressUnit());
			// mEditor = settings.edit();
			mEditor.putInt("TempUnit", tyreData.getTempUnit());

			mEditor.putInt("ThemeIndex", iThemeIndex);
			// mEditor.putBoolean("blue connectStatus", isConnecting);
			mEditor.putString("connected addr of latest times", sConnectedBlueAddr);
			mEditor.putInt("CardIndex", iCardIndex);
			mEditor.putInt("Series", iCarSeriesIndex);
			mEditor.putInt("Type", iCarTypeIndex);
			mEditor.putString("CarSeries", sCarSeries);
			mEditor.putString("Year", sYear);
			mEditor.putString("Pressure", sPressure);
			mEditor.putString("Name", sName);
			mEditor.putString("NickName", sNickName);
			mEditor.putString("Code", sCode);
			mEditor.putString("CodeAffirm", sCodeAffirm);
			mEditor.putString("Email", sEmail);
			mEditor.putString("ProductSN", sProductSN);
			// mEditor.putBoolean("notification status", notificationStatus);
		} // 内存被系统清理情况下不保存
			// mEditor = settings.edit();
		Log.i(TAG, "set lf temp is" + tyreData.getTempValue(lf).value);
		mEditor.putInt("leftFrontTemp", tyreData.getTempValue(lf).value);
		mEditor.putInt("leftFrontPressure", tyreData.getPressValue(lf).value);

		mEditor.putInt("rightFrontTemp", tyreData.getTempValue(rf).value);
		mEditor.putInt("rightFrontPressure", tyreData.getPressValue(rf).value);

		mEditor.putInt("leftBackTemp", tyreData.getTempValue(lb).value);
		mEditor.putInt("leftBackPressure", tyreData.getPressValue(lb).value);

		mEditor.putInt("rightBackTemp", tyreData.getTempValue(rb).value);
		mEditor.putInt("rightBackPressure", tyreData.getPressValue(rb).value);

		mEditor.putInt("leftFontStatus", tyreData.update_status[lf]);
		mEditor.putInt("rightFontStatus", tyreData.update_status[rf]);
		mEditor.putInt("leftBackStatus", tyreData.update_status[lb]);
		mEditor.putInt("rightBackStatus", tyreData.update_status[rb]);

		mEditor.putInt("leftFontAlarm", tyreData.alarm_flag[lf]);
		mEditor.putInt("rightFontAlarm", tyreData.alarm_flag[rf]);
		mEditor.putInt("leftBackAlarm", tyreData.alarm_flag[lb]);
		mEditor.putInt("rightBackAlarm", tyreData.alarm_flag[rb]);

		mEditor.commit();
	}

	public class SysExitSelfReceiver extends BroadcastReceiver {

		public static final String AUTODESTORY = "AutoDestory";
		private Activity context;
		private IntentFilter DisconnectFilter = new IntentFilter(AUTODESTORY);

		SysExitSelfReceiver(Activity context) {
			this.context = context;
		}

		void joinSelf() {
			context.registerReceiver(this, DisconnectFilter);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// if(D) Log.e(TAG, "** ON RECEIVE **");
			String action = intent.getAction();
			if (action.equals(AUTODESTORY)) {
				this.context.finish();
			} else {
				Log.e(TAG, "another action: " + action);
			}
		}
	}
}
