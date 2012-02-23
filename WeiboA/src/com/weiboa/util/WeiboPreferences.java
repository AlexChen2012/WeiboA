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

package com.weiboa.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class WeiboPreferences {
	
	public static final String TAG = WeiboPreferences.class.getSimpleName();
	
	private static Context context;
	private static String origin;
	
	
	public static SharedPreferences getDefaultSharedPreferences(){
		if(context == null){
			Log.e(TAG, "Was not initialized yet");
			return null;
		} else {
			return PreferenceManager.getDefaultSharedPreferences(context);
		}
	}
	
	public static void initialize(Context context_in, java.lang.Object object){
		String origin_in = object.getClass().getSimpleName();
		if(context == null){
			context = context_in.getApplicationContext();
			origin = origin_in;
			Log.d(TAG, "Initialized by " + origin);
		} else{
			Log.d(TAG, "Already initialized by " + origin +  " (called by: " + origin_in + ")");
		}
	}
	
	public static boolean isInitialized(){
		return (context != null);
	}
	
    public static SharedPreferences getSharedPreferences(String name, int mode) {
        if (context == null) {
            Log.e(TAG, "Was not initialized yet");
            return null;
        } else {
            return context.getSharedPreferences(name, mode);
        }
    }
    
    public static Context getContext(){
    	if(isInitialized()){
    		return context;
    	}else{
    		return null;
    	}
    }
}
