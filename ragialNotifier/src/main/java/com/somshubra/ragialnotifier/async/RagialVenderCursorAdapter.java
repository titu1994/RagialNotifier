package com.somshubra.ragialnotifier.async;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.somshubra.ragialnotifier.R;
import com.somshubra.ragialnotifier.database.RagialDatabase;

/**
 * Created by Yue on 6/5/2015.
 */
public class RagialVenderCursorAdapter extends CursorRecyclerViewAdapter<RagialVenderCursorAdapter.RagialViewHolder> {

    private LayoutInflater inflater;
    private boolean  isFirstBuy;

    public RagialVenderCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        inflater = LayoutInflater.from(context);
        isFirstBuy = true;
    }

    @Override
    public RagialViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.card_venders, parent, false);
        return new RagialViewHolder(v);
    }


    @Override
    public void onBindViewHolder(RagialViewHolder v, Cursor cursor, int position) {
        RagialVendListItem i = RagialVendListItem.fromCursor(cursor);

        v.vendName.setText(convertText( v.vendName, i.vendName));
        v.vendShop.setText(convertText(v.vendShop, i.vendShop));
        v.vendCount.setText(convertText(v.vendCount, i.vendCount + ""));
        v.vendPrice.setText(convertText(v.vendPrice, i.vendPrice + ""));
        v.vendStd.setText(convertText(v.vendStd, i.vendStd + ""));
        if(v.vendIsBuying != null)
            v.vendIsBuying.setText(i.isBuying == 0? "V" : "B");
    }

    private String convertText(TextView v, String text) {
        Double d = null;
        try {
            d = Double.parseDouble(text);
            d = Math.ceil(d);
        }
        catch (NumberFormatException e) {
            return text;
        }
        if(v.getId() == R.id.vend_price) {
            if(d >= 1000000) {
                d /= 1000000;
                return d + "M ea.";
            }
            else if(d >= 1000) {
                d /= 1000;
                return d + "K ea.";
            }
            return text;
        }
        else if(v.getId() == R.id.vend_std) {
            if(d < 0) {
                v.setTextColor(Color.parseColor("#43A047"));
            }
            else if(d > 0) {
                v.setTextColor(Color.parseColor("#F44336"));
            }
            else {
                v.setTextColor(Color.BLACK);
            }
            return d + "%";
        }

        return text;
    }


    public class RagialViewHolder extends RecyclerView.ViewHolder {
        public CardView cv;

        public TextView vendName;
        public TextView vendCount;
        public TextView vendShop;
        public TextView vendPrice;
        public TextView vendStd;
        public TextView vendIsBuying;

        public RagialViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_vends);

            vendName = (TextView) cv.findViewById(R.id.vend_name);

            vendCount = (TextView) cv.findViewById(R.id.vend_count);
            vendShop = (TextView) cv.findViewById(R.id.vend_shop);
            vendPrice = (TextView) cv.findViewById(R.id.vend_price);
            vendStd = (TextView) cv.findViewById(R.id.vend_std);
            vendIsBuying = (TextView) cv.findViewById(R.id.vend_type);
        }

    }

    public static class RagialVendListItem {
        public String vendName;
        public int vendCount;
        public String vendShop;
        public long vendPrice;
        public double vendStd;
        public int isBuying;

        public static RagialVendListItem fromCursor(Cursor cursor) {
            RagialVendListItem i = new RagialVendListItem();
            i.vendName = cursor.getString(cursor.getColumnIndex(RagialDatabase.COL_VENDER_NAME));
            i.vendCount = cursor.getInt(cursor.getColumnIndex(RagialDatabase.COL_VENDER_COUNT));
            i.vendPrice = cursor.getLong(cursor.getColumnIndex(RagialDatabase.COL_VENDER_PRICE));
            i.vendShop = cursor.getString(cursor.getColumnIndex(RagialDatabase.COL_VENDER_SHOP_NAME));
            i.vendStd = cursor.getDouble(cursor.getColumnIndex(RagialDatabase.COL_VENDER_STD));
            i.isBuying = cursor.getInt(cursor.getColumnIndex(RagialDatabase.COL_VENDER_IS_BUYING_TYPE));

            return i;
        }
    }
}
