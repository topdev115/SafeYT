package com.zaytoona.youtube.safe.gui.fragments.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zaytoona.pincode.managers.AppLock;
import com.zaytoona.pincode.managers.LockManager;
import com.zaytoona.youtube.safe.BuildConfig;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.Logger;
import com.zaytoona.youtube.safe.businessobjects.db.ChannelFilteringDb;
import com.zaytoona.youtube.safe.businessobjects.periods.ViewingManager;
import com.zaytoona.youtube.safe.gui.activities.CustomPinActivity;
import com.zaytoona.youtube.safe.gui.activities.KidMainActivity;
import com.zaytoona.youtube.safe.gui.activities.MainActivity;
import com.zaytoona.youtube.safe.gui.businessobjects.MultiSelectListPreferenceDialog;
import com.zaytoona.youtube.safe.gui.businessobjects.MultiSelectListPreferenceItem;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsMaterialDialog;
import com.zaytoona.youtube.safe.gui.businessobjects.SubscriptionsBackupsManager;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.SubsAdapter;
import com.zaytoona.youtube.safe.gui.businessobjects.updates.UpdatesCheckerTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GeneralPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = GeneralPreferenceFragment.class.getSimpleName();

    private SubscriptionsBackupsManager subscriptionsBackupsManager;

    private static final int REQUEST_CODE_ENABLE = 11;

    // Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private String [] mSupportEmails = null;
    private String mSupportEmailSubject = null;
    private String mSupportEmailBody = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_general);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        subscriptionsBackupsManager = new SubscriptionsBackupsManager(getActivity(), this);


/*
        final Preference channelBlacklistPreference = findPreference(getString(R.string.pref_key_channel_blacklist));
        // enable/disable the video blocker
        enablePreferences(isVideoBlockerEnabled(), channelBlacklistPreference);

        findPreference(getString(R.string.pref_key_enable_video_blocker)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                enablePreferences((boolean) newValue, channelBlacklistPreference);
                Toast.makeText(getActivity(), R.string.setting_updated, Toast.LENGTH_LONG).show();
                return true;
            }
        });


        final Preference.OnPreferenceChangeListener settingUpdatesPreferenceChange = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(getActivity(), R.string.setting_updated, Toast.LENGTH_LONG).show();
                return true;
            }
        };
*/
        // Download public play lists from server (Firebase)
        Preference importPublicPlayListsPref = findPreference(getString(R.string.pref_key_download_public_play_lists_from_server));
        importPublicPlayListsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                subscriptionsBackupsManager.displayDwonloadPublicPlayListsFromServerDialog();
                return true;
            }
        });


        // Download common lists from server (Firebase)
