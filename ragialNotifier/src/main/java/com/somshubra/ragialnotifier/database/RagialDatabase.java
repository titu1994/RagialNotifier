package com.somshubra.ragialnotifier.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RagialDatabase extends SQLiteOpenHelper {

	private static final String TAG = RagialDatabase.class.getSimpleName();
	
	private static final String DB_NAME = "ragial.sqlite";
	private static final int DB_VERSION = 3;
	
	public static final String TABLE_RAGIAL = "ragial_table";
	public static final String _ID = "_id";
	public static final String COL_RAGIAL_NAME = "ragial_name";
	public static final String COL_SHORT_COUNT = "short_count";
	public static final String COL_SHORT_MIN = "short_min";
	public static final String COL_SHORT_MAX = "short_max";
	public static final String COL_SHORT_AVG = "short_avg";
	public static final String COL_SHORT_STD = "short_std_def";
	public static final String COL_SHORT_CONFIDENCE = "short_confidence";
	public static final String COL_LONG_COUNT = "long_count";
	public static final String COL_LONG_MIN = "long_min";
	public static final String COL_LONG_MAX = "long_max";
	public static final String COL_LONG_AVG = "long_avg";
	public static final String COL_LONG_STD = "long_std_def";
	public static final String COL_LONG_CONFIDENCE = "long_confidence";
	
	public static final String TABLE_VENDERS = "venders_table";
	public static final String COL_VENDER_NAME = "vender_name";
	public static final String COL_VENDER_SHOP_NAME = "vender_shop_name";
	public static final String COL_VENDER_COUNT = "vender_count";
	public static final String COL_VENDER_PRICE = "vender_price";
	public static final String COL_VENDER_STD = "vender_std";
	public static final String COL_VENDER_IS_BUYING_TYPE = "vender_buying_type";
	
	private static final String CREATE_TABLE_RAGIAL = "create table if not exists " + TABLE_RAGIAL + "("
			+ _ID + " integer primary key autoincrement, " 
			+ COL_RAGIAL_NAME + " text unique, "
			+ COL_SHORT_COUNT + " integer default 0, "
			+ COL_SHORT_MIN + " real default 0, " 
			+ COL_SHORT_MAX + " real default 0, "
			+ COL_SHORT_AVG + " real default 0, " 
			+ COL_SHORT_STD + " real default 0, "
			+ COL_SHORT_CONFIDENCE + " real default 0, "
			+ COL_LONG_COUNT + " integer default 0, "
			+ COL_LONG_MIN + " real default 0, " 
			+ COL_LONG_MAX + " real default 0, "
			+ COL_LONG_AVG + " real default 0, "
			+ COL_LONG_STD + " real default 0, "
			+ COL_LONG_CONFIDENCE + " real default 0);";
	
	private static final String CREATE_TABLE_VENDER = "create table if not exists " + TABLE_VENDERS +"("
			+ _ID + " integer primary key autoincrement, "
			+ COL_RAGIAL_NAME + " text, "
			+ COL_VENDER_NAME + " text, " 
			+ COL_VENDER_COUNT + " integer default 0, " 
			+ COL_VENDER_PRICE + " real default 0, " 
			+ COL_VENDER_SHOP_NAME + " text , "
			+ COL_VENDER_STD + " real default 0, "
			+ COL_VENDER_IS_BUYING_TYPE + " integer default 0, "
			+ "FOREIGN KEY(" + COL_VENDER_NAME + ") REFERENCES " + TABLE_RAGIAL + "(" 
			+ COL_RAGIAL_NAME + ")"
			+ " on delete cascade);";
			
	public RagialDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_RAGIAL);
		//Log.d(TAG, "Table RAGIAL created");
		
		db.execSQL(CREATE_TABLE_VENDER);
		//Log.d(TAG, "Table VENDERS created");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//db.execSQL("drop table if exists " + TABLE_RAGIAL);
		db.execSQL("drop table if exists " + TABLE_VENDERS);
		//Log.d(TAG, "Tables dropped");
		onCreate(db);
	}

}
