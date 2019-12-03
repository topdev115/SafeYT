package com.zaytoona.youtube.safe.gui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.VideoCategory;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.categories.SafetoonsCategory;
import com.zaytoona.youtube.safe.businessobjects.db.PublicPlayListsDb;
import com.zaytoona.youtube.safe.gui.businessobjects.MainActivityListener;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsListClickListener;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.CategoryGridAdapter;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.PublicPlaylistsGridAdapter;

import butterknife.BindView;

/**
 * A fragment that displays the Playlists belonging to a Channel
 */
public class SafetoonsPublicPlayListsFragment extends VideosGridFragment implements SafetoonsListClickListener, SwipeRefreshLayout.OnRefreshListener, PublicPlayListsDb.PublicPlayListsDbListener{
	@BindView(R.id.noSafetoonsPlayListِAddedText)
	View noSafetoonsPlayListِAddedText;
	@BindView(R.id.public_list_header)
	View header_layout;
	@BindView(R.id.category_header_text)
	TextView category_header_text;
	@BindView(R.id.category_back_arrow)
	ImageView category_header_image;

	private CategoryGridAdapter categoryGridAdapter;
	private PublicPlaylistsGridAdapter publicPlaylistsGridAdapter;
	private MainActivityListener    mainActivityListener;

	private boolean refreshInProgress = false;

	protected String category = null;
	protected String categoryName = null;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		((TextView)noSafetoonsPlayListِAddedText).setText(R.string.no_public_play_lists_added_text);

		swipeRefreshLayout.setOnRefreshListener(this);

		if(category != null) {
			if (publicPlaylistsGridAdapter == null) {
				publicPlaylistsGridAdapter = new PublicPlaylistsGridAdapter(getActivity(), this, category, displayMode);
			} else {
				publicPlaylistsGridAdapter.setContext(getActivity());
			}

			gridView.setAdapter(publicPlaylistsGridAdapter);

			category_header_text.setText(categoryName);

			header_layout.setVisibility(View.VISIBLE);
		}
		else {
			if (categoryGridAdapter == null) {
				categoryGridAdapter = new CategoryGridAdapter(getActivity(), this, displayMode);
			} else {
				categoryGridAdapter.setContext(getActivity());
			}

			gridView.setAdapter(categoryGridAdapter);

			header_layout.setVisibility(View.GONE);
		}

		category_header_image.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});

		return view;
	}

	/**
	 * @param  category Specifies the category
	 */
	protected void setCategory(String category) {
		this.category = category;

		if(header_layout != null) {

			if (category == null) {
				header_layout.setVisibility(View.GONE);
			} else {
				header_layout.setVisibility(View.VISIBLE);
			}
		}
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
		return SafetoonsApp.getStr(R.string.public_play_lists_tab);
	}

	@Override
	public void onFragmentSelected() {
		super.onFragmentSelected();

		if(category != null) {

			if(header_layout != null) {
				header_layout.setVisibility(View.VISIBLE);
				category_header_text.setText(categoryName);
			}

			if (publicPlaylistsGridAdapter != null) {
				// To avoid duplicate entries public lists
				if (refreshInProgress == false) {
					refreshInProgress = true;
					publicPlaylistsGridAdapter.refresh(new Runnable() {
						@Override
						public void run() {
							swipeRefreshLayout.setRefreshing(false);
							refreshInProgress = false;

							if (publicPlaylistsGridAdapter != null && publicPlaylistsGridAdapter.getItemCount() <= 0) {
								noSafetoonsPlayListِAddedText.setVisibility(View.VISIBLE);
								swipeRefreshLayout.setVisibility(View.GONE);
							} else {
								noSafetoonsPlayListِAddedText.setVisibility(View.GONE);
								swipeRefreshLayout.setVisibility(View.VISIBLE);
							}
						}
					});
				}
			}
		}
		else {
			if(header_layout != null) {
				header_layout.setVisibility(View.GONE);
			}

			if (categoryGridAdapter != null) {
				// To avoid duplicate entries public lists
				if (refreshInProgress == false) {
					refreshInProgress = true;
					categoryGridAdapter.refresh(new Runnable() {
						@Override
						public void run() {
							swipeRefreshLayout.setRefreshing(false);
							refreshInProgress = false;

							if (publicPlaylistsGridAdapter !=null && categoryGridAdapter.getItemCount() <= 0) {
								noSafetoonsPlayListِAddedText.setVisibility(View.VISIBLE);
								swipeRefreshLayout.setVisibility(View.GONE);
							} else {
								noSafetoonsPlayListِAddedText.setVisibility(View.GONE);
								swipeRefreshLayout.setVisibility(View.VISIBLE);
							}
						}
					});
				}
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

		setCategory(category.getId());

		header_layout.setVisibility(View.VISIBLE);

		category_header_text.setText(category.getTitle());

		categoryName = category.getTitle();

		if (publicPlaylistsGridAdapter == null) {
			publicPlaylistsGridAdapter = new PublicPlaylistsGridAdapter(getActivity(), this, category.getId(), displayMode);
		} else {
			publicPlaylistsGridAdapter.setContext(getActivity());
		}

		gridView.setAdapter(publicPlaylistsGridAdapter);

		publicPlaylistsGridAdapter.notifyDataSetChanged();

		onRefresh();
	}

	public void setMainActivityListener(MainActivityListener mainActivityListener) {
		this.mainActivityListener = mainActivityListener;
	}

	@Override
	public void onRefresh() {

		if(category != null && publicPlaylistsGridAdapter != null) {

			publicPlaylistsGridAdapter.refresh(new Runnable() {
				@Override
				public void run() {
					swipeRefreshLayout.setRefreshing(false);
				}
			});
		}
		else {
			categoryGridAdapter.refresh(new Runnable() {
				@Override
				public void run() {
					swipeRefreshLayout.setRefreshing(false);
				}
			});
		}
	}

	public void openForCategories() {
		category = null;
		publicPlaylistsGridAdapter = null;

		if(header_layout != null) {
			header_layout.setVisibility(View.GONE);
		}

		gridView.setAdapter(categoryGridAdapter);

		categoryGridAdapter.notifyDataSetChanged();

		onRefresh();

	}

	@Override
	protected VideoCategory getVideoCategory() {
		return null;
	}

	// TODO: Implement Refresh Here If needed
	@Override
	public void onPublicPlayListsDbUpdated() {

	}
}
