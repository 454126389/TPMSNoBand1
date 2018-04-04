package com.hongking.hktpms.views;

/**
 * Created by Administrator on 2016/12/2.
 */
public class EventBus_bt_connect {
     int i;//i=0,就是失败，需要重新连接

    public EventBus_bt_connect(int i) {
        this.i = i;
    }

    public int getI() {

        return i;
    }

    public void setI(int i) {
        this.i = i;
    }
}
