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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.zaytoona.youtube.safe.BuildConfig;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsMaterialDialog;
import com.zaytoona.youtube.safe.gui.businessobjects.updates.UpdatesCheckerTask;

/**
 * Preference fragment for about (this app) related settings.
 */
public class AboutPreferenceFragment extends PreferenceFragment {

	private static final String TAG = AboutPreferenceFragment.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_about);

		// set the app's version number
		Preference versionPref = findPreference(getString(R.string.pref_key_version));
		versionPref.setSummary(getAppVersion());

		// check for updates option
		/*
		Preference updatesPref = findPreference(getString(R.string.pref_key_updates));
		if (BuildConfig.FLAVOR.equalsIgnoreCase("oss")) {
			// remove the updates option if the user is running the OSS flavor...
			getPreferenceScreen().removePreference(updatesPref);
		} else {
			updatesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					new UpdatesCheckerTask(getActivity(), true).executeInParallel();
					return true;
				}
			});
		}
*/
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

	}


	/**
	 * @return The app's version number.
	 */
	private String getAppVersion() {
		StringBuilder ver = new StringBuilder(BuildConfig.VERSION_NAME);

		if (BuildConfig.FLAVOR.equalsIgnoreCase("extra")) {
			//ver.append(" Extra");
		}

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
