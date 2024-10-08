/*
 * Safetoons
 * Copyright (C) 2017  Ramon Mifsud
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.zaytoona.youtube.safe.gui.fragments.preferences;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import java.util.Arrays;

import com.zaytoona.youtube.safe.BuildConfig;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.YouTube.VideoStream.VideoResolution;

/**
 * Preference fragment for video player related settings.
 */
public class VideoPlayerPreferenceFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_video_player);

		// set up the list of available video resolutions
		ListPreference resolutionPref = (ListPreference) findPreference(getString(R.string.pref_key_preferred_res));
		resolutionPref.setEntries(VideoResolution.getAllVideoResolutionsNames());
		resolutionPref.setEntryValues(VideoResolution.getAllVideoResolutionsIds());

		// set up the list of available video resolutions on mobile network
        ListPreference resolutionPrefMobile = (ListPreference) findPreference(getString(R.string.pref_key_preferred_res_mobile));
		resolutionPrefMobile.setEntries(VideoResolution.getAllVideoResolutionsNames());
		resolutionPrefMobile.setEntryValues(VideoResolution.getAllVideoResolutionsIds());

		// if we are running an OSS version, then remove the last option (i.e. the "official" player
		// option)
		if (BuildConfig.FLAVOR.equals("oss")) {
			final ListPreference    videoPlayersListPref = (ListPreference) getPreferenceManager().findPreference(getString(R.string.pref_key_choose_player));
			final CharSequence[]    videoPlayersList = videoPlayersListPref.getEntries();
			CharSequence[]          modifiedVideoPlayersList = Arrays.copyOf(videoPlayersList, videoPlayersList.length - 1);

			videoPlayersListPref.setEntries(modifiedVideoPlayersList);
		}
	}

}
