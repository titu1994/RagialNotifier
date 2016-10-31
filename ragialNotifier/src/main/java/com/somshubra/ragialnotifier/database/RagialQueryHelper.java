package com.somshubra.ragialnotifier.database;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ragialquery.data.RagialData;
import com.ragialquery.data.RagialQueryMatcher;
import com.somshubra.ragialnotifier.MainActivity;
import com.somshubra.ragialnotifier.async.RagialDataInsert;
import com.somshubra.ragialnotifier.async.RagialDataUpdate;
import com.somshubra.ragialnotifier.fragment.RagialErrorDialog;
import com.somshubra.ragialnotifier.settings.SettingsActivity;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RagialQueryHelper {
    private static final String RAGIAL_GSON_REQUEST = "Ragial Gson Request";
    private static final String TAG = "RagialQueryHelper";
    private static Context context;

    private QueryThread queryThread;
    private ReQueryThread requeryThread;
    private QueryVenderThread venderThread;
    private GetAllRagialTask getAllRagial;

    private static ExecutorService executer = Executors.newCachedThreadPool();

    private static Callbacks callbacks;

    public RagialQueryHelper(Context context) {
        RagialQueryHelper.context = context;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        RagialQueryMatcher.setSearchURL(Integer.parseInt(sp.getString(SettingsActivity.RagialPreferences.KEY_SERVER_CHOICE, "1")));
    }

    public static void setRagialSearchServer(int code) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        RagialQueryMatcher.setSearchURL(code);
    }

    public void queryRagial(String query) {
        if(isExecutorAvailable()) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            boolean strict = sp.getBoolean("StrictName", true);

            queryThread = new QueryThread(strict);
            queryThread.executeOnExecutor(executer, query);
        }
    }

    @SuppressWarnings("WrongThread")
    private static class QueryThread extends AsyncTask<String, Void, RagialData[]> {

        private ProgressDialog pd;

        boolean strict;

        public QueryThread(boolean strict) {
            this.strict = strict;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(context, "Searching Ragial", "Please wait...", true);
        }

        @Override
        protected void onPostExecute(RagialData[] result) {
            super.onPostExecute(result);
            if(pd != null) {
                pd.dismiss();
            }

            if(result == null)
                Toast.makeText(context, "Timed out. Check internet connection", Toast.LENGTH_LONG).show();

            if(callbacks != null)
                callbacks.finishedGettingData(result);
        }

        @Override
        protected RagialData[] doInBackground(String... query) {
            System.out.println("Entered QueryThread");
            RagialQueryMatcher matcher = RagialQueryMatcher.getMatcher();
            RagialData[] datas = null;

            boolean isDisconected = false;

            if(!isCancelled()) {
                try {
                    datas = matcher.searchRagial(query[0], strict).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    isDisconected = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

                if(!isDisconected) {
                    if(datas != null && !isCancelled() ) {
                        RagialDataInsert inserter = new RagialDataInsert(context, query[0]);
                        inserter.execute(datas);
                    }
                }
                else {
                    return null;
                }
            }
            return datas;
        }

    }

    public void updateQuery(String name) {
        if(isExecutorAvailable()) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            boolean strict = sp.getBoolean("StrictName", true);

            venderThread = new QueryVenderThread();
            venderThread.strict = strict;
            venderThread.executeOnExecutor(executer, name);
        }
    }

    @SuppressWarnings("WrongThread")
    private class QueryVenderThread extends AsyncTask<String, Void, RagialData[]> {
        boolean strict;

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(context, "Searching Venders", "Please wait...", true);
        }

        @Override
        protected void onPostExecute(RagialData[] result) {
            super.onPostExecute(result);
            if(pd != null) {
                pd.dismiss();
            }
            if(callbacks != null)
                callbacks.finishedGettingData(result);

            if(listener != null)
                listener.finishedLoading(result);
        }

        @Override
        protected RagialData[] doInBackground(String... query) {
            System.out.println("Entered QueryVenderThread");
            RagialQueryMatcher matcher;
            matcher = RagialQueryMatcher.getMatcher();
            RagialData[] datas = null;

            boolean isDisconnected = false;

            if(!isCancelled()) {
                //.i(TAG, "Trying to delete old data of VENDER");
                context.getContentResolver().delete(RagialProvider.CONTENT_URI_VENDERS, RagialDatabase.COL_RAGIAL_NAME + " =?", new String[]{query[0]});
                //Log.i(TAG, "Deleted old data of VENDER");

                try {
                    if(RagialQueryMatcher.isExecutorAvailable())
                        datas = matcher.searchRagial(query[0], strict).get();
                    isDisconnected = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isDisconnected = true;
                    return null;
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    isDisconnected = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    isDisconnected = true;
                    return null;
                }

                //Log.d(TAG, "Data size : " + datas.length);

                Cursor c = null;
                int id = 0;
                Uri uri;
                if(!isDisconnected) {
                    if(datas != null && !isCancelled()) {
                        for(RagialData data : datas) {
                            if(data.name.equalsIgnoreCase(query[0])) {
                                //Log.i(TAG, "Querying data : " + data.name);
                                if(!isCancelled()) {
                                    String projection[] = new String[] {RagialDatabase._ID, RagialDatabase.COL_RAGIAL_NAME};
                                    String selection = RagialDatabase.COL_RAGIAL_NAME + "=?";
                                    String selectonArgs[] = new String[] {data.name};

                                    //Log.i(TAG, "Trying to Query Base URI");
                                    c = context.getContentResolver().query(RagialProvider.CONTENT_URI, projection, selection, selectonArgs, null);
                                    if(c.moveToFirst()) {
                                        //Log.i(TAG, "Succesful in Querying Base URI");
                                        id = c.getInt(c.getColumnIndex(RagialDatabase._ID));
                                        uri = ContentUris.withAppendedId(RagialProvider.CONTENT_URI, id);

                                        c.close();
                                        //Log.i(TAG, "Cursor for data : " + data.name + " closed.");
                                        RagialDataUpdate inserter = new RagialDataUpdate(context, uri, true);
                                        if(isExecutorAvailable())
                                            inserter.execute(datas);
                                    }
                                }
                            }

                        }

                    }
                }
                else {
                    return null;
                }

                if(c != null && !c.isClosed())
                    c.close();
            }
            return datas;
        }

    }

    public void updateAllQuery() {
        if(isExecutorAvailable()) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            boolean strict = sp.getBoolean("StrictName", true);

            requeryThread = new ReQueryThread();
            requeryThread.strict = strict;
            requeryThread.executeOnExecutor(executer);

        }
    }

    @SuppressWarnings("WrongThread")
    private static class ReQueryThread extends AsyncTask<Void, Void, Void> {
        boolean strict;

        private String projection[] = new String[] { RagialDatabase._ID , RagialDatabase.COL_RAGIAL_NAME};
        private boolean nameNotFound = false;
        private String nameWhichWasNotFound = "";

        @Override
        protected Void doInBackground(Void... query) {
            //Log.d(TAG, "Entered Requery Thread");
            Cursor ragialData = context.getContentResolver().query(RagialProvider.CONTENT_URI, projection, null, null, null);
            int id;
            String name;
            Uri uri;
            RagialData[] datas = null;

            if(ragialData.moveToFirst()) {
                if(!isCancelled()) {
                    RagialQueryMatcher matcher = RagialQueryMatcher.getMatcher();
                    do {
                        if(!isCancelled()) {
                            if(!ragialData.isClosed()) {
                                id = ragialData.getInt(ragialData.getColumnIndex(RagialDatabase._ID));
                                name = ragialData.getString(ragialData.getColumnIndex(RagialDatabase.COL_RAGIAL_NAME));

                                boolean isDisconnected = false;

                                try {
                                    //Log.d(TAG, "Item name : " + name);
                                    if(RagialQueryMatcher.isExecutorAvailable())
                                        datas = matcher.searchRagial(name, strict).get();
                                    isDisconnected = false;

                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                    isDisconnected = true;
                                    if(!ragialData.isClosed())
                                        ragialData.close();
                                } catch(Exception e) {
                                    e.printStackTrace();
                                    if(!ragialData.isClosed())
                                        ragialData.close();
                                    return null;
                                }

                                if(!isDisconnected) {
                                    if(datas != null) {
                                        //Log.d(TAG, "Name : " + name + " has data");
                                        for(RagialData data : datas) {
                                            if(!isCancelled()) {
                                                if(data.name.equalsIgnoreCase(name)) {
                                                    //Log.d(TAG, "Updating the data");
                                                    uri = Uri.withAppendedPath(RagialProvider.CONTENT_URI, "" + (id));

                                                    RagialDataUpdate updater = new RagialDataUpdate(context, uri, false);
                                                    if(isExecutorAvailable())
                                                        updater.execute(data);

                                                    break;
                                                }
                                            }
                                            else {
                                                return null;
                                            }
                                        }
                                    }
                                }
                                else {
                                    nameNotFound = true;
                                    nameWhichWasNotFound = name;
                                }
                            }
                        }
                    } while(!ragialData.isClosed() && ragialData.moveToNext());
                    if(!ragialData.isClosed())
                        ragialData.close();
                }
            }
            if(!ragialData.isClosed())
                ragialData.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(nameNotFound) {
                RagialErrorDialog er = new RagialErrorDialog();
                er.setErrorMessage("Item " + nameWhichWasNotFound + " was not found. Please delete it and add the item again. If you have recently switched ragial.com servers, then the item may not exist on the server");
                if (context instanceof MainActivity)
                    try {
                        er.show(((MainActivity) context).getSupportFragmentManager(), "ERROR_ITEM_NOT_FOUND");
                    } catch (IllegalStateException e) {

                    }

            }
        }
    }

    public Callbacks getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(Callbacks callbacks) {
        RagialQueryHelper.callbacks = callbacks;
    }

    /**
     * Implement the callback to receive the data.
     * @param getImmediately
     */
    public void getAllRagial(boolean getImmediately) {
        if(isExecutorAvailable()) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            boolean strict = sp.getBoolean("StrictName", true);

            getAllRagial = new GetAllRagialTask();
            getAllRagial.strict = strict;

            if(getImmediately) {
                try {
                    getAllRagial.executeOnExecutor(executer).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            else {
                getAllRagial.executeOnExecutor(executer);
            }
        }
    }

    private static class GetAllRagialTask extends AsyncTask<Void, Void, RagialData[]> {
        boolean strict;

        private boolean isDisconected;

        @Override
        protected void onPostExecute(RagialData[] result) {
            super.onPostExecute(result);

            if(isDisconected)
                Toast.makeText(context, "Timed out. Check internet connection", Toast.LENGTH_LONG).show();

            if(callbacks != null)
                callbacks.finishedGettingData(result);
        }

        @Override
        protected RagialData[] doInBackground(Void... params) {
            //System.out.println("Entered GetAllRagialThread");
            RagialQueryMatcher matcher = RagialQueryMatcher.getMatcher();
            RagialData[] datas = null;
            boolean isDisconected = false;
            LinkedList<RagialData> list = new LinkedList<>();

            if(!isCancelled()) {
                ContentResolver resolver = context.getContentResolver();
                String projection[] = new String[] {RagialDatabase.COL_RAGIAL_NAME };
                Cursor cursor = resolver.query(RagialProvider.CONTENT_URI, projection, null, null, null);
                cursor.moveToFirst();
                String name = null;

                do {
                    if(!isCancelled()) {
                        @SuppressWarnings("unused")
                        boolean isDisconnected = false;
                        if(!cursor.isClosed() && cursor.getCount() != 0) {
                            name = cursor.getString(cursor.getColumnIndex(RagialDatabase.COL_RAGIAL_NAME));
                            //TODO: Find alternate hotfix for FAW [1]
                            if(name.equals("Fallen Angel Wing [1]"))
                                name = "Fallen Angel Wing";

                            try {
                                if(RagialQueryMatcher.isExecutorAvailable())
                                    datas = matcher.searchRagial(name, strict).get();
                                isDisconnected = false;

                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                                isDisconnected = true;
                                if(!cursor.isClosed())
                                    cursor.close();
                                return null;
                            } catch(Exception e) {
                                e.printStackTrace();
                                if(!cursor.isClosed())
                                    cursor.close();
                                return null;
                            }
                            if(datas != null)
                                for(RagialData data : datas) {
                                    if(!isCancelled())
                                        list.add(data);
                                    else {
                                        break;
                                    }
                                }

                            else {
                                isDisconected = true;
                            }
                        }
                    }
                    else {
                        break;
                    }
                } while(cursor.moveToNext());
                if(!cursor.isClosed())
                    cursor.close();
                datas = list.toArray(new RagialData[list.size()]);
            }
            return datas;
        }



    }


    public void cancelAllRunningThreads() {
        if(requeryThread != null)
            requeryThread.cancel(true);

        if(queryThread != null)
            queryThread.cancel(true);

        if(venderThread != null)
            venderThread.cancel(true);

        if(getAllRagial != null)
            getAllRagial.cancel(true);

        queryThread = null;
        requeryThread = null;
        venderThread = null;
        getAllRagial = null;
    }

    public boolean destroyExecutor() {
        if(executer != null && !executer.isShutdown()) {
            cancelAllRunningThreads();
            executer.shutdownNow();
            executer = null;
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean saveArray(LinkedList<String> array) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(array);
        editor.putString(RAGIAL_GSON_REQUEST, json);
        return editor.commit();
    }

    public static LinkedList<String> loadArray() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(RAGIAL_GSON_REQUEST, "");
        Gson gson = new Gson();

        LinkedList<String> list = null;
        Type collectionType = new TypeToken<LinkedList<String>>(){}.getType();
        list = gson.fromJson(json, collectionType);

        return list;
    }

    public static LinkedList<String> loadArray(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(RAGIAL_GSON_REQUEST, "");
        Gson gson = new Gson();

        LinkedList<String> list = null;
        Type collectionType = new TypeToken<LinkedList<String>>(){}.getType();
        list = gson.fromJson(json, collectionType);

        return list;
    }

    public boolean restartExecutor() {
        if(executer == null) {
            executer = Executors.newCachedThreadPool();
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean isExecutorAvailable() {
        return executer != null && !executer.isShutdown();
    }

    public interface Callbacks {
        void finishedGettingData(RagialData[] data);
    }

    private DataListener listener;

    public interface DataListener {
        void finishedLoading(RagialData[] datas);
    }

    public DataListener getListener() {
        return listener;
    }

    public void setListener(DataListener listener) {
        this.listener = listener;
    }
}
