package com.weiboa.oauth;

import com.weiboa.data.WeiboUser;
import com.weiboa.data.WeiboUserDB;
import com.weiboa.util.WeiboPreferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class OAuthConntect {
	
	private static final String TAG = OAuthConntect.class.getSimpleName();
	
	public OAuthConntect(){
	}
	
	public OAuthConsumer getConsumer(){
		if(mOAuthConsumer == null){
			mOAuthConsumer = new CommonsHttpOAuthConsumer(OAuthKeys.CONSUMER_KEY, OAuthKeys.CONSUMER_SECRET);
		}
		return mOAuthConsumer;
	}
	
	private OAuthConsumer mOAuthConsumer;
	private OAuthProvider mOAuthProvider;
	
	public static String SERVER = "http://api.t.sina.com.cn/";
	public static String URL_OAUTH_TOKEN = "http://api.t.sina.com.cn/oauth/request_token";
	public static String URL_AUTHORIZE = "http://api.t.sina.com.cn/oauth/authorize";
	public static String URL_ACCESS_TOKEN = "http://api.t.sina.com.cn/oauth/access_token";
	public static String URL_AUTHENTICATION = "http://api.t.sina.com.cn/oauth/authenticate";

	public static final Uri CALLBACK_URL = Uri.parse("weiboa://authorize");

	public boolean oauthConnect(Context context){
		
		boolean successed = false;
		
		try {
			getRequestToken(context);
			successed = true;
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return successed;
	}
	
	public String getRequestToken(Context context) throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException{
		
		mOAuthConsumer = new CommonsHttpOAuthConsumer(OAuthKeys.CONSUMER_KEY, OAuthKeys.CONSUMER_SECRET);
		
		mOAuthProvider = new CommonsHttpOAuthProvider(URL_OAUTH_TOKEN, URL_ACCESS_TOKEN, URL_AUTHORIZE);
		
		String authUrl = mOAuthProvider.retrieveRequestToken(mOAuthConsumer, CALLBACK_URL.toString());
		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		context.startActivity(intent);
		return authUrl;
	}
	
	public boolean getAccessToken(Uri uri, WeiboUserDB db) {
		
		String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
		mOAuthProvider.setOAuth10a(true);
		boolean isSucessed = false;
		WeiboUser wu = WeiboUser.getInstance(db);
		String accessToken = null;
		String accessTokenSecret = null;
		String userID;
		
		try {
			mOAuthProvider.retrieveAccessToken(mOAuthConsumer, verifier);
			userID = mOAuthProvider.getResponseParameters().getFirst("user_id");
			accessToken = mOAuthConsumer.getToken();
			accessTokenSecret = mOAuthConsumer.getTokenSecret();

			SharedPreferences mPerferences = WeiboPreferences.getDefaultSharedPreferences();
			SharedPreferences.Editor mEditor = mPerferences.edit();

			mEditor.putString(WeiboUser.KEY_USEID, userID);
			mEditor.putString(WeiboUser.KEY_ACCESS_TOKEN, accessToken);
			mEditor.putString(WeiboUser.KEY_ACCESS_TOKEN_SECRET, accessTokenSecret);

			mEditor.commit();

			db.insertUserInfo(userID, "", "", accessToken, accessTokenSecret);
			mOAuthConsumer.setTokenWithSecret(accessToken, accessTokenSecret);
			isSucessed = true;
			
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(isSucessed){
			wu.saveAuthInfomation(accessToken, accessTokenSecret);
		}
		
		return isSucessed;
	}
	
}
