package com.zaytoona.youtube.safe.businessobjects;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;

/**
 * A BroadcastReceiver that will receive a Broadcast when the device boots up. It will check if the user has set the app to
 * automatically check for new videos from subscribed channels, and set the repeating alarm if they have.
 */
public class FeedUpdaterSetupReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		int feedUpdaterInterval = Integer.parseInt(SafetoonsApp.getPreferenceManager().getString(SafetoonsApp.getStr(R.string.pref_key_feed_notification), "0"));

		Intent i = new Intent(context, FeedUpdaterReceiver.class);
		PendingIntent intentExecuted = PendingIntent.getBroadcast(context, 0, i,
						PendingIntent.FLAG_CANCEL_CURRENT);
		if(feedUpdaterInterval > 0) {
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
							SystemClock.elapsedRealtime()+feedUpdaterInterval, feedUpdaterInterval, intentExecuted);
		}
	}
}
