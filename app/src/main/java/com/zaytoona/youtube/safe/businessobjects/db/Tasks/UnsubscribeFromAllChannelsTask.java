package com.zaytoona.youtube.safe.businessobjects.db.Tasks;

import android.os.Handler;

import java.io.IOException;
import java.util.List;

import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.AsyncTaskParallel;
import com.zaytoona.youtube.safe.businessobjects.Logger;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.db.SubscriptionsDb;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.SubsAdapter;

/**
 * An Asynctask class that unsubscribes user from all the channels at once.
 */
public class UnsubscribeFromAllChannelsTask extends AsyncTaskParallel<YouTubeChannel, Void, Void> {

	private Handler handler = new Handler();

	@Override
	protected Void doInBackground(YouTubeChannel... youTubeChannels) {
		try {
			List<YouTubeChannel> channelList = SubscriptionsDb.getSubscriptionsDb().getSubscribedChannels();

			for (final YouTubeChannel youTubeChannel : channelList) {
				SubscriptionsDb.getSubscriptionsDb().unsubscribe(youTubeChannel);

				handler.post(new Runnable() {
					@Override
					public void run() {
						SubsAdapter.get(SafetoonsApp.getContext()).removeChannel(youTubeChannel);
					}
				});
			}
		} catch (IOException e) {
			Logger.e(this, "Error while unsubscribing from all channels", e);
		}

		return null;
	}

}
