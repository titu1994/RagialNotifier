package com.somshubra.ragialnotifier.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.somshubra.ragialnotifier.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class LibraryAckActivityFragment extends Fragment {

    private ListView lv;
    private LibAckAdapter adapter;

    public LibraryAckActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_library_ack, container, false);
        lv = (ListView) v.findViewById(R.id.list_lib_ack);
        adapter = new LibAckAdapter(getActivity());

        lv.setAdapter(adapter);
        return v;
    }

    private static class LibAckAdapter extends BaseAdapter {
        private static final String[] libNames = {
                "Ragial2 (Ragial Searcher)",
                "Gson-2.2.4",
                "Jsoup-1.7.3",
                "appCompat-v7",
                "cardview-v7",
                "recyclerview-v7"};

        private static final String[] libContents = {
                "A pure Java library created by me in order to parse a html webpage (Ragial.com) and extract the information. Uses Gson and Jsoup to store and parse the ragial html document.",
                "Gson is used to internally store the parsed data by Ragial2 and then retrieve it quickly.",
                "Jsoup is used to efficiently parse the html webpage using the jsoup api. Manages the communication with Ragial.com automatically.",
                "appCompat-v7 is used to bring Material view and animations to 4.0+ devices.",
                "cardview-v7 is used for the awesome CardView to 4.0+ devices.",
                "recyclerview-v7 is used in the starting to efficiently display all the cards while using less memory and resources."
        };

        private static LayoutInflater inflater;

        public LibAckAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return libNames.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            ViewHolder vh;

            if(convertView == null) {
                v = inflater.inflate(R.layout.list_item_lib_ack, parent, false);
                vh = new ViewHolder();
                vh.libName = (TextView) v.findViewById(R.id.lib_ack_item_text_name);
                vh.libContent = (TextView) v.findViewById(R.id.lib_ack_item_text_details);
                v.setTag(vh);
            }
            else {
                v = convertView;
                vh = (ViewHolder) v.getTag();
            }

            vh.libName.setText(libNames[position]);
            vh.libContent.setText(libContents[position]);

            return v;
        }

        private class ViewHolder {
            public TextView libName, libContent;
        }
    }
}