//        Preference importCommonListsPref = findPreference(getString(R.string.pref_key_download_recommended_channels_from_server));
//        importCommonListsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                subscriptionsBackupsManager.displayDwonloadCommonListsFromServerDialog();
//                return true;
//            }
//        });

        // Viewing Periods
        final ViewingManager viewingManager = new ViewingManager(getActivity());

        final Preference viewingPeriodsEnablePreference = findPreference(getString(R.string.pref_key_enable_viewing_periods));
        final Preference viewingPeriods = findPreference(getString(R.string.pref_key_viewing_periods));

        viewingPeriods.setEnabled(viewingManager.isViewingPeriodsEnabled());

        viewingPeriodsEnablePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                viewingPeriods.setEnabled((boolean) newValue);

                viewingManager.setViewingPeriodsEnabled((boolean) newValue);

                Toast.makeText(getActivity(), R.string.setting_updated, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        // Viewing periods settings
        viewingPeriods.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                View customView = getActivity().getLayoutInflater().inflate(R.layout.dialog_viewing_periods_settings, null, false);

                // Daily Start Time
                final Spinner dailyStartTimeSpinner = customView.findViewById(R.id.spinner_daily_viewing_periods_start_time);

                // Daily End Time
                final Spinner dailyEndTimeSpinner = customView.findViewById(R.id.spinner_daily_viewing_periods_end_time);

                // Reset max daily period
                final Spinner maxDailyViewingPeriodSpinner = customView.findViewById(R.id.spinner_viewing_periods_max_daily_period);

                // Reset max daily period
                final Spinner maxSessionViewingPeriodSpinner = customView.findViewById(R.id.spinner_viewing_periods_max_session_period);

                // Reset session after period
                final Spinner sessionResetsAfterSpinner = customView.findViewById(R.id.spinner_viewing_periods_session_resets_after_period);

                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> dailyStartTimeViewingAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.viewing_periods_daily_start_time, android.R.layout.simple_spinner_item);

                // Specify the layout to use when the list of choices appears
                dailyStartTimeViewingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> dailyEndTimeViewingAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.viewing_periods_daily_end_time, android.R.layout.simple_spinner_item);

                // Specify the layout to use when the list of choices appears
                dailyEndTimeViewingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> maxDailyViewingAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.viewing_periods_max_daily_period, android.R.layout.simple_spinner_item);

                // Specify the layout to use when the list of choices appears
                maxDailyViewingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> maxSessionViewingAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.viewing_periods_max_session_period, android.R.layout.simple_spinner_item);

                // Specify the layout to use when the list of choices appears
                maxSessionViewingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> sessionResetsAfterAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.viewing_periods_session_resets_after_values_list, android.R.layout.simple_spinner_item);

                // Specify the layout to use when the list of choices appears
                sessionResetsAfterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Apply the adapter to the spinner
                dailyStartTimeSpinner.setAdapter(dailyStartTimeViewingAdapter);

                // Apply the adapter to the spinner
                dailyEndTimeSpinner.setAdapter(dailyEndTimeViewingAdapter);

                // Apply the adapter to the spinner
                maxDailyViewingPeriodSpinner.setAdapter(maxDailyViewingAdapter);

                // Apply the adapter to the spinner
                maxSessionViewingPeriodSpinner.setAdapter(maxSessionViewingAdapter);

                // Apply the adapter to the spinner
                sessionResetsAfterSpinner.setAdapter(sessionResetsAfterAdapter);
