package com.weiboa.data;

import com.weiboa.R;

import android.R.string;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

public class TimeLineAdapter extends SimpleCursorAdapter{

	public static final String C_TEXT 		= "text";
	public static final String C_USER 		= "user";
	public static final String C_CREATED_AT = "create_at";
	public static final String C_PIRCTURE 	= "pircture";
	
	public static final String[] FROM = {C_CREATED_AT,
		C_USER,
		C_TEXT,
		C_PIRCTURE};

	public static final int[] TO = {R.id.tlCreatedAt, R.id.tlUser, R.id.tlText, R.id.tlPircture};

	public TimeLineAdapter(Context context, int layout, Cursor c) {
		super(context, layout, c, FROM, TO);
	}
	
}
