package com.zaytoona.youtube.safe.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.services.SafetoonsFirebaseMessagingService;

import java.util.Calendar;

public class General {

    public static String getDeviceId(Context context) {

        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static void startService(Context context) {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 10);

        Intent intent = new Intent(context, SafetoonsFirebaseMessagingService.class);

        PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //for 1 mint 1*60*1000
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                1*60*1000, pintent);
    }
}
