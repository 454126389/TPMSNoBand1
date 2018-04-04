//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hongking.hktpms.Utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CH34xAndroidDriver {
    public static final String TAG = "123456789";
    private UsbManager mUsbmanager;
    private PendingIntent mPendingIntent;
    private UsbDevice mUsbDevice;
    private UsbInterface mInterface;
    private UsbEndpoint mCtrlPoint;
    private UsbEndpoint mBulkInPoint;
    private UsbEndpoint mBulkOutPoint;
    private UsbDeviceConnection mDeviceConnection;
    private Context mContext;
    private String mString;
    private boolean BroadcastFlag = false;
    public boolean READ_ENABLE = false;
    public CH34xAndroidDriver.read_thread readThread;
    private byte[] readBuffer = new byte[65536];
    private byte[] usbdata = new byte[1024];
    private int writeIndex = 0;
    private int readIndex = 0;
    private int readcount;
    private int totalBytes;
    private ArrayList<String> DeviceNum = new ArrayList();
    protected final Object ReadQueueLock = new Object();
    protected final Object WriteQueueLock = new Object();
    private int DeviceCount;
    private int mBulkPacketSize;
    final int maxnumbytes = 65536;
    public int WriteTimeOutMillis;
    public int ReadTimeOutMillis;
    private int DEFAULT_TIMEOUT = 500;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(!"android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)) {
                if(CH34xAndroidDriver.this.mString.equals(action)) {
                    synchronized(this) {
                        UsbDevice localUsbDevice = (UsbDevice)intent.getParcelableExtra("device");
                        if(intent.getBooleanExtra("permission", false)) {
                            CH34xAndroidDriver.this.OpenUsbDevice(localUsbDevice);
                        } else {
//                            Toast.makeText(CH34xAndroidDriver.this.mContext, "Deny USB Permission", Toast.LENGTH_SHORT).show();
                            Log.d("123456789", "permission denied");
                        }
                    }
                } else if("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)) {
                    CH34xAndroidDriver.this.CloseDevice();
                } else {
                    Log.d("123456789", "......");
                }

            }
        }
    };

    public CH34xAndroidDriver(UsbManager manager, Context context, String AppName) {
        this.mUsbmanager = manager;
        this.mContext = context;
        this.mString = AppName;
        this.WriteTimeOutMillis = 10000;
        this.ReadTimeOutMillis = 10000;
        this.ArrayAddDevice("1a86:7523");
        this.ArrayAddDevice("1a86:5523");
    }

    private void ArrayAddDevice(String str) {
        this.DeviceNum.add(str);
        this.DeviceCount = this.DeviceNum.size();
    }

    public boolean SetTimeOut(int WriteTimeOut, int ReadTimeOut) {
        this.WriteTimeOutMillis = WriteTimeOut;
        this.ReadTimeOutMillis = ReadTimeOut;
        return true;
    }

    public synchronized void OpenUsbDevice(UsbDevice mDevice) {
        if(mDevice != null) {
            UsbInterface intf = this.getUsbInterface(mDevice);
            if(mDevice != null && intf != null) {
                UsbDeviceConnection localObject = this.mUsbmanager.openDevice(mDevice);
                if(localObject != null && ((UsbDeviceConnection)localObject).claimInterface(intf, true)) {
                    this.mUsbDevice = mDevice;
                    this.mDeviceConnection = (UsbDeviceConnection)localObject;
                    this.mInterface = intf;
                    if(!this.enumerateEndPoint(intf)) {
                        return;
                    }

//                    Toast.makeText(this.mContext, "Device Has Attached to Android", Toast.LENGTH_SHORT).show();
                    if(!this.READ_ENABLE) {
                        this.READ_ENABLE = true;
                        this.readThread = new CH34xAndroidDriver.read_thread(this.mBulkInPoint, this.mDeviceConnection);
                        this.readThread.start();
                    }

                    return;
                }
            }

        }
    }

    public synchronized void CloseDevice() {
        try {
            Thread.sleep(10L);
        } catch (Exception var2) {
            ;
        }

        if(this.mDeviceConnection != null) {
            if(this.mInterface != null) {
                this.mDeviceConnection.releaseInterface(this.mInterface);
                this.mInterface = null;
            }

            this.mDeviceConnection.close();
        }

        if(this.mUsbDevice != null) {
            this.mUsbDevice = null;
        }

        if(this.mUsbmanager != null) {
            this.mUsbmanager = null;
        }

        if(this.READ_ENABLE) {
            this.READ_ENABLE = false;
        }

        if(this.BroadcastFlag) {
            this.mContext.unregisterReceiver(this.mUsbReceiver);
            this.BroadcastFlag = false;
        }

    }

    public boolean UsbFeatureSupported() {
        boolean bool = this.mContext.getPackageManager().hasSystemFeature("android.hardware.usb.host");
        return bool;
    }

    public int ResumeUsbList() {
        this.mUsbmanager = (UsbManager)this.mContext.getSystemService("usb");
        this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(this.mString), 0);
        HashMap deviceList = this.mUsbmanager.getDeviceList();
        if(deviceList.isEmpty()) {
//            Toast.makeText(this.mContext, "No Device Or Device Not Match", Toast.LENGTH_SHORT).show();
            return 2;
        } else {
            Iterator localIterator = deviceList.values().iterator();

            while(localIterator.hasNext()) {
                UsbDevice localUsbDevice = (UsbDevice)localIterator.next();

                for(int i = 0; i < this.DeviceCount; ++i) {
                    if(String.format("%04x:%04x", new Object[]{Integer.valueOf(localUsbDevice.getVendorId()), Integer.valueOf(localUsbDevice.getProductId())}).equals(this.DeviceNum.get(i))) {
                        IntentFilter filter = new IntentFilter(this.mString);
                        filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
                        this.mContext.registerReceiver(this.mUsbReceiver, filter);
                        this.BroadcastFlag = true;
                        if(this.mUsbmanager.hasPermission(localUsbDevice)) {
                            this.OpenUsbDevice(localUsbDevice);
                        } else {
                            BroadcastReceiver var6 = this.mUsbReceiver;
                            synchronized(this.mUsbReceiver) {
                                this.mUsbmanager.requestPermission(localUsbDevice, this.mPendingIntent);
                            }
                        }
                    } else {
                        Log.d("123456789", "String.format not match");
                    }
                }
            }

            return 0;
        }
    }

    public boolean isConnected() {
        return this.mUsbDevice != null && this.mInterface != null && this.mDeviceConnection != null;
    }

    protected UsbDevice getUsbDevice() {
        return this.mUsbDevice;
    }

    public int Uart_Control_Out(int request, int value, int index) {
        boolean retval = false;
        int retval1 = this.mDeviceConnection.controlTransfer(64, request, value, index, (byte[])null, 0, this.DEFAULT_TIMEOUT);
        return retval1;
    }

    public int Uart_Control_In(int request, int value, int index, byte[] buffer, int length) {
        boolean retval = false;
        int retval1 = this.mDeviceConnection.controlTransfer(192, request, value, index, buffer, length, this.DEFAULT_TIMEOUT);
        return retval1;
    }

    private int Uart_Set_Handshake(int control) {
        return this.Uart_Control_Out(164, ~control, 0);
    }

    public int Uart_Tiocmset(int set, int clear) {
        int control = 0;
        if((set & 4) == 4) {
            control |= 64;
        }

        if((set & 2) == 2) {
            control |= 32;
        }

        if((clear & 4) == 4) {
            control &= -65;
        }

        if((clear & 2) == 2) {
            control &= -33;
        }

        return this.Uart_Set_Handshake(control);
    }

    public boolean UartInit() {
        byte size = 8;
        byte[] buffer = new byte[size];
        this.Uart_Control_Out(161, 0, 0);
        int ret = this.Uart_Control_In(95, 0, 0, buffer, 2);
        if(ret < 0) {
            return false;
        } else {
            this.Uart_Control_Out(154, 4882, '\ud982');
            this.Uart_Control_Out(154, 3884, 4);
            ret = this.Uart_Control_In(149, 9496, 0, buffer, 2);
            if(ret < 0) {
                return false;
            } else {
                this.Uart_Control_Out(154, 10023, 0);
                this.Uart_Control_Out(164, 255, 0);
                return true;
            }
        }
    }

    public boolean SetConfig(int baudRate, byte dataBit, byte stopBit, byte parity, byte flowControl) {
        byte value = 0;
        byte index = 0;
        byte valueHigh = 0;
        boolean valueLow = false;
        boolean indexHigh = false;
        boolean indexLow = false;
        char valueHigh1;
        switch(parity) {
            case 0:
                valueHigh1 = 0;
                break;
            case 1:
                valueHigh1 = (char)(valueHigh | 8);
                break;
            case 2:
                valueHigh1 = (char)(valueHigh | 24);
                break;
            case 3:
                valueHigh1 = (char)(valueHigh | 40);
                break;
            case 4:
                valueHigh1 = (char)(valueHigh | 56);
                break;
            default:
                valueHigh1 = 0;
        }

        if(stopBit == 2) {
            valueHigh1 = (char)(valueHigh1 | 4);
        }

        switch(dataBit) {
            case 5:
                valueHigh1 = (char)(valueHigh1 | 0);
                break;
            case 6:
                valueHigh1 = (char)(valueHigh1 | 1);
                break;
            case 7:
                valueHigh1 = (char)(valueHigh1 | 2);
                break;
            case 8:
                valueHigh1 = (char)(valueHigh1 | 3);
                break;
            default:
                valueHigh1 = (char)(valueHigh1 | 3);
        }

        valueHigh1 = (char)(valueHigh1 | 192);
        short valueLow1 = 156;
        int value1 = value | valueLow1;
        value1 |= valueHigh1 << 8;
        short indexHigh1;
        byte indexLow1;
        switch(baudRate) {
            case 50:
                indexLow1 = 0;
                indexHigh1 = 22;
                break;
            case 75:
                indexLow1 = 0;
                indexHigh1 = 100;
                break;
            case 110:
                indexLow1 = 0;
                indexHigh1 = 150;
                break;
            case 135:
                indexLow1 = 0;
                indexHigh1 = 169;
                break;
            case 150:
                indexLow1 = 0;
                indexHigh1 = 178;
                break;
            case 300:
                indexLow1 = 0;
                indexHigh1 = 217;
                break;
            case 600:
                indexLow1 = 1;
                indexHigh1 = 100;
                break;
            case 1200:
                indexLow1 = 1;
                indexHigh1 = 178;
                break;
            case 1800:
                indexLow1 = 1;
                indexHigh1 = 204;
                break;
            case 2400:
                indexLow1 = 1;
                indexHigh1 = 217;
                break;
            case 4800:
                indexLow1 = 2;
                indexHigh1 = 100;
                break;
            case 9600:
                indexLow1 = 2;
                indexHigh1 = 178;
                break;
            case 19200:
                indexLow1 = 2;
                indexHigh1 = 217;
                break;
            case 38400:
                indexLow1 = 3;
                indexHigh1 = 100;
                break;
            case 57600:
                indexLow1 = 3;
                indexHigh1 = 152;
                break;
            case 115200:
                indexLow1 = 3;
                indexHigh1 = 204;
                break;
            case 230400:
                indexLow1 = 3;
                indexHigh1 = 230;
                break;
            case 460800:
                indexLow1 = 3;
                indexHigh1 = 243;
                break;
            case 500000:
                indexLow1 = 3;
                indexHigh1 = 244;
                break;
            case 921600:
                indexLow1 = 7;
                indexHigh1 = 243;
                break;
            case 1000000:
                indexLow1 = 3;
                indexHigh1 = 250;
                break;
            case 2000000:
                indexLow1 = 3;
                indexHigh1 = 253;
                break;
            case 3000000:
                indexLow1 = 3;
                indexHigh1 = 254;
                break;
            default:
                indexLow1 = 2;
                indexHigh1 = 178;
        }

        int index1 = index | 136 | indexLow1;
        index1 |= indexHigh1 << 8;
        this.Uart_Control_Out(161, value1, index1);
        if(flowControl == 1) {
            this.Uart_Tiocmset(6, 0);
        }

        return true;
    }

    public int ReadData(char[] data, int length) {
        if(length >= 1 && this.totalBytes != 0) {
            if(length > this.totalBytes) {
                length = this.totalBytes;
            }

            this.totalBytes -= length;

            for(int count = 0; count < length; ++count) {
                data[count] = (char)this.readBuffer[this.readIndex];
                ++this.readIndex;
                this.readIndex %= 65536;
            }

            return length;
        } else {
            byte mLen = 0;
            return mLen;
        }
    }

    public int ReadData(byte[] data, int length, int timeoutMillis) throws IOException {
        if(this.mBulkInPoint == null) {
            return -1;
        } else {
            Object var5 = this.ReadQueueLock;
            int totalBytesRead;
            synchronized(this.ReadQueueLock) {
                int readAmt = Math.min(length, this.mBulkPacketSize);
                totalBytesRead = this.mDeviceConnection.bulkTransfer(this.mBulkInPoint, data, readAmt, timeoutMillis);
            }

            if(totalBytesRead < 0) {
                throw new IOException("Expected Over 0 Byte");
            } else {
                return totalBytesRead;
            }
        }
    }

    public int WriteData(byte[] buf, int length) throws IOException {
        boolean mLen = false;
        int mLen1 = this.WriteData(buf, length, this.WriteTimeOutMillis);
        if(mLen1 < 0) {
            throw new IOException("Expected Write Actual Bytes");
        } else {
            return mLen1;
        }
    }

    public int WriteData(byte[] buf, int length, int timeoutMillis) {
        int offset = 0;
        boolean HasWritten = false;
        int odd_len = length;
        if(this.mBulkOutPoint == null) {
            return -1;
        } else {
            while(offset < length) {
                Object var7 = this.WriteQueueLock;
                synchronized(this.WriteQueueLock) {
                    int mLen = Math.min(odd_len, this.mBulkPacketSize);
                    byte[] arrayOfByte = new byte[mLen];
                    if(offset == 0) {
                        System.arraycopy(buf, 0, arrayOfByte, 0, mLen);
                    } else {
                        System.arraycopy(buf, offset, arrayOfByte, 0, mLen);
                    }

                    int HasWritten1 = this.mDeviceConnection.bulkTransfer(this.mBulkOutPoint, arrayOfByte, mLen, timeoutMillis);
                    if(HasWritten1 < 0) {
                        return -2;
                    }

                    offset += HasWritten1;
                    odd_len -= HasWritten1;
                }
            }

            return offset;
        }
    }

    private boolean enumerateEndPoint(UsbInterface sInterface) {
        if(sInterface == null) {
            return false;
        } else {
            for(int i = 0; i < sInterface.getEndpointCount(); ++i) {
                UsbEndpoint endPoint = sInterface.getEndpoint(i);
                if(endPoint.getType() == 2 && endPoint.getMaxPacketSize() == 32) {
                    if(endPoint.getDirection() == 128) {
                        this.mBulkInPoint = endPoint;
                    } else {
                        this.mBulkOutPoint = endPoint;
                    }

                    this.mBulkPacketSize = endPoint.getMaxPacketSize();
                } else if(endPoint.getType() == 0) {
                    this.mCtrlPoint = endPoint;
                }
            }

            return true;
        }
    }

    private UsbInterface getUsbInterface(UsbDevice paramUsbDevice) {
        if(this.mDeviceConnection != null) {
            if(this.mInterface != null) {
                this.mDeviceConnection.releaseInterface(this.mInterface);
                this.mInterface = null;
            }

            this.mDeviceConnection.close();
            this.mUsbDevice = null;
            this.mInterface = null;
        }

        if(paramUsbDevice == null) {
            return null;
        } else {
            for(int i = 0; i < paramUsbDevice.getInterfaceCount(); ++i) {
                UsbInterface intf = paramUsbDevice.getInterface(i);
                if(intf.getInterfaceClass() == 255 && intf.getInterfaceSubclass() == 1 && intf.getInterfaceProtocol() == 2) {
                    return intf;
                }
            }

            return null;
        }
    }

    public final class UartCmd {
        public static final int VENDOR_WRITE_TYPE = 64;
        public static final int VENDOR_READ_TYPE = 192;
        public static final int VENDOR_READ = 149;
        public static final int VENDOR_WRITE = 154;
        public static final int VENDOR_SERIAL_INIT = 161;
        public static final int VENDOR_MODEM_OUT = 164;
        public static final int VENDOR_VERSION = 95;

        public UartCmd() {
        }
    }

    public final class UartIoBits {
        public static final int UART_BIT_RTS = 64;
        public static final int UART_BIT_DTR = 32;

        public UartIoBits() {
        }
    }

    public final class UartModem {
        public static final int TIOCM_LE = 1;
        public static final int TIOCM_DTR = 2;
        public static final int TIOCM_RTS = 4;
        public static final int TIOCM_ST = 8;
        public static final int TIOCM_SR = 16;
        public static final int TIOCM_CTS = 32;
        public static final int TIOCM_CAR = 64;
        public static final int TIOCM_RNG = 128;
        public static final int TIOCM_DSR = 256;
        public static final int TIOCM_CD = 64;
        public static final int TIOCM_RI = 128;
        public static final int TIOCM_OUT1 = 8192;
        public static final int TIOCM_OUT2 = 16384;
        public static final int TIOCM_LOOP = 32768;

        public UartModem() {
        }
    }

    public final class UartState {
        public static final int UART_STATE = 0;
        public static final int UART_OVERRUN_ERROR = 1;
        public static final int UART_PARITY_ERROR = 2;
        public static final int UART_FRAME_ERROR = 6;
        public static final int UART_RECV_ERROR = 2;
        public static final int UART_STATE_TRANSIENT_MASK = 7;

        public UartState() {
        }
    }

    public final class UsbType {
        public static final int USB_TYPE_VENDOR = 64;
        public static final int USB_RECIP_DEVICE = 0;
        public static final int USB_DIR_OUT = 0;
        public static final int USB_DIR_IN = 128;

        public UsbType() {
        }
    }

    private class read_thread extends Thread {
        UsbEndpoint endpoint;
        UsbDeviceConnection mConn;

        read_thread(UsbEndpoint point, UsbDeviceConnection con) {
            this.endpoint = point;
            this.mConn = con;
            this.setPriority(10);
        }

        public void run() {
            while(CH34xAndroidDriver.this.READ_ENABLE) {
                while(CH34xAndroidDriver.this.totalBytes > '\uffc1') {
                    try {
                        Thread.sleep(5L);
                    } catch (InterruptedException var3) {
                        var3.printStackTrace();
                    }
                }

                Object e = CH34xAndroidDriver.this.ReadQueueLock;
                synchronized(CH34xAndroidDriver.this.ReadQueueLock) {
                    if(this.endpoint != null) {
                        CH34xAndroidDriver.this.readcount = this.mConn.bulkTransfer(this.endpoint, CH34xAndroidDriver.this.usbdata, 64, CH34xAndroidDriver.this.ReadTimeOutMillis);
                        if(CH34xAndroidDriver.this.readcount > 0) {
                            for(int count = 0; count < CH34xAndroidDriver.this.readcount; ++count) {
                                CH34xAndroidDriver.this.readBuffer[CH34xAndroidDriver.this.writeIndex] = CH34xAndroidDriver.this.usbdata[count];
                                CH34xAndroidDriver.this.writeIndex = CH34xAndroidDriver.this.writeIndex + 1;
                                CH34xAndroidDriver.this.writeIndex = CH34xAndroidDriver.this.writeIndex % 65536;
                            }

                            if(CH34xAndroidDriver.this.writeIndex >= CH34xAndroidDriver.this.readIndex) {
                                CH34xAndroidDriver.this.totalBytes = CH34xAndroidDriver.this.writeIndex - CH34xAndroidDriver.this.readIndex;
                            } else {
                                CH34xAndroidDriver.this.totalBytes = 65536 - CH34xAndroidDriver.this.readIndex + CH34xAndroidDriver.this.writeIndex;
                            }
                        }
                    }
                }
            }

        }
    }
}
