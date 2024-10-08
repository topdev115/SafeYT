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

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.db.SubscriptionsDb;
import com.zaytoona.youtube.safe.gui.businessobjects.views.SubscribeButton;

/**
 * A task that checks if a user is subscribed to a particular YouTube channel.
 */
public class CheckIfUserSubbedToChannelTask extends AsyncTask<Void, Void, Boolean> {

	private SubscribeButton	subscribeButton;
	private String			channelId;

	private static String TAG = CheckIfUserSubbedToChannelTask.class.getSimpleName();


	/**
	 * Constructor.
	 *
	 * @param subscribeButton	The subscribe button that the user has just clicked.
	 * @param channelId			The channel ID the user wants to subscribe / unsubscribe.
	 */
	public CheckIfUserSubbedToChannelTask(SubscribeButton subscribeButton, String channelId) {
		this.subscribeButton = subscribeButton;
		this.channelId = channelId;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		Boolean isUserSubbed;

		try {
			isUserSubbed = SubscriptionsDb.getSubscriptionsDb().isUserSubscribedToChannel(channelId);
		} catch (Throwable tr) {
			Log.e(TAG, "Unable to check if user has subscribed to channel id=" + channelId, tr);
			isUserSubbed = null;
		}

		return isUserSubbed;
	}

	@Override
	protected void onPostExecute(Boolean isUserSubbed) {
		if (isUserSubbed == null) {
			String err = String.format(SafetoonsApp.getStr(R.string.error_check_if_user_has_subbed), channelId);
			Toast.makeText(subscribeButton.getContext(), err, Toast.LENGTH_LONG).show();
		} else if (isUserSubbed) {
			subscribeButton.setUnsubscribeState();
		} else {
			subscribeButton.setSubscribeState();
		}
	}

}
