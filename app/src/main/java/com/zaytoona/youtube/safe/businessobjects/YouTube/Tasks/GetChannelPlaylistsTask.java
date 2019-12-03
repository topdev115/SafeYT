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

package com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks;

import java.util.List;

import com.zaytoona.youtube.safe.businessobjects.AsyncTaskParallel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.GetChannelPlaylists;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePlaylist;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.PlaylistsGridAdapter;

/**
 * An asynchronous task that will retrieve YouTube playlists for a specific channel and displays them in the supplied Adapter.
 */
public class GetChannelPlaylistsTask extends AsyncTaskParallel<Void, Void, List<YouTubePlaylist>> {
	// Used to retrieve the playlists
	private GetChannelPlaylists getChannelPlaylists = new GetChannelPlaylists();

	// The adapter where the playlists will be displayed
	private PlaylistsGridAdapter playlistsGridAdapter;

	// Runnable to run after playlists are retrieved
	private Runnable onFinished;

	public GetChannelPlaylistsTask(GetChannelPlaylists getChannelPlaylists, PlaylistsGridAdapter playlistsGridAdapter) {
		this.playlistsGridAdapter = playlistsGridAdapter;
		this.getChannelPlaylists = getChannelPlaylists;
	}

	public GetChannelPlaylistsTask(GetChannelPlaylists getChannelPlaylists, PlaylistsGridAdapter playlistsGridAdapter, Runnable onFinished) {
			this.playlistsGridAdapter = playlistsGridAdapter;
			this.getChannelPlaylists = getChannelPlaylists;
			this.onFinished = onFinished;
			getChannelPlaylists.reset();
			playlistsGridAdapter.clearList();
	}

	@Override
	protected List<YouTubePlaylist> doInBackground(Void... voids) {
		List<YouTubePlaylist> playlists = null;

		if (!isCancelled()) {
			playlists = getChannelPlaylists.getNextPlaylists();
		}

		return playlists;
	}

	@Override
	protected void onPostExecute(List<YouTubePlaylist> youTubePlaylists) {
		playlistsGridAdapter.appendList(youTubePlaylists);
		if(onFinished != null)
			onFinished.run();
	}
}
