package com.zaytoona.youtube.safe.gui.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.VideoCategory;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePlaylist;
import com.zaytoona.youtube.safe.businessobjects.db.PrivateListsDb;
import com.zaytoona.youtube.safe.businessobjects.db.PublicPlayListsDb;
import com.zaytoona.youtube.safe.businessobjects.db.SubscriptionsDb;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsMaterialDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A Fragment that displays the videos of a playlist in a {@link VideosGridFragment}
 */
public class SafetoonsPlaylistVideosFragment extends VideosGridFragment {

	private SafetoonsList safetoonsPlaylist;

	@BindView(R.id.toolbar)
	Toolbar     toolbar;
	@BindView(R.id.playlist_banner_image_view)
	ImageView   playlistBannerImageView;
	@BindView(R.id.playlist_thumbnail_image_view)
	ImageView   playlistThumbnailImageView;
	@BindView(R.id.playlist_title_text_view)
	TextView    playlistTitleTextView;

	// Public List Header
	@BindView(R.id.public_play_list_header)
	View publicPlayListHeaderLayout;
	@BindView(R.id.public_play_list_remove_image)
	ImageView removeImage;
	@BindView(R.id.public_play_list_show_hide_image)
	ImageView showHideImage;

	// Private List Header
    @BindView(R.id.private_list_header)
    View privateListHeaderLayout;
    @BindView(R.id.private_list_delete_image)
    ImageView deleteImage;
    @BindView(R.id.private_list_show_hide_image)
    ImageView showHideImagePrivate;

    // Channel Header
	@BindView(R.id.public_list_header)
	View publicListHeaderLayout;
	@BindView(R.id.public_list_show_hide_image)
	ImageView showHideImageChannel;


	public String fragmentName = null;
	public String category = null;
	private Handler hideHudTimerHandler = null;

	public static final String PLAYLIST_OBJ = "SafetoonsPlaylistVideosFragment.PLAYLIST_OBJ";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// sets the play list
		safetoonsPlaylist = (SafetoonsList)getArguments().getSerializable(PLAYLIST_OBJ);

		fragmentName = safetoonsPlaylist.getId();
		View view = super.onCreateView(inflater, container, savedInstanceState);

		ButterKnife.bind(this, view);
		playlistTitleTextView.setText(safetoonsPlaylist.getTitle());

