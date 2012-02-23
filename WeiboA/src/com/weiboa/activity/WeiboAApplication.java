package com.weiboa.activity;

import java.io.File;

import com.weiboa.data.WeiboUserDB;
import com.weiboa.util.WeiboPreferences;

import android.app.Application;
import android.content.SharedPreferences;

public class WeiboAApplication extends Application{
	
	public static String Dir = null;
	private static final String TAG = WeiboAApplication.class.getSimpleName();

    private WeiboUserDB mUserDB = null;
    private boolean mServiceRunning;
    
	@Override
	public void onCreate() {
		super.onCreate();
		
		mUserDB = new WeiboUserDB(getApplicationContext());
		WeiboPreferences.initialize(this, this);
		Dir = getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + "/Pic/";
		File dir = new File(Dir);
		dir.mkdirs();
	}
    
    public WeiboUserDB getWeiboUserDB(){
    	return mUserDB;
    }

	public boolean isServiceRunning()
	{
		return mServiceRunning;
	}
	
	public void setServiceRunning(boolean serviceRunning)
	{
		this.mServiceRunning = serviceRunning;
	}
	
}
