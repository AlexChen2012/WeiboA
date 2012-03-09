package com.weiboa.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.FocusFinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.weiboa.activity.WeiboAApplication;
import com.weiboa.data.DownLoadPircuter;

public class ImageUtil {
	
	public static void loadImageToView(ImageView view, String name, final String original_url){
		BufferedInputStream buf;
		try {
			buf = new BufferedInputStream(new FileInputStream(new File(WeiboAApplication.Dir + name)));
            final Bitmap bitmap = BitmapFactory.decodeStream(buf);
            view.setImageBitmap(bitmap);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ImageView imageView = (ImageView)v;
					if(original_url == null){
						return;
					}
					if(ImageUtil.checkImageExist(original_url)){
						AnimationUtil.stopAnimation(imageView);
						String name = ImageUtil.getImageName(original_url);
						ImageUtil.loadImageToView(imageView, name, null);
					}else{
						
						imageView.setVisibility(View.VISIBLE);
						JSONObject tObject = new JSONObject();
						try {
							tObject.put("url", original_url);
							tObject.put("imageview", imageView);
							tObject.put("original_url", "");
							new DownLoadPircuter(tObject).execute();
						} catch (JSONException e) {
							tObject = null;
							e.printStackTrace();
						}
					}
				}
			});
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getImageName(String url){
    	String[] ss = url.split("/");
    	String name = ss[ss.length -2 ] + "_" + ss[ss.length - 1];
		return name;
	}
	
	public static boolean checkImageExist(String url){
		String name = getImageName(url);
		File file = new File(WeiboAApplication.Dir + name);
		if(file.exists()){
			return true;
		}else{
			return false;
		}
	}

}
