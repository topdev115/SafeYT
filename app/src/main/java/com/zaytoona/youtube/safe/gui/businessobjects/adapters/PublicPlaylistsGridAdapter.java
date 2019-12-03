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

package com.zaytoona.youtube.safe.gui.businessobjects.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.YouTube.GetPublicPlaylists;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetPublicPlaylistsTask;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsListClickListener;

import java.io.IOException;

/**
 * An adapter that will display public playlists in a {@link android.widget.GridView}.
 */
public class PublicPlaylistsGridAdapter extends RecyclerViewAdapterEx<SafetoonsList, SafetoonsListViewHolder> {
	private GetPublicPlaylists getPublicPlaylists;
	private static final String TAG = PublicPlaylistsGridAdapter.class.getSimpleName();
	private SafetoonsListClickListener safetoonsListClickListener;

	private String category = null;

	public PublicPlaylistsGridAdapter(Context context, SafetoonsListClickListener safetoonsListClickListener, String category, boolean displayMode) {
		super(context);

		try {
			this.safetoonsListClickListener = safetoonsListClickListener;
			this.category = category;

			getPublicPlaylists = new GetPublicPlaylists();

			getPublicPlaylists.init();

			getPublicPlaylists.setDisplayMode(displayMode);
			getPublicPlaylists.setCategory(category);


		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getContext(),
					String.format(getContext().getString(R.string.could_not_get_videos), "Public Play Lists"),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public SafetoonsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_cell, parent, false);
		return new SafetoonsListViewHolder(v, safetoonsListClickListener);
	}

	@Override
	public void onBindViewHolder(SafetoonsListViewHolder viewHolder, int position) {
		if (viewHolder != null) {
			viewHolder.setPlaylist(get(position), getContext());
		}
		// if it reached the bottom of the list, then try to get the next page of videos
		if (position >= getItemCount() - 1) {
			Log.w(TAG, "BOTTOM REACHED!!!");
			if(getPublicPlaylists != null)
				new GetPublicPlaylistsTask(getPublicPlaylists, this).executeInParallel();
		}
	}

	public void refresh(Runnable onFinished) {
		getPublicPlaylists.reset();

		if(getPublicPlaylists != null)
			new GetPublicPlaylistsTask(getPublicPlaylists, this, onFinished).executeInParallel();
	}
}
