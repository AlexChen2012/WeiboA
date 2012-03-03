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

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

public class WeiboOAuthConnect extends WeiboConnect{

    private static final String TAG = WeiboOAuthConnect.class.getSimpleName();
    
    private HttpClient mClient;
    private OAuthConsumer mConsumer = null;
    private String mToken;
    private String mSecret;
    
	public WeiboOAuthConnect(SharedPreferences sp) {
		super(sp);
		
		// initialize http connection
        HttpParams parameters = new BasicHttpParams();
        HttpProtocolParams.setVersion(parameters, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(parameters, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(parameters, false);
        HttpConnectionParams.setTcpNoDelay(parameters, true);
        HttpConnectionParams.setSocketBufferSize(parameters, 8192);
        
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        ClientConnectionManager tsccm = new ThreadSafeClientConnManager(parameters, schReg);
        mClient = new DefaultHttpClient(tsccm, parameters);
        
        mConsumer = new CommonsHttpOAuthConsumer(com.weiboa.oauth.OAuthKeys.CONSUMER_KEY,
        		com.weiboa.oauth.OAuthKeys.CONSUMER_SECRET);
        
        loadSavedKeys(sp);
	}

    private void loadSavedKeys(SharedPreferences sp) {
        // We look for saved user keys
        if (sp.contains(WeiboUser.KEY_ACCESS_TOKEN) && sp.contains(WeiboUser.KEY_ACCESS_TOKEN_SECRET)) {
            mToken = sp.getString(WeiboUser.KEY_ACCESS_TOKEN, null);
            mSecret = sp.getString(WeiboUser.KEY_ACCESS_TOKEN_SECRET, null);
            if (!(mToken == null || mSecret == null)) {
                mConsumer.setTokenWithSecret(mToken, mSecret);
            }
        }
    }
    
    private JSONObject sendGetRequest(HttpGet get){
        JSONObject jso = null;
        boolean ok = false;
        try {
            mConsumer.sign(get);
            String response = mClient.execute(get, new BasicResponseHandler());
            jso = new JSONObject(response);
            Log.d(TAG, "authenticatedQuery: " + jso.toString(2));
            ok = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!ok) {
            jso = null;
        }
        return jso;
    }
    
    private JSONObject getUrl(String url){
    	HttpGet get = new HttpGet(url);
    	return sendGetRequest(get);
    }
    
	@Override
	public JSONObject verifyCredentials() {
		return getUrl(ACCOUNT_VERIFY_CREDENTIALS_URL);
	}

	@Override
	public JSONArray getFriendsTimeline(long sinceId, long limit) {
		String url = STATUSES_FRIENDS_TIMELINE_URL;
		return getTimeline(url, sinceId, 0, limit, 1);
	}

	private JSONArray getTimeline(String url, long sinceId, long maxId, long limit, int page){
		
		JSONArray jArray = null;
		
		Uri uri = Uri.parse(url);
		Uri.Builder builder = uri.buildUpon();
		if(sinceId != 0){
			builder.appendQueryParameter("since_id", String.valueOf(sinceId));
		}else if(maxId != 0){
			builder.appendQueryParameter("max_id", String.valueOf(maxId));
		}else{
			builder.appendQueryParameter("since_id", String.valueOf(sinceId));
		}
		if(limit != 0){
			builder.appendQueryParameter("count", String.valueOf(limit));
		}else{
			builder.appendQueryParameter("count", String.valueOf(50));
		}
		builder.appendQueryParameter("page", String.valueOf(page));
		
		HttpGet get = new HttpGet(builder.build().toString());
		try {
			mConsumer.sign(get);
			String response = mClient.execute(get, new BasicResponseHandler());
			jArray = new JSONArray(response);
		} catch (Exception e) {
			e.printStackTrace();
			jArray = null;
		}
			
		return jArray;
	}

	@Override
	public JSONArray getFriendsTimelineUnderMax(long maxId, long limit) {
		String url = STATUSES_FRIENDS_TIMELINE_URL;
		return getTimeline(url, 0, maxId, limit, 1);

	}

}
