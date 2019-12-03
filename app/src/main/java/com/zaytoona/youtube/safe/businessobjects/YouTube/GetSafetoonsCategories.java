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

import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetChannelPlaylistsTask;
import com.zaytoona.youtube.safe.businessobjects.categories.SafetoonsCategory;
import com.zaytoona.youtube.safe.businessobjects.db.PrivateListsDb;
import com.zaytoona.youtube.safe.businessobjects.db.SafetoonsCategoriesDb;
import com.zaytoona.youtube.safe.businessobjects.db.model.PrivateListInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Returns a list of Safetoons categories.
 *
 * <p>Do not run this directly, but rather use {@link GetSafetoonsCategoriesTask}.</p>
 */
public class GetSafetoonsCategories {

	//protected String nextPageToken = null;
	protected boolean noMoreSafetoonsCategory = false;

	private boolean displayMode = false;

	private static final String	TAG = GetSafetoonsCategories.class.getSimpleName();

	public void init() throws IOException {
	}

	public void setDisplayMode(boolean displayMode) {
		this.displayMode = displayMode;
	}

	public List<SafetoonsCategory> getNextPlaylists() {

		List<SafetoonsCategory> safetoonsCategoryList = new ArrayList<>();

		if (!noMoreSafetoonsCategory()) {
			safetoonsCategoryList = SafetoonsCategoriesDb.getSafetoonsCategoriesDb().getSafetoonsCategories();

			noMoreSafetoonsCategory = true;
		}

		return safetoonsCategoryList;
	}

	public boolean noMoreSafetoonsCategory() {
		return noMoreSafetoonsCategory;
	}

	/**
	 * Reset the fetching of playlists. This will be called when a swipe to refresh is done.
	 */
	public void reset() {
		noMoreSafetoonsCategory = false;
	}
}
