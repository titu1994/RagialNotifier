package com.somshubra.ragialnotifier.async;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import com.ragialquery.data.RagialData;
import com.ragialquery.data.RagialData.VendingNow;
import com.ragialquery.data.RagialQueryMatcher;
import com.somshubra.ragialnotifier.MainActivity;
import com.somshubra.ragialnotifier.R;
import com.somshubra.ragialnotifier.database.RagialQueryHelper;
import com.somshubra.ragialnotifier.settings.SettingsActivity.RagialPreferences;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class RagialNotifierService extends IntentService {
	public static final String RAGIAL_ACTION_NOTIFY = "com.somshubra.ragialnotifier.ACTION_NOTIFY_RAGIAL";
	private static final String TAG = "RagialNotififService";
	private RagialQueryMatcher matcher;
	private RagialQueryHelper helper;
	private RagialData[] datas = null;

	private ArrayList<String> notificationNames;
	private static final int mID = 61514;

	public RagialNotifierService() {
		super("Ragial Notification Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean onlyWhenOnWiFi = prefs.getBoolean(RagialPreferences.KEY_WIFI_ONLY, false); 
		
		if(onlyWhenOnWiFi) {
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (mWifi.isConnected()) {
			    handleIntent(intent);
			}
		}
		else {
			handleIntent(intent);
		}
		
	}
	
	private void handleIntent(Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final LinkedList<String> list = RagialQueryHelper.loadArray();

		if(list.size() > 0) {
			matcher = RagialQueryMatcher.getMatcher();
			helper = new RagialQueryHelper(this);
			helper.setCallbacks(new RagialQueryHelper.Callbacks() {

				@Override
				public void finishedGettingData(RagialData[] data) {
					datas = data;
					handleDatas(list);
				}
			});
			helper.getAllRagial(false);

			boolean shouldContinueNotification = prefs.getBoolean(RagialPreferences.KEY_PREFS_NOTIFS_ALLOWED, false);

			if(!shouldContinueNotification) {
				AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				Intent i = new Intent(this, RagialNotifierService.class);
				i.setAction(RAGIAL_ACTION_NOTIFY);
				PendingIntent pi = PendingIntent.getService(this, 0, i, 0);

				try {
					manager.cancel(pi);
					pi.cancel();
				} catch(Exception e) {
					//Log.d(TAG, "Prior pending intents cancel failed or didn't exist.");
				}
			}
		}
	}

	protected void handleDatas(LinkedList<String> list) {
		notificationNames = new ArrayList<>();

		for(String name : list) {
			if(RagialQueryMatcher.isExecutorAvailable()) {
				handleEveryName(name, notificationNames);
			}
			else {
				handleEveryName(name, notificationNames);
			}
		}

		notifyUser(notificationNames);
	}

	private void handleEveryName(String name, ArrayList<String> notificationNames) {
		try {
			Vector<VendingNow> vends = matcher.getOnSaleItems(name, datas).get();
			if(vends != null && vends.size() > 0) {
				notificationNames.add(name);
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	private void notifyUser(ArrayList<String> notificationNames) {
		if(notificationNames.size() == 0)
			return;

		//Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.zeny_background))
				.setContentTitle("Ragial Notifier")
				.setContentText("Items are on sale!")
				.setVibrate(new long[]{1000, 1000, 1000})
				.setLights(0xFFFFFFFF, 3000, 3000)
				.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
				.setAutoCancel(true);

		Intent resultIntent = new Intent(this, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
				stackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_UPDATE_CURRENT
				);
		mBuilder.setContentIntent(resultPendingIntent);

		NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
		for(String name : notificationNames) {
			style.addLine(name + " is on sale.");
		}

		mBuilder.setStyle(style);
		NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);

		mNotificationManager.notify(mID, mBuilder.build());
		notificationNames.clear();
	}
}
