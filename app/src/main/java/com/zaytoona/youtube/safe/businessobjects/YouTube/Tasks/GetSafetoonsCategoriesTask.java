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

import com.zaytoona.youtube.safe.businessobjects.AsyncTaskParallel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.GetSafetoonsCategories;
import com.zaytoona.youtube.safe.businessobjects.categories.SafetoonsCategory;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.CategoryGridAdapter;

import java.util.List;

/**
 * An asynchronous task that will retrieve safetoons categories and displays them in the supplied Adapter.
 */
public class GetSafetoonsCategoriesTask extends AsyncTaskParallel<Void, Void, List<SafetoonsCategory>> {

	// Used to retrieve the categories
	private GetSafetoonsCategories getSafetoonsCategories;

	// The adapter where the categories will be displayed
	private CategoryGridAdapter categoryGridAdapter;

	// Runnable to run after categories are retrieved
	private Runnable onFinished;

	public GetSafetoonsCategoriesTask(GetSafetoonsCategories getSafetoonsCategories, CategoryGridAdapter categoryGridAdapter) {
		this.categoryGridAdapter = categoryGridAdapter;
		this.getSafetoonsCategories = getSafetoonsCategories;
	}

	public GetSafetoonsCategoriesTask(GetSafetoonsCategories getSafetoonsCategories, CategoryGridAdapter categoryGridAdapter, Runnable onFinished) {
		this.categoryGridAdapter = categoryGridAdapter;
		this.getSafetoonsCategories = getSafetoonsCategories;
		this.onFinished = onFinished;
		getSafetoonsCategories.reset();
		categoryGridAdapter.clearList();
	}

	@Override
	protected List<SafetoonsCategory> doInBackground(Void... voids) {

		List<SafetoonsCategory> safetoonsCategorys = null;

		if (!isCancelled()) {
			safetoonsCategorys = getSafetoonsCategories.getNextPlaylists();
		}

		return safetoonsCategorys;
	}

	@Override
	protected void onPostExecute(List<SafetoonsCategory> safetoonsCategory) {

		categoryGridAdapter.appendList(safetoonsCategory);
		if(onFinished != null)
			onFinished.run();
	}
}
