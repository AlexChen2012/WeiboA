package com.weiboa.util;

import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

public class AnimationUtil {
	
	  public static void startAnimation(ImageView pImageView,AnimationDrawable pAnimation, int resId)
	  {
	    if (pImageView != null && pAnimation == null)
	    {
	      pImageView.setBackgroundResource(resId);
	      pAnimation = (AnimationDrawable) pImageView.getBackground();
	    }
	    if (pAnimation != null && !pAnimation.isRunning())
	    {
	      pAnimation.start();
	    }
	  }
	  
	  public static void stopAnimation(ImageView pImageView)
	  {
	    AnimationDrawable pAnimation = null;
	    if (pImageView != null)
	    {
	      pAnimation = (AnimationDrawable) pImageView.getBackground();
	    }
	    if (pAnimation != null && pAnimation.isRunning())
	    {
	      pAnimation.stop();
	    }
	  }
}
