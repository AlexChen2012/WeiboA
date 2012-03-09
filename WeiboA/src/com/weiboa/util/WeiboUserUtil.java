package com.weiboa.util;

import java.sql.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.weiboa.activity.WeiboAApplication;
import com.weiboa.data.StatusProvider;
import com.weiboa.data.WeiboUser;


public class WeiboUserUtil {

	private static final String TAG = WeiboUserUtil.class.getSimpleName();
	public static final String TWEET_LAST_SINCED_ID		= "last_sinced_id";

	
	public static int fetchStatusUnderMax(Application mApplication, Long maxID){
		
		WeiboUser wu = WeiboUser.getInstance(((WeiboAApplication) mApplication).getWeiboUserDB());

		JSONArray jArray = wu.getWeiboConnect().getFriendsTimelineUnderMax(maxID, 0);
		
		int count = 0;
		if(jArray != null){
			try {
				for(int i = 0; i < jArray.length(); i++){
					JSONObject jObject = jArray.getJSONObject(i);

					long id = jObject.getLong("id");
					if(id < maxID){
						Log.d(TAG, "ID is : " + id);
					}
					Log.d(TAG, jObject.toString());
					if(insertTweet(jObject, mApplication) != null){
						count ++;
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		Log.d(TAG, "Count = " + count);
		return count;
	}
	
	public static int fetchStatusUpdates(Application mApplication){

		WeiboUser wu = WeiboUser.getInstance(((WeiboAApplication) mApplication).getWeiboUserDB());

		int count = 0;
		long sinceId = WeiboPreferences.getDefaultSharedPreferences().getLong(TWEET_LAST_SINCED_ID, 0);
		long lastId = 0;
		JSONArray jArray = wu.getWeiboConnect().getFriendsTimeline(sinceId, 0);


		if(jArray != null){
			try {
				for(int i = 0; i < jArray.length(); i++){
					JSONObject jObject = jArray.getJSONObject(i);

					long id = jObject.getLong("id");
					if(id > lastId) {
						lastId = id;
					}
					if(insertTweet(jObject, mApplication) != null){
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
	
	private static Uri insertTweet(JSONObject jObject, Application mApplication){
		
		ContentValues values = new ContentValues();
		Uri lTweetUri;
		try {
			long lTweetId = jObject.getLong("id");
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
				values.put(StatusProvider.C_ORIGINAL_PIC, jObject.getString("original_pic"));
			}else {
				values.put(StatusProvider.C_PIRCTURE, "");
			}
			
			if(mApplication.getContentResolver().update(lTweetUri, values, null, null) == 0){
				mApplication.getContentResolver().insert(StatusProvider.CONTENT_URI, values);
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
	
}
