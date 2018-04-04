package com.hongking.hktpms;


import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.hongking.hktpms.Utils.CH34xAndroidDriver;
import com.hongking.hktpms.views.EventBus_INITCONFIG;

import de.greenrobot.event.EventBus;

public class SerialPortConnModel {

    private final String TAG = "SerialPortConnModel";

    private Context context;
    private Handler mHandler;
    private boolean isOpen = false;
    private boolean i = false;

    int actualNumBytes;

    /* thread to read the data */
    public ReadThread handlerThread;
    protected final Object ThreadLock = new Object();

    public SerialPortConnModel(Context context, Handler handler) {
        this.context = context;
        mHandler = handler;

		/* allocate buffer */
    }

    byte stopBit = 1; /* 1:1stop bits, 2:2 stop bits */
    byte dataBit = 8; /* 8:8bit, 7: 7bit 6: 6bit 5: 5bit */
    byte parity = 0; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
    byte flowControl = 0; /* 0:none, 1: flow control(CTS,RTS) */
    boolean flag = true;

    public void startSession() {
        Log.i("20161130", "start session");
        if (!isOpen && SysApplication.driver != null&&flag) {
            flag = false;
           /* new CountDownTimer(5000, 5000) {

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    flag = true;
                }
            }.start();*/
            Log.i("20161130", "really start session");

            try {
                if (2 == SysApplication.driver.ResumeUsbList()) {
                // ResumeUsbList方法用于枚举CH34X设备以及打开相关设备
                    Log.e("20161130", "打开设备失败!");
                    SysApplication.driver.CloseDevice();
                } else {
                    try {
                        if (!SysApplication.driver.UartInit()) {
                        // 对串口设备进行初始化操作
                            Log.i("20161130", "设备初始化失败!");
                            return;
                        } else {
                            if (SysApplication.driver.SetConfig(115200, dataBit, stopBit, parity, flowControl)) {
                                Log.d("20161130", "Configed");
                            }
                        }
                        Log.d("20161130", " mHandler.obtainMessage");
                        isOpen = true;
//                        mHandler.obtainMessage(UsbControllerService.MESSAGE_INIT_CONFIG, -1, -1, "").sendToTarget();
                        EventBus.getDefault().post(new EventBus_INITCONFIG(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("20161130", "ConnThread");
                        ConnThread conn = new ConnThread();
                        conn.start();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.i("20161130", "设备已开启");
        }

    }

    public void write(byte[] msg) {
        int length = 0;
        try {
            length = SysApplication.driver.WriteData(msg, msg.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (msg.length != length) {
//            Toast.makeText(context, "WriteData really start session", Toast.LENGTH_SHORT).show();
            mHandler.obtainMessage(UsbControllerService.MESSAGE_CONNECT_RESTART, -1, -1, msg).sendToTarget();
        } else {
            mHandler.obtainMessage(UsbControllerService.MESSAGE_WRITE, -1, -1, msg).sendToTarget();
        }
        Log.d(TAG, "WriteData Length is " + length + " msg: " + msg.toString());
    }

    public void disconnectDevice() {
        if (isOpen == true) {
            isOpen = false;
            if (SysApplication.driver != null) {
                SysApplication.driver.CloseDevice();
            }

            if (handlerThread != null) {
                handlerThread.interrupt();
                handlerThread = null;
            }
        }
    }

    public void reconnectDevice() {
        Log.e(TAG, "reconnect device");
        if (isOpen == true) {
            isOpen = false;
        }
        if (SysApplication.driver != null) {
            try {
                if (handlerThread != null) {
                    handlerThread.interrupt();
                    handlerThread = null;
                }
                SysApplication.driver.CloseDevice();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if(SysApplication.driver==null) {
            SysApplication.driver = new CH34xAndroidDriver((UsbManager) context.getSystemService(Context.USB_SERVICE), context.getApplicationContext(), "cn.wch.wchusbdriver.USB_PERMISSION");
        }}
        startSession();
    }

    public void closeDevice() {
        if (SysApplication.driver != null) {
            try {
                isOpen = false;
                handlerThread.interrupt();
                handlerThread = null;
                SysApplication.driver.CloseDevice();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * usb input data handler
     */
    private class ReadThread extends Thread {
        /* constructor */
        ReadThread() {
            this.setPriority(Thread.MAX_PRIORITY);
        }

        public void run() {
            byte tempBuf[];
            char r_buf[] = new char[1024];
            int i;
            while (isOpen) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
                // Log.d(TAG, "Thread");
                synchronized (ThreadLock) {
                    if (SysApplication.driver != null) {
                        byte[] readBuffer = new byte[1024];
                        try {
                            actualNumBytes = SysApplication.driver.ReadData(r_buf, 1024);
                            if (actualNumBytes > 0) {
                                for (i = 0; i < actualNumBytes; i++) {
                                    readBuffer[i] = (byte) r_buf[i];
                                }
                                mHandler.obtainMessage(SysControllerService.MESSAGE_READ, actualNumBytes, -1, readBuffer).sendToTarget();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private Integer initTimes = 0;

    private class ConnThread extends Thread {
        public void run() {
            try {
                Thread.sleep(5000);
                Log.e("20161130", "sleep(2000)");
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
            do {
                synchronized (initTimes) {
                    try {

                        if (!SysApplication.driver.UartInit()) {
                            // 对串口设备进行初始化操作
                            Log.i("20161130", "设备初始化失败!");
                            return;
                        } else {
                            if (SysApplication.driver.SetConfig(115200, dataBit, stopBit, parity, flowControl)) {
                                Log.d("20161130", "Configed");
                            }
                        }
                        initTimes = 0;
                        isOpen = true;
                        mHandler.obtainMessage(UsbControllerService.MESSAGE_INIT_CONFIG, -1, -1, "").sendToTarget();
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            initTimes++;
                            Thread.sleep(5000);
                            Log.e("20161130", "sleep(2001)");
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                        if (initTimes > 30) {
                            initTimes = 0;
                            try {
                                if (2 != SysApplication.driver.ResumeUsbList()) {// ResumeUsbList方法用于枚举CH34X设备以及打开相关设备
                                    SysApplication.driver.CloseDevice();
                                }
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
//						mHandler.obtainMessage(UsbControllerService.MESSAGE_CONNECT_AGAIN, -1, -1, "").sendToTarget();
                }
            } while (!isOpen);

        }
    }

    public void startReadThread() {
        Log.e("20161205","这里发送了个广播UsbControllerService.MESSAGE_CONNECT");
        mHandler.obtainMessage(UsbControllerService.MESSAGE_CONNECT, -1, -1, "").sendToTarget();
        handlerThread = new ReadThread();
        handlerThread.start();
    }

}
