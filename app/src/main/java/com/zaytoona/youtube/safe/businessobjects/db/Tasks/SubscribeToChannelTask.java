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

import android.content.Context;
import android.widget.Toast;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.AsyncTaskParallel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.db.SubscriptionsDb;
import com.zaytoona.youtube.safe.gui.businessobjects.views.SubscribeButton;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.SubsAdapter;

/**
 * A task that subscribes / unsubscribes to a YouTube channel.
 */
public class SubscribeToChannelTask extends AsyncTaskParallel<Void, Void, Boolean> {

	/** Set to true if the user wants to subscribe to a youtube channel;  false if the user wants to
	 *  unsubscribe. */
	private boolean			subscribeToChannel;
	private SubscribeButton subscribeButton;
	private Context         context;
	private YouTubeChannel	channel;
	private boolean         displayToastMessage = true;


	/**
	 * Constructor.
	 *
	 * @param subscribeButton	The subscribe button that the user has just clicked.
	 * @param channel			The channel the user wants to subscribe / unsubscribe.
	 */
	public SubscribeToChannelTask(SubscribeButton subscribeButton, YouTubeChannel channel) {
		this.subscribeToChannel = !subscribeButton.isUserSubscribed();
		this.subscribeButton = subscribeButton;
		this.context = subscribeButton.getContext();
		this.channel = channel;
	}


	/**
	 * Constructor.  Will unsubscribe the given channel.  No toast messages will be displayed.
	 *
	 * @param channel   Channel the user wants to unsubscribe.
	 */
	public SubscribeToChannelTask(YouTubeChannel channel) {
		this.subscribeToChannel = false;
		this.subscribeButton = null;
		this.context = SafetoonsApp.getContext();
		this.channel = channel;
		displayToastMessage = false;
	}


	@Override
	protected Boolean doInBackground(Void... params) {
		if (subscribeToChannel) {
			return SubscriptionsDb.getSubscriptionsDb().subscribe(channel);
		} else {
			return SubscriptionsDb.getSubscriptionsDb().unsubscribe(channel);
		}
	}


	@Override
	protected void onPostExecute(Boolean success) {
		if (success) {
			SubsAdapter adapter = SubsAdapter.get(context);

			if (subscribeToChannel) {
				// change the state of the button
				if (subscribeButton != null)
					subscribeButton.setUnsubscribeState();
				// Also change the subscription state of the channel
				channel.setUserSubscribed(true);

				// append the channel to the SubsAdapter (i.e. the channels subscriptions list/drawer)
				adapter.appendChannel(channel);

				if (displayToastMessage) {
					Toast.makeText(context, R.string.subscribed, Toast.LENGTH_LONG).show();
				}
			} else {
				// change the state of the button
				if (subscribeButton != null)
					subscribeButton.setSubscribeState();
				// Also change the subscription state of the channel
				channel.setUserSubscribed(false);
				
				// remove the channel from the SubsAdapter (i.e. the channels subscriptions list/drawer)
				adapter.removeChannel(channel);

				if (displayToastMessage) {
					Toast.makeText(context, R.string.unsubscribed, Toast.LENGTH_LONG).show();
				}
			}
		} else {
			String err = String.format(SafetoonsApp.getStr(R.string.error_unable_to_subscribe), channel.getId());
			Toast.makeText(context, err, Toast.LENGTH_LONG).show();
		}

		this.subscribeButton = null;
		this.context = null;
	}

}
