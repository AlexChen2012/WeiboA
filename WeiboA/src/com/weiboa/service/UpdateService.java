package com.weiboa.service;

import java.sql.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.weiboa.activity.WeiboAApplication;
import com.weiboa.data.StatusProvider;
import com.weiboa.data.WeiboUser;
import com.weiboa.util.WeiboPreferences;
import com.weiboa.util.WeiboUserUtil;

import android.R.string;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class UpdateService extends Service {
	
	private static final String TAG = UpdateService.class.getSimpleName();
	
	static final int DELAY = 60000;
	private boolean mRunFlag = false;
	private Updater mUpdater;
	private WeiboAApplication mApplication;
	public static final String NEW_STATUS_INTENT 		= "com.weiboa.data.NEW_STATUS";
	public static final String NEW_STATUS_EXTRA_COUNT 	= "com.weiboa.data.COUNT_NEW_STATUS";
	public static final String TWEET_LAST_SINCED_ID		= "last_sinced_id";
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.mApplication = (WeiboAApplication)getApplication();
		mUpdater = new Updater();
		Log.i(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mRunFlag = false;
		mApplication.setServiceRunning(false);
		mUpdater.interrupt();
		mUpdater = null;
		Log.d(TAG, "onDestroy");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		mRunFlag = true;
		if(!mUpdater.isAlive()){
			mUpdater.start();
		}
		mApplication.setServiceRunning(true);
		
		Log.d(TAG, "onStartCommand");
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	/*
	 *  Thread that performs the actual update form the online service
	 */
	private class Updater extends Thread{
//		static final String RECEIVE_TIMELINE_NOTIFICATIONS = "com.weiboa.action.RECEIVE_TIMELINE_NOTIFICATIONS";
		
		Intent intent;
		
		public Updater()
		{
			super("UpdaterService-Updater");
		}

		@Override
		public void run() {
			UpdateService updaterService = UpdateService.this;
			while(updaterService.mRunFlag)
			{
				Log.d(TAG, "Updater running");
				try{
					int newUpdates = WeiboUserUtil.fetchStatusUpdates(mApplication);
					if(newUpdates > 0)
					{
						Log.d(TAG, "We have a new status");
						intent = new Intent(NEW_STATUS_INTENT); 
						intent.putExtra(NEW_STATUS_EXTRA_COUNT, newUpdates);
						updaterService.sendBroadcast(intent);
					}
					Thread.sleep(DELAY);
				}catch( InterruptedException e ){
					updaterService.mRunFlag = false;
				}
			}
		}
	}
}
