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

import android.util.Log;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeAPI;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeAPIKey;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePlaylist;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePublicPlayList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetChannelPlaylistsTask;
import com.zaytoona.youtube.safe.businessobjects.db.PrivateListsDb;
import com.zaytoona.youtube.safe.businessobjects.db.PublicPlayListsDb;
import com.zaytoona.youtube.safe.businessobjects.db.model.PrivateListInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Returns a list of YouTube playlists for a specific channel.
 *
 * <p>Do not run this directly, but rather use {@link GetChannelPlaylistsTask}.</p>
 */
public class GetPrivatePlaylists {
	//protected static final Long	MAX_RESULTS = 45L;

	//protected String nextPageToken = null;
	protected boolean noMorePlaylistPages = false;

	private boolean displayMode = false;

	private static final String	TAG = GetPrivatePlaylists.class.getSimpleName();

	public void init() throws IOException {
	}

	public void setDisplayMode(boolean displayMode) {
		this.displayMode = displayMode;
	}

	public List<SafetoonsList> getNextPlaylists() {

		List<SafetoonsList> safetoonsListList = new ArrayList<>();

		if (!noMorePlaylistPages()) {
			List<PrivateListInfo> privateListsList = PrivateListsDb.getPrivateListsDb().getPrivateListsInfo();

			for (PrivateListInfo playlist : privateListsList) {

				if(displayMode == true || (displayMode == false && playlist.getPrivateListShow() == 1)) {
					SafetoonsList safetoonsList = new SafetoonsList(playlist, SafetoonsList.SAFETOONS_LIST_TYPE_PRIVATE_LIST);

					safetoonsListList.add(safetoonsList);
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
