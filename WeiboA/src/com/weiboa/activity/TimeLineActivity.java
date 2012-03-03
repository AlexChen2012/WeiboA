package com.weiboa.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.weiboa.R;
import com.weiboa.data.StatusAdapter;
import com.weiboa.data.StatusProvider;
import com.weiboa.data.TimeLineAdapter;
import com.weiboa.data.TweetStatus;
import com.weiboa.data.WeiboUser;
import com.weiboa.service.UpdateService;
import com.weiboa.util.AnimationUtil;
import com.weiboa.util.WeiboUserUtil;

import android.R.integer;
import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TimeLineActivity extends ListActivity implements AbsListView.OnScrollListener,
OnItemClickListener, OnItemLongClickListener{

	public static final String SEND_TIMELINE_NOTIFICATIONS = "com.weiboa.action.SEND_TIMELINE_NOTIFICATIONS";

	private static final String TAG = TimeLineActivity.class.getSimpleName();
	private WeiboAApplication mApplication;
	private ListView mListView;
	private Cursor mCursor;
	private StatusAdapter mStatusAdapter;
	private IntentFilter mFilter;
	private TimelineReceiver mReceiver;

	private static final int STOP = 1;

	class TimelineReceiver extends BroadcastReceiver{
		TimeLineActivity timeline = TimeLineActivity.this;

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(UpdateService.NEW_STATUS_INTENT)){

				timeline.mStatusAdapter = new StatusAdapter(timeline, R.layout.row, getStatus());
				timeline.setListAdapter(mStatusAdapter);
				timeline.mStatusAdapter.notifyDataSetChanged();
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
		mListView = getListView();
		mReceiver = new TimelineReceiver();
		mFilter = new IntentFilter(UpdateService.NEW_STATUS_INTENT);
		View footerView = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listfooter, null, false);
		mListView.addFooterView(footerView);

		startService(new Intent(TimeLineActivity.this, UpdateService.class));
	}

	private List<TweetStatus> getStatus(){
		Cursor cursor = getContentResolver().query(StatusProvider.CONTENT_URI, StatusProvider.DB_QUERYBASIC_COLUMNS, null, null, StatusProvider.GET_ALL_ORDER_BY);
		startManagingCursor(cursor);
		List<TweetStatus> ret = new ArrayList<TweetStatus>();
		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
			TweetStatus status = new TweetStatus((new Long(cursor.getLong(0))), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getString(4));
			ret.add(status);
		}
		return ret;
	}

	private List<TweetStatus> getSpecialStatus(Long maxID, Integer num){
		Cursor cursor = getContentResolver().query(StatusProvider.CONTENT_URI, StatusProvider.DB_QUERYBASIC_COLUMNS, StatusProvider.C_ID + " < " + maxID, null, StatusProvider.GET_ALL_ORDER_BY);
		startManagingCursor(cursor);
		List<TweetStatus> ret = new ArrayList<TweetStatus>();
		if(cursor.getCount() == 0){
			return ret;
		}
		int i = 0;
		for(cursor.moveToFirst();!cursor.isAfterLast() && i < num; cursor.moveToNext(), i++){
			TweetStatus status = new TweetStatus((new Long(cursor.getLong(0))), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getString(4));
			ret.add(status);
		}
		return ret;
	}

	private void setupList()
	{
		mStatusAdapter = new StatusAdapter(this, R.layout.row, getStatus());
		setListAdapter(mStatusAdapter);

		mListView = getListView();
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		//		mStatusAdapter = new StatusAdapter<TweetStatus>(this, R.layout.row, getStatus());
		//		mListView.setAdapter(mStatusAdapter);

		//Register the receiver
		registerReceiver(mReceiver, mFilter, SEND_TIMELINE_NOTIFICATIONS, null);
	}

	public class DownLoadStatus extends AsyncTask<Void, Void, Void> {

		Long id;
		Integer num;
		int newUpdates = 0;

		public DownLoadStatus(JSONObject jObject){
			try {
				id = jObject.getLong("ID");
				num = jObject.getInt("NUM");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected Void doInBackground(Void... params) {

			newUpdates = WeiboUserUtil.fetchStatusUnderMax(mApplication, id);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if(newUpdates != 0){
				List<TweetStatus> list = getSpecialStatus(id, 7);
				int i = 0;
				for(TweetStatus status : list){
					mStatusAdapter.insert(status, num + i);
					i++;
				}
				mStatusAdapter.notifyDataSetChanged();

			}
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

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		boolean loadingMore = 
				(firstVisibleItem + visibleItemCount == totalItemCount );
				int last = firstVisibleItem + visibleItemCount - 1;
				Log.i(TAG, "firstVisible="+firstVisibleItem+" visibleCount="+visibleItemCount+" totalCount="+totalItemCount);

				if(loadingMore ){
					if(last > 0){
						HeaderViewListAdapter adapter = (HeaderViewListAdapter)view.getAdapter();
						StatusAdapter sta = (StatusAdapter)adapter.getWrappedAdapter();
						Log.i(TAG, "The index is :" + last);
						Object item = sta.getItem(sta.getCount() - 1);
						int i = 0;
						if(item instanceof TweetStatus){
							TweetStatus ts = (TweetStatus)item;
							Log.d(TAG, "Max_id  = " + String.valueOf(ts.getId()));
							List<TweetStatus> list = getSpecialStatus(ts.getId(), 7);
							for(TweetStatus status : list){
								sta.add(status);
								i++;
							}

							if( i == 0){
								JSONObject jObject = new JSONObject();
								try {
									jObject.put("ID", ts.getId());
									jObject.put("NUM", last);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								new DownLoadStatus(jObject).execute();
							}
							sta.notifyDataSetChanged();
						}
					}

				}


	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

}
