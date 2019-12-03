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
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePublicPlayList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetChannelPlaylistsTask;
import com.zaytoona.youtube.safe.businessobjects.db.PublicPlayListsDb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Returns a list of YouTube playlists for a specific channel.
 *
 * <p>Do not run this directly, but rather use {@link GetChannelPlaylistsTask}.</p>
 */
public class GetPublicPlaylists {
	protected YouTube.Playlists.List playlistList = null;

	// Number of videos to return each time
	private static final long MAX_RESULTS = 45L;

	protected String nextPageToken = null;
	protected boolean noMorePlaylistPages = false;

	private boolean displayMode = false;

	private String category = null;

	private static final String	TAG = GetPublicPlaylists.class.getSimpleName();

	public void init() throws IOException {
		playlistList = YouTubeAPI.create().playlists().list("id, snippet, contentDetails");
		playlistList.setKey(YouTubeAPIKey.get().getYouTubeAPIKey());
		playlistList.setFields("items(id, snippet/title, snippet/description, snippet/thumbnails, snippet/publishedAt, contentDetails/itemCount)," +
				"nextPageToken");
		playlistList.setMaxResults(MAX_RESULTS);
		nextPageToken = null;

	}

	public void setDisplayMode(boolean displayMode) {
		this.displayMode = displayMode;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<SafetoonsList> getNextPlaylists() {

		List<Playlist> list = new ArrayList<>();

		if (!noMorePlaylistPages()) {
			try {

				List<YouTubePublicPlayList> publicListsList = null;

				if(category == null) {
					publicListsList = PublicPlayListsDb.getPublicPlayListsDb().getPublicPlayLists();
				}
				else {
					publicListsList = PublicPlayListsDb.getPublicPlayListsDb().getPublicPlayListsInCategory(category);
				}


				if (publicListsList.size() > 0) {

					String id = null;

					for (YouTubePublicPlayList playlist : publicListsList) {

						if(displayMode == true || (displayMode == false && PublicPlayListsDb.getPublicPlayListsDb().getPublicPlayListShowHide(category, playlist.getId()) == 1)) {
							if (id != null) {
								id += "," + playlist.getId();
							} else {
								id = playlist.getId();
							}
						}
					}

					this.playlistList.setId(id);

					this.playlistList.setPageToken(nextPageToken);

					// communicate with YouTube
					PlaylistListResponse listResponse = this.playlistList.execute();

					// get playlists
					List<Playlist> playlistList = listResponse.getItems();

					// get playlists
					playlistList = listResponse.getItems();

					if (playlistList != null && playlistList.size() > 0) {

						// Replace the playlist title with Safetoons specified title
						for(Playlist youtubeList : playlistList) {

							for (YouTubePublicPlayList playlist : publicListsList) {

								if(youtubeList.getId().equals(playlist.getId())) {
									youtubeList.getSnippet().setTitle(playlist.getTitle());
									break;
								}

							}

						}
						list.addAll(playlistList);
					}

					// set the next page token
					nextPageToken = listResponse.getNextPageToken();

					// if nextPageToken is null, it means that there are no more videos
					if (nextPageToken == null) {
						noMorePlaylistPages = true;
					}
				}
			} catch (IOException ex) {
				Log.e(TAG, ex.getLocalizedMessage());
			}
		}

		return toSafetoonsList(list);
	}

	public boolean noMorePlaylistPages() {
		return noMorePlaylistPages;
	}

	private List<SafetoonsList> toSafetoonsList(List<Playlist> playlistList) {
		List<SafetoonsList> safetoonsListList = new ArrayList<>();

		if(playlistList != null) {

			for (Playlist playlist : playlistList) {
				SafetoonsList safetoonsList = new SafetoonsList(playlist, category, SafetoonsList.SAFETOONS_LIST_TYPE_PLAY_LIST);

				safetoonsListList.add(safetoonsList);
			}
		}
		return safetoonsListList;
	}

	/**
	 * Reset the fetching of playlists. This will be called when a swipe to refresh is done.
	 */
	public void reset() {
		nextPageToken = null;
		noMorePlaylistPages = false;
	}
}
