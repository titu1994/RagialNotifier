package com.somshubra.ragialnotifier.async;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.somshubra.ragialnotifier.R;
import com.somshubra.ragialnotifier.database.RagialDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yue on 5/28/2015.
 */
public class RagialCursorAdapterV2 extends CursorRecyclerViewAdapter<RagialCursorAdapterV2.RagialViewHolder>{

    private LayoutInflater inflater;
    private OnItemClickedListener onItemClickedListener;
    private OnItemLongClickedListener onItemLongClickedListener;

    private SparseBooleanArray selectedArray;

    public RagialCursorAdapterV2(Context context, Cursor cursor) {
        super(context, cursor);
        selectedArray = new SparseBooleanArray();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RagialViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.card_layout, parent, false);
        return new RagialViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RagialViewHolder v, Cursor cursor, int pos) {
        RagialListItem i = RagialListItem.fromCursor(cursor);

        //Log.d("RagialCAdapter2", "Selected Array : " + selectedArray.toString());
        v.cv.setCardBackgroundColor(selectedArray.get(pos, false) ? Color.LTGRAY : Color.WHITE);
        Log.d("RagialCAdapter2", "Selected item : " + selectedArray.get(pos, false) + " at " + pos + " with name : " + i.ragialName);
        v.ragialName.setText(i.ragialName);
        v.ragialName.setSelected(true);

        v.shortCount.setText(convertText(v.shortCount, i.shortCount + ""));
        v.shortMin.setText(convertText(v.shortMin, "" + i.shortMin));
        v.shortMax.setText(convertText(v.shortMax, "" + i.shortMax));
        v.shortAverage.setText(convertText(v.shortAverage, "" + i.shortAverage));
        v.shortStdDev.setText(convertText(v.shortStdDev, "" + i.shortStdDev));
        v.shortConfidence.setText(convertText(v.shortConfidence, "" + i.shortConfidence));

        v.longCount.setText(convertText(v.longCount, i.longCount + ""));
        v.longMin.setText(convertText(v.longMin, "" + i.longMin));
        v.longMax.setText(convertText(v.longMax, "" + i.longMax));
        v.longAverage.setText(convertText(v.longAverage, "" + i.longAverage));
        v.longStdDev.setText(convertText(v.longStdDev, "" + i.longStdDev));
        v.longConfidence.setText(convertText(v.longConfidence, "" + i.longConfidence));

    }

    private String convertText(TextView v, String text) {
        Double d = null;
        try {
            d = Double.parseDouble(text);
        }
        catch (NumberFormatException e) {
            return text;
        }
        if(v.getId() == R.id.short_min) {
            if(d >= 1000000) {
                d /= 1000000;
                return d + "M";
            }
            else if(d >= 1000) {
                d /= 1000;
                return d + "K";
            }
            return text;
        }
        else if(v.getId() == R.id.short_max) {
            if(d >= 1000000) {
                d /= 1000000;
                return d + "M";
            }
            else if(d >= 1000) {
                d /= 1000;
                return d + "K";
            }
            return text;
        }
        else if(v.getId() == R.id.short_avg) {
            if(d >= 1000000) {
                d /= 1000000;
                return d + "M";
            }
            else if(d >= 1000) {
                d /= 1000;
                return d + "K";
            }
            return text;
        }
        else if(v.getId() == R.id.short_count) {
            return text;
        }
        else if(v.getId() == R.id.short_std) {
            if(d >= 1000000) {
                d /= 1000000;
                return d + "M";
            }
            else if(d >= 1000) {
                d /= 1000;
                return d + "K";
            }
            return text;
        }
        else if(v.getId() == R.id.short_conf) {
            return text;
        }
        else if(v.getId() == R.id.long_min) {
            if(d >= 1000000) {
                d /= 1000000;
                return d + "M";
            }
            else if(d >= 1000) {
                d /= 1000;
                return d + "K";
            }
            return text;
        }
        else if(v.getId() == R.id.long_max) {
            if(d >= 1000000) {
                d /= 1000000;
                return d + "M";
            }
            else if(d >= 1000) {
                d /= 1000;
                return d + "K";
            }
            return text;
        }
        else if(v.getId() == R.id.long_avg) {
            if(d >= 1000000) {
                d /= 1000000;
                return d + "M";
            }
            else if(d >= 1000) {
                d /= 1000;
                return d + "K";
            }
            return text;
        }
        else if(v.getId() == R.id.long_std) {
            if(d >= 1000000) {
                d /= 1000000;
                return d + "M";
            }
            else if(d >= 1000) {
                d /= 1000;
                return d + "K";
            }
            return text;
        }
        else if(v.getId() == R.id.long_count) {
            return text;
        }
        else if(v.getId() == R.id.long_conf) {
            return text;
        }

        return text;
    }

    public void toggleSelection(int position) {

        if (selectedArray.get(position, false)) {
            Log.d("RagialCAdapter2", "Unselected item : " + position);
            selectedArray.put(position, false);
        }
        else {
            Log.d("RagialCAdapter2", "Selected item : " + position);
            selectedArray.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void clearSelections() {
        selectedArray.clear();
        notifyItemRangeChanged(0, getItemCount());
    }

    public int getSelectedItemCount() {
        return selectedArray.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<Integer>(selectedArray.size());
        for (int i = 0; i < selectedArray.size(); i++) {
            items.add(selectedArray.keyAt(i));
        }
        return items;
    }

    public class RagialViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public CardView cv;

        public TextView ragialName;
        public TextView shortCount;
        public TextView shortMin;
        public TextView shortMax;
        public TextView shortAverage;
        public TextView shortStdDev;
        public TextView shortConfidence;
        public TextView longCount;
        public TextView longMin;
        public TextView longMax;
        public TextView longAverage;
        public TextView longStdDev;
        public TextView longConfidence;

        public RagialViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            cv = (CardView) itemView.findViewById(R.id.card_view);

            ragialName = (TextView) cv.findViewById(R.id.ragial_name);

            shortCount = (TextView) cv.findViewById(R.id.short_count);
            shortMin = (TextView) cv.findViewById(R.id.short_min);
            shortMax = (TextView) cv.findViewById(R.id.short_max);
            shortAverage = (TextView) cv.findViewById(R.id.short_avg);
            shortStdDev = (TextView) cv.findViewById(R.id.short_std);
            shortConfidence = (TextView) cv.findViewById(R.id.short_conf);

            longCount = (TextView) cv.findViewById(R.id.long_count);
            longMin = (TextView) cv.findViewById(R.id.long_min);
            longMax = (TextView) cv.findViewById(R.id.long_max);
            longAverage = (TextView) cv.findViewById(R.id.long_avg);
            longStdDev = (TextView) cv.findViewById(R.id.long_std);
            longConfidence = (TextView) cv.findViewById(R.id.long_conf);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickedListener != null)
                onItemClickedListener.onItemClicked(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if(onItemLongClickedListener != null) {
                return onItemLongClickedListener.onItemLongClicked(v, getAdapterPosition());
            }
            return false;
        }
    }

    public interface OnItemClickedListener {
        void onItemClicked(View v, int position);
    }

    public interface  OnItemLongClickedListener {
        boolean onItemLongClicked(View v, int position);
    }

    public void setOnItemClickedListener(OnItemClickedListener mItemClickedListener) {
        this.onItemClickedListener = mItemClickedListener;
    }

    public void setOnItemLongClickedListener(OnItemLongClickedListener onItemLongClickedListener) {
        this.onItemLongClickedListener = onItemLongClickedListener;
    }

    public static class RagialListItem{
        public String ragialName;
        public int shortCount;
        public long shortMin;
        public long shortMax;
        public long shortAverage;
        public long shortStdDev;
        public double shortConfidence;
        public int longCount;
        public long longMin;
        public long longMax;
        public long longAverage;
        public long longStdDev;
        public double longConfidence;

        public static RagialListItem fromCursor(Cursor cursor) {
            RagialListItem i = new RagialListItem();
            i.ragialName = cursor.getString(cursor.getColumnIndex(RagialDatabase.COL_RAGIAL_NAME));

            i.shortCount = cursor.getInt(cursor.getColumnIndex(RagialDatabase.COL_SHORT_COUNT));
            i.shortMin = cursor.getLong(cursor.getColumnIndex(RagialDatabase.COL_SHORT_MIN));
            i.shortMax = cursor.getLong(cursor.getColumnIndex(RagialDatabase.COL_SHORT_MAX));
            i.shortAverage = cursor.getLong(cursor.getColumnIndex(RagialDatabase.COL_SHORT_AVG));
            i.shortStdDev = cursor.getLong(cursor.getColumnIndex(RagialDatabase.COL_SHORT_COUNT));
            i.shortConfidence = cursor.getDouble(cursor.getColumnIndex(RagialDatabase.COL_SHORT_COUNT));

            i.longCount = cursor.getInt(cursor.getColumnIndex(RagialDatabase.COL_LONG_COUNT));
            i.longMin = cursor.getLong(cursor.getColumnIndex(RagialDatabase.COL_LONG_MIN));
            i.longMax = cursor.getLong(cursor.getColumnIndex(RagialDatabase.COL_LONG_MAX));
            i.longAverage = cursor.getLong(cursor.getColumnIndex(RagialDatabase.COL_LONG_AVG));
            i.longStdDev = cursor.getLong(cursor.getColumnIndex(RagialDatabase.COL_LONG_COUNT));
            i.longConfidence = cursor.getDouble(cursor.getColumnIndex(RagialDatabase.COL_LONG_COUNT));

            return i;
        }
    }
}

