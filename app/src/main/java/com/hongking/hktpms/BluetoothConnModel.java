/*
 * Copyright (C) 2011 Wireless Network and Multimedia Laboratory, NCU, Taiwan
 * 
 * You can reference http://wmlab.csie.ncu.edu.tw
 * 
 * This class is used to process connection operation, including server side or client side. * 
 * 
 * @author Fiona
 * @version 0.0.1
 *
 */

package com.hongking.hktpms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.hongking.hktpms.views.EventBus_bt_connect;

import de.greenrobot.event.EventBus;

import static com.hongking.hktpms.SysControllerService.CONNECT_REQUEST_ACTION_SEND;

public class BluetoothConnModel {
    // Debugging
    private static final boolean D = true;
    private static final String TAG = "BluetoothConnModel";
    private static final String NAME = "BluetoothConn";
    private static final UUID CUSTOM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //  private static final UUID CUSTOM_UUID = UUID.fromString("abbcddef-abc0-abc0-abc0-aaaaaaaaaaaa");

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private int count = 0;
    // private final Context mContext;
    // private Map<BluetoothDevice, BluetoothSocketConfig> mBluetoothSocekts;
    private ServerSocketThread mServerSocketThread = null;
    private BluetoothSocketConfig mSocketConfig = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    public BluetoothConnModel(Context bluetoothConnController1, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;

    }

    public synchronized void startSession() {
        if (D) Log.d(TAG, "[startSession] ServerSocketThread start...");

        if (mServerSocketThread == null) {
            Log.i("20161212", "[startSession] mServerSocketThread is dead");
            mServerSocketThread = new ServerSocketThread();
            mServerSocketThread.start();
        } else {
            Log.i("20161212", "[startSession] mServerSocketThread is alive : " + this);
        }

        mSocketConfig = BluetoothSocketConfig.getInstance();
    }

    public synchronized void connectTo(BluetoothDevice device) {
        if (D) Log.d("20161212", "[connectTo] ClientSocketThread start...,这里重新创建socket");
        SocketThread mSocketThread = new SocketThread(device);
        mSocketThread.start();
    }

    public synchronized void connected(BluetoothSocket socket) {
        if (D) Log.d(TAG, "[connected]");
        notifyUiFromToast(socket.getRemoteDevice().getName() + " has connected.-----");
        mHandler.obtainMessage(SysControllerService.MESSAGE_CONNECT, -1, -1, socket.getRemoteDevice())
                .sendToTarget();


        ConnectedThread connectedThread = new ConnectedThread(socket);
        mSocketConfig.registerSocket(socket, connectedThread, BluetoothSocketConfig.SOCKET_CONNECTED);
        Log.e(TAG, "[connected] connectedThread hashcode = " + connectedThread.toString());
        connectedThread.start();
    }

    public void writeToSocket(BluetoothSocket socket, byte[] out) {
        if (D) Log.d(TAG, "writeToDevice start...");
        ConnectedThread connectedThread = mSocketConfig.getConnectedThread(socket);
        Log.i(TAG, "[writeToDevice] connectedThread hashcode = " + connectedThread.toString());
        if (mSocketConfig.isSocketConnected(socket)) {
            Log.w(TAG, "[writeToDevice] The socket is alived.");
            connectedThread.write(out);
        } else
            Log.w(TAG, "[writeToDevice] The socket has beenF closed.");
    }

    public void writeToSockets(Set<BluetoothSocket> sockets, byte[] out) {
        if (D) Log.d(TAG, "writeToDevices start...");
        for (BluetoothSocket socket : sockets) {
            synchronized (this) {
                //  if (mState != STATE_CONNECTED) return;
                writeToSocket(socket, out);
            }
        }
    }

    public void writeToAllSockets(byte[] out) {
        if (D) Log.d(TAG, "writeToAllDevices start...");
        for (BluetoothSocket socket : mSocketConfig.getConnectedSocketList()) {
            synchronized (this) {
                //  if (mState != STATE_CONNECTED) return;
                writeToSocket(socket, out);
                Log.i(TAG, "[writeToAllDevices] currentTimeMillis: " + System.currentTimeMillis());
            }
        }
    }

    public void disconnectServerSocket() {
        Log.e(TAG, "[disconnectServerSocket] ----------------");
        if (mServerSocketThread != null) {
            mServerSocketThread.disconnect();
            mServerSocketThread = null;
            Log.w(TAG, "[disconnectServerSocket] NULL mServerSocketThread");
        }
    }

