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
import com.zaytoona.youtube.safe.businessobjects.YouTube.GetPrivatePlaylists;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.PrivatePlaylistsGridAdapter;

import java.util.List;

/**
 * An asynchronous task that will retrieve YouTube playlists for a specific channel and displays them in the supplied Adapter.
 */
public class GetPrivatePlaylistsTask extends AsyncTaskParallel<Void, Void, List<SafetoonsList>> {

	// Used to retrieve the playlists
	private GetPrivatePlaylists getPrivatePlaylists;

	// The adapter where the playlists will be displayed
	private PrivatePlaylistsGridAdapter privatePlaylistsGridAdapter;

	// Runnable to run after playlists are retrieved
	private Runnable onFinished;

	public GetPrivatePlaylistsTask(GetPrivatePlaylists getPrivatePlaylists, PrivatePlaylistsGridAdapter privatePlaylistsGridAdapter) {
		this.privatePlaylistsGridAdapter = privatePlaylistsGridAdapter;
		this.getPrivatePlaylists = getPrivatePlaylists;
	}

	public GetPrivatePlaylistsTask(GetPrivatePlaylists getPrivatePlaylists, PrivatePlaylistsGridAdapter privatePlaylistsGridAdapter, Runnable onFinished) {
		this.privatePlaylistsGridAdapter = privatePlaylistsGridAdapter;
		this.getPrivatePlaylists = getPrivatePlaylists;
		this.onFinished = onFinished;
		getPrivatePlaylists.reset();
		privatePlaylistsGridAdapter.clearList();
	}

	@Override
	protected List<SafetoonsList> doInBackground(Void... voids) {
		List<SafetoonsList> playlists = null;

		if (!isCancelled()) {
			playlists = getPrivatePlaylists.getNextPlaylists();
		}

		return playlists;
	}

	@Override
	protected void onPostExecute(List<SafetoonsList> safetoonsList) {

		privatePlaylistsGridAdapter.appendList(safetoonsList);
		if(onFinished != null)
			onFinished.run();
	}
}
