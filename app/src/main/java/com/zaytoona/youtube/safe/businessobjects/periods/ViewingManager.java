package com.zaytoona.youtube.safe.businessobjects.periods;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ViewingManager {

    private static final String TAG = ViewingManager.class.getSimpleName();

    private Context context = null;


    // Shared Preferences Keys
    private static final String VIEWING_DAY_START_TIME_SETTINGS_KEY = "viewing.day.start.time.settings.key";
    private static final String VIEWING_DAY_END_TIME_SETTINGS_KEY = "viewing.day.end.time.settings.key";
    private static final String MAX_DAILY_VIEWING_PERIOD_SETTINGS_KEY = "max.daily.viewing.period.settings.key";
    private static final String MAX_SESSION_VIEWING_PERIOD_SETTINGS_KEY = "max.session.viewing.period.settings.key";
    private static final String SESSION_RESETS_AFTER_PERIOD_SETTINGS_KEY = "session.resets.after.period.settings.key";

    private static final String REMAINING_DAILY_VIEWING_PERIOD_KEY = "remaining.daily.viewing.period.key";
    private static final String REMAINING_SESSION_VIEWING_PERIOD_KEY = "remaining.session.viewing.period.key";
    private static final String LAST_SESSION_RESET_TIME_KEY = "last.session.reset.time.key";
    private static final String LAST_VIEWING_INFO_UPDATE_DATE_KEY = "last.viewing.info.update.date.key";
    private static final String IS_SESSION_ENDED_KEY = "session.ended.key";

    private static final DateFormat sdf = new SimpleDateFormat("HH:mm");
    private static final DateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy");

    // Viewing Settings
    private boolean viewingPeriodsEnabled = true;
    private Date viewingDayStartTime = null;
    private Date viewingDayEndTime = null;
    private int maxDailyViewingPeriod = 0;  // In minutes
    private int maxSessionViewingPeriod = 0;  // In minutes
    private int sessionResetsAfterPeriod = 0;  // In minutes

    // Current Viewing info
    private float remainingDailyViewingPeriod = maxDailyViewingPeriod;  // In minutes
    private float remainingSessionViewingPeriod = maxSessionViewingPeriod;  // In minutes
    private Date lastSessionResetTime = null;  // In minutes
    private Date lastViewingInfoUpdateDate = null;  // In minutes
    private boolean isSessionEnded = false;

    // Constructor
//    public ViewingManager() {
//
//        loadViewingSettings();
//
//        //saveViewingSettings("07:00", "19:00", 5, 2, 2);
//    }

    // Constructor
    public ViewingManager(Context context) {

        this.context = context;

        loadViewingSettings();

        //saveViewingSettings("07:00", "19:00", 5, 2, 2);

    }

    public boolean isViewingAllowed(float timePlayed) {

        // If viewing periods are not enabled, always allow
        if(isViewingPeriodsEnabled() == false) {
            return true;
        }

        loadViewingInfo();

        // Get current date and time
        Date now = null;
        Date today = null;
        //Date maxTime = null;

        try {
            now = sdf.parse(sdf.format(new Date()));
            today = sdf2.parse(sdf2.format(new Date()));
            //maxTime = sdf.parse("23:59:59");
        }
        catch (ParseException ex){
            Log.e(TAG, ex.getMessage());
        }

        // Is it a new day? if so reset all settings.
        if(today.after(lastViewingInfoUpdateDate)) {

            remainingDailyViewingPeriod = maxDailyViewingPeriod;
            remainingSessionViewingPeriod = maxSessionViewingPeriod;

            // TODO: Not sure this shall be set now
            //lastSessionResetTime = maxTime;

            lastViewingInfoUpdateDate = today;

            isSessionEnded = false;

            updateViewingInfo();
        }

        // Before or After daily time limits
        if(now.before(viewingDayStartTime) || now.after(viewingDayEndTime)) {

            remainingDailyViewingPeriod = maxDailyViewingPeriod;
            remainingSessionViewingPeriod = maxSessionViewingPeriod;

            // TODO: Not sure this shall be set now
            //lastSessionResetTime = maxTime;

            lastViewingInfoUpdateDate = today;

            isSessionEnded = false;

            updateViewingInfo();

            String msg = context.getString(R.string.msg_invalid_viewing_time, sdf.format(viewingDayStartTime), sdf.format(viewingDayEndTime));

            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

            return false;
        }

        // Increment viewing times.
        remainingDailyViewingPeriod -= timePlayed;

        // If max daily reached
        if(remainingDailyViewingPeriod <= 0) {

            updateViewingInfo();

            String msg = context.getString(R.string.msg_exceeded_daily_viewing_time);

            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

            return false;
        }

        // If timePlayed is zero then we are trying to start
        // In this case we only check if sessionResetsAfterPeriod elapsed
        if(timePlayed == 0) {

            long diff = now.getTime() - lastSessionResetTime.getTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

            if(minutes >= sessionResetsAfterPeriod) {

                //lastSessionResetTime = maxTime;
                remainingSessionViewingPeriod = maxSessionViewingPeriod;
                isSessionEnded = false;

                updateViewingInfo();

                return true;
            }
            else if(isSessionEnded == true) {

                String msg = context.getString(R.string.msg_exceeded_session_viewing_time, sessionResetsAfterPeriod - minutes);

                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

                return false;
            }
            // Go out of a video and come back again before session expire
        }

        remainingSessionViewingPeriod -= timePlayed;

        lastSessionResetTime = now;

        // If max session reached
        if(remainingSessionViewingPeriod <= 0) {

            isSessionEnded = true;

            updateViewingInfo();

            String msg = context.getString(R.string.msg_exceeded_session_viewing_time, sessionResetsAfterPeriod);

            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

            return false;
        }
        else {
            isSessionEnded = false;
        }

        updateViewingInfo();

        return true;
    }

    private void updateViewingInfo() {

        // Store remaining daily viewing period.
        SafetoonsApp.getPreferenceManager().edit().putFloat(REMAINING_DAILY_VIEWING_PERIOD_KEY, remainingDailyViewingPeriod).apply();

        // Store remaining session viewing period.
        SafetoonsApp.getPreferenceManager().edit().putFloat(REMAINING_SESSION_VIEWING_PERIOD_KEY, remainingSessionViewingPeriod).apply();

        // TODO: Not sure this shall be set now
        // Store last session reset time
        SafetoonsApp.getPreferenceManager().edit().putString(LAST_SESSION_RESET_TIME_KEY, sdf.format(lastSessionResetTime)).apply();

        // Store today
        SafetoonsApp.getPreferenceManager().edit().putString(LAST_VIEWING_INFO_UPDATE_DATE_KEY, sdf2.format(lastViewingInfoUpdateDate)).apply();

        // store is session ended
        SafetoonsApp.getPreferenceManager().edit().putBoolean(IS_SESSION_ENDED_KEY, isSessionEnded).apply();
    }

    private void loadViewingInfo() {

        remainingDailyViewingPeriod = SafetoonsApp.getPreferenceManager().getFloat(REMAINING_DAILY_VIEWING_PERIOD_KEY, maxDailyViewingPeriod);

        remainingSessionViewingPeriod = SafetoonsApp.getPreferenceManager().getFloat(REMAINING_SESSION_VIEWING_PERIOD_KEY, maxSessionViewingPeriod);

        try {
            lastSessionResetTime = sdf.parse(SafetoonsApp.getPreferenceManager().getString(LAST_SESSION_RESET_TIME_KEY, "23:59:59"));

            lastViewingInfoUpdateDate = sdf2.parse(SafetoonsApp.getPreferenceManager().getString(LAST_VIEWING_INFO_UPDATE_DATE_KEY, sdf2.format(new Date())));
        }
        catch (ParseException ex){
            Log.e(TAG, ex.getMessage());
        }

        isSessionEnded = SafetoonsApp.getPreferenceManager().getBoolean(IS_SESSION_ENDED_KEY, false);

        Log.i(TAG, "remainingDailyViewingPeriod: " + remainingDailyViewingPeriod);
        Log.i(TAG, "remainingSessionViewingPeriod: " + remainingSessionViewingPeriod);
        Log.i(TAG, "lastSessionResetTime: " + sdf.format(lastSessionResetTime));
        Log.i(TAG, "lastViewingInfoUpdateDate: " + sdf2.format(lastViewingInfoUpdateDate));
        Log.i(TAG, "sessionEnded: " + isSessionEnded);
    }

    public void loadViewingSettings() {

        String dayStartTime = SafetoonsApp.getPreferenceManager().getString(VIEWING_DAY_START_TIME_SETTINGS_KEY, "07:00");
        String dayEndTime = SafetoonsApp.getPreferenceManager().getString(VIEWING_DAY_END_TIME_SETTINGS_KEY, "19:00");

        viewingPeriodsEnabled = SafetoonsApp.getPreferenceManager().getBoolean(context.getString(R.string.pref_key_enable_viewing_periods), true);

        maxDailyViewingPeriod = SafetoonsApp.getPreferenceManager().getInt(MAX_DAILY_VIEWING_PERIOD_SETTINGS_KEY, 90);
        maxSessionViewingPeriod = SafetoonsApp.getPreferenceManager().getInt(MAX_SESSION_VIEWING_PERIOD_SETTINGS_KEY, 30);
        sessionResetsAfterPeriod = SafetoonsApp.getPreferenceManager().getInt(SESSION_RESETS_AFTER_PERIOD_SETTINGS_KEY, 60);

        try {
            viewingDayStartTime = sdf.parse(dayStartTime);
            viewingDayEndTime = sdf.parse(dayEndTime);
        }
        catch (ParseException ex){
            Log.e(TAG, ex.getMessage());
        }

        Log.i(TAG, "viewingPeriodsEnabled: " + viewingPeriodsEnabled);
        Log.i(TAG, "viewingDayStartTime: " + sdf.format(viewingDayStartTime));
        Log.i(TAG, "viewingDayEndTime: " + sdf.format(viewingDayEndTime));
        Log.i(TAG, "maxDailyViewingPeriod: " + maxDailyViewingPeriod);
        Log.i(TAG, "maxSessionViewingPeriod: " + maxSessionViewingPeriod);
        Log.i(TAG, "sessionResetsAfterPeriod: " + sessionResetsAfterPeriod);

    }

    public void saveViewingSettings(String dayStartTime, String dayEndTime, int maxDailyViewingPeriod, int maxSessionViewingPeriod, int sessionResetsAfterPeriod) {

        // Store day start viewing time.
        SafetoonsApp.getPreferenceManager().edit().putString(VIEWING_DAY_START_TIME_SETTINGS_KEY, dayStartTime).apply();

        // Store day end viewing time.
        SafetoonsApp.getPreferenceManager().edit().putString(VIEWING_DAY_END_TIME_SETTINGS_KEY, dayEndTime).apply();

        // Store max daily viewing period.
        SafetoonsApp.getPreferenceManager().edit().putInt(MAX_DAILY_VIEWING_PERIOD_SETTINGS_KEY, maxDailyViewingPeriod).apply();

        // Store max session viewing period.
        SafetoonsApp.getPreferenceManager().edit().putInt(MAX_SESSION_VIEWING_PERIOD_SETTINGS_KEY, maxSessionViewingPeriod).apply();

        // Store session reset after period.
        SafetoonsApp.getPreferenceManager().edit().putInt(SESSION_RESETS_AFTER_PERIOD_SETTINGS_KEY, sessionResetsAfterPeriod).apply();


        try {
            viewingDayStartTime = sdf.parse(dayStartTime);
            viewingDayEndTime = sdf.parse(dayEndTime);
        }
        catch (ParseException ex){
            Log.e(TAG, ex.getMessage());
        }

        Log.i(TAG, "viewingDayStartTime: " + sdf.format(viewingDayStartTime));
        Log.i(TAG, "viewingDayEndTime: " + sdf.format(viewingDayEndTime));
        Log.i(TAG, "maxDailyViewingPeriod: " + maxDailyViewingPeriod);
        Log.i(TAG, "maxSessionViewingPeriod: " + maxSessionViewingPeriod);
        Log.i(TAG, "sessionResetsAfterPeriod: " + sessionResetsAfterPeriod);

        // Clear Viewing Information
        loadViewingSettings();

        remainingDailyViewingPeriod = maxDailyViewingPeriod;
        remainingSessionViewingPeriod = maxSessionViewingPeriod;

        try {
            lastSessionResetTime = sdf.parse("23:59:59");
            lastViewingInfoUpdateDate = sdf2.parse(sdf2.format(new Date()));
        }
        catch (ParseException ex){
            Log.e(TAG, ex.getMessage());
        }

        isSessionEnded = false;

        updateViewingInfo();
    }

    /**
     * @param viewingPeriodsEnabled true\false to enable\disable.
     */
    public void setViewingPeriodsEnabled(boolean viewingPeriodsEnabled) {
        // Store remaining daily viewing period.
        SafetoonsApp.getPreferenceManager().edit().putBoolean(context.getString(R.string.pref_key_enable_viewing_periods), viewingPeriodsEnabled).apply();

        this.viewingPeriodsEnabled = viewingPeriodsEnabled;
    }

    /**
     * @return True if the viewing periods are enabled, false otherwise.
     */
    public boolean isViewingPeriodsEnabled() {
        return SafetoonsApp.getPreferenceManager().getBoolean(context.getString(R.string.pref_key_enable_viewing_periods), true);
    }

    // Store viewing periods enabled.
    //SafetoonsApp.getPreferenceManager().edit().putBoolean(VIEWING_PERIODS_ENABLED_SETTINGS_KEY, true).apply();

    public String getViewingDayStartTime() {
        return sdf.format(viewingDayStartTime);
    }

    public String getViewingDayEndTime() {
        return sdf.format(viewingDayEndTime);
    }

    public int getMaxDailyViewingPeriod() {
        return maxDailyViewingPeriod;
    }

    public int getMaxSessionViewingPeriod() {

        return maxSessionViewingPeriod;
    }

    public int getSessionResetsAfterPeriod() {
        return sessionResetsAfterPeriod;
    }
}
