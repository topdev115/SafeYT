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

package com.zaytoona.youtube.safe.businessobjects.YouTube;

import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeVideo;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetChannelPlaylistsTask;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetChannelVideosTask;
import com.zaytoona.youtube.safe.businessobjects.db.SubscriptionsDb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Returns a list of YouTube playlists for a specific channel.
 *
 * <p>Do not run this directly, but rather use {@link GetChannelPlaylistsTask}.</p>
 */
public class GetRecommendedChannels {

	protected boolean noMorePlaylistPages = false;

	private boolean displayMode = false;

	private static final String	TAG = GetRecommendedChannels.class.getSimpleName();

	public void init() throws IOException {
	}

	public void setDisplayMode(boolean displayMode) {
		this.displayMode = displayMode;
	}

	public List<SafetoonsList> getNextPlaylists() {

		List<SafetoonsList> safetoonsListList = new ArrayList<>();

		if (!noMorePlaylistPages()) {

			List<String> channels = null;

			if(displayMode == true) {
				channels = SubscriptionsDb.getSubscriptionsDb().getAllowedChannelIds();
			}
			else {
				channels = SubscriptionsDb.getSubscriptionsDb().getShowChannelIds();
			}

			for (String allowedChannel : channels) {

				try {
					YouTubeChannel channel = new GetChannelsDetails().getYouTubeChannel(allowedChannel);

					if(channel != null) {
						SafetoonsList safetoonsList = new SafetoonsList(channel, SafetoonsList.SAFETOONS_LIST_TYPE_CHANNEL);

						safetoonsListList.add(safetoonsList);
					}
				}
				catch(IOException ex) {

				}
			}

			noMorePlaylistPages = true;
		}

		return safetoonsListList;
	}

	public boolean noMorePlaylistPages() {
		return noMorePlaylistPages;
	}

	/**
	 * Reset the fetching of playlists. This will be called when a swipe to refresh is done.
	 */
	public void reset() {
		noMorePlaylistPages = false;
	}
}
