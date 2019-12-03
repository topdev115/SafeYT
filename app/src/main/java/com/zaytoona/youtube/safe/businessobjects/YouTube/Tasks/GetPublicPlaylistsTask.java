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

import android.util.Log;

import com.zaytoona.youtube.safe.businessobjects.AsyncTaskParallel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.GetPublicPlaylists;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.PublicPlaylistsGridAdapter;

import java.util.List;

/**
 * An asynchronous task that will retrieve YouTube playlists for a specific channel and displays them in the supplied Adapter.
 */
public class GetPublicPlaylistsTask extends AsyncTaskParallel<Void, Void, List<SafetoonsList>> {

	// Used to retrieve the playlists
	private GetPublicPlaylists getPublicPlaylists;

	// The adapter where the playlists will be displayed
	private PublicPlaylistsGridAdapter publicPlaylistsGridAdapter;

	// Runnable to run after playlists are retrieved
	private Runnable onFinished;

	public GetPublicPlaylistsTask(GetPublicPlaylists getPublicPlaylists, PublicPlaylistsGridAdapter publicPlaylistsGridAdapter) {
		this.publicPlaylistsGridAdapter = publicPlaylistsGridAdapter;
		this.getPublicPlaylists = getPublicPlaylists;
	}

	public GetPublicPlaylistsTask(GetPublicPlaylists getPublicPlaylists, PublicPlaylistsGridAdapter publicPlaylistsGridAdapter, Runnable onFinished) {
			this.publicPlaylistsGridAdapter = publicPlaylistsGridAdapter;
			this.getPublicPlaylists = getPublicPlaylists;
			this.onFinished = onFinished;
			getPublicPlaylists.reset();
			publicPlaylistsGridAdapter.clearList();
	}

	@Override
	protected List<SafetoonsList> doInBackground(Void... voids) {
		List<SafetoonsList> playlists = null;

		if (!isCancelled()) {
			playlists = getPublicPlaylists.getNextPlaylists();
		}

		return playlists;
	}

	@Override
	protected void onPostExecute(List<SafetoonsList> safetoonsList) {
		publicPlaylistsGridAdapter.appendList(safetoonsList);
		if(onFinished != null)
			onFinished.run();
	}
}
