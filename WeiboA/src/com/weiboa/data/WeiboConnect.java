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

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.weiboa.oauth.OAuthConntect;
import com.weiboa.util.WeiboPreferences;

import android.content.SharedPreferences;
import android.util.Log;

public abstract class WeiboConnect {

	private static final String TAG = WeiboConnect.class.getSimpleName();
	
	private static final String BASE_URL = "https://api.weibo.com/2";
	private static final String OLD_BASE_URL = OAuthConntect.SERVER;
	
	private static final String EXTENSION = ".json";
	
    protected static final String ACCOUNT_VERIFY_CREDENTIALS_URL = OLD_BASE_URL
            + "/account/verify_credentials" + EXTENSION;

    protected static final String STATUSES_FRIENDS_TIMELINE_URL = OLD_BASE_URL
    		+ "/statuses/friends_timeline" + EXTENSION;
    
	protected long mSinceId;
	protected int mLimit = 100;
    protected String mUsername;
    protected String mPassword;
	
    public abstract JSONObject verifyCredentials();
    
	public static WeiboConnect getConnect(SharedPreferences sp, boolean oauth){
		WeiboConnect connect;
		
		if(sp == null){
			Log.e(TAG, "SharedPreferences are null ??");
		}
		
		if(oauth) {
			connect = new WeiboOAuthConnect(sp);
		}else{
			connect = new WeiboBaseConnect(sp);
		}
		
		return connect;
	}
	
    protected WeiboConnect(SharedPreferences sp) {
    	
        mUsername = sp.getString(WeiboUser.KEY_USERNAME, "");
        mPassword = sp.getString(WeiboUser.KEY_PASSWORD, "");
    }

	
	protected long setSinceId(long sinceId) {
		if(sinceId > 0){
			mSinceId = sinceId;
		}
		return mSinceId;
	}
	
	protected int setlimit(int limit) {

		if(limit > 0){
			mLimit = limit;
			if(mLimit > 100){
				mLimit = 100;
			}
		}
		return mLimit;
	}
	
    public static String getScreenName(JSONObject credentials) {
        return credentials.optString("screen_name");
    }

    public String getUsername() {
        return mUsername;
    }
    
    public abstract JSONArray getFriendsTimeline(long sinceId, long limit);

}
