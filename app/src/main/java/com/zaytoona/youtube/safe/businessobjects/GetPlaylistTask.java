package com.zaytoona.youtube.safe.businessobjects;

import java.io.IOException;

import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePlaylist;
import com.zaytoona.youtube.safe.gui.businessobjects.PlaylistClickListener;

/**
 * An asynchronous task that will retrieve a YouTube playlist for a specified playlist URL.
 */
public class GetPlaylistTask extends AsyncTaskParallel<Void, Void, YouTubePlaylist> {
	private String playlistId;
	private PlaylistClickListener playlistClickListener;
	private GetPlaylist getPlaylist;

	public GetPlaylistTask(String playlistId, PlaylistClickListener playlistClickListener) {
		this.playlistId = playlistId;
		this.playlistClickListener = playlistClickListener;
	}

	@Override
	protected YouTubePlaylist doInBackground(Void... voids) {
		try {
			getPlaylist = new GetPlaylist();
			getPlaylist.init(playlistId);
			return getPlaylist.getNextPlaylists().get(0);
		} catch (IOException e) {
			Logger.e(this, "Couldn't initialize GetPlaylist");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(YouTubePlaylist youTubePlaylist) {
		if(playlistClickListener != null)
			playlistClickListener.onClickPlaylist(youTubePlaylist);
	}
}
