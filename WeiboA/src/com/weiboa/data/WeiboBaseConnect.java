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

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.SharedPreferences;

public class WeiboBaseConnect extends WeiboConnect{

	public WeiboBaseConnect(SharedPreferences sp) {
		super(sp);
	}

	@Override
	public JSONObject verifyCredentials() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray getFriendsTimeline(long sinceId, long limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray getFriendsTimelineUnderMax(long maxId, long limit) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
