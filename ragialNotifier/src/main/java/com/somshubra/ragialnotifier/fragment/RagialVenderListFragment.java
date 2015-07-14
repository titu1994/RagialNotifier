package com.somshubra.ragialnotifier.fragment;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ragialquery.data.RagialData;
import com.ragialquery.data.RagialQueryMatcher;
import com.somshubra.ragialnotifier.R;
import com.somshubra.ragialnotifier.async.RagialVenderCursorAdapter;
import com.somshubra.ragialnotifier.database.RagialDatabase;
import com.somshubra.ragialnotifier.database.RagialProvider;
import com.somshubra.ragialnotifier.database.RagialQueryHelper;

public class RagialVenderListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> , RagialQueryHelper.DataListener{
    private static final String TAG = "RagialVenderListFrag";
    private RecyclerView rv;
    private RecyclerView.LayoutManager rvlm;

    private RagialVenderCursorAdapter adapter;

    private String ragialName;
    public static final String RAGIAL_NAME_KEY = "ragial_name_key";

    public static final int DATA_LOAD = 5;

    private RagialQueryHelper helper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_recycle, container, false);
        rv = (RecyclerView) v.findViewById(R.id.list_recycler_view);

        if(isTablet(getActivity()) && isHorizontal(getActivity())) {
            rvlm = new GridLayoutManager(getActivity(), 2);
        }
        else {
            rvlm = new LinearLayoutManager(getActivity());
        }

        rv.setLayoutManager(rvlm);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        ragialName = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(RAGIAL_NAME_KEY, "");

        adapter = new RagialVenderCursorAdapter(getActivity(), null);
        rv.setAdapter(adapter);

        helper.setListener(this);
        helper.updateQuery(ragialName);

        return v;
    }

    @Override
    public void finishedLoading(RagialData[] datas) {
        if(datas != null) {
            RagialData d = RagialQueryMatcher.searchRagialSpecificly(ragialName, datas);
            if (d != null) {
                if (d.vendingList == null || d.vendingList.size() == 0) {
                    if (rv != null)
                        Snackbar.make(rv, "Items is not being vended at the moment", Snackbar.LENGTH_LONG).show();
                    else {
                        Toast.makeText(getActivity(), "Items is not being vended at the moment", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().getLoaderManager().initLoader(DATA_LOAD, null, this).startLoading();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(!isNetworkAvailable()) {
            Snackbar.make(rv, "Check network connection", Snackbar.LENGTH_LONG).show();
        }
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHorizontal(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private ContentObserver observer;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setUpContentObserver();

        if(helper == null)
            helper = new RagialQueryHelper(getActivity());
        if(!RagialQueryHelper.isExecutorAvailable())
            helper.restartExecutor();
    }

    private void setUpContentObserver() {
        if(observer == null)
            observer = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    getActivity().getLoaderManager().restartLoader(DATA_LOAD, null, RagialVenderListFragment.this).startLoading();
                }
            };

        getActivity().getContentResolver().registerContentObserver(RagialProvider.CONTENT_URI_VENDERS, true, observer);
    }

    @Override
    public void onDetach() {
        getActivity().getContentResolver().unregisterContentObserver(observer);
        observer = null;
        super.onDetach();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == DATA_LOAD) {
            String projection = RagialDatabase.COL_RAGIAL_NAME + " = ?";
            String projectionArgs[] = new String[] {ragialName};

            return new CursorLoader(getActivity(), RagialProvider.CONTENT_URI_VENDERS, null, projection, projectionArgs, RagialDatabase.COL_VENDER_IS_BUYING_TYPE + " asc, " + RagialDatabase.COL_VENDER_PRICE + " asc");
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null || data.getCount() == 0) {
            //Log.d(TAG, "Cursor is empty");
            //Toast.makeText(getActivity(), "No one is vending this item yet", Toast.LENGTH_LONG).show();
        }
        if(adapter != null) {
            adapter.changeCursor(data);
        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
