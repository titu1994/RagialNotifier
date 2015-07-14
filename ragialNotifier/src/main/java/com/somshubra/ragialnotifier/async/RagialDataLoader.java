package com.somshubra.ragialnotifier.async;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import com.somshubra.ragialnotifier.database.RagialDatabase;
import com.somshubra.ragialnotifier.database.RagialProvider;

public class RagialDataLoader extends CursorLoader {

	private static final Uri uri = RagialProvider.CONTENT_URI;
	private static final String projection[] = null; //We need all the item
	
	private static final String selection = null;
	private static final String selectionArgs[] = null;
	private static final String sortOrder = RagialDatabase.COL_RAGIAL_NAME + " asc";
	
	public RagialDataLoader(Context context) {
		super(context, uri, projection, selection, selectionArgs, sortOrder);
		
	}
	
}
