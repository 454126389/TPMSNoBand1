package com.hongking.hktpms.views;

/**
 * Created by Administrator on 2016/11/23.
 */
public class EventBus {
    int i;//1代表关闭，0代表开得时候关，关的时候开。

    public EventBus(int i) {
        this.i = i;
    }

    public int getI() {

        return i;
    }

    public void setI(int i) {
        this.i = i;
    }


}
