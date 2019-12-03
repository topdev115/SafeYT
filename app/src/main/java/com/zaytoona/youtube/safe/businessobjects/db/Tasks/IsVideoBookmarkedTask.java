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

package com.zaytoona.youtube.safe.businessobjects.db.Tasks;

import android.view.Menu;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.AsyncTaskParallel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeVideo;
import com.zaytoona.youtube.safe.businessobjects.db.BookmarksDb;

/**
 * A task that checks if this video is bookmarked or not.  If it is bookmarked, then it will hide
 * the menu option to bookmark the video; otherwise it will hide the option to unbookmark the
 * video.
 */
public class IsVideoBookmarkedTask extends AsyncTaskParallel<Void, Void, Boolean> {
	private Menu menu;
	private YouTubeVideo youTubeVideo;

	public IsVideoBookmarkedTask(YouTubeVideo youTubeVideo, Menu menu) {
		this.youTubeVideo = youTubeVideo;
		this.menu = menu;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		return BookmarksDb.getBookmarksDb().isBookmarked(youTubeVideo);
	}

	@Override
	protected void onPostExecute(Boolean videoIsBookmarked) {
		// if this video has been bookmarked, hide the bookmark option and show the unbookmark option.
		menu.findItem(R.id.bookmark_video).setVisible(!videoIsBookmarked);
		menu.findItem(R.id.unbookmark_video).setVisible(videoIsBookmarked);
	}
}
