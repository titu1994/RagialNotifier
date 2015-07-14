package com.somshubra.ragialnotifier.async;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;

import com.ragialquery.data.RagialData;
import com.ragialquery.data.RagialData.VendingNow;
import com.somshubra.ragialnotifier.database.RagialDatabase;
import com.somshubra.ragialnotifier.database.RagialProvider;

import java.util.ArrayList;

public class RagialDataUpdate extends AsyncTask<RagialData, Void, Void> {
    private Uri uri;
    private Context context;
    private boolean updateOnlyVenders = false;

    public RagialDataUpdate(Context context, Uri uri, boolean updateOnlyVenders) {
        this.context = context;
        this.uri = uri;
        this.updateOnlyVenders = updateOnlyVenders;
    }

    @Override
    protected Void doInBackground(RagialData... params) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();

        for(RagialData data : params) {
            if(!updateOnlyVenders) {

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

                resolver.update(uri, values, null, null);
            }

            //Log.d("Update", "Data is being vended : " + data.isBeingVended);
            if(data.isBeingVended) {

                String where = RagialDatabase.COL_RAGIAL_NAME + " = ?";
                String whereArgs[] = new String[] {data.name};

                //Log.d("Delete the old data", "Data Name : " + data.name + " : " + resolver.delete(RagialProvider.CONTENT_URI_VENDERS, where, whereArgs));
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
        return null;
    }

}
