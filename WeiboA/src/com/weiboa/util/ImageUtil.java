package com.weiboa.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import com.weiboa.activity.WeiboAApplication;

public class ImageUtil {
	
	public static void loadImageToView(ImageView view, String name){
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
