package com.somshubra.ragialnotifier.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class RagialProvider extends ContentProvider {
	private RagialDatabase mDatabase;

	public static final String AUTHORITY = "com.somshubra.ragialnotifier.database";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + RagialDatabase.TABLE_RAGIAL);
	public static final Uri CONTENT_URI_VENDERS = Uri.parse("content://" + AUTHORITY + "/" + RagialDatabase.TABLE_VENDERS);
	
	private static final int ALL_ROWS = 1;
	private static final int SINGLE_ROW = 2;
	private static final int ALL_ROWS_VENDERS = 3;
	private static final int SINGLE_ROW_VENDERS = 4;

	private static final UriMatcher matcher;

	private static final String TAG = "RagialProvider";

	static {
		matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, RagialDatabase.TABLE_RAGIAL, ALL_ROWS);
		matcher.addURI(AUTHORITY, RagialDatabase.TABLE_RAGIAL + "/#", SINGLE_ROW);
		matcher.addURI(AUTHORITY, RagialDatabase.TABLE_VENDERS, ALL_ROWS_VENDERS);
		matcher.addURI(AUTHORITY, RagialDatabase.TABLE_VENDERS + "/#", SINGLE_ROW_VENDERS);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDatabase.getWritableDatabase();

		switch(matcher.match(uri)) {
		case SINGLE_ROW_VENDERS :
		case SINGLE_ROW : {
			String id = uri.getPathSegments().get(1);
			selection = RagialDatabase._ID + " = " + id + ((!TextUtils.isEmpty(selection)) ? " and (" + selection + ")" : "");
			break;
		}
		}
		
		int deleteCount = 0;
		switch(matcher.match(uri)) {
		case SINGLE_ROW : {
			deleteCount= db.delete(RagialDatabase.TABLE_RAGIAL, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			break;
		}
		case SINGLE_ROW_VENDERS : {
			deleteCount= db.delete(RagialDatabase.TABLE_VENDERS, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(CONTENT_URI_VENDERS, null);
		}
		case ALL_ROWS : {
			//Log.i(TAG, "May delete all data if where clause is faulty");
			deleteCount = db.delete(RagialDatabase.TABLE_RAGIAL, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			
			break;
		}
		case ALL_ROWS_VENDERS : {
			//Log.i(TAG, "May delete all data if where clause is faulty");
			deleteCount = db.delete(RagialDatabase.TABLE_VENDERS, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(CONTENT_URI_VENDERS, null);
			
			break;
		}
		}
		return deleteCount;
	}

	@Override
	public String getType(Uri uri) {
		switch(matcher.match(uri)) {
		case ALL_ROWS_VENDERS :
		case ALL_ROWS : 
			return "vnd.android.cursor.dir/vnd.somshubra.ragialnotifier.database";
		case SINGLE_ROW_VENDERS :
		case SINGLE_ROW :
			return "vnd.android.cursor.item/vnd.somshubra.ragialnotifier.database";
		
		default :
			throw new UnsupportedOperationException("Uri did not match");
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mDatabase.getWritableDatabase();
		long id = 0;
		Uri insertedUri = null;
		
		switch(matcher.match(uri)) {
		case ALL_ROWS : {
			id = db.insert(RagialDatabase.TABLE_RAGIAL, null, values);
			break;
		}
		case ALL_ROWS_VENDERS : {
			id = db.insert(RagialDatabase.TABLE_VENDERS, null, values);
			break;
		}
		}
		
		if(id > -1) {
			insertedUri = ContentUris.withAppendedId(uri, id);
			getContext().getContentResolver().notifyChange(insertedUri, null);
		}
		else {
			return null;
		}
		return uri;
	}

	@Override
	public boolean onCreate() {
		mDatabase = new RagialDatabase(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		SQLiteDatabase db = mDatabase.getReadableDatabase();

		switch(matcher.match(uri)) {
		case ALL_ROWS : {
			builder.setTables(RagialDatabase.TABLE_RAGIAL);
			break;
		}
		case SINGLE_ROW : {
			String rowID = uri.getPathSegments().get(1);
			builder.appendWhere(RagialDatabase._ID + " = " + rowID);
			builder.setTables(RagialDatabase.TABLE_RAGIAL);
			break;
		}
		case ALL_ROWS_VENDERS : {
			builder.setTables(RagialDatabase.TABLE_VENDERS);
			break;
		}
		case SINGLE_ROW_VENDERS : {
			String rowID = uri.getPathSegments().get(1);
			builder.appendWhere(RagialDatabase._ID + " = " + rowID);
			builder.setTables(RagialDatabase.TABLE_VENDERS);
			break;
		}
		}
		
		Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.moveToFirst();

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDatabase.getWritableDatabase();
		int updateCount = 0;
		
		switch(matcher.match(uri)) {
		
		case SINGLE_ROW : {
			String rowID = uri.getPathSegments().get(1);
			selection = RagialDatabase._ID + " = " + rowID + ((!TextUtils.isEmpty(selection)) ? " and (" + selection + ")" : "");
			updateCount = db.update(RagialDatabase.TABLE_RAGIAL, values, selection, selectionArgs);
			break;
		}
		case SINGLE_ROW_VENDERS : {
			String rowID = uri.getPathSegments().get(1);
			selection = RagialDatabase._ID + " = " + rowID + ((!TextUtils.isEmpty(selection)) ? " and (" + selection + ")" : "");
			updateCount = db.update(RagialDatabase.TABLE_VENDERS, values, selection, selectionArgs);
			break;
		}
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return updateCount;
	}
}
