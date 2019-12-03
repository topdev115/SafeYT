/*
 * Safetoons
 * Copyright (C) 2018  Ramon Mifsud
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

package com.zaytoona.youtube.safe.businessobjects.db.Tasks;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.AsyncTaskParallel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.VideoBlocker;
import com.zaytoona.youtube.safe.businessobjects.db.SubscriptionsDb;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.SubsAdapter;

/**
 * Gets a list of channels (from the DB) that the user is subscribed to and then passes the channels
 * list to the given {@link SubsAdapter}.
 */
public class GetSubscribedChannelsTask extends AsyncTaskParallel<Void, Void, List<YouTubeChannel>> {

	private SubsAdapter adapter;
	private View progressBar;
	/** Set to true if the user wants channels to be sorted alphabetically (as set in the
	 * preference). */
	private final boolean sortChannelsAlphabetically;

	private static final String TAG = GetSubscribedChannelsTask.class.getSimpleName();


	public GetSubscribedChannelsTask(SubsAdapter adapter, View progressBar) {
		this.adapter = adapter;
		this.progressBar = progressBar;
		this.sortChannelsAlphabetically = SafetoonsApp.getPreferenceManager().getBoolean(SafetoonsApp.getStr(R.string.pref_key_subscriptions_alphabetical_order), false);
	}


	@Override
	protected void onPreExecute() {
		if (progressBar != null)
			progressBar.setVisibility(View.VISIBLE);
	}


	@Override
	protected List<YouTubeChannel> doInBackground(Void... params) {
		List<YouTubeChannel> subbedChannelsList = null;

		try {
			subbedChannelsList = SubscriptionsDb.getSubscriptionsDb().getSubscribedChannels();

			// sort channels alphabetically (by channel name) if the user wants so...
			if (sortChannelsAlphabetically) {
				subbedChannelsList = sortChannelsAlphabetically(subbedChannelsList);
			}

			// filter out for any whitelisted/blacklisted channels
			subbedChannelsList = new VideoBlocker().filterChannels(subbedChannelsList);
		} catch (Throwable tr) {
			Log.e(TAG, "An error has occurred while getting subbed channels", tr);
		}

		return subbedChannelsList;
	}


	/**
	 * Sort channels (by channel name) alphabetically.
	 *
	 * @param channelsList  Channels to be sorted.
	 *
	 * @return  The sorted channels.
	 */
	private List<YouTubeChannel> sortChannelsAlphabetically(List<YouTubeChannel> channelsList) {
		Collections.sort(channelsList, new Comparator<YouTubeChannel>() {
			@Override
			public int compare(YouTubeChannel channel, YouTubeChannel t1) {
				return channel.getTitle().compareToIgnoreCase(t1.getTitle());
			}
		});

		return channelsList;
	}


	@Override
	protected void onPostExecute(List<YouTubeChannel> subbedChannelsList) {
		if (progressBar != null) {
			progressBar.setVisibility(View.INVISIBLE);
			progressBar = null;
		}

		if (subbedChannelsList == null) {
			Toast.makeText(adapter.getContext(), R.string.error_get_subbed_channels, Toast.LENGTH_LONG).show();
		} else {
			adapter.appendList(subbedChannelsList);
		}

		// Notify the SubsAdapter that the subbed channel list has been retrieved and populated.  If
		// there is an error we still need to notify the adapter that the task has been completed
		// from this end...
		adapter.subsListRetrieved();
	}

}
