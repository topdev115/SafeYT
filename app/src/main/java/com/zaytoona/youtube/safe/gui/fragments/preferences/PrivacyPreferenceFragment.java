package com.zaytoona.youtube.safe.gui.fragments.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.db.PlaybackStatusDb;
import com.zaytoona.youtube.safe.businessobjects.db.SearchHistoryDb;

public class PrivacyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_privacy);

		Preference clearPlaybackStatus = findPreference(getString(R.string.pref_key_clear_playback_status));
		clearPlaybackStatus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				PlaybackStatusDb.getVideoDownloadsDb().deleteAllPlaybackHistory();
				Toast.makeText(getActivity(), getString(R.string.pref_playback_status_cleared), Toast.LENGTH_LONG).show();
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
		if (key.equals(getString(R.string.pref_key_disable_search_history))) {
			CheckBoxPreference disableSearchHistoryPreference = (CheckBoxPreference)findPreference(key);
			// If Search History is disabled, clear the Search History database.
			if(disableSearchHistoryPreference.isChecked()) {
				SearchHistoryDb.getSearchHistoryDb().deleteAllSearchHistory();
				Toast.makeText(getActivity(), getString(R.string.pref_disable_search_history_deleted), Toast.LENGTH_LONG).show();
			}
		} else if (key.equals(getString(R.string.pref_key_disable_playback_status))) {
			CheckBoxPreference disablePlaybackStatusPreference = (CheckBoxPreference)findPreference(key);
			if(disablePlaybackStatusPreference.isChecked()) {
				PlaybackStatusDb.getVideoDownloadsDb().deleteAllPlaybackHistory();
				Toast.makeText(getActivity(), getString(R.string.pref_disable_playback_status_deleted), Toast.LENGTH_LONG).show();
			}
		}
	}
}
