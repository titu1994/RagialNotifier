package com.somshubra.ragialnotifier;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.somshubra.ragialnotifier.async.RagialNotifierService;
import com.somshubra.ragialnotifier.database.RagialDatabase;
import com.somshubra.ragialnotifier.database.RagialProvider;
import com.somshubra.ragialnotifier.database.RagialQueryHelper;
import com.somshubra.ragialnotifier.fragment.NavigationDrawerFragment;
import com.somshubra.ragialnotifier.fragment.RagialListFragment;
import com.somshubra.ragialnotifier.fragment.RagialListFragment.RagialListFragmentListener;
import com.somshubra.ragialnotifier.fragment.RagialQueryDialog;
import com.somshubra.ragialnotifier.fragment.RagialVenderListFragment;
import com.somshubra.ragialnotifier.intro.IntroActivity;
import com.somshubra.ragialnotifier.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, RagialListFragmentListener, ActionMode.Callback {

    private static final String TAG = "MAINACTIVITY";

    private RagialQueryHelper ragialHelper;
    /**
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in
     * {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private RagialListFragment f;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean introShown = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(IntroActivity.INTRO_FLAG, false);
        if(!introShown) {
            Intent x = new Intent(this, IntroActivity.class);
            startActivity(x);
        }

        f = new RagialListFragment();
        f.setToolbar(toolbar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.container, f).commit();

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setMainActivity(this);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,	(DrawerLayout) findViewById(R.id.drawer_layout));

        ragialHelper = new RagialQueryHelper(this);
        ragialHelper.updateAllQuery();

        ServiceRestart service = new ServiceRestart();
        service.execute(this);

        if(!isNetworkAvailable()) {
            Snackbar.make(toolbar, "Check network connection", Snackbar.LENGTH_LONG).show();
        }
    }

    public static void handleOrientation(Activity a) {
        if(a == null)
            return;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(a);
        int selected = Integer.parseInt(sp.getString(SettingsActivity.RagialPreferences.KEY_ORIENTATION, "0"));
        switch(selected) {
            case 0: {
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                break;
            }
            case 1: {
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                break;
            }
            case 2: {
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            }
            default: {
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        if(position == 0) {
            //fragmentManager.beginTransaction().replace(R.id.container, new RagialListFragment()).commit();
        }
        else if(position == 1) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_ragial) {
            RagialQueryDialog dialog = new RagialQueryDialog();
            dialog.setRagialHelper(ragialHelper);
            dialog.show(getSupportFragmentManager(), "QUERY");

            return true;
        }
        else if(id == R.id.action_restart) {
            if(RagialQueryHelper.isExecutorAvailable()) {
                ragialHelper.cancelAllRunningThreads();
                ragialHelper.updateAllQuery();
            }
            else {
                ragialHelper.restartExecutor();
                ragialHelper.updateAllQuery();
            }
            return true;
        }
        else if(id == R.id.action_web) {
            String url = "http://ragial.com/iRO-Renewal";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(ragialHelper != null)
            ragialHelper.updateAllQuery();

        handleOrientation(this);
    }

    @Override
    protected void onPause() {

        //Log.d("RagialMainActivity", "All threads paused = " + ragialHelper.destroyExecutor());
        super.onPause();
    }

    @Override
    protected void onStop() {
        ragialHelper.destroyExecutor();
        //Log.d("RagialMainActivity", "All threads stopped");
        super.onStop();
    }

    private ActionMode actionMode;

    @Override
    public void onListItemClicked(String name, boolean longClicked) {
        if(longClicked) {
            if(name != null)
                nameList.add(name);
            else if(actionMode != null)
                actionMode.finish();

        }
        else {
            ragialHelper.cancelAllRunningThreads();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(RagialVenderListFragment.RAGIAL_NAME_KEY, name).commit();

            RagialVenderListFragment vender = new RagialVenderListFragment();

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).addToBackStack(TAG+"QUERY").replace(R.id.container,vender).commit();
        }

    }

    private HashSet<String> nameList;

    @Override
    public void onListItemLongClicked(String name) {
        if(name == null)
            if(actionMode != null) {
                actionMode.finish();
                return;
            }
        startSupportActionMode(this);

        if(nameList == null || nameList.size() != 0)
            nameList = new HashSet<>();

        if(name != null)
            nameList.add(name);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        this.actionMode = actionMode;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_notification, menu);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_notify: {
                handleActionClicked(mode);
                return true;
            }
            case R.id.menu_delete: {
                handleActionDelete(mode);
                return true;
            }
        }
        return false;
    }

    private void handleActionDelete(ActionMode mode) {
        ArrayList<ContentProviderOperation> opers = new ArrayList<>();
        ContentProviderOperation oper;

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
            getContentResolver().applyBatch(RagialProvider.AUTHORITY, opers);
        }
        catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

        f.setIsLongClickCanceled();
        nameList.clear();
        mode.finish();
    }

    private void handleActionClicked(ActionMode mode) {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, RagialNotifierService.class);
        i.setAction(RagialNotifierService.RAGIAL_ACTION_NOTIFY);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);

        try {
            manager.cancel(pi);
            pi.cancel();
        } catch(Exception e) {
            Log.d(TAG, "Prior pending intents cancel failed or didn't exist.");
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        LinkedList<String> wrapper = new LinkedList<>(nameList);
        RagialQueryHelper.saveArray(wrapper);
        //Log.d("NameList", nameList.toString());

        long limit = Integer.parseInt(sp.getString(SettingsActivity.RagialPreferences.KEY_PREFS_TIME, "60")) * 60 * 1000;
        long next = SystemClock.elapsedRealtime() + limit;

        pi = PendingIntent.getService(this, 0, i, 0);
        //Log.d("RagialNotifier", "Added " + nameList.toString() + " items to be notified.");
        manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, next, limit, pi);

        f.setIsLongClickCanceled();
        nameList.clear();
        mode.finish();
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        nameList.clear();
        nameList = null;
        f.setIsLongClickCanceled();
    }

    private class ServiceRestart extends AsyncTask<Context, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Context... params) {
            Context context = params[0];
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, RagialNotifierService.class);
            i.setAction(RagialNotifierService.RAGIAL_ACTION_NOTIFY);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            if(!isMyServiceRunning(context, i)) {
                boolean notifsAllowed = sp.getBoolean(SettingsActivity.RagialPreferences.KEY_PREFS_NOTIFS_ALLOWED, false);

                if(notifsAllowed) {
                    PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
                    try {
                        manager.cancel(pi);
                        pi.cancel();
                    } catch (Exception e) {
                        //Log.d(TAG, "Prior pending intents cancel failed or didn't exist.");
                    }

                    LinkedList<String> wrapper = RagialQueryHelper.loadArray(context);
                    long limit = Integer.parseInt(sp.getString(SettingsActivity.RagialPreferences.KEY_PREFS_TIME, "60")) * 60 * 1000;
                    long next = SystemClock.elapsedRealtime() + limit;

                    pi = PendingIntent.getService(context, 0, i, 0);
                    manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, next, limit, pi);
                    //Log.d("RagialNotifier", "Restarted RagialNotifierService");
                }
            }

            return null;
        }

        private boolean isMyServiceRunning(Context context, Intent i) {
            boolean alarmUp = (PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE) != null);
            return alarmUp;
        }

    }
}
