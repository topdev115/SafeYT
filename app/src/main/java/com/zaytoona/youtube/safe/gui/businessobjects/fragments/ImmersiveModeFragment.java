/*
 * Safetoons
 * Copyright (C) 2017  Ramon Mifsud
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

package com.zaytoona.youtube.safe.gui.businessobjects.fragments;

import android.content.res.Resources;
import android.os.Build;
import android.view.View;

import com.zaytoona.youtube.safe.businessobjects.Logger;

/**
 * A fragment that can enables and disables the immersive mode (i.e. hides the navigation bar and
 * the status bar).
 */
public class ImmersiveModeFragment extends FragmentEx {

	/**
	 * Hide Android's bottom navigation bar.
	 */
	protected void hideNavigationBar() {
		changeNavigationBarVisibility(false);
	}


	/**
	 * Change the navigation bar's visibility status.
	 */
	private void changeNavigationBarVisibility(boolean setBarToVisible) {
		try {
			int newUiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();

			// navigation bar hiding:  backwards compatible to ICS.
			if (Build.VERSION.SDK_INT >= 14) {
				newUiOptions = setBarToVisible
						? newUiOptions & ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						: newUiOptions | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
			}

			// status bar hiding:  backwards compatible to Jellybean
			if (Build.VERSION.SDK_INT >= 16) {
				newUiOptions = setBarToVisible
						? newUiOptions & ~View.SYSTEM_UI_FLAG_FULLSCREEN & ~View.SYSTEM_UI_FLAG_LAYOUT_STABLE & ~View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION & ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						: newUiOptions | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			}

			// immersive mode:  backward compatible to KitKat
			if (Build.VERSION.SDK_INT >= 19) {
				newUiOptions = setBarToVisible
						? newUiOptions & ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
						: newUiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			}

			getActivity().getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
		} catch (Throwable tr) {
			Logger.e(this, "Exception caught while trying to change the nav bar visibility...", tr);
		}
	}


	/**
	 * @return The navigation bar's height in pixels.  0 is the device does not have any nav bar.
	 */
	protected int getNavBarHeightInPixels() {
		Resources   resources  = getResources();
		int         resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");

		return (resourceId > 0)  ?  resources.getDimensionPixelSize(resourceId)  :  0;
	}

}