package com.hongking.hktpms;
/*
 * 包说明
 * 数据库操作API类
 * */
import com.hongking.hktpms.StorageConstants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class StorageMyDBhelper extends SQLiteOpenHelper { 
  
    private static final String CREATE_TABLE = "create table "+StorageConstants.TABLE_NAME+" ("
    		+ StorageConstants.KEY_ID+" integer primary key autoincrement, "
    		+ StorageConstants.TITLE_NAME+" text not null, "
    		+ StorageConstants.CONTENT_NAME+" text not null, "
    		+ StorageConstants.DATE_NAME+" long);";      
    public StorageMyDBhelper(Context context,String name,CursorFactory factory,int version) { 
        super(context, name, factory, version); 
        // TODO Auto-generated constructor stub 
    } 
  
    @Override
    public void onCreate(SQLiteDatabase db) { 
        Log.v("MyDBhelper onCreate","Creating all the tables");
        Log.v("command ",CREATE_TABLE);
        try{
        	db.execSQL(CREATE_TABLE);
        }catch(SQLiteException ex){
        	Log.v("Create table exception", ex.getMessage());
        }
   
    } 
  
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion) {
    	// TODO Auto-generated method stub 
    	Log.w("TaskDBAdapter","Upgrading from version"+ oldVersion 
    			+"to"+ newVersion 
    			+",which will destroy all old data");
    	db.execSQL("drop table if exists "+StorageConstants.TABLE_NAME);
    	onCreate(db);
  
    } 
    /** * 删除数据库 * @param context * @return */
    public boolean deleteDatabase(Context context)
    { 
    	return context.deleteDatabase(StorageConstants.TABLE_NAME);
    } 

}