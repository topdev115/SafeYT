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

package com.zaytoona.youtube.safe.gui.businessobjects;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeVideo;
import com.zaytoona.youtube.safe.gui.activities.YouTubePlayerActivity;

import static com.zaytoona.youtube.safe.gui.activities.YouTubePlayerActivity.YOUTUBE_VIDEO_OBJ;

/**
 * Launches YouTube player.
 */
public class YouTubePlayer {

	/**
	 * Launches the custom-made YouTube player so that the user can view the selected video.
	 *
	 * @param youTubeVideo Video to be viewed.
	 */
	public static void launch(YouTubeVideo youTubeVideo, Context context) {
		Intent i = new Intent(context, YouTubePlayerActivity.class);
		i.putExtra(YOUTUBE_VIDEO_OBJ, youTubeVideo);
		context.startActivity(i);
	}


	/**
	 * Launches the custom-made YouTube player so that the user can view the selected video.
	 *
	 * @param videoUrl URL of the video to be watched.
	 */
	public static void launch(String videoUrl, Context context) {
		Intent i = new Intent(context, YouTubePlayerActivity.class);
		i.setAction(Intent.ACTION_VIEW);
		i.setData(Uri.parse(videoUrl));
		context.startActivity(i);
	}

}
