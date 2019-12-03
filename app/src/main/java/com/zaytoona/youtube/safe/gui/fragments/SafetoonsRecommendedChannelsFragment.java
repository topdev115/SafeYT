package com.zaytoona.youtube.safe.gui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.VideoCategory;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.categories.SafetoonsCategory;
import com.zaytoona.youtube.safe.businessobjects.db.SubscriptionsDb;
import com.zaytoona.youtube.safe.gui.businessobjects.MainActivityListener;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsListClickListener;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.RecommendedChannelsGridAdapter;

import butterknife.BindView;

/**
 * A fragment that displays the Playlists belonging to a Channel
 */
public class SafetoonsRecommendedChannelsFragment extends VideosGridFragment implements SafetoonsListClickListener, SwipeRefreshLayout.OnRefreshListener, SubscriptionsDb.PublicListsDbListener{
	@BindView(R.id.noSafetoonsPlayListِAddedText)
	View noSafetoonsPlayListِAddedText;

	private RecommendedChannelsGridAdapter recommendedChannelsGridAdapter;
	private MainActivityListener    mainActivityListener;

	private boolean refreshInProgress = false;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		((TextView)noSafetoonsPlayListِAddedText).setText(R.string.no_public_lists_added_text);

		swipeRefreshLayout.setOnRefreshListener(this);

		if (recommendedChannelsGridAdapter == null) {
			recommendedChannelsGridAdapter = new RecommendedChannelsGridAdapter(getActivity(), this, displayMode);
		} else {
			recommendedChannelsGridAdapter.setContext(getActivity());
		}

		gridView.setAdapter(recommendedChannelsGridAdapter);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		// The fragment starts empty if it is the selected unless we do this call
		onFragmentSelected();
	}


	@Override
	protected int getLayoutResource() {
		return R.layout.safe_toons_videos_gridview;
	}

	@Override
	public String getFragmentName() {
		return SafetoonsApp.getStr(R.string.recommended_channels_tab);
	}

	@Override
	public void onFragmentSelected() {
		super.onFragmentSelected();

		if(recommendedChannelsGridAdapter != null) {
			// To avoid duplicate entries channels lists
			if(refreshInProgress == false) {
				refreshInProgress = true;
				recommendedChannelsGridAdapter.refresh(new Runnable() {
					@Override
					public void run() {
						swipeRefreshLayout.setRefreshing(false);
						refreshInProgress = false;

						if(recommendedChannelsGridAdapter.getItemCount() <= 0) {
							swipeRefreshLayout.setVisibility(View.GONE);
							noSafetoonsPlayListِAddedText.setVisibility(View.VISIBLE);
						}
						else {
							swipeRefreshLayout.setVisibility(View.VISIBLE);
							noSafetoonsPlayListِAddedText.setVisibility(View.GONE);
						}
					}
				});
			}
		}
	}

	// We need to tell Main Activity to open the playlist in its own fragment.
	@Override
	public void onClickPlaylist(SafetoonsList playlist) {
		if(mainActivityListener != null)
			mainActivityListener.onSafetoonsListClick(playlist);
	}

	@Override
	public void onClickCategory(SafetoonsCategory category) {

	}

	public void setMainActivityListener(MainActivityListener mainActivityListener) {
		this.mainActivityListener = mainActivityListener;
	}

	@Override
	public void onRefresh() {
		recommendedChannelsGridAdapter.refresh(new Runnable() {
			@Override
			public void run() {
				swipeRefreshLayout.setRefreshing(false);
			}
		});
	}

	@Override
	protected VideoCategory getVideoCategory() {
		return null;
	}

	// TODO: Implement Refresh Here If Needed
	@Override
	public void onPublicListsDbUpdated(String channelId) {

	}
}
