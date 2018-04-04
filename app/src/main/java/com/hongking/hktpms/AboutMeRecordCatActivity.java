package com.hongking.hktpms;
/*
 * 包说明
 * 系统日志查看Activity
 * */
import java.util.ArrayList;
import java.util.List;
import android.app.Dialog;
import com.hongking.oemtpms.R;
import com.hongking.hktpms.SysCustomDialog;
import com.hongking.hktpms.SysParam;
import android.os.Bundle;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AboutMeRecordCatActivity extends SysActivity   {	
	StorageMyDB dba;
	private class MyDiary{
		public MyDiary(String t,String c,String r,int _id){
			
			title=t;
			content=c;
			recorddate=r;
		}
		public String title;
		public String content;
		public String recorddate;
	}
	private TextView myTextView1 = null;
	private List<MyDiary> mData; 
	private List<String> listTag = new ArrayList<String>(); 
	private ListView listView; 
	private Button myButtonCreateLog = null;
	private SysParam TPMSParam;
	private int diaryCount;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TPMSParam = ((SysParam)SysParam.getInstance(this));
		SysParam.getInstance(this).addActivity(this);
		setContentView(R.layout.aboutme_logview);
		
		new SysParam.Builder(this).setTitle(getResources().getString(R.string.ViewSystemLogs)).setBackButton(getResources().getString(R.string.StringAboutMe), new View.OnClickListener(){
			public void onClick(View v){
				
				KeyEvent newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK);
				AboutMeRecordCatActivity.this.onKeyDown(KeyEvent.KEYCODE_BACK,newEvent);
			}
		}).create();
		View linearLayout = findViewById(R.id.content);
		Drawable dr = this.getResources().getDrawable(TPMSParam.iThemeTable[TPMSParam.iThemeIndex]); 
		linearLayout.setBackgroundDrawable(dr);
		dba = new StorageMyDB(this);
		dba.open();
		myTextView1 = (TextView)findViewById(R.id.DebugViewLine1);
		myTextView1.setText("报警设置栏\n");
		listView  = (ListView)findViewById(R.id.ListView01); 
		updateList();
        myButtonCreateLog = (Button)findViewById(R.id.ButtonPreview);
        myButtonCreateLog.setOnClickListener(new MyButtonListenerCommon());
        this.AutoDestroryListen();
	}
	  @Override  
	 public boolean onKeyDown(int keyCode, KeyEvent event) {          
		 if (keyCode == KeyEvent.KEYCODE_BACK) {   
			 finish();
			 return true;   
		  }  
		  return super.onKeyDown(keyCode, event);  
	 }
	
	private List<MyDiary> updateList() { 
        
		mData = new ArrayList<MyDiary>();
		Cursor c = dba.getDiares();
		diaryCount = c.getCount();
		myTextView1.setText("日志总条数 "+diaryCount+ " "+"允许存储条数"+TPMSParam.iRecordValue);
		startManagingCursor(c);
		if(c.moveToFirst()){
			do{
				int id = Integer.parseInt(c.getString(0));
				String title = c.getString(c.getColumnIndex(StorageConstants.TITLE_NAME));
				String content = c.getString(c.getColumnIndex(StorageConstants.CONTENT_NAME));
				String dateRecord = c.getString(c.getColumnIndex(StorageConstants.DATE_NAME));
				
				MyDiary temp = new MyDiary(title,content,dateRecord,id);
				mData.add(0,temp);
			}while(c.moveToNext());
		}
		DiaryAdapter myAdapter = new DiaryAdapter(this);  
        listView.setAdapter(myAdapter); 
        listView.setOnItemClickListener(new MyListViewListenerCommon()); 
        return mData;
    }
	
	@Override
    public void onStart() {
		TPMSParam.activityStart();	
        super.onStart();  
    }	
	@Override
	public void onStop() {
		TPMSParam.activityStop();	
		super.onStop();				
	}
    public void onDestroy(){
	    Log.v("LOG ","OnDestory is starting");
  		super.onDestroy();
    }
	class DiaryAdapter extends BaseAdapter {  
    	private LayoutInflater mInflater; 
        public DiaryAdapter(Context context){ 
            this.mInflater = LayoutInflater.from(context); 
        }  
        @Override
        public int getCount() { 
            // TODO Auto-generated method stub 
            return mData.size(); 
        } 
        @Override
        public Object getItem(int arg0) { 
            // TODO Auto-generated method stub 
            return null; 
        } 
        @Override
        public long getItemId(int arg0) { 
            // TODO Auto-generated method stub 
            return 0; 
        } 
        @Override  
        public boolean areAllItemsEnabled() {  
            return false;   
        }  
       @Override  
       public boolean isEnabled(int position) {  
           // 如果-开头，则该项不可选   
           return !listTag.contains((String)mData.get(position).title);   
       }
       @Override  
       public View getView(int position, View convertView, ViewGroup parent) {  
           ViewHolder holder;
           if (convertView == null) {
               convertView = mInflater.inflate(R.layout.list_type2, null);
               holder = new ViewHolder();
               holder.title = (TextView) convertView.findViewById(R.id.title);
               //holder.info = (TextView) convertView.findViewById(R.id.text1);
               View bgview = (View) convertView.findViewById(R.id.bgview); 
               View listview = (View) convertView.findViewById(R.id.listview); 
               bgview.setBackgroundResource(getstrokebg("middle"));            
        	   listview.setBackgroundResource(getbg("middle"));
               convertView.setTag(holder);
           }
           else{
               holder = (ViewHolder)convertView.getTag();
           }
            
           holder.title.setText((""+(position+1)+". "+mData.get(position).title));
           return convertView;
       }  
       private int getbg(String flag)
       {
    	   // TODO Auto-generated method stub
    	   if(flag.equals("single")){  //仅一项
	    		return R.drawable.list_corner_round;
	    	}
    	   else if (flag.equals("top")){  //第一项
	        	return R.drawable.list_corner_round_top;
	    	}
    	   else if (flag.equals("bottom")){//最后一项
	    		return R.drawable.list_corner_round_bottom;
	    	}
    	   else
	            return R.drawable.list_corner_shape; //中间项
	   }
       private int getstrokebg(String flag){
    	   // TODO Auto-generated method stub
    	   if(flag.equals("single")){  //仅一项
	    		return R.drawable.background_corner_round;
	    	}
    	   else if (flag.equals("top")){  //第一项
	        	return R.drawable.background_corner_round_top;
	    	}
    	   else if (flag.equals("bottom")){//最后一项
	    		return R.drawable.background_corner_round_bottom;
	    	}
    	   else
	            return R.drawable.background_corner_shape; //中间项
	   }  
    }  
	class MyListViewListenerCommon implements OnItemClickListener{
    	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, 
                long arg3) { 
    			Dialog dialog = null; 
    			boolean dismiss=true;
    			LayoutInflater inflater = LayoutInflater.from(AboutMeRecordCatActivity.this);  
    			View view = inflater.inflate(R.layout.set_custom_dialog, null); 
    			SysCustomDialog.Builder customBuilder = new SysCustomDialog.Builder(AboutMeRecordCatActivity.this); 
	            customBuilder.setTitle("日志详情").setMessage("标题:\n"+mData.get(arg2).title+"\n"+
	            		"内容:\n"+mData.get(arg2).content+"\n"+
	            		"创建时间:\n"+mData.get(arg2).recorddate)  
        				.setPositiveButton("返回",new DialogInterface.OnClickListener(){ 
            		 		public void onClick(DialogInterface dialog, int whichButton){ 
            		 			dialog.dismiss();
            				}}); 
	            
	            dialog = customBuilder.create(view,dismiss);  
	    		dialog.show();
            }
	}
	class MyButtonListenerCommon implements OnClickListener{
    	@Override
		public void onClick(View v)  {
			// TODO Auto-generated method stub 
    		if(v == myButtonCreateLog){
    			dba.ClearDiares(AboutMeRecordCatActivity.this);
    			updateList();
    		}
    	}
    }
    private final class ViewHolder{
	   private TextView  title;
	}
}