		// setup the toolbar/actionbar
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(safetoonsPlaylist.getTitle());
		}

		toolbar.setTitleTextColor(Color.BLACK);
		toolbar.getNavigationIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
		/*
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
*/

		if(category != null) {
			// Public List Header
			// Show header if parent mode
			if (displayMode == true && safetoonsPlaylist.getType() == 0) {
				publicPlayListHeaderLayout.setVisibility(View.VISIBLE);
			}

			int showHide = PublicPlayListsDb.getPublicPlayListsDb().getPublicPlayListShowHide(category, fragmentName);

			if( showHide == 1) {
				showHideImage.setImageResource(R.drawable.ic_show_black);
				ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
			}
			else {
				showHideImage.setImageResource(R.drawable.ic_hide_black);
				ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.red_color)));
			}

				showHideImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						int showHide = PublicPlayListsDb.getPublicPlayListsDb().getPublicPlayListShowHide(category, fragmentName);

						//Toast.makeText(getActivity(), "" + showHide, Toast.LENGTH_LONG).show();

						if (showHide == 1) {
							PublicPlayListsDb.getPublicPlayListsDb().setPublicPlayListShow(category, fragmentName, 0);
							Toast.makeText(SafetoonsPlaylistVideosFragment.this.getContext(), R.string.public_play_list_not_visible_to_kids_msg, Toast.LENGTH_LONG).show();
							getActivity().runOnUiThread(new Runnable() {
								public void run() {
									ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
									showHideImage.setImageResource(R.drawable.ic_show_black);
								}
							});
						} else {
							PublicPlayListsDb.getPublicPlayListsDb().setPublicPlayListShow(category, fragmentName, 1);
							Toast.makeText(SafetoonsPlaylistVideosFragment.this.getContext(), R.string.public_play_list_visible_to_kids_msg, Toast.LENGTH_LONG).show();
							getActivity().runOnUiThread(new Runnable() {
								public void run() {
									showHideImage.setImageResource(R.drawable.ic_hide_black);
									ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.red_color)));
								}
							});
						}

						// To force a refresh...
						hideHudTimerHandler = new Handler();
						hideHudTimerHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								hideHudTimerHandler = null;

								int showHide = PublicPlayListsDb.getPublicPlayListsDb().getPublicPlayListShowHide(category, fragmentName);
								if (showHide == 1) {
									ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
									showHideImage.setImageResource(R.drawable.ic_show_black);
								} else {
									ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.red_color)));
									showHideImage.setImageResource(R.drawable.ic_hide_black);
								}
							}
						}, 100);
					}
				});

			removeImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					new SafetoonsMaterialDialog(getActivity())
							.content(R.string.public_play_list_confirm_remove_msg)
							.positiveText(R.string.remove_public_play_list)
							.onPositive(new MaterialDialog.SingleButtonCallback() {
								@Override
								public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
									PublicPlayListsDb.getPublicPlayListsDb().remove(category, fragmentName);
								}
							})
							.negativeText(R.string.no)
							.show();
				}
			});
		}

        // Private List Header
        // Show header if parent mode
        if (displayMode == true && safetoonsPlaylist.getType() == 1) {
            privateListHeaderLayout.setVisibility(View.VISIBLE);
        }

        int showHide = PrivateListsDb.getPrivateListsDb().getPrivateListShowHide(fragmentName);

        if( showHide == 1) {
            showHideImagePrivate.setImageResource(R.drawable.ic_show_black);
            ImageViewCompat.setImageTintList(showHideImagePrivate, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
        }
        else {
            showHideImagePrivate.setImageResource(R.drawable.ic_hide_black);
            ImageViewCompat.setImageTintList(showHideImagePrivate, ColorStateList.valueOf(getResources().getColor(R.color.red_color)));
        }

        showHideImagePrivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int showHide = PrivateListsDb.getPrivateListsDb().getPrivateListShowHide(fragmentName);

                //Toast.makeText(getActivity(), "" + showHide, Toast.LENGTH_LONG).show();

                if( showHide == 1) {
                    PrivateListsDb.getPrivateListsDb().setPrivateListShow(fragmentName, 0);
					Toast.makeText(SafetoonsPlaylistVideosFragment.this.getContext(), R.string.private_list_not_visible_to_kids_msg, Toast.LENGTH_LONG).show();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            ImageViewCompat.setImageTintList(showHideImagePrivate, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
                            showHideImagePrivate.setImageResource(R.drawable.ic_show_black);
                        }
                    });
                }
                else {
                    PrivateListsDb.getPrivateListsDb().setPrivateListShow(fragmentName, 1);
					Toast.makeText(SafetoonsPlaylistVideosFragment.this.getContext(), R.string.private_list_visible_to_kids_msg, Toast.LENGTH_LONG).show();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            showHideImagePrivate.setImageResource(R.drawable.ic_hide_black);
                            ImageViewCompat.setImageTintList(showHideImagePrivate, ColorStateList.valueOf(getResources().getColor(R.color.red_color)));
                        }
                    });
                }

                // To force a refresh...
                hideHudTimerHandler = new Handler();
                hideHudTimerHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideHudTimerHandler = null;

                        int showHide = PrivateListsDb.getPrivateListsDb().getPrivateListShowHide(fragmentName);
                        if( showHide == 1) {
                            ImageViewCompat.setImageTintList(showHideImagePrivate, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
                            showHideImagePrivate.setImageResource(R.drawable.ic_show_black);
                        }
                        else {
                            ImageViewCompat.setImageTintList(showHideImagePrivate, ColorStateList.valueOf(getResources().getColor(R.color.red_color)));
                            showHideImagePrivate.setImageResource(R.drawable.ic_hide_black);
                        }
                    }
                }, 100);
            }
        });

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SafetoonsMaterialDialog(getActivity())
                        .content(R.string.private_list_confirm_delete_msg)
                        .positiveText(R.string.delete_private_list)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                PrivateListsDb.getPrivateListsDb().remove(fragmentName);
                            }
                        })
                        .negativeText(R.string.no)
                        .show();
            }
        });

        // Channel Header
		// Show header if parent mode
		if (displayMode == true && safetoonsPlaylist.getType() == 2) {
			publicListHeaderLayout.setVisibility(View.VISIBLE);
		}

		boolean bShowHide = SubscriptionsDb.getSubscriptionsDb().isdChannelHidden(fragmentName);

		if( bShowHide == false) {
			showHideImageChannel.setImageResource(R.drawable.ic_show_black);
			ImageViewCompat.setImageTintList(showHideImageChannel, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
		}
		else {
			showHideImageChannel.setImageResource(R.drawable.ic_hide_black);
			ImageViewCompat.setImageTintList(showHideImageChannel, ColorStateList.valueOf(getResources().getColor(R.color.red_color)));
		}

		showHideImageChannel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				boolean showHide = SubscriptionsDb.getSubscriptionsDb().isdChannelHidden(fragmentName);

				//Toast.makeText(getActivity(), "" + showHide, Toast.LENGTH_LONG).show();

				if( showHide == false) {
					SubscriptionsDb.getSubscriptionsDb().updateChannelHiddenStatus(fragmentName, 1);

					Toast.makeText(SafetoonsPlaylistVideosFragment.this.getContext(), R.string.public_list_not_visible_to_kids_msg, Toast.LENGTH_LONG).show();

					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							ImageViewCompat.setImageTintList(showHideImageChannel, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
							showHideImageChannel.setImageResource(R.drawable.ic_show_black);
						}
					});
				}
				else {
					SubscriptionsDb.getSubscriptionsDb().updateChannelHiddenStatus(fragmentName, 0);

					Toast.makeText(SafetoonsPlaylistVideosFragment.this.getContext(), R.string.public_list_visible_to_kids_msg, Toast.LENGTH_LONG).show();

					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							showHideImageChannel.setImageResource(R.drawable.ic_hide_black);
							ImageViewCompat.setImageTintList(showHideImageChannel, ColorStateList.valueOf(getResources().getColor(R.color.red_color)));
						}
					});
				}

				// To force a refresh...
				hideHudTimerHandler = new Handler();
				hideHudTimerHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						hideHudTimerHandler = null;

						boolean showHide = SubscriptionsDb.getSubscriptionsDb().isdChannelHidden(SafetoonsPlaylistVideosFragment.this.fragmentName);

						if( showHide == false) {
							ImageViewCompat.setImageTintList(showHideImageChannel, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
							showHideImageChannel.setImageResource(R.drawable.ic_show_black);
						}
						else {
							ImageViewCompat.setImageTintList(showHideImageChannel, ColorStateList.valueOf(getResources().getColor(R.color.red_color)));
							showHideImageChannel.setImageResource(R.drawable.ic_hide_black);
						}
					}
				}, 100);
			}
		});

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

		if(safetoonsPlaylist.getType() == 1) {
			return VideoCategory.PRIVATE_LIST_VIDEOS;
		}
		else if(safetoonsPlaylist.getType() == 2) {
			return VideoCategory.CHANNEL_VIDEOS;
		}

		return VideoCategory.PLAYLIST_VIDEOS;
	}


	@Override
	protected String getSearchString() {
		return safetoonsPlaylist.getId();
	}

	public void setDisplayMode(boolean displayMode) {
		this.displayMode = displayMode;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}
