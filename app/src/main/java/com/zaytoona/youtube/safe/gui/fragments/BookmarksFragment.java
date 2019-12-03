/*
 * SkyTube
 * Copyright (C) 2016  Ramon Mifsud
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

package com.zaytoona.youtube.safe.gui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.AsyncTaskParallel;
import com.zaytoona.youtube.safe.businessobjects.VideoCategory;
import com.zaytoona.youtube.safe.businessobjects.db.BookmarksDb;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.OrderableVideoGridAdapter;
import com.zaytoona.youtube.safe.gui.businessobjects.fragments.OrderableVideosGridFragment;

import butterknife.BindView;

/**
 * Fragment that displays bookmarked videos.
 */
public class BookmarksFragment extends OrderableVideosGridFragment implements BookmarksDb.BookmarksDbListener {
	@BindView(R.id.noBookmarkedVideosText)
	View noBookmarkedVideosText;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		videoGridAdapter = new OrderableVideoGridAdapter(getActivity(), BookmarksDb.getBookmarksDb(), displayMode);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		swipeRefreshLayout.setEnabled(false);
		populateList();
	}


	private void populateList() {
		new PopulateBookmarksTask().executeInParallel();
	}


	@Override
	public void onFragmentSelected() {
		super.onFragmentSelected();

		if (BookmarksDb.getBookmarksDb().isHasUpdated()) {
			populateList();
			BookmarksDb.getBookmarksDb().setHasUpdated(false);
		}
	}
	

	@Override
	public void onBookmarksDbUpdated() {
		populateList();
		if(videoGridAdapter != null)
			videoGridAdapter.refresh(true);
	}
	

	@Override
	protected int getLayoutResource() {
		return R.layout.fragment_bookmarks;
	}
	

	@Override
	protected VideoCategory getVideoCategory() {
		return VideoCategory.BOOKMARKS_VIDEOS;
	}
	

	@Override
	public String getFragmentName() {
		return SafetoonsApp.getStr(R.string.bookmarks);
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * A task that:
	 *   1. gets the current total number of bookmarks
	 *   2. updated the UI accordingly (wrt step 1)
	 *   3. get the bookmarked videos asynchronously.
	 */
	private class PopulateBookmarksTask extends AsyncTaskParallel<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			return BookmarksDb.getBookmarksDb().getTotalBookmarks();
		}

		@Override
		protected void onPostExecute(Integer numVideosBookmarked) {
			// If no videos have been bookmarked, show the text notifying the user, otherwise
			// show the swipe refresh layout that contains the actual video grid.
			if (numVideosBookmarked <= 0) {
				swipeRefreshLayout.setVisibility(View.GONE);
				noBookmarkedVideosText.setVisibility(View.VISIBLE);
			} else {
				swipeRefreshLayout.setVisibility(View.VISIBLE);
				noBookmarkedVideosText.setVisibility(View.GONE);

				// set video category and get the bookmarked videos asynchronously
				videoGridAdapter.setVideoCategory(VideoCategory.BOOKMARKS_VIDEOS);
			}
		}
	}
}
