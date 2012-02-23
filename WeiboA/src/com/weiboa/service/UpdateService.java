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
	private ContentResolver mResolver;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.mApplication = (WeiboAApplication)getApplication();
		mUpdater = new Updater();
		mResolver = getContentResolver();
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

	public int fetchStatusUpdates(){

		WeiboUser wu = WeiboUser.getInstance(mApplication.getWeiboUserDB());

		int count = 0;
		long sinceId = WeiboPreferences.getDefaultSharedPreferences().getLong(TWEET_LAST_SINCED_ID, 0);
		long lastId = 0;
		JSONArray jArray = wu.getWeiboConnect().getFriendsTimeline(sinceId, 0);

		Log.d(TAG, jArray.toString());
		Log.d(TAG, "Fetching status update");

		if(jArray != null){
			try {
				for(int i = 0; i < jArray.length(); i++){
					JSONObject jObject = jArray.getJSONObject(i);

					long id = jObject.getLong("id");
					if(id > lastId) {
						lastId = id;
					}
					if(insertTweet(jObject) != null){
						count ++;
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(lastId > sinceId){
				WeiboPreferences.getDefaultSharedPreferences().edit().putLong(TWEET_LAST_SINCED_ID, sinceId).commit();
			}
		}
		return count;
	}
	
	private Uri insertTweet(JSONObject jObject){
		
		ContentValues values = new ContentValues();
		Uri lTweetUri;
		try {
			long lTweetId = Long.parseLong(jObject.getString("id"));
			lTweetUri = ContentUris.withAppendedId(StatusProvider.CONTENT_URI, lTweetId);
		
			JSONObject user;

			user = jObject.getJSONObject("user");
			values.put(StatusProvider.C_ID, lTweetId);
			values.put(StatusProvider.C_USER, user.getString("screen_name"));
			String message = null;
			message = jObject.getString("text");
			
			values.put(StatusProvider.C_TEXT, message);
			values.put(StatusProvider.C_SOURCE, jObject.getString("source"));
			long created = Date.parse(jObject.getString("created_at"));
			values.put(StatusProvider.C_CREATED_AT, created);
			values.put(StatusProvider.C_USER_ID, user.getString("id"));
			if(jObject.has("thumbnail_pic")){
				values.put(StatusProvider.C_PIRCTURE, jObject.getString("thumbnail_pic"));
			}else {
				values.put(StatusProvider.C_PIRCTURE, "");
			}
			
			if(mResolver.update(lTweetUri, values, null, null) == 0){
				mResolver.insert(StatusProvider.CONTENT_URI, values);
			}else {
				lTweetUri = null;
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lTweetUri = null;
		}
		
		return lTweetUri;
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
					int newUpdates = updaterService.fetchStatusUpdates();
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