    public void disconnectSocketFromAddress(String address) {
        Set<BluetoothSocket> socketSets = mSocketConfig.containSockets(address);
        for (BluetoothSocket socket : socketSets) {
            //disconnectSocket(socket);
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public synchronized void disconnectSocket(BluetoothSocket socket) {
        Log.w(TAG, "[disconnectSocket] ------------------" + socket.toString() + " ; device name is " + socket.getRemoteDevice().getName());
        if (!mSocketConfig.isSocketConnected(socket)) {
            Log.w(TAG, "[disconnectSocket] mSocketConfig doesn't contain the socket: " + socket.toString() + " ; device name is " + socket.getRemoteDevice().getName());
            return;
        }
        notifyUiFromToast(socket.getRemoteDevice().getName() + " connection was lost");
        mHandler.obtainMessage(SysControllerService.MESSAGE_CONNECT_LOST, -1, -1, socket.getRemoteDevice())
                .sendToTarget();
        mSocketConfig.unregisterSocket(socket);
    }

    public void terminated() {
        Log.w(TAG, "[terminated] --------------");

        disconnectServerSocket();
        for (BluetoothSocket socket : mSocketConfig.getConnectedSocketList()) {
            Log.w(TAG, "[terminated] Left Socket(s): " + mSocketConfig.getConnectedSocketList().size());
            disconnectSocket(socket);
        }
        Log.w(TAG, "[terminated] Final Left Socket(s): " + mSocketConfig.getConnectedSocketList().size());
    }

    private void notifyUiFromToast(String str) {
        Message msg = mHandler.obtainMessage(SysControllerService.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(SysControllerService.TOAST, str);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private class ServerSocketThread implements Runnable {
        private BluetoothServerSocket mmServerSocket = null;
        private Thread thread = null;
        private boolean isServerSocketValid = false;

        //	private final ExecutorService pool;
        public ServerSocketThread() {
            this.thread = new Thread(this);

            BluetoothServerSocket serverSocket = null;
            try {
                Log.i(TAG, "[ServerSocketThread] Enter the listen server socket");
                serverSocket = mAdapter.listenUsingRfcommWithServiceRecord(NAME, CUSTOM_UUID);
                Log.i(TAG, "[ServerSocketThread] serverSocket hash code = " + serverSocket.hashCode());
                isServerSocketValid = true;

            } catch (IOException e) {
                Log.e(TAG, "[ServerSocketThread] Constructure: listen() failed", e);
                e.printStackTrace();
                notifyUiFromToast("Listen failed. Restart application again");
                isServerSocketValid = false;
                mServerSocketThread = null;
                //BluetoothConnService.this.startSession();
            }
            mmServerSocket = serverSocket;

//            String serverSocketName = mmServerSocket.toString();
//            Log.i(TAG, "[ServerSocketThread] serverSocket name = "+serverSocketName);
        }

        public void start() {
            this.thread.start();
        }

        @Override
        public void run() {
            if (D) Log.d(TAG, "BEGIN ServerSocketThread" + this);
            BluetoothSocket socket = null;

            while (isServerSocketValid) {
                try {
                    Log.i(TAG, "[ServerSocketThread] Enter while loop");
                    Log.i(TAG, "[ServerSocketThread] serverSocket hash code = " + mmServerSocket.hashCode());
                    socket = mmServerSocket.accept();
                    Log.i(TAG, "[ServerSocketThread] Got client socket");
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }
                if (socket != null) {
                    synchronized (BluetoothConnModel.this) {
                        Log.i(TAG, "[ServerSocketThread] " + socket.getRemoteDevice() + " is connected.");
                        connected(socket);
                        BluetoothConnModel.this.disconnectServerSocket();

                        SysApplication.BluetoothState=true;
                        EventBus.getDefault().post(new EventBus_bt_connect(99));

                        break;
                    }
                }
            }
            Log.i(TAG, "[ServerSocketThread] break from while");
        }

        public void disconnect() {
            if (D) Log.d(TAG, "[ServerSocketThread] disconnect " + this);
            try {
                if (mmServerSocket != null)
                    mmServerSocket.close();
                Log.i(TAG, "[ServerSocketThread] mmServerSocket is closed.");
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }

    }

    private class SocketThread implements Runnable {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private Thread thread = null;

        //	private final ExecutorService pool;
        public SocketThread(BluetoothDevice device) {
            Log.i("20161202", "创建连接线程,这里重新开启socket？");
            this.thread = new Thread(this);
            Log.i(TAG, "[SocketThread] Enter these server sockets");
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(CUSTOM_UUID);
                Log.i(TAG, "[SocketThread] Constructure: Get a BluetoothSocket for a connection, create Rfcomm");
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;

        }

        public void start() {
            Log.i("20161202", "开始连接线程");
            this.thread.start();
        }

        @Override
        public void run() {
            if (D) Log.d(TAG, "BEGIN ServerSocketThread" + this);
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
//                if (mmSocket.isConnected()) {
//                    Log.i("20161202", "isConnected");
                    mmSocket.connect();
//                } else {
//                    Log.i("20161202", "noConnected");
//                }
                Log.i("20161202", "[SocketThread] Return a successful connection");
                count = 0;
            } catch (IOException e) {
                //连接失败
                if (count < 10) {
                    EventBus.getDefault().post(new EventBus_bt_connect(0));
                    count++;
                }
                notifyUiFromToast("Unable to connect device: " + mmDevice.getName());
//            	mHandler.obtainMessage(SysControllerService.MESSAGE_DISCONNECT, -1, -1, mmSocket.getRemoteDevice())
//                .sendToTarget();
                Log.i("20161202", "[SocketThread] Connection failed", e);
                // Close the socket

                try {
                    mmSocket.close();
                   /* mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null) {
                        return;
                    }else {
                        mBluetoothAdapter.disable();
                        Log.i("20161214", "蓝牙关闭了");
                    }
                    if(mBluetoothAdapter.disable()) {
                        Log.i("20161214", "蓝牙开启了");
                        mBluetoothAdapter.enable();
                    }*/
                    Log.i("20161202", "[SocketThread] Connect fail, close the client socket,重启蓝牙");
                } catch (IOException e2) {
                    Log.e("20161202", "unable to close() socket during connection failure", e2);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                //disconnectSocket(mmSocket);
                // Start the service over to restart listening mode
                //BluetoothChatService.this.start();
                this.thread = null;
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothConnModel.this) {
                connected(mmSocket);
                SysApplication.BluetoothState=true;
                EventBus.getDefault().post(new EventBus_bt_connect(99));
                Log.i(TAG, "[SocketThread] " + mmDevice + " is connected.");
            }
            this.thread = null;
            if (D) Log.i(TAG, "END mConnectThread");

        }
        /*
        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
            	mmSocket.close();
                Log.i(TAG, "[SocketThread] mmSocket is closed.");
            } catch (IOException e) {
                Log.e(TAG, "[SocketThread] close() of client failed", e);
            }
        }
        */

    }

    public class ConnectedThread implements Runnable {
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        private Thread thread = null;


        private ConnectedThread(BluetoothSocket socket) {
            this.thread = new Thread(this, socket.getRemoteDevice().toString());
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.i(TAG, "[ConnectedThread] Constructure: Set up bluetooth socket i/o stream");
            } catch (IOException e) {
                Log.e(TAG, "[ConnectedThread] temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

        public void start() {
            this.thread.start();
        }

        @Override
        public void run() {
            if (D) Log.d(TAG, "BEGIN ConnectedThread" + this);
            byte[] buffer = new byte[1024];
            int bytes;
            byte tempBuf[];
            //   BluetoothSocketConfig socketConfig = mBluetoothSocekts.get(mmSocket.getRemoteDevice());
            //    StringBuilder inStreamBuilder = new StringBuilder("");
            Log.i(TAG, "[ConnectedThread] Create string builder");
            BluetoothConnModel.this.disconnectServerSocket();   //lyp add 蓝牙连接以后不再监听蓝牙   
            while (mSocketConfig.isSocketConnected(mmSocket)) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer, 0, 1024);
                    tempBuf = new byte[bytes];
                    for (int i = 0; i < bytes; i++)
                        tempBuf[i] = buffer[i];
                    Log.i("dataExchange", "[ConnectedThread] read bytes: " + bytes + "add" + tempBuf);
                    mHandler.obtainMessage(SysControllerService.MESSAGE_READ, bytes, -1, tempBuf).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "[ConnectedThread] connection lost", e);
                    disconnectSocket(mmSocket);
//					EventBus.getDefault().post();
                    Log.w(TAG, "[ConnectedThread] disconnect the socket");
                    e.printStackTrace();
                    break;
                }

            }
            //	notifyUiFromToast("Socket is disconnected");
            //	if (!socketConfig.isSocketConnected()) {
            //		disconnectSocket(mmSocket.getRemoteDevice());
            ///		Log.w(TAG, "[ConnectedThread] disconnect the socket");
            //	}
            if (D) Log.i(TAG, "[ConnectedThread] break from while");
            BluetoothConnModel.this.startSession();
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(SysControllerService.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();

            } catch (IOException e) {
                Log.e(TAG, "[ConnectedThread] Exception during write", e);
            }
        }
/*
        public void cancel() {
            try {
            	if (mmInStream != null){
            		mmInStream.close();
            	}
            	if (mmOutStream != null){
            		mmOutStream.close();
            	}
            	if (mmSocket != null) {
                    mmSocket.close();
            	    Log.w(TAG, "[ConnectedThread] close() bluetooth socket");
            	}
            } catch (IOException e) {
                Log.e(TAG, "[ConnectedThread] close() of connect socket failed", e);
            }
        }
        */

    }


}
