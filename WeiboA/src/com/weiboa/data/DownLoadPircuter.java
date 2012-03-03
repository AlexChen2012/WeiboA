package com.weiboa.data;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;

import com.weiboa.R;
import com.weiboa.activity.WeiboAApplication;
import com.weiboa.util.AnimationUtil;
import com.weiboa.util.ImageUtil;

import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

public class DownLoadPircuter extends AsyncTask<Void, Void, Void>{

	ImageView mImageView;
	String name;
	AnimationDrawable animation = null;
	URL url;
	
	public DownLoadPircuter(JSONObject jObject){
		
    	try {
    		url = new URL(jObject.getString("url"));
    		name = ImageUtil.getImageName(jObject.getString("url"));
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
		ImageUtil.loadImageToView(mImageView, name);
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
//                publishProgress((int)(total*100/lenghtOfFile));
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