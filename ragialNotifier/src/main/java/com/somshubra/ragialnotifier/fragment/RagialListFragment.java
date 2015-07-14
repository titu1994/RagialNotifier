package com.somshubra.ragialnotifier.fragment;


import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.somshubra.ragialnotifier.R;
import com.somshubra.ragialnotifier.async.RagialCursorAdapterV2;
import com.somshubra.ragialnotifier.async.RagialDataLoader;
import com.somshubra.ragialnotifier.database.RagialDatabase;
import com.somshubra.ragialnotifier.database.RagialProvider;
import com.somshubra.ragialnotifier.database.RagialQueryHelper;
import com.somshubra.ragialnotifier.utils.SwipeableRecyclerViewTouchListener;

import java.util.ArrayList;

public class RagialListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "RagialListFragment";

    private ListView lv;
    private RecyclerView rv;
    private RecyclerView.LayoutManager rvlm;

    private RagialCursorAdapterV2 adapter2;

    public interface RagialListFragmentListener {
        void onListItemClicked(String name, boolean longClicked);
        void onListItemLongClicked(String name);
    }

    private RagialQueryHelper helper;
    private RagialListFragmentListener listener;
    public static final int DATA_LOAD = 1;

    private Toolbar toolbar;
    private boolean isLongClicked = false;
    private ArrayList<String> cvList;

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

        adapter2 = new RagialCursorAdapterV2(getActivity(), null);
        rv.setAdapter(adapter2);

        adapter2.setOnItemClickedListener(new RagialCursorAdapterV2.OnItemClickedListener() {
            @Override
            public void onItemClicked(View view, int position) {
                if (listener != null) {
                    if (isLongClicked) {
                        RagialCursorAdapterV2.RagialViewHolder holder = (RagialCursorAdapterV2.RagialViewHolder) rv.findViewHolderForAdapterPosition(position);
                        String name = holder.ragialName.getText().toString();

                        if (!cvList.contains(name)) {
                            cvList.add(name);

                            adapter2.toggleSelection(position);

                            listener.onListItemClicked(name, true);
                        } else {
                            cvList.remove(name);
                            adapter2.toggleSelection(position);


                            if (cvList.isEmpty()) {
                                adapter2.clearSelections();
                                listener.onListItemClicked(null, true);
                            }
                        }
                    } else {
                        RagialCursorAdapterV2.RagialViewHolder holder = (RagialCursorAdapterV2.RagialViewHolder) rv.findViewHolderForAdapterPosition(position);
                        String name = holder.ragialName.getText().toString();

                        listener.onListItemClicked(name, false);
                    }
                }
            }
        });

        adapter2.setOnItemLongClickedListener(new RagialCursorAdapterV2.OnItemLongClickedListener() {
            @Override
            public boolean onItemLongClicked(View view, int position) {
                isLongClicked = true;
                if (cvList == null)
                    cvList = new ArrayList<>();

                RagialCursorAdapterV2.RagialViewHolder holder = (RagialCursorAdapterV2.RagialViewHolder) rv.findViewHolderForAdapterPosition(position);
                String name = holder.ragialName.getText().toString();

                if (!cvList.contains(name)) {
                    cvList.add(name);
                    adapter2.toggleSelection(position);

                    if (listener != null)
                        listener.onListItemLongClicked(name);
                } else {
                    cvList.remove(name);
                    adapter2.toggleSelection(position);

                    if (listener != null) {
                        if (cvList.isEmpty()) {
                            adapter2.clearSelections();
                            listener.onListItemLongClicked(null);
                        }
                    }
                }
                return true;
            }
        });
        getActivity().getSupportLoaderManager().initLoader(DATA_LOAD, null, this).startLoading();

        SwipeableRecyclerViewTouchListener swiper = new SwipeableRecyclerViewTouchListener(rv, new SwipeableRecyclerViewTouchListener.SwipeListener() {
            private ArrayList<String> nameList = new ArrayList<>();

            @Override
            public boolean canSwipe(int position) {
                return true;
            }

            @Override
            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                handleDelete(reverseSortedPositions);
            }

            @Override
            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                handleDelete(reverseSortedPositions);
            }

            private void handleDelete(int pos[]) {
                ArrayList<ContentProviderOperation> opers = new ArrayList<>();
                ContentProviderOperation oper;
                if(nameList == null ) {
                    nameList = new ArrayList<>();
                }
                else if(nameList.size() > 0) {
                    nameList.clear();
                }

                for(int position : pos) {
                    RagialCursorAdapterV2.RagialViewHolder holder = (RagialCursorAdapterV2.RagialViewHolder) rv.findViewHolderForAdapterPosition(position);
                    String name = holder.ragialName.getText().toString();
                    nameList.add(name);
                }

                for(String name : nameList) {
                    oper = ContentProviderOperation.newDelete(RagialProvider.CONTENT_URI_VENDERS)
                            .withSelection(RagialDatabase.COL_RAGIAL_NAME + " = ?", new String[]{name})
                            .build();
                    opers.add(oper);

                    oper = ContentProviderOperation.newDelete(RagialProvider.CONTENT_URI)
                            .withSelection(RagialDatabase.COL_RAGIAL_NAME + " = ?", new String[]{name})
                            .build();
                    opers.add(oper);
                }

                try {
                    getActivity().getContentResolver().applyBatch(RagialProvider.AUTHORITY, opers);
                }
                catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                catch (RemoteException e) {
                    e.printStackTrace();
                }

                Snackbar.make(toolbar, "Removed " + nameList, Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RagialQueryHelper helper = new RagialQueryHelper(getActivity());
                        String query = nameList.get(0).trim();

                        if (!TextUtils.isEmpty(query)) {
                            if (query.equals("Fallen Angel Wing [1]")) {
                                query = "Fallen Angel Wing";
                            }

                            if (RagialQueryHelper.isExecutorAvailable())
                                helper.queryRagial(query);
                            else {
                                helper.restartExecutor();
                                helper.queryRagial(query);
                            }
                        }
                    }
                }).show();
            }
        });

        rv.addOnItemTouchListener(swiper);

        return v;
    }

    public void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHorizontal(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }


    public void setIsLongClickCanceled() {
        this.isLongClicked = false;
        adapter2.clearSelections();
        cvList.clear();
    }

    private ContentObserver observer;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setUpContentObserver();

        try {
            listener = (RagialListFragmentListener) activity;
        } catch (ClassCastException e) {
            listener = null;
        }

    }

    private void setUpContentObserver() {
        if(observer == null)
            observer = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {
                    getActivity().getSupportLoaderManager().restartLoader(DATA_LOAD, null, RagialListFragment.this).startLoading();
                }
            };

        getActivity().getContentResolver().registerContentObserver(RagialProvider.CONTENT_URI, true, observer);
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
            return new RagialDataLoader(getActivity());
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null || data.getCount() == 0) {
            //Log.d(TAG, "DATA_LOAD returned null cursor");

        }

        if(adapter2 != null) {
            adapter2.changeCursor(data);
        }
        else {
            //Log.d(TAG, "Adapter is null");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(adapter2 != null) {
            adapter2.changeCursor(null);
            adapter2.notifyDataSetChanged();
        }
    }

}
