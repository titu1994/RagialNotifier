package com.somshubra.ragialnotifier.async;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import com.ragialquery.data.RagialData;
import com.ragialquery.data.RagialData.VendingNow;
import com.ragialquery.data.RagialQueryMatcher;
import com.somshubra.ragialnotifier.MainActivity;
import com.somshubra.ragialnotifier.database.RagialDatabase;
import com.somshubra.ragialnotifier.database.RagialProvider;
import com.somshubra.ragialnotifier.fragment.RagialErrorDialog;

import java.util.ArrayList;

public class RagialDataInsert extends AsyncTask<RagialData, Void, Void> {

	private Context context;
	private String strictName;
	private boolean dataIsNull;

	public RagialDataInsert(Context context, String strictName) {
		this.context = context;
		this.strictName = strictName;

	}

	@Override
	protected Void doInBackground(RagialData... params) {
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues();

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		boolean strict = sp.getBoolean("StrictName", true);

		if(!strict) {
			for(RagialData data : params) {
				insertRagialData(resolver, values, data);
			}
		}
		else {
			RagialData data = RagialQueryMatcher.searchRagialSpecificly(strictName, params);
			insertRagialData(resolver, values, data);
		}
		return null;
	}

	private void insertRagialData(ContentResolver resolver, ContentValues values,
								  RagialData data) {
		if(data == null) {
			dataIsNull = true;
			return;
		}
		values.put(RagialDatabase.COL_RAGIAL_NAME, data.name);

		values.put(RagialDatabase.COL_SHORT_COUNT, data.shortNumber);
		values.put(RagialDatabase.COL_SHORT_MIN, data.shortMin);
		values.put(RagialDatabase.COL_SHORT_MAX, data.shortMax);
		values.put(RagialDatabase.COL_SHORT_AVG, data.shortAverage);
		values.put(RagialDatabase.COL_SHORT_STD, data.shortStdDev);
		values.put(RagialDatabase.COL_SHORT_CONFIDENCE, data.shortConfidence);

		values.put(RagialDatabase.COL_LONG_COUNT, data.longNumber);
		values.put(RagialDatabase.COL_LONG_MIN, data.longMin);
		values.put(RagialDatabase.COL_LONG_MAX, data.longMax);
		values.put(RagialDatabase.COL_LONG_AVG, data.longAverage);
		values.put(RagialDatabase.COL_LONG_STD, data.longStdDev);
		values.put(RagialDatabase.COL_LONG_CONFIDENCE, data.longConfidence);

		resolver.insert(RagialProvider.CONTENT_URI, values);

		if(data.isBeingVended) {
			ArrayList<ContentProviderOperation> opers = new ArrayList<>();
			ContentProviderOperation oper;
			for(VendingNow v : data.vendingList) {
				values.clear();
				values.put(RagialDatabase.COL_RAGIAL_NAME, data.name);
				values.put(RagialDatabase.COL_VENDER_NAME, v.venderName);
				values.put(RagialDatabase.COL_VENDER_SHOP_NAME, v.shopName);
				values.put(RagialDatabase.COL_VENDER_COUNT, v.vendCount);
				values.put(RagialDatabase.COL_VENDER_STD, v.stdCh);
				values.put(RagialDatabase.COL_VENDER_IS_BUYING_TYPE, v.isTypeBuying);
				values.put(RagialDatabase.COL_VENDER_PRICE, v.vendPrice);

				oper = ContentProviderOperation.newInsert(RagialProvider.CONTENT_URI_VENDERS).withValues(values).build();
				opers.add(oper);
				//Log.i("RagialDataInsertThread", "Name : " + data.name + "\n" + v.toString());
			}

			try {
				resolver.applyBatch(RagialProvider.AUTHORITY, opers);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}
		}
		values.clear();
	}

	@Override
	protected void onPostExecute(Void aVoid) {
		super.onPostExecute(aVoid);

		if(dataIsNull) {
			if(context instanceof MainActivity) {
				MainActivity m = (MainActivity) context;
				RagialErrorDialog errDialog = new RagialErrorDialog();
				errDialog.setErrorMessage("The name that you have entered could not be matched to any Ragial item. Please make sure to copy the name exactly from ragial.com. If you wish for a general search, in Settings, un-select the \"Enable strict name check\" feature.");
				errDialog.show(m.getSupportFragmentManager(), "ERROR Dialog");
			}
		}
	}
}

