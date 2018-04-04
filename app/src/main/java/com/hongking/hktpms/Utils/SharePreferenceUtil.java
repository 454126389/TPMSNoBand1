package com.hongking.hktpms.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016/11/25.
 */

public class SharePreferenceUtil {

    private final String SP_NAME = "tpms";
    private final String TPMS_CONNECT_TYPE = "connect_type";
    private final String TPMS_BT_ADDRESS = "bt_address";
    private final String firsttimeoncreate = "isfirsttime";
    private final String shutdowntime = "shutdowntime";
    public static final int CONNECT_TYPE_USB = 1;
    public static final int CONNECT_TYPE_BLUETOOTH = 2;

    private final String rebootflag = "noflag";
    private final String autostart = "isflag";
    private Context context;
    private SharedPreferences sp;

    public SharePreferenceUtil(Context context){
        this.context = context;
        sp = context.getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE);
    }

    public void setConnectType(int type){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(TPMS_CONNECT_TYPE,type);
        editor.commit();
    }
    public void setFirsttimeoncreate(String firsttimeoncreated){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(firsttimeoncreate, firsttimeoncreated);
        editor.commit();
    }

    public void setRebootflag(String mmrebootflag){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(rebootflag, mmrebootflag);
        editor.commit();
    }

    public void setAutostart(String mmautostart){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(autostart, mmautostart);
        editor.commit();
    }

    public void setshutdowntime(String shutdowntimes){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(shutdowntime, shutdowntimes);
        editor.commit();
    }
    public void setBT_ADDRESS(String address){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(TPMS_BT_ADDRESS, address);
        editor.commit();
    }

    public int getConnectType(){
        return sp.getInt(TPMS_CONNECT_TYPE,0);
    }
    public String getBT_ADDRESS(){
        return sp.getString(TPMS_BT_ADDRESS, "");
    }
    public String getFirsttimeoncreate(){
        return sp.getString(firsttimeoncreate,"");
    }

    public String getRebootflag(){
        return sp.getString(rebootflag,"");
    }

    public String getAutostart(){
        return sp.getString(autostart,"");
    }

    public String getshutdowntime(){
        return sp.getString(shutdowntime,"");
    }


}
