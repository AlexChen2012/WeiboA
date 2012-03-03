package com.weiboa.data;

import java.util.ArrayList;
import java.util.List;

import android.R.string;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;


public class StatusProvider extends ContentProvider{

	public static final String TAG = "StatusProvider";
	public static final Uri CONTENT_URI = Uri
	.parse("content://com.weiboa.provider.statusprovider");
	
	public static final String SINGLE_RECORD_MIME_TYPE =
		"vnd.android.cursor.item/vnd.weiboa.provider.status";

	public static final String MULTIPLE_RECORD_MIME_TYPE =
		"vnd.android.cursor.dir/vnd.weiboa.provider.mstatus";

	static final String DB_NAME = "timeline.db";
	static final int DB_VERSION = 1;
	static final String TABLE = "timeline";

	public static final String C_ID = BaseColumns._ID;
	public static final String C_CREATED_AT = "create_at";
	public static final String C_SOURCE = "source";
	public static final String C_TEXT = "text";
	public static final String C_USER = "user";
	public static final String C_USER_ID = "user_id";
	public static final String C_PIRCTURE = "pircture";
	
	public static final String GET_ALL_ORDER_BY = C_CREATED_AT + " DESC ";
	
	public static final String[] DB_QUERYBASIC_COLUMNS = {C_ID, C_TEXT, C_USER, C_CREATED_AT, C_PIRCTURE};
	
	static final String[] MAX_CREATED_AT_COLUMNS = {"max(" 
		+ C_CREATED_AT + ")"};

	private static final String[] DB_TEXT_COLUMNS = { C_TEXT };
	Context context;

	class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		//Called only once, first time the DB is created
		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "create table " + TABLE + "(" + C_ID + " long primary key, "
			+ C_CREATED_AT + " long, " + C_USER + " text, " + C_SOURCE + " text, " + C_TEXT + " text, " + C_PIRCTURE +  " text, " + C_USER_ID+ " integer)";

			db.execSQL(sql);

			Log.d(TAG, "onCreate sql: " + sql);
		}
		
		// Called whenever newVersion != oldVersion
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			db.execSQL("drop table if exists " + TABLE);
			Log.d(TAG, "onUpdate");
			onCreate(db);

		}
	}
	
	public DbHelper dbHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		long id = this.getId(uri);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
			if(id < 0){
				return db.delete(TABLE, selection, selectionArgs);
			}else{
				return db.delete(TABLE, C_ID+"="+id, null);
			}

	}

	@Override
	public String getType(Uri uri) {
		return this.getId(uri)<0?MULTIPLE_RECORD_MIME_TYPE:
				SINGLE_RECORD_MIME_TYPE;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
			long id = db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			if(id == -1){
				throw new RuntimeException(String.format
						("%s : Failed to insert [%s] to [%s] for unknow reason,", 
						TAG, values, uri));
			}else{
				return ContentUris.withAppendedId(uri, id);
			}
	}
	
	private long getId(Uri uri){
		String lastPathSegment = uri.getLastPathSegment();
		if(lastPathSegment != null){
			try{
				return Long.parseLong(lastPathSegment);
			}catch(NumberFormatException e){
				//at least we tried
			}
		}
		return -1;
	}
	
	@Override
	public boolean onCreate() {
		this.dbHelper = new DbHelper(getContext());
		return true;
	}
	
//	public Cursor getStatusUpdates()
//	{
//		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
//		return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
//	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		long id = this.getId(uri);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		if(id < 0){
			if(selection == null){
				return db.query(TABLE, projection, null, null, null, null, GET_ALL_ORDER_BY);
			}else{
				return db.query(TABLE, projection, selection, null, null, null, GET_ALL_ORDER_BY);
			}
		}else{
			return db.query(TABLE, projection, C_ID+"="+id, null, null, null, null);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		long id = this.getId(uri);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if(db.isOpen()){
				if( id < 0 ){
					return db.update(TABLE, values, selection, selectionArgs);
				}else{
					return db.update(TABLE, values, C_ID+"="+id, null);
				}	
				}else{
			return -1;
		}
	}
	
	
}
