package com.weiboa.data;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AbstractAdapter<T> extends ArrayAdapter<T>{
	
	List<T> items;
	LayoutInflater inflater;
	Context context;
	
    public AbstractAdapter(Context context, int view, List<T> objects) {
        super(context, view, objects);

        this.context = context;
        this.items = objects;
        inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    static class ViewHolder {
        ImageView iv;
        TextView text;
        TextView screenName;
        TextView time;
    }
}
