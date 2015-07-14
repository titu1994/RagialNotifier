package com.somshubra.ragialnotifier.settings;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.somshubra.ragialnotifier.MainActivity;
import com.somshubra.ragialnotifier.R;
import com.somshubra.ragialnotifier.async.RagialNotifierService;
import com.somshubra.ragialnotifier.database.RagialQueryHelper;
import com.somshubra.ragialnotifier.intro.IntroActivity;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		getFragmentManager().beginTransaction().replace(R.id.settings_frame, new RagialPreferences()).commit();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MainActivity.handleOrientation(this);
	}

	public static class RagialPreferences extends PreferenceFragment implements OnSharedPreferenceChangeListener{

		public static final String KEY_PREFS_NOTIFS_ALLOWED = "NotificationKey";
		public static final String KEY_PREFS_TIME = "TimePeriodKey";
		public static final String KEY_WIFI_ONLY = "WifiOnly";
		public static final String KEY_DONATE = "KEY_DONATE";
		public static final String KEY_LIB_ACK = "KEY_LIB_ACK";
		public static final String KEY_SERVER_CHOICE = "SEARCH_TYPE_KEY";
		public static final String KEY_TUTORIAL = "KEY_TUTORIAL";
		public static final String KEY_ORIENTATION = "KEY_ORIENTATION";

		private static final String TAG = "RagialPreference";
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			this.setRetainInstance(true);
			addPreferencesFromResource(R.xml.standard_prefs);
			Preference pref = findPreference(KEY_DONATE);
			pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					String url = "http://ragial.com/iRO-Renewal";
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					Toast.makeText(getActivity(), "Please click the Donate button!", Toast.LENGTH_LONG).show();
					startActivity(i);
					return true;
				}
			});

			Preference pref2 = findPreference(KEY_LIB_ACK);
			pref2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent i = new Intent(getActivity(), LibraryAckActivity.class);
					getActivity().startActivity(i);
					return true;
				}
			});

			Preference pref3 = findPreference(KEY_TUTORIAL);
			pref3.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent x = new Intent(getActivity(), IntroActivity.class);
					startActivity(x);
					return true;
				}
			});
		}

		@Override
		public void onResume() {
		    super.onResume();
		    getPreferenceScreen().getSharedPreferences()
		            .registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
		    super.onPause();
		    getPreferenceScreen().getSharedPreferences()
		            .unregisterOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			
			if(key.equalsIgnoreCase(KEY_PREFS_NOTIFS_ALLOWED)) {
				if (!sharedPreferences.getBoolean(key, false)) {
					AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
					Intent i = new Intent(getActivity(), RagialNotifierService.class);
					i.setAction(RagialNotifierService.RAGIAL_ACTION_NOTIFY);
					PendingIntent pi = PendingIntent.getService(getActivity(), 0, i, 0);

					try {
						manager.cancel(pi);
						pi.cancel();
					} catch (Exception e) {
						Log.d(TAG, "Prior pending intents cancel failed or didn't exist.");
					}
				}
			}
            else if(key.equalsIgnoreCase(KEY_SERVER_CHOICE)) {
                RagialQueryHelper.setRagialSearchServer(Integer.parseInt(sharedPreferences.getString(KEY_SERVER_CHOICE, "1")));
            }
			else if(key.equals(KEY_ORIENTATION)) {
				MainActivity.handleOrientation(getActivity());
			}
			
		}
		
	}
}