/*
                AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        switch(parent.getId()) {
                            case R.id.spinner_daily_viewing_periods_start_time:

                                //Toast.makeText(getActivity(), "" + parent.getItemAtPosition(position).toString() , Toast.LENGTH_LONG).show();
                                break;
                            case R.id.spinner_daily_viewing_periods_end_time:

                                //Toast.makeText(getActivity(), "" + parent.getItemAtPosition(position).toString() , Toast.LENGTH_LONG).show();
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                };

                dailyStartTimeSpinner.setOnItemSelectedListener(onItemSelectedListener);
                dailyEndTimeSpinner.setOnItemSelectedListener(onItemSelectedListener);
                maxDailyViewingPeriodSpinner.setOnItemSelectedListener(onItemSelectedListener);
                maxSessionViewingPeriodSpinner.setOnItemSelectedListener(onItemSelectedListener);
                sessionResetsAfterSpinner.setOnItemSelectedListener(onItemSelectedListener);
*/

                dailyStartTimeSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.viewing_periods_daily_start_time)).indexOf(viewingManager.getViewingDayStartTime()));
                dailyEndTimeSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.viewing_periods_daily_end_time)).indexOf(viewingManager.getViewingDayEndTime()));
                maxDailyViewingPeriodSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.viewing_periods_max_daily_period)).indexOf(new Integer(viewingManager.getMaxDailyViewingPeriod()).toString()));
                maxSessionViewingPeriodSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.viewing_periods_max_session_period)).indexOf(new Integer(viewingManager.getMaxSessionViewingPeriod()).toString()));
                sessionResetsAfterSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.viewing_periods_session_resets_after_values_list)).indexOf(new Integer(viewingManager.getSessionResetsAfterPeriod()).toString()));

                new SafetoonsMaterialDialog(getActivity())
                        .title(R.string.setup_viewing_periods)
                        .positiveText(R.string.setup_viewing_periods_button)
                        .customView(customView, false)

                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                viewingManager.saveViewingSettings(
                                        dailyStartTimeSpinner.getSelectedItem().toString(),
                                        dailyEndTimeSpinner.getSelectedItem().toString(),
                                        new Integer(maxDailyViewingPeriodSpinner.getSelectedItem().toString()).intValue(),
                                        new Integer(maxSessionViewingPeriodSpinner.getSelectedItem().toString()).intValue(),
                                        new Integer(sessionResetsAfterSpinner.getSelectedItem().toString()).intValue());

                                Toast.makeText(getActivity(), R.string.pref_viewing_period_settinbgs_updated, Toast.LENGTH_LONG).show();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .build()
                        .show();
                return true;
            }
        });

        // Change Pincode
        Preference changePinCode = findPreference(getString(R.string.pref_key_change_pin_code));
        changePinCode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                //Toast.makeText(getActivity(), "", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getActivity(), CustomPinActivity.class);
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.CHANGE_PIN);
                startActivityForResult(intent, REQUEST_CODE_ENABLE);
                return true;

            }
        });

        // About
        // set the app's version number
        Preference versionPref = findPreference(getString(R.string.pref_key_version));
        versionPref.setSummary(getAppVersion());

        // if the user clicks on the website link, then open it using an external browser
        Preference websitePref = findPreference(getString(R.string.pref_key_website));
        websitePref.setSummary(BuildConfig.SAFETOONS_WEBSITE);
        websitePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // view the app's website in a web browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.SAFETOONS_WEBSITE));
                startActivity(browserIntent);
                return true;
            }
        });

        // if the user clicks on the privacy link, then open it using an external browser
        Preference privacyLinkPref = findPreference(getString(R.string.pref_key_privacy_website));
        privacyLinkPref.setSummary(BuildConfig.SAFETOONS_WEBSITE_PRIVACY);
        privacyLinkPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // view the app's website in a web browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.SAFETOONS_WEBSITE_PRIVACY));
                startActivity(browserIntent);
                return true;
            }
        });

        // Read values from settings
        mDatabase.child("/settings/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {

                    String supportEmail = dataSnapshot.child("supportEmail").getValue().toString();
                    mSupportEmails = supportEmail.split(",");

                    mSupportEmailSubject = dataSnapshot.child("supportEmailSubject").getValue().toString();

                    mSupportEmailBody = dataSnapshot.child("supportEmailBody").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Support
        // if the user clicks on the support link, then open email client
        Preference supportPref = findPreference(getString(R.string.pref_key_support));
        supportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent mailIntent = new Intent(Intent.ACTION_SENDTO);

                mailIntent.setData(Uri.parse("mailto:"));

                mailIntent.putExtra(Intent.EXTRA_EMAIL  , mSupportEmails);
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, mSupportEmailSubject);

                final String androidId = Settings.Secure.getString(getActivity().getContentResolver(),
                        Settings.Secure.ANDROID_ID);

                String supportEmailText = getResources().getString(R.string.support_id)+ " " + mAuth.getUid() + "   " + androidId + "\n\n" + mSupportEmailBody + "\n\n";

                mailIntent.putExtra(Intent.EXTRA_TEXT, supportEmailText);

                if (mailIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(mailIntent);
                }
                else {
                    Toast.makeText(getActivity(), R.string.menu_no_email_client, Toast.LENGTH_LONG).show();
                }


                return true;
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CODE_ENABLE:
                break;
             default:
                 subscriptionsBackupsManager.onActivityResult(requestCode, resultCode, data);
                 break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        subscriptionsBackupsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * @return True if the user wants to use the video blocker, false otherwise.
     */
    private boolean isVideoBlockerEnabled() {
        return SafetoonsApp.getPreferenceManager().getBoolean(getString(R.string.pref_key_enable_video_blocker), true);
    }


    /**
     * Enable/Disable the preferences "views" based on whether the video blocker is enabled or not.
     *
     * @param enableBlocker                 True means video blocker is enabled by the user.
     * @param channelBlacklistPreference    {@link Preference} for channel blacklisting.
     */
    private void enablePreferences(boolean enableBlocker, Preference channelBlacklistPreference) {
        initChannelFilteringPreferences(enableBlocker, channelBlacklistPreference);
    }


    /**
     * Initialized the channel filtering preference.
     *
     * @param videoBlockerEnabled           True means video blocker is enabled by the user.
     * @param channelBlacklistPreference    {@link Preference} for channel blacklisting.
     */
    private void initChannelFilteringPreferences(boolean videoBlockerEnabled, Preference channelBlacklistPreference) {
        // get the current channel filtering method selected by the user...
        final String channelFilter = SafetoonsApp.getPreferenceManager().getString(getString(R.string.pref_key_channel_filter_method), getString(R.string.channel_blacklisting_filtering));
        initChannelFilteringPreferences(videoBlockerEnabled, channelFilter, channelBlacklistPreference);
    }


    /**
     * Initialized the channel filtering preference.
     *
     * @param channelFilter                 The current channel filtering method (e.g. "Channel Blacklisting").
     * @param channelBlacklistPreference    {@link Preference} for channel blacklisting.
     */
    private void initChannelFilteringPreferences(String channelFilter, Preference channelBlacklistPreference) {
        initChannelFilteringPreferences(isVideoBlockerEnabled(), channelFilter, channelBlacklistPreference);
    }


    /**
     * Initialized the channel filtering preference.
     *
     * @param videoBlockerEnabled           True means video blocker is enabled by the user.
     * @param channelFilter                 The current channel filtering method (e.g. "Channel Blacklisting").
     * @param channelBlacklistPreference    {@link Preference} for channel blacklisting.
     */
    private void initChannelFilteringPreferences(boolean videoBlockerEnabled, String channelFilter, Preference channelBlacklistPreference) {
        if (videoBlockerEnabled) {
            if (channelFilter.equals(getString(R.string.channel_blacklisting_filtering))) {
                initChannelBlacklistingPreference(channelBlacklistPreference);
                channelBlacklistPreference.setEnabled(true);
            } else if (channelFilter.equals(getString(R.string.channel_whitelisting_filtering))) {
                channelBlacklistPreference.setEnabled(false);
            } else {
                Logger.e(this, "Unknown channel filtering preference", channelFilter);
            }
        } else {
            channelBlacklistPreference.setEnabled(false);
        }

        // User has just changed the filtering method and hence there might be a subbed channel
        // that needs to be filtered out...  Therefore, we need to notify the SubsAdapter to
        // refresh...
        //SubsAdapter.get(getActivity()).refreshSubsList();
    }


    /**
     * Initialized the channel blacklist preference.
     */
    private void initChannelBlacklistingPreference(final Preference channelBlacklistPreference) {
        channelBlacklistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new GeneralPreferenceFragment.BlacklistChannelsDialog(getActivity()).show();
                return true;
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }



    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key != null) {
        }
    }

    /**
     * Display a dialog which allows the user to select his preferred language(s).
     */
    private class BlacklistChannelsDialog extends MultiSelectListPreferenceDialog {

        public BlacklistChannelsDialog(@NonNull Context context) {
            super(context);

            // set the items of this list as the blacklisted channels
            setItems(ChannelFilteringDb.getChannelFilteringDb().getBlacklistedChannels());

            title(R.string.pref_title_channel_blacklist);
            positiveText(R.string.unblock);
            onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    final List<MultiSelectListPreferenceItem> channels = getSelectedItems();

                    if (channels != null  &&  !channels.isEmpty()) {
                        // remove the selected channels from the blacklist
                        final boolean success = ChannelFilteringDb.getChannelFilteringDb().unblacklist(channels);

                        Toast.makeText(getActivity(),
                                success ? R.string.channel_blacklist_updated : R.string.channel_blacklist_update_failure,
                                Toast.LENGTH_LONG)
                                .show();
                    }

                    dialog.dismiss();
                }
            });
        }

    }


    /**
     * @return The app's version number.
     */
    private String getAppVersion() {
        StringBuilder ver = new StringBuilder(BuildConfig.VERSION_NAME);

        Integer verCode = new Integer(BuildConfig.VERSION_CODE);

        if (BuildConfig.FLAVOR.equalsIgnoreCase("extra")) {
            //ver.append(" Extra");
        }

        ver.append(" (" + verCode.toString() + ")");

        if (BuildConfig.DEBUG) {
            ver.append(" (Debug ");
            ver.append(getAppBuildTimeStamp());
            ver.append(')');
        }

        return ver.toString();
    }

    /**
     * @return App's build timestamp.
     */
    private static String getAppBuildTimeStamp() {
        String timeStamp = "???";

        try {
            ApplicationInfo appInfo = SafetoonsApp.getContext().getPackageManager().getApplicationInfo(SafetoonsApp.getContext().getPackageName(), 0);
            String appFile = appInfo.sourceDir;
            long time = new File(appFile).lastModified();

            SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmm", Locale.US);
            timeStamp = formatter.format(time);
        } catch (Throwable tr) {
            Log.d(TAG, "An error occurred while getting app's build timestamp", tr);
        }

        return timeStamp;

    }

}
