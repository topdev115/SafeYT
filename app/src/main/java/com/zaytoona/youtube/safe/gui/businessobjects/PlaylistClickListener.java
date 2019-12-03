package com.zaytoona.youtube.safe.gui.businessobjects;

import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePlaylist;

/**
 * Interface for an object that will respond to a Playlist being clicked on
 */
public interface PlaylistClickListener {
	void onClickPlaylist(YouTubePlaylist playlist);
}
