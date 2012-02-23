package com.weiboa.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import com.weiboa.R;
import com.weiboa.data.StatusProvider;
import com.weiboa.data.TimeLineAdapter;
import com.weiboa.service.UpdateService;
import com.weiboa.util.AnimationUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TimeLineActivity extends Activity{
	
	public static final String SEND_TIMELINE_NOTIFICATIONS = "com.weiboa.action.SEND_TIMELINE_NOTIFICATIONS";
	
	private static final String TAG = TimeLineActivity.class.getSimpleName();
	private WeiboAApplication mApplication;
	private ListView mListView;
	private Cursor mCursor;
	private TimeLineAdapter mAdapter;
	private IntentFilter mFilter;
	private TimelineReceiver mReceiver;
	
//	public DownLoadPircuter mTask = new DownLoadPircuter();
	private static final int STOP = 1;
	
	class TimelineReceiver extends BroadcastReceiver{
		TimeLineActivity timeline = TimeLineActivity.this;

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(UpdateService.NEW_STATUS_INTENT)){

				Cursor cursorOnReceiver = getContentResolver().query(StatusProvider.CONTENT_URI, null, null, null, StatusProvider.GET_ALL_ORDER_BY);
				timeline.startManagingCursor(cursorOnReceiver);

				timeline.mAdapter.changeCursor(cursorOnReceiver);
				timeline.mListView.setAdapter(mAdapter);
				Log.d("TimelineReceiver", "onReceived");
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline_basic);

		
		mApplication = (WeiboAApplication)getApplication();
		mListView = (ListView)findViewById(R.id.listTimeline);
		mReceiver = new TimelineReceiver();
		mFilter = new IntentFilter(UpdateService.NEW_STATUS_INTENT);
		
		startService(new Intent(TimeLineActivity.this, UpdateService.class));
	}

	private void setupList()
	{
		mCursor = getContentResolver().query(StatusProvider.CONTENT_URI, null, null, null, StatusProvider.GET_ALL_ORDER_BY);
		startManagingCursor(mCursor);
		
		//Set up the adapter
		mAdapter = new TimeLineAdapter(this, R.layout.row, mCursor);
		mAdapter.setViewBinder(VIEW_BINDER);
		mListView.setAdapter(mAdapter);
		
		//Register the receiver
		registerReceiver(mReceiver, mFilter, SEND_TIMELINE_NOTIFICATIONS, null);
	}

	private SimpleCursorAdapter.ViewBinder VIEW_BINDER = new SimpleCursorAdapter.ViewBinder() 
	{
		String url = null;

		public boolean setViewValue(View view, Cursor cursor, int columIndex) {
			switch (view.getId()) {
			case R.id.tlCreatedAt:
				//Update the created at text to relative time
				long timestamp = cursor.getLong(columIndex);
				CharSequence relTime = DateUtils.getRelativeTimeSpanString(view.getContext(), timestamp);
				((TextView)view).setText(relTime);
				return true;
			case R.id.tlText:
				String text = cursor.getString(columIndex);
				text = Html.fromHtml(text).toString();
				((TextView)view).setText(text);
				return true;
			case R.id.tlPircture:
				url = cursor.getString(columIndex);
				ImageView cView = (ImageView)view;
				Log.d(TAG, url);
				if(url.equals("")){
					((ImageView)view).setVisibility(View.GONE);
				}else{
					
					if(checkImageExist(url)){
						AnimationUtil.stopAnimation(cView);
						String name = getImageName(url);
						loadImageToView(cView, name);
					}else{
						((ImageView)view).setVisibility(View.VISIBLE);
						JSONObject tObject = new JSONObject();
						try {
							tObject.put("url", url);
							tObject.put("imageview", cView);
							new DownLoadPircuter(tObject).execute();
						} catch (JSONException e) {
							tObject = null;
							e.printStackTrace();
						}
					}
				}
				return true;
			default:
				break;
			}
			return false;
		}
		
		
	};
	
	private String getImageName(String url){
    	String[] ss = url.split("/");
    	String name = ss[ss.length -2 ] + "_" + ss[ss.length - 1];
		return name;
	}
	
	private boolean checkImageExist(String url){
		String name = getImageName(url);
		File file = new File(WeiboAApplication.Dir + name);
		if(file.exists()){
			return true;
		}else{
			return false;
		}
	}
	
	private void loadImageToView(ImageView view, String name){
		BufferedInputStream buf;
		try {
			buf = new BufferedInputStream(new FileInputStream(new File(WeiboAApplication.Dir + name)));
            Bitmap bitmap = BitmapFactory.decodeStream(buf);
            view.setImageBitmap(bitmap);
            view.setVisibility(View.VISIBLE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private class DownLoadPircuter extends AsyncTask<Void, Void, Void>{

		ImageView mImageView;
		String name;
		AnimationDrawable animation = null;
		URL url;
		
		public DownLoadPircuter(JSONObject jObject){
			
        	try {
        		url = new URL(jObject.getString("url"));
        		name = getImageName(jObject.getString("url"));
	            mImageView = (ImageView)jObject.get("imageview");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            
		}
		
		@Override
		protected void onPreExecute() {
			AnimationUtil.startAnimation(mImageView, animation, R.anim.loading);
		}


		@Override
		protected void onPostExecute(Void result) {
			AnimationUtil.stopAnimation(mImageView);
			loadImageToView(mImageView, name);
		}

		@Override
		protected Void doInBackground(Void... params) {
	        int count;
	        try {
	        	
	            
	            URLConnection conexion = url.openConnection();
	            conexion.connect();
	            
	            int lenghtOfFile = conexion.getContentLength();

	            // download the file
	            InputStream input = new BufferedInputStream(url.openStream());
	            OutputStream output = new FileOutputStream(new File(WeiboAApplication.Dir + name));

	            byte data[] = new byte[1024];

	            long total = 0;

	            while ((count = input.read(data)) != -1) {
	                total += count;
	                // publishing the progress....
//	                publishProgress((int)(total*100/lenghtOfFile));
	                output.write(data, 0, count);
	            }

	            output.flush();
	            output.close();
	            input.close();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	        return null;
		}
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mReceiver, mFilter);
		this.setupList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
}
