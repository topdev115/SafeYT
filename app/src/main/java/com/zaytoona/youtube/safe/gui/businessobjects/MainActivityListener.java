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

package com.zaytoona.youtube.safe.gui.businessobjects;

import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePlaylist;

/**
 * This methods will be called when the user clicks on the view whose aim is to open the ChannelBrowser.
 */
public interface MainActivityListener {

	/**
	 * Called whenever a channel view has been clicked.
	 *
	 * @param channelId The ID of the Channel
	 */
	void onChannelClick(String channelId);

	/**
	 * Called whenever a channel view has been clicked.
	 *
	 * @param channel Channel
	 */
	void onChannelClick(YouTubeChannel channel);

	/**
	 * Called when a playlist is clicked on
	 */
	void onPlaylistClick(YouTubePlaylist playlist);

	/**
	 * Called when a playlist is clicked on
	 */
	void onSafetoonsListClick(SafetoonsList playlist);
}
