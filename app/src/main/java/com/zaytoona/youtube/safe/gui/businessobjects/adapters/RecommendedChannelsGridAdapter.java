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
import com.zaytoona.youtube.safe.businessobjects.YouTube.GetRecommendedChannels;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetRecommendedChannelsTask;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsListClickListener;

import java.io.IOException;

/**
 * An adapter that will display private playlists in a {@link android.widget.GridView}.
 */
public class RecommendedChannelsGridAdapter extends RecyclerViewAdapterEx<SafetoonsList, SafetoonsListViewHolder> {
	private GetRecommendedChannels getRecommendedChannels;
	private static final String TAG = RecommendedChannelsGridAdapter.class.getSimpleName();
	private SafetoonsListClickListener safetoonsListClickListener;

	public RecommendedChannelsGridAdapter(Context context, SafetoonsListClickListener safetoonsListClickListener, boolean displayMode) {
		super(context);

		try {
			getRecommendedChannels = new GetRecommendedChannels();

			getRecommendedChannels.init();

			getRecommendedChannels.setDisplayMode(displayMode);

		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getContext(),
					String.format(getContext().getString(R.string.could_not_get_videos), "Get Recommended Channels"),
					Toast.LENGTH_LONG).show();
		}

		this.safetoonsListClickListener = safetoonsListClickListener;
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
			if(getRecommendedChannels != null)
				new GetRecommendedChannelsTask(getRecommendedChannels, this).executeInParallel();
		}
	}

	public void refresh(Runnable onFinished) {
		if(getRecommendedChannels != null)
			new GetRecommendedChannelsTask(getRecommendedChannels, this, onFinished).executeInParallel();
	}
}
