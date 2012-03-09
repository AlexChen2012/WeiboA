package com.weiboa.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.weiboa.R;
import com.weiboa.activity.WeiboAApplication;
import com.weiboa.util.AnimationUtil;
import com.weiboa.util.ImageUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class StatusAdapter<T> extends AbstractAdapter<T>{
	int view;
	private static final String TAG = StatusAdapter.class.getSimpleName();
	
	public StatusAdapter(Context context, int view, List<T> objects) {
		super(context, view, objects);
		this.view = view;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		StatusWrapper wrapper = null;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.row, parent, false);
			wrapper = new StatusWrapper(convertView);
			convertView.setTag(wrapper);
		}else {
			wrapper = (StatusWrapper)convertView.getTag();
		}

		if (position %2 == 0)
			convertView.setBackgroundColor(Color.BLACK);
		else
			convertView.setBackgroundColor(Color.DKGRAY);

		wrapper.populateFrom((TweetStatus) getItem(position));

		return convertView;
	}

	class StatusWrapper{
		private TextView text = null;
		private View row = null;
		private TextView name = null;
		private TextView time = null;
		private ImageView image = null;


		public StatusWrapper(View row) {
			this.row = row;
		}

		ImageView getImage(){
			if(image == null){
				image = (ImageView)row.findViewById(R.id.tlPircture);
			}
			return image;
		}

		TextView getName(){
			if(name == null){
				name = (TextView)row.findViewById(R.id.tlUser);
			}
			return name;
		}

		TextView getTime(){
			if(time == null){
				time = (TextView)row.findViewById(R.id.tlCreatedAt);
			}
			return time;
		}

		TextView getText(){
			if(text == null){
				text = (TextView)row.findViewById(R.id.tlText);
			}
			return text;
		}

		void populateFrom(TweetStatus status){
			getName().setText(status.getUserName());
			CharSequence relTime = DateUtils.getRelativeTimeSpanString(row.getContext(), status.getTime());
			getTime().setText(relTime);
			String text = Html.fromHtml(status.getText()).toString();
			getText().setText(text);
			String url = status.getUrl();
			String original_url = status.getOriginal_url();
//			Log.d(TAG, "URl: "+ url);
			if(url.equals("")){
				getImage().setVisibility(View.GONE);
			}else{

				if(ImageUtil.checkImageExist(url)){
					AnimationUtil.stopAnimation(getImage());
					String name = ImageUtil.getImageName(url);
					
					ImageUtil.loadImageToView(getImage(), name, original_url);
				}else{
					getImage().setVisibility(View.VISIBLE);
					JSONObject tObject = new JSONObject();
					try {
						tObject.put("url", url);
						tObject.put("original_url", original_url);
						tObject.put("imageview", getImage());
						new DownLoadPircuter(tObject).execute();
					} catch (JSONException e) {
						tObject = null;
						e.printStackTrace();
					}
				}

			}
		}
	}

}
