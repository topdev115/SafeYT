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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.VideoCategory;
import com.zaytoona.youtube.safe.businessobjects.YouTube.GetPrivatePlaylists;
import com.zaytoona.youtube.safe.businessobjects.YouTube.GetSafetoonsCategories;
import com.zaytoona.youtube.safe.businessobjects.YouTube.GetYouTubeVideos;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeVideo;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetPrivatePlaylistsTask;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetSafetoonsCategoriesTask;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetYouTubeVideosTask;
import com.zaytoona.youtube.safe.businessobjects.categories.SafetoonsCategory;
import com.zaytoona.youtube.safe.gui.businessobjects.MainActivityListener;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsListClickListener;

import java.io.IOException;

/**
 * An adapter that will display videos in a {@link android.widget.GridView}.
 */
public class CategoryGridAdapter extends RecyclerViewAdapterEx<SafetoonsCategory, CategoryGridViewHolder> {

	private GetSafetoonsCategories getSafetoonsCategories;

	/** Set to true to display options button (three vertical dots). */
	protected boolean				displayMode = false;

	// This allows the grid items to pass messages back to MainActivity
	protected MainActivityListener listener;

	private static final String TAG = CategoryGridAdapter.class.getSimpleName();

	private SafetoonsListClickListener safetoonsListClickListener;

	public void setListener(MainActivityListener listener) {
		this.listener = listener;
	}

	/**
	 * Constructor.
	 *
	 * @param context			Context.
	 * @param displayMode	    True for parent mode, kid mode otherwise.
	 */
	public CategoryGridAdapter(Context context, SafetoonsListClickListener safetoonsListClickListener, boolean displayMode) {
		super(context);

		getSafetoonsCategories = new GetSafetoonsCategories();

		getSafetoonsCategories.setDisplayMode(displayMode);

		this.safetoonsListClickListener = safetoonsListClickListener;

		this.displayMode = displayMode;
	}

	@Override
	public CategoryGridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_cell, parent, false);
		return new CategoryGridViewHolder(v, safetoonsListClickListener);
	}

	@Override
	public void onBindViewHolder(CategoryGridViewHolder viewHolder, int position) {

		if (viewHolder != null) {
			viewHolder.setSafetoonsCategory(get(position), getContext());
		}
		// if it reached the bottom of the list, then try to get the next page of videos
		if (position >= getItemCount() - 1) {
			Log.w(TAG, "BOTTOM REACHED!!!");
			if(getSafetoonsCategories != null)
				new GetSafetoonsCategoriesTask(getSafetoonsCategories, this).executeInParallel();
		}
	}


	/**
	 * Refresh the video grid, by running the task to get the videos again.
	 */
	public void refresh() {
		refresh(false);
	}


	/**
	 * Refresh the video grid, by running the task to get the videos again.
	 *
	 * @param clearVideosList If set to true, it will clear out any previously loaded videos (found
	 *                        in this adapter).
	 */
	public void refresh(boolean clearVideosList) {
/*
		if (getYouTubeVideos != null) {
			if (clearVideosList) {
				getYouTubeVideos.reset();
			}

			// now, we consider this as initialized - sometimes 'refresh' can be called before the initializeList is called.
			initialized = true;
			new GetYouTubeVideosTask(getYouTubeVideos, this, swipeRefreshLayout, clearVideosList).executeInParallel();
		}
		*/
	}

	public void refresh(Runnable onFinished) {
		if (getSafetoonsCategories != null)
			new GetSafetoonsCategoriesTask(getSafetoonsCategories, this, onFinished).executeInParallel();
	}
}
