package com.hongking.hktpms;

/*
 * 包说明
 * 日志存储到数据库类
 * */
import com.hongking.hktpms.StorageConstants;
import com.hongking.hktpms.StorageMyDBhelper;
import com.hongking.hktpms.SysParam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.format.Time;
import android.util.Log;

public class StorageMyDB{ 
  
    private SQLiteDatabase db;
    private final Context context;
    private final StorageMyDBhelper dbhelper;
    private SysParam TPMSParam;
    public StorageMyDB(Context c){
    	context = c;
    	TPMSParam = ((SysParam)SysParam.getInstance(context));
    	dbhelper = new StorageMyDBhelper(context,StorageConstants.DATABASE_NAME,null,StorageConstants.DATABASE_VERSION);
    }
  
    public void close(){
    	db.close();
    }
  
    public void open() throws SQLiteException
    {
    	try{
    		db = dbhelper.getWritableDatabase();
    	} catch(SQLiteException ex){
    		Log.v("open database exception caughtg",ex.getMessage());
    		db = dbhelper.getReadableDatabase();
    	}
    }
	public long inserdiary(String title ,String content)
    {
    	try{
    		if(getDiaresCount() >= TPMSParam.iRecordValue){
    			Log.v("MYDB","count is out of set value"+TPMSParam.iRecordValue);
    			rmDiaresTop();//删除最早的日志记录
    		}
    		Time localTime = new Time("Asia/Hong_Kong");
    		localTime.setToNow(); 
    		String  date  =localTime.format("%Y-%m-%d %H:%M:%S"); 
    		ContentValues newTaskValue = new ContentValues();
    		newTaskValue.put(StorageConstants.TITLE_NAME,title);
    		newTaskValue.put(StorageConstants.CONTENT_NAME,content);   		
    		newTaskValue.put(StorageConstants.DATE_NAME,date);
    		return db.insert(StorageConstants.TABLE_NAME, null, newTaskValue);
    	}catch(SQLiteException ex){
    		Log.v("Insert into dataBase exception caught",ex.getMessage());
    		return -1;
    	}
    }
    
    public boolean deleteDiary(int id){
	    //delete方法第一参数：数据库表名，第二个参数表示条件语句,第三个参数为条件带?的替代值
	    //返回值大于0表示删除成功
    	String where = StorageConstants.KEY_ID+" = " + id;
    	Log.v("mdata",where);
    	return db.delete(StorageConstants.TABLE_NAME, where, null)>0;
    }
    public int getDiaresCount(){
    	Cursor c = db.query(StorageConstants.TABLE_NAME,null,null,null,null,null,null);
    	return c.getCount();
    }
    public Cursor getDiares()
    {
    	Cursor c = db.query(StorageConstants.TABLE_NAME,null,null,null,null,null,null);
    	return c;
    }
    public boolean rmDiaresTop(){
    	Cursor c = db.query(StorageConstants.TABLE_NAME,null,null,null,null,null,null);
    	c.moveToFirst();
    	deleteDiary(Integer.parseInt(c.getString(0)));
    	c.close();
    	return true;
    }
    public boolean ClearDiares(Context context){
    	return db.delete(StorageConstants.TABLE_NAME,null, null)>0;  	
    }
 }
