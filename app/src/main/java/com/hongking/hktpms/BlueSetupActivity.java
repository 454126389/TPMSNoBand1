
/*
 * Copyright (C) 2011 Wireless Network and Multimedia Laboratory, NCU, Taiwan
 * 
 * You can reference http://wmlab.csie.ncu.edu.tw
 * 
 * This class is used to interact with remote device. We use this as a user interface to help us debug.
 * 
 * 
 * @author Fiona
 * @version 0.0.1
 *
 */

package com.hongking.hktpms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hongking.hktpms.Utils.SharePreferenceUtil;
import com.hongking.oemtpms.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
//import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;

import android.os.Bundle;
import android.util.Log;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import static android.R.id.closeButton;

public class BlueSetupActivity extends  Activity{
	
	
	private static final String TAG = "TPMSBlueSetupActivity";
	public static String EXTRA_DEVICE_ADDRESS = "device_address";


	private List<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
	private ListView listView; 
	private TextView myTextView1 = null;
	private ProgressBar progressbar;
	private Button scanButton,closeButton;
	
	private BluetoothAdapter mBluetoothAdapter = null;
	
	private BluetoothSocketConfig mSocketConfig = null;  
	SysListviewbase myAdapter;
	private SysParam TPMSParam;
	private int scanDeviceLocation;
	private int scanDeviceNum = 0;

	private SharePreferenceUtil sp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
			Log.i(TAG, "[onCreate]");
	        super.onCreate(savedInstanceState);

			sp = new SharePreferenceUtil(BlueSetupActivity.this);
	        TPMSParam = ((SysParam)SysParam.getInstance(this));

	        SysParam.getInstance(this).addActivity(this);

	        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			setContentView(R.layout.bluethooth_setup);
	        //setContentView(R.layout.display_view);
		    new SysParam.Builder(this).setTitle(getResources().getString(R.string.PleaseContectTPMS)).setBackButton(getResources().getString(R.string.Back), new View.OnClickListener(){
				public void onClick(View v){
					finish();
//					KeyEvent newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK);
//					BlueSetupActivity.this.onKeyDown(KeyEvent.KEYCODE_BACK,newEvent);
				}
			}).create();

			myTextView1 = (TextView)findViewById(R.id.DebugViewLine1);
//			View linearLayout = findViewById(R.id.content);
//			Log.i(TAG,"theme id "+TPMSParam.iThemeIndex);
//			Drawable dr = this.getResources().getDrawable(TPMSParam.iThemeTable[TPMSParam.iThemeIndex]); 
//			linearLayout.setBackgroundDrawable(dr);
			
			//mData = getData();
			//myTextView1.setText(getResources().getString(R.string.Sysinit));
			listView  = (ListView)findViewById(R.id.ListView01); 
			//listView1  = (ListView)findViewById(R.id.ListView02); 
			
			myAdapter = new SysListviewbase(this,mData,1);
	        listView.setAdapter(myAdapter); 
	        //listView1.setAdapter(myAdapter); 
	        listView.setOnItemClickListener(new MyListViewListenerCommon()); 
		
	        getData();
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	        this.registerReceiver(mReceiver, filter);

	        // Register for broadcasts when discovery has finished
	        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	        this.registerReceiver(mReceiver, filter);
	        
