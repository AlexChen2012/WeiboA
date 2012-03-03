/* 
 * Copyright (C) 2008 Torgny Bjers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *NOTE: This is a modified version of the original source code file which has been modified by Reg Wang in 2012.
 */

package com.weiboa.data;

import java.io.File;
import java.util.List;

import org.json.JSONObject;

import com.weiboa.util.WeiboPreferences;

import android.R.string;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

public class WeiboUser {
		
	private static final String TAG = WeiboUser.class.getSimpleName();
	
    public static final String FILE_PREFIX = "user_";
    public static final String FILE_EXTENSION = ".xml";

    public static final String KEY_ACCESS_TOKEN 		= "user_access_token";
    public static final String KEY_ACCESS_TOKEN_SECRET 	= "user_access_token_secret";
    
	public static final String KEY_USERNAME 		  	= "username";
	public static final String KEY_PASSWORD 		   	= "password";
	public static final String KEY_USEID			   	= "userid";


    private boolean mOAuth = true;
    
    private WeiboConnect mConnection = null;
    
    private static WeiboUser mWeiboUser = null;
    private String mUsername = null;
    private WeiboUserDB mUserDB = null;
    private String mAccessToken = null;
    private String mAccessTokenSecret = null;
    
    public WeiboConnect getWeiboConnect(){
    	if(mConnection == null){
    		mConnection = WeiboConnect.getConnect(getSharedPreferences(), mOAuth);
    	}
    	return mConnection;
    }
    
    public SharedPreferences getSharedPreferences(){
    	SharedPreferences sp = null;
    	sp = WeiboPreferences.getDefaultSharedPreferences();
    	return sp;
    }
        
    public void saveAuthInfomation(String token, String secret){
    	SharedPreferences sp = getSharedPreferences();
        synchronized (sp) {
            SharedPreferences.Editor editor = sp.edit();
            if (token == null) {
                editor.remove(KEY_ACCESS_TOKEN);
                Log.d(TAG, "Clearing OAuth Token");
            } else {
                editor.putString(KEY_ACCESS_TOKEN, token);
                Log.d(TAG, "Saving OAuth Token: " + token);
            }
            if (secret == null) {
                editor.remove(KEY_ACCESS_TOKEN_SECRET);
                Log.d(TAG, "Clearing OAuth Secret");
            } else {
                editor.putString(KEY_ACCESS_TOKEN_SECRET, secret);
                Log.d(TAG, "Saving OAuth Secret: " + secret);
            }
            editor.commit();
        }

    }
    
    private WeiboUser(WeiboUserDB db){
    	this.mUserDB = db;
    	mAccessToken = WeiboPreferences.getDefaultSharedPreferences().getString(WeiboUser.KEY_ACCESS_TOKEN, null);
    	mAccessTokenSecret = WeiboPreferences.getDefaultSharedPreferences().getString(WeiboUser.KEY_ACCESS_TOKEN_SECRET, null);
    	if(mAccessToken != null && mAccessTokenSecret != null){
    		mUsername = mUserDB.getUserNameByAccessToken(mAccessToken, mAccessTokenSecret);
    	}
    }
    
    public static WeiboUser getInstance(WeiboUserDB db){
    	if(mWeiboUser == null){
    		mWeiboUser = new WeiboUser(db);
    	}
    	return mWeiboUser;
    }

    public synchronized void setCurrentUser(){
    	
    	SharedPreferences.Editor ed = getSharedPreferences().edit();
    	ed.putString(KEY_USERNAME, mUsername);
    }
    
    public boolean verifyCredentials(boolean reVerify){
    	JSONObject jso = null;
    	boolean ok = false;
    	String userName = null;
    	jso = getWeiboConnect().verifyCredentials();
    	ok = (jso != null);
    	
    	if(ok){
    		userName = WeiboConnect.getScreenName(jso);
    		mUsername = userName;
    		mAccessToken = WeiboPreferences.getDefaultSharedPreferences().getString(WeiboUser.KEY_ACCESS_TOKEN, null);
    		mAccessTokenSecret = WeiboPreferences.getDefaultSharedPreferences().getString(WeiboUser.KEY_ACCESS_TOKEN_SECRET, null);
    		mUserDB.updateUserNameByAccessToken(mAccessToken, mAccessTokenSecret, mUsername);
    	}
    	
    	return ok;
    }

	public String getUsername() {
		return mUsername;
	}
    

}
