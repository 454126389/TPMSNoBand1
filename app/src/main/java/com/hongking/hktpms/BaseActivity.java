package com.hongking.hktpms;

import java.util.Vector;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {

	public static Vector<Activity> activityList = new Vector<Activity>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		activityList.add(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		activityList.remove(this);
	}

	/**
	 * finish all activity
	 */
	public void finishAllActivity() {
		if (activityList.size() > 0) {
			Vector<Activity> oldActivityList = new Vector<Activity>(activityList);
			activityList.clear();
			for (Activity activity : oldActivityList) {
				activity.finish();
			}
		}
	}

}