	        progressbar = (ProgressBar) findViewById(R.id.wait);
	        scanButton = (Button) findViewById(R.id.ButtonSearch);
	        scanButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	                doDiscovery();
	                v.setVisibility(View.INVISIBLE);
	                progressbar.setVisibility(View.VISIBLE);
	                
	            }
	        });


			closeButton = (Button) findViewById(R.id.ButtonClose);
		    closeButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					sp.setAutostart("noflag");
					System.exit(0);//正常退出App

				}
			});


	        doDiscovery();
	        scanButton.setVisibility(View.INVISIBLE);
            progressbar.setVisibility(View.VISIBLE);
	        //TPMSParam.showNotification(this);
	         
		}
		
		private List<Map<String, Object>> getData() { 
	        
			//List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			
			Map<String, Object> map = new HashMap<String, Object>(); 

	        map.put("title", " "); 
	        map.put("flag","tag");
	        mData.add(map);        
			
	        
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (!mBluetoothAdapter.isEnabled()) {
//				mBluetoothAdapter.enable();
				return null;
	        // Otherwise, setup the chat session
	        }
		        // Get a set of currently paired devices
	        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

	        // If there are paired devices, add each one to the ArrayAdapter
	       

        	/*map = new HashMap<String, Object>();
	        map.put("title", getResources().getString(R.string.PairedDevice)); 
	        map.put("flag","tag");
	        mData.add(map);*/
		        
	        int pairDeviesNum = 0;
            for (BluetoothDevice device : pairedDevices) {
            	
            	if(device.getName() == null ||!device.getName().contains("TPMS")){
            		continue;
            	}
               
            	Log.i(TAG,"find device:"+device.getName());
    	        
            	pairDeviesNum++;
            	if(pairDeviesNum == 1){
                	map = new HashMap<String, Object>(); 
	    	        map.put("title", device.getName()); 
	    	        //map.put("img", R.drawable.card_arrohw); 
	    	        map.put("info", device.getAddress()); 
	    	        map.put("flag","middle");
	    	        mData.add(map);
            	}else if(pairDeviesNum == 0x02){
            		map = mData.get(mData.size()-1);
            		map.put("flag","middle");
            		
            		map = new HashMap<String, Object>(); 
	    	        map.put("title", device.getName()); 
	    	        //map.put("img", R.drawable.card_arrohw); 
	    	        map.put("info", device.getAddress()); 
	    	        map.put("flag","middle");
	    	        mData.add(map);
            	}else{
            		map = mData.get(mData.size()-1);
            		map.put("flag","middle");
            		
            		map = new HashMap<String, Object>(); 
	    	        map.put("title", device.getName()); 
	    	        //map.put("img", R.drawable.card_arrohw); 
	    	        map.put("info", device.getAddress()); 
	    	        map.put("flag","middle");
	    	        mData.add(map);
            	}
	        } 
	        
			
			
			mSocketConfig = BluetoothSocketConfig.getInstance();
			Set<BluetoothSocket> connectedSockets = mSocketConfig.getConnectedSocketList();
	        if (connectedSockets.size()>0){
	        	/*map = new HashMap<String, Object>();
		        map.put("title", getResources().getString(R.string.ConnectDevice)); 
		        map.put("flag","tag");
		        mData.add(map); */
	        	//findViewById(R.id.title_connected_devices).setVisibility(View.VISIBLE);
	        	for(BluetoothSocket socket:connectedSockets){
	        		//mConnectedDeviceArrayAdapter.add(socket.getRemoteDevice().getName()+"\n"+socket.getRemoteDevice().getAddress());
	        		map = new HashMap<String, Object>(); 
	    	        map.put("title", socket.getRemoteDevice().getName()); 
	    	        //map.put("img", R.drawable.card_arrohw); 
	    	        map.put("info", socket.getRemoteDevice().getAddress()); 
	    	        map.put("flag","middle");
	    	        mData.add(map); 
	        	}
	        }
	        
	        
	        /*map = new HashMap<String, Object>();
	        map.put("title", getResources().getString(R.string.MatchingDevice)); 
	        map.put("flag","tag");
	        mData.add(map); */
	        scanDeviceLocation = mData.size();
	        Log.i(TAG,"scanDevice location is "+scanDeviceLocation);
	        scanDeviceNum = 0;
	        return mData; 
	    }
		@Override  
		 public boolean onKeyDown(int keyCode, KeyEvent event) {          
			 if (keyCode == KeyEvent.KEYCODE_BACK) {   
				 finish();
				 return true;   
			  }  
			  return super.onKeyDown(keyCode, event);  
		 }
		private void doDiscovery() {
	        //if (D) Log.d(TAG, "doDiscovery()");

	        // Indicate scanning in the title


	        // Turn on sub-title for new devices
	        //findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

	        // If we're already discovering, stop it
	        if (mBluetoothAdapter.isDiscovering()) {
	        	mBluetoothAdapter.cancelDiscovery();
	        }

	        // Request discover from BluetoothAdapter
	        mBluetoothAdapter.startDiscovery();
	    }
		@Override
	    public void onStart() {
			Log.i(TAG, "[onStart]");
			TPMSParam.activityStart();	
	        super.onStart();
	        
	        
	        //if(D) Log.e(TAG, "++ ON START ++");	        
	        // If BT is not on, request that it be enabled.
	        // setupChat() will then be called during onActivityResult     
	    }	
		@Override
		public void onPause() {
			Log.i(TAG, "[onPause]");
			//TPMSParam.activityStop();	
			super.onPause();
			//TPMSParam.showNotification(this);	
			//TPMSParam.showNotification(this);			
		}
		@Override
		public void onStop() {
			Log.i(TAG, "[onStop]");
			TPMSParam.activityStop();	
			
			super.onStop();
			//TPMSParam.showNotification(this);	
			//TPMSParam.showNotification(this);			
		}
		@Override
		public void onDestroy() {
			Log.e(TAG, "[onDestroy]");
			//TPMSParam.cancelNotification();
			if (mBluetoothAdapter != null) {
				mBluetoothAdapter.cancelDiscovery();
	        }

	        // Unregister broadcast listeners
	        this.unregisterReceiver(mReceiver);
			
			super.onDestroy();
			//System.exit(0);
			//TPMSParam.cancelNotification(this);
		}
		

		class MyListViewListenerCommon implements OnItemClickListener{
	    	public void onItemClick(AdapterView<?> arg0, View v, int arg2, 
	                long arg3) { 
	    		mBluetoothAdapter.cancelDiscovery();

	            // Get the device MAC address, which is the last 17 chars in the View
	            String info = (String) mData.get(arg2).get("info");
	            //String address = info.substring(info.length() - 17);

	            // Create the result Intent and include the MAC address
	            Intent intent = new Intent();
	            intent.putExtra(EXTRA_DEVICE_ADDRESS, info);

	            // Set result and finish this Activity
	            setResult(Activity.RESULT_OK, intent);
	            finish();

	    		
	    	}      
	   	}
		private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();
	            Map<String, Object> map = new HashMap<String, Object>(); 	
	            // When discovery finds a device
	            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	                // Get the BluetoothDevice object from the Intent
	                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	                // If it's already paired, skip it, because it's been listed already
	                //if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
	                if(device.getName() == null || !device.getName().contains("TPMS"))
	                	return;
		        	for(int i = 0;i < mData.size();i++){
		            	if(mData.get(i).get("info") != null && mData.get(i).get("info").equals(device.getAddress())){
		            		return;
		            	}
		            }
		        	Log.i(TAG,"find device"+device.getName());
			        
		        	scanDeviceNum++;
		        	if(scanDeviceNum == 1){
		            	map = new HashMap<String, Object>(); 
		    	        map.put("title", device.getName()); 
		    	        //map.put("img", R.drawable.card_arrohw); 
		    	        map.put("info", device.getAddress()); 
		    	        map.put("flag","middle");
		    	        mData.add(map);
		        	}else if(scanDeviceNum == 0x02){
		        		map = mData.get(mData.size()-1);
		        		map.put("flag","middle");
		        		
		        		map = new HashMap<String, Object>(); 
		    	        map.put("title", device.getName()); 
		    	        //map.put("img", R.drawable.card_arrohw); 
		    	        map.put("info", device.getAddress()); 
		    	        map.put("flag","middle");
		    	        mData.add(map);
		        	}else{
		        		map = mData.get(mData.size()-1);
		        		map.put("flag","middle");
		        		
		        		map = new HashMap<String, Object>(); 
		    	        map.put("title", device.getName()); 
		    	        //map.put("img", R.drawable.card_arrohw); 
		    	        map.put("info", device.getAddress()); 
		    	        map.put("flag","middle");
		    	        mData.add(map);
		        	}
		        	
		                	((SysListviewbase)listView.getAdapter()).notifyDataSetChanged();  
		                	//mData.add(device.getName() + "\n" + device.getAddress());
		                //}
		            // When discovery is finished, change the Activity title
		            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
		                //setProgressBarIndeterminateVisibility(false);
		                //setTitle(R.string.select_device);

//		            	scanButton.setVisibility(View.VISIBLE);
		            	progressbar.setVisibility(View.INVISIBLE);
		            }
	        }
	    };

}