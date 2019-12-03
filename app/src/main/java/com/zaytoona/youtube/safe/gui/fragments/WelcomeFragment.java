/*
 * Safetoons
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

import butterknife.BindView;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.VideoCategory;
import com.zaytoona.youtube.safe.gui.businessobjects.fragments.OrderableVideosGridFragment;

/**
 * Fragment that displays bookmarked videos.
 */
public class WelcomeFragment extends OrderableVideosGridFragment {
	@BindView(R.id.welcomeText)
	View welcomewText;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onFragmentSelected() {
		super.onFragmentSelected();
	}

	@Override
	protected int getLayoutResource() {
		return R.layout.fragment_welcome;
	}

	@Override
	protected VideoCategory getVideoCategory() {
		return null;//VideoCategory.BOOKMARKS_VIDEOS;
	}

	@Override
	public String getFragmentName() {
		return SafetoonsApp.getStr(R.string.welcome);
	}
}
