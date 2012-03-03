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


package com.weiboa.activity;

import org.json.JSONException;
import org.json.JSONObject;


import com.weiboa.data.WeiboUser;
import com.weiboa.data.WeiboUserDB;
import com.weiboa.oauth.OAuthConntect;
import com.weiboa.util.WeiboPreferences;
import com.weiboa.R;

import android.R.integer;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SignInActivity extends Activity{

	private static final String TAG = SignInActivity.class.getSimpleName();
	
	private OAuthConntect mOAuthConntect;
	private Button mSignInButton;
	private TextView mWelcomeText;
	private TextView mPromptText;
	private Button mOAuthButton;
	private WeiboAApplication mApplication;
	private WeiboUserDB mDb;
	
	private ProgressDialog mDialog;
	private Dialog mFailedDialog;
	Context mContext;
	
    public static final int MSG_ACCOUNT_VALID = 1;
    public static final int MSG_ACCOUNT_INVALID = 2;
    public static final int MSG_NONE = 21;

    
    private boolean checkhadAccessToken(){
		if(WeiboPreferences.getDefaultSharedPreferences().getString(WeiboUser.KEY_ACCESS_TOKEN, null) == null ||
		WeiboPreferences.getDefaultSharedPreferences().getString(WeiboUser.KEY_ACCESS_TOKEN_SECRET, null) == null){
				return false;
		}
		return true;
    }
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		mContext = getApplicationContext();
		mApplication = (WeiboAApplication)getApplication();
		mDb = mApplication.getWeiboUserDB();
		mWelcomeText = (TextView)findViewById(R.id.welcome);
		mSignInButton = (Button)findViewById(R.id.buttonLogin);
		mOAuthButton = (Button)findViewById(R.id.buttonOAuth);
		
		mOAuthConntect = new OAuthConntect();
		mOAuthButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(!checkhadAccessToken()){
					new OAuthAcquireRequestTokenTask().execute();
				}else {
                    Toast.makeText(SignInActivity.this, getText(R.string.do_not_need_authrize), Toast.LENGTH_LONG).show();
				}
			}
		} );

		mSignInButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(checkhadAccessToken()){
					startActivity(new Intent(SignInActivity.this, TimeLineActivity.class));
				}else{
					Toast.makeText(SignInActivity.this, getText(R.string.text_authrize_first), Toast.LENGTH_LONG).show();
				}
			}
		} );

		if(checkhadAccessToken()){
			WeiboUser wu = WeiboUser.getInstance(mDb);
			if(wu.getUsername() != null){
				mWelcomeText.setText("Welcome " + wu.getUsername());
			}else{
				mWelcomeText.setText("Please first to authrize!");
			}
		}else{
			mWelcomeText.setText("Please first to authrize!");
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Uri uri = intent.getData();
		if(uri != null){
			
			if(OAuthConntect.CALLBACK_URL.getScheme().equals(uri.getScheme())){
				
				getIntent().setData(null);
				boolean isAuthenticated = mOAuthConntect.getAccessToken(uri, mDb);
				if(isAuthenticated){
					new VerifyCredentialTask().execute();
				}
			}
		}
		
		super.onNewIntent(intent);
	}
	
	private class VerifyCredentialTask extends AsyncTask<Void, Void, JSONObject>{

		private ProgressDialog fDlg;
		
		@Override
		protected void onPostExecute(JSONObject result) {
			
			fDlg.dismiss();
			
			if(result != null){
				
				try {
					boolean isSuccess = result.getBoolean("isSuccess");
					String message = "";
					if(isSuccess){
						message = "Welcome " + WeiboPreferences.getDefaultSharedPreferences().getString(WeiboUser.KEY_USERNAME, "");
						mWelcomeText.setText("Welcome " + WeiboPreferences.getDefaultSharedPreferences().getString(WeiboUser.KEY_USERNAME, ""));
	                    Toast.makeText(SignInActivity.this, message, Toast.LENGTH_LONG).show();
					}else{
						message = "Sorry to verify";
	                    Toast.makeText(SignInActivity.this, message, Toast.LENGTH_LONG).show();
					}
				}catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onPreExecute() {
			
			fDlg = ProgressDialog.show(SignInActivity.this, getText(R.string.title_verify_credential), 
					getText(R.string.text_verify_credential));
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			
			JSONObject json = new JSONObject();
			int what = MSG_NONE;
			String message = "";
			boolean isSuccess = false;
			
			try {
				WeiboUser wu = WeiboUser.getInstance(mDb);
				if(wu.verifyCredentials(true)){
					what = MSG_ACCOUNT_VALID;
					wu.setCurrentUser();
					WeiboPreferences.getDefaultSharedPreferences().edit().putString(WeiboUser.KEY_USERNAME, wu.getUsername()).commit();
					isSuccess = true;
				}
			} catch (Exception e) {
				what = MSG_ACCOUNT_INVALID;
			}
			try {
				json.put("what", what);
				json.put("isSuccess", isSuccess);
			} catch (JSONException e) {
				json = null;
				e.printStackTrace();
			}

			return json;
		}
		
	}

	
	private class OAuthAcquireRequestTokenTask extends AsyncTask<Void, Void, JSONObject>{

		private ProgressDialog fDlg;
		
		
		
		@Override
		protected void onPostExecute(JSONObject result) {
			fDlg.dismiss();
			
			if(result != null){
				
				try {
					
					boolean isSuccess = result.getBoolean("successed");
					String message = result.getString("message");
					
					if(isSuccess){
						
					}else{
                        Toast.makeText(SignInActivity.this, message, Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		}

		@Override
		protected void onPreExecute() {
			
			fDlg = ProgressDialog.show(SignInActivity.this, getText(R.string.title_acquiring_a_request_token), 
					getText(R.string.text_acquiring_a_request_token));
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			
			boolean isSuccess = mOAuthConntect.oauthConnect(getApplicationContext());
			String message = (String) getText(R.string.request_token_successed);
			
			if(!isSuccess){
				message = (String) getText(R.string.request_token_failed);
			}
			
			JSONObject json = new JSONObject();
			
			try {
				json.put("successed", isSuccess);
				json.put("message", message);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return json;
		}
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}


	@Override
	public void onBackPressed() {
		this.finish();
	}
	
	
	
}
