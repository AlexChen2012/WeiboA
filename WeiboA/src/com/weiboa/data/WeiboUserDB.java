package com.weiboa.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WeiboUserDB {
	
	
    private Object mutex = new Object();

	private static final String TAG = WeiboUserDB.class.getSimpleName();
	
    private static final String DB_NAME                     = "userdata.db";
    private static final int DB_VERSION                     = 1;
    private static final String WEIBO_USER_TABLE          	= "weibouser";

    public static final String C_USERNAME					= WeiboUser.KEY_USERNAME;
    public static final String C_PASSWORD					= WeiboUser.KEY_PASSWORD;
    public static final String C_USERID						= WeiboUser.KEY_USEID;
    public static final String C_ACCESS_TOKEN				= WeiboUser.KEY_ACCESS_TOKEN;
    public static final String C_ACCESS_TOKEN_SECRET		= WeiboUser.KEY_ACCESS_TOKEN_SECRET;

	class DbHelper extends SQLiteOpenHelper{

		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			
			final String createUserTableSql = "create table weibouser ( username text, password text, userid text, user_access_token text, user_access_token_secret text, PRIMARY KEY (user_access_token, user_access_token_secret))";
			
			db.execSQL(createUserTableSql);
			Log.d(TAG, "onCreate");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + WEIBO_USER_TABLE);
            Log.d(TAG, "onUpgrade");
		}
		
	}
	
	private final DbHelper dbHelper;
	
	public WeiboUserDB(Context context){
		this.dbHelper = new DbHelper(context);
	}
	
	public void close(){
		this.dbHelper.close();
	}
	
	public void insertUserInfo(String userID, String userName, String password, String accessToken, String accessTokenSecret){
		
		synchronized (mutex) {

			ContentValues value = new ContentValues();
			value.put(C_USERID, userID);
			value.put(C_USERNAME, userName);
			value.put(C_PASSWORD, password);
			value.put(C_ACCESS_TOKEN, accessToken);
			value.put(C_ACCESS_TOKEN_SECRET, accessTokenSecret);

			insertOrIgnore(value, WEIBO_USER_TABLE);
		}
	}
	
    private void insertOrIgnore(ContentValues values, String tableName)
    {
        synchronized (mutex) {
            Log.d(TAG, "insertOrIgnore on " + values);
            SQLiteDatabase db = this.dbHelper.getWritableDatabase();

            db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);

            db.close();
        }
    }
    
    public String getUserNameByAccessToken(String accessToken, String accessTokenSecret){
        synchronized (mutex) {
            SQLiteDatabase db = this.dbHelper.getReadableDatabase();
            try{
                String sql = "select username from weibouser where user_access_token = '"+ accessToken + "' and user_access_token_secret = '" + accessTokenSecret + "'";
                Cursor cursor = null;
                try{
                    cursor = db.rawQuery(sql, null);
                    return cursor.moveToNext() ? cursor.getString(0): null;
                }catch (Exception e) {
                    return null;
                }finally{
                    if(cursor != null){
                        cursor.close();
                    }
                }
            }finally{
                db.close();
            }
        }
    }

    
    public void updateUserNameByAccessToken(String accessToken, String accessTokenSecret, String username){
        synchronized (mutex) {
            SQLiteDatabase db = this.dbHelper.getReadableDatabase();
            try{
                String sql = "update weibouser set username = '" + username + "' where user_access_token = '"+ accessToken + "' and user_access_token_secret = '" + accessTokenSecret + "'";
                Cursor cursor = null;
                try{
                	db.execSQL(sql);
                }catch (Exception e) {
                	
                }finally{
                    if(cursor != null){
                        cursor.close();
                    }
                }
            }finally{
                db.close();
            }
        }
    }

}
