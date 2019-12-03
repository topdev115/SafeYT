package com.zaytoona.youtube.safe.gui.fragments;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.VideoCategory;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePlaylist;

/**
 * A Fragment that displays the videos of a playlist in a {@link VideosGridFragment}
 */
public class PlaylistVideosFragment extends VideosGridFragment {

	private YouTubePlaylist youTubePlaylist;

	@BindView(R.id.toolbar)
	Toolbar     toolbar;
	@BindView(R.id.playlist_banner_image_view)
	ImageView   playlistBannerImageView;
	@BindView(R.id.playlist_thumbnail_image_view)
	ImageView   playlistThumbnailImageView;
	@BindView(R.id.playlist_title_text_view)
	TextView    playlistTitleTextView;

	public static final String PLAYLIST_OBJ = "PlaylistVideosFragment.PLAYLIST_OBJ";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// sets the play list
		youTubePlaylist = (YouTubePlaylist)getArguments().getSerializable(PLAYLIST_OBJ);

		View view = super.onCreateView(inflater, container, savedInstanceState);

		ButterKnife.bind(this, view);
		playlistTitleTextView.setText(youTubePlaylist.getTitle());

		// setup the toolbar/actionbar
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null && youTubePlaylist.getChannel() != null) {
			actionBar.setTitle(youTubePlaylist.getChannel().getTitle());
		}

		// set the playlist's thumbnail
		Glide.with(getActivity())
				.load(youTubePlaylist.getThumbnailUrl())
				.apply(new RequestOptions().placeholder(R.drawable.channel_thumbnail_default))
				.into(playlistThumbnailImageView);

		// set the channel's banner
		Glide.with(getActivity())
				.load(youTubePlaylist.getBannerUrl())
				.apply(new RequestOptions().placeholder(R.drawable.banner_default))
				.into(playlistBannerImageView);

		// Force initialization
		videoGridAdapter.initializeList();
		return view;
	}


	@Override
	protected int getLayoutResource() {
		return R.layout.fragment_playlist_videos;
	}

	@Override
	public String getFragmentName() {
		return null;
	}


	@Override
	protected VideoCategory getVideoCategory() {
		return VideoCategory.PLAYLIST_VIDEOS;
	}


	@Override
	protected String getSearchString() {
		return youTubePlaylist.getId();
	}

}
