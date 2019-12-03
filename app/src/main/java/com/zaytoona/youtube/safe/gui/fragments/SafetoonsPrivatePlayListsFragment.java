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
import com.zaytoona.youtube.safe.businessobjects.db.PrivateListsDb;
import com.zaytoona.youtube.safe.gui.businessobjects.MainActivityListener;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsListClickListener;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.PrivatePlaylistsGridAdapter;

import butterknife.BindView;

/**
 * A fragment that displays the Playlists belonging to a Channel
 */
public class SafetoonsPrivatePlayListsFragment extends VideosGridFragment implements SafetoonsListClickListener, SwipeRefreshLayout.OnRefreshListener, PrivateListsDb.PrivateListsDbListener{
	@BindView(R.id.noSafetoonsPlayListِAddedText)
	View noSafetoonsPlayListِAddedText;

	private PrivatePlaylistsGridAdapter privatePlaylistsGridAdapter;
	private MainActivityListener    mainActivityListener;

	private boolean refreshInProgress = false;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		((TextView)noSafetoonsPlayListِAddedText).setText(R.string.no_private_lists_added_text);

		swipeRefreshLayout.setOnRefreshListener(this);

		if (privatePlaylistsGridAdapter == null) {
			privatePlaylistsGridAdapter = new PrivatePlaylistsGridAdapter(getActivity(), this, displayMode);
		} else {
			privatePlaylistsGridAdapter.setContext(getActivity());
		}

		gridView.setAdapter(privatePlaylistsGridAdapter);

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
		return SafetoonsApp.getStr(R.string.private_play_lists_tab);
	}

	@Override
	public void onFragmentSelected() {
		super.onFragmentSelected();

		if(privatePlaylistsGridAdapter != null) {
			// To avoid duplicate entries private lists
			if(refreshInProgress == false) {
				refreshInProgress = true;
				privatePlaylistsGridAdapter.refresh(new Runnable() {
					@Override
					public void run() {
						swipeRefreshLayout.setRefreshing(false);
						refreshInProgress = false;

						if(privatePlaylistsGridAdapter.getItemCount() <= 0) {
							noSafetoonsPlayListِAddedText.setVisibility(View.VISIBLE);
							swipeRefreshLayout.setVisibility(View.GONE);
						}
						else {
							noSafetoonsPlayListِAddedText.setVisibility(View.GONE);
							swipeRefreshLayout.setVisibility(View.VISIBLE);
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
		privatePlaylistsGridAdapter.refresh(new Runnable() {
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

	// TODO: Implement Refresh Here
	@Override
	public void onPrivateListsDbUpdated() {

	}

	// TODO: Implement Refresh Here
	@Override
	public void onPrivateListItemsDbUpdated() {

	}

}
