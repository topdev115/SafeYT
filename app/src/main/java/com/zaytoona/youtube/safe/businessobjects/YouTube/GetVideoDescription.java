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

import java.io.IOException;

/**
 * Queries YouTube and return the description of a video.  The description is set by the YouTuber
 * who uploaded the video.
 */
public class GetVideoDescription extends GetVideosDetailsByIDs {

	/**
	 * Initialise object.
	 *
	 * @param videoId		The video ID to query about.
	 * @throws IOException
	 */
	public void init(String videoId) throws IOException {
		super.init(videoId);
		super.videosList.setFields("items(snippet/description)");
	}

}
