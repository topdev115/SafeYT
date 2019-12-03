package com.zaytoona.youtube.safe.gui.businessobjects;

import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePlaylist;
import com.zaytoona.youtube.safe.businessobjects.categories.SafetoonsCategory;

/**
 * Interface for an object that will respond to a Playlist being clicked on
 */
public interface SafetoonsListClickListener {
	void onClickPlaylist(SafetoonsList playlist);
	void onClickCategory(SafetoonsCategory category);
}
