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

package com.zaytoona.youtube.safe.gui.businessobjects.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.VideoCategory;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeVideo;
import com.zaytoona.youtube.safe.businessobjects.db.PlaybackStatusDb;
import com.zaytoona.youtube.safe.businessobjects.db.Tasks.IsVideoBookmarkedTask;
import com.zaytoona.youtube.safe.businessobjects.db.Tasks.IsVideoPrivateListedTask;
import com.zaytoona.youtube.safe.businessobjects.db.Tasks.IsVideoWatchedTask;
import com.zaytoona.youtube.safe.businessobjects.firebase.VideoRecommend;
import com.zaytoona.youtube.safe.businessobjects.firebase.VideoReport;
import com.zaytoona.youtube.safe.common.Constants;
import com.zaytoona.youtube.safe.gui.businessobjects.MainActivityListener;
import com.zaytoona.youtube.safe.gui.businessobjects.MobileNetworkWarningDialog;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsMaterialDialog;
import com.zaytoona.youtube.safe.gui.businessobjects.YouTubePlayer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * A ViewHolder for the videos grid view.
 */
class GridViewHolder extends RecyclerView.ViewHolder {
	/** YouTube video */
	private YouTubeVideo            youTubeVideo = null;
	private Context                 context = null;
	private MainActivityListener    mainActivityListener;
	private boolean                 showChannelInfo;

	// Kid or parent mode (parent => true, kid => false)
	private boolean 				displayMode = false;

	/** Current video category */
	private VideoCategory videoCategory = null;
	private String searchQuery = null;

	private TextView titleTextView;
	private TextView channelTextView;
	private TextView thumbsUpPercentageTextView;
	private TextView videoDurationTextView;
	private TextView publishDateTextView;
	private ImageView thumbnailImageView;
	private TextView viewsTextView;
	private ProgressBar videoPositionProgressBar;
	private ImageView optionsImageView;
	private ImageView privateListImageView;
	private ImageView unPrivateListImageView;

    private DatabaseReference mDatabase = null;


	/**
	 * Constructor.
	 *
	 * @param view              Cell view (parent).
	 * @param listener          MainActivity listener.
	 * @param showChannelInfo   True to display channel information (e.g. channel name) and allows
	 *                          user to open and browse the channel; false to hide such information.
	 */
	GridViewHolder(final View view, MainActivityListener listener, boolean showChannelInfo, boolean displayMode, final String searchQuery, VideoCategory videoCategory) {
		super(view);

		mDatabase = FirebaseDatabase.getInstance().getReference();

		titleTextView = view.findViewById(R.id.title_text_view);
		channelTextView = view.findViewById(R.id.channel_text_view);
		thumbsUpPercentageTextView = view.findViewById(R.id.thumbs_up_text_view);
		videoDurationTextView = view.findViewById(R.id.video_duration_text_view);
		publishDateTextView = view.findViewById(R.id.publish_date_text_view);
		thumbnailImageView = view.findViewById(R.id.thumbnail_image_view);
		viewsTextView = view.findViewById(R.id.views_text_view);
		videoPositionProgressBar = view.findViewById(R.id.video_position_progress_bar);

		optionsImageView = view.findViewById(R.id.options_button);
		privateListImageView = view.findViewById(R.id.video_private_list_button);
		unPrivateListImageView = view.findViewById(R.id.video_unprivate_list_button);

		this.mainActivityListener = listener;
		this.showChannelInfo = showChannelInfo;
		this.searchQuery = searchQuery;

		// kid or parent?
		this.displayMode = displayMode;

		this.videoCategory = videoCategory;

		thumbnailImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View thumbnailView) {
				if (youTubeVideo != null) {
					if(gridViewHolderListener != null)
						gridViewHolderListener.onClick();
					YouTubePlayer.launch(youTubeVideo, context);
				}
			}
		});

		View.OnClickListener channelOnClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mainActivityListener != null)
					mainActivityListener.onChannelClick(youTubeVideo.getChannelId());
			}
		};

		view.findViewById(R.id.channel_layout).setOnClickListener(showChannelInfo ? channelOnClickListener : null);

        if(displayMode == true) {

        	/*
			view.findViewById(R.id.options_button).setVisibility(View.VISIBLE);

            view.findViewById(R.id.options_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onOptionsButtonClick(v);
                }
            });
*/

			privateListImageView.setVisibility(View.VISIBLE);

			privateListImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					youTubeVideo.privateListVideo(v.getContext(), unPrivateListImageView);
				}
			});

			//unPrivateListImageView.setVisibility(View.VISIBLE);

			unPrivateListImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					youTubeVideo.unPrivateListVideo(v.getContext(), unPrivateListImageView, searchQuery);
				}
			});

			view.findViewById(R.id.video_recommend_button).setVisibility(View.VISIBLE);

			view.findViewById(R.id.video_recommend_button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					recommendVideo(v.getContext());
				}
			});

			view.findViewById(R.id.video_report_button).setVisibility(View.VISIBLE);

			view.findViewById(R.id.video_report_button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					reportVideo(v.getContext());
				}
			});


		}
        else {
        	// Hide information under the video if kid mode.
			//view.findViewById(R.id.channel_layout).setVisibility(View.GONE);

			// Hide thumbs info if kid mode.
			//thumbsUpPercentageTextView.setVisibility(View.GONE);
        }
	}



	/**
	 * Updates the contents of this ViewHold such that the data of these views is equal to the
	 * given youTubeVideo.
	 *
	 * @param youTubeVideo		{@link YouTubeVideo} instance.
	 */
	void updateInfo(YouTubeVideo youTubeVideo, Context context, MainActivityListener listener) {

		if(youTubeVideo == null) {
			return;
		}

		this.youTubeVideo = youTubeVideo;

		// Parent only
		if(displayMode == true) {
			new IsVideoPrivateListedTask(youTubeVideo, null, unPrivateListImageView, videoCategory).executeInParallel();
		}
		this.context = context;
		this.mainActivityListener = listener;
		updateViewsData();
	}


	public void updateViewsData() {
		updateViewsData(this.context);
	}

	/**
	 * This method will update the {@link View}s of this object reflecting this GridView's video.
	 *
	 * @param context			{@link Context} current context.
	 */
	public void updateViewsData(Context context) {
		this.context = context;
		titleTextView.setText(youTubeVideo.getTitle());
		channelTextView.setText(showChannelInfo ? youTubeVideo.getChannelName() : "");
		publishDateTextView.setText(youTubeVideo.getPublishDatePretty());
		videoDurationTextView.setText(youTubeVideo.getDuration());
		viewsTextView.setText(youTubeVideo.getViewsCount());
		Glide.with(context)
						.load(youTubeVideo.getThumbnailUrl())
						.apply(new RequestOptions().placeholder(R.drawable.thumbnail_default))
						.into(thumbnailImageView);

		if (youTubeVideo.getThumbsUpPercentageStr() != null) {
			thumbsUpPercentageTextView.setVisibility(View.VISIBLE);
			thumbsUpPercentageTextView.setText(youTubeVideo.getThumbsUpPercentageStr());
		} else {
			thumbsUpPercentageTextView.setVisibility(View.INVISIBLE);
		}

		if(SafetoonsApp.getPreferenceManager().getBoolean(context.getString(R.string.pref_key_disable_playback_status), false)) {
			videoPositionProgressBar.setVisibility(View.INVISIBLE);
		} else {
			PlaybackStatusDb.VideoWatchedStatus videoWatchedStatus = PlaybackStatusDb.getVideoDownloadsDb().getVideoWatchedStatus(youTubeVideo);
			if (videoWatchedStatus.isWatched()) {
				videoPositionProgressBar.setVisibility(View.VISIBLE);
				videoPositionProgressBar.setMax(youTubeVideo.getDurationInSeconds() * 1000);
				if (videoWatchedStatus.isFullyWatched()) {
					videoPositionProgressBar.setProgress(youTubeVideo.getDurationInSeconds() * 1000);
				} else {
					videoPositionProgressBar.setProgress((int) videoWatchedStatus.getPosition());
				}
			} else {
				videoPositionProgressBar.setVisibility(View.INVISIBLE);
			}
		}
	}



 	private void onOptionsButtonClick(final View view) {
		final PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
		popupMenu.getMenuInflater().inflate(R.menu.video_options_menu, popupMenu.getMenu());
		Menu menu = popupMenu.getMenu();

		new IsVideoBookmarkedTask(youTubeVideo, menu).executeInParallel();
		new IsVideoPrivateListedTask(youTubeVideo, menu, null,videoCategory).executeInParallel();

		// If playback history is not disabled, see if this video has been watched. Otherwise, hide the "mark watched" & "mark unwatched" options from the menu.
		if(!SafetoonsApp.getPreferenceManager().getBoolean(context.getString(R.string.pref_key_disable_playback_status), false)) {
			new IsVideoWatchedTask(youTubeVideo, menu).executeInParallel();
		} else {
			//popupMenu.getMenu().findItem(R.id.mark_watched).setVisible(false);
			//popupMenu.getMenu().findItem(R.id.mark_unwatched).setVisible(false);
		}

		if(youTubeVideo.isDownloaded()) {
			popupMenu.getMenu().findItem(R.id.delete_download).setVisible(true);
			//popupMenu.getMenu().findItem(R.id.download_video).setVisible(false);
		} else {
			popupMenu.getMenu().findItem(R.id.delete_download).setVisible(false);
		}
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch(item.getItemId()) {
					case R.id.delete_download:
						youTubeVideo.removeDownload();
						return true;
/*
					case R.id.download_video:
						final boolean warningDialogDisplayed = new MobileNetworkWarningDialog(view.getContext())
								.onPositive(new MaterialDialog.SingleButtonCallback() {
									@Override
									public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
										youTubeVideo.downloadVideo(context);
									}
								})
								.showAndGetStatus(MobileNetworkWarningDialog.ActionType.DOWNLOAD_VIDEO);

						if (!warningDialogDisplayed) {
							youTubeVideo.downloadVideo(context);
						}
						return true;
*/
                    case R.id.bookmark_video:
                        youTubeVideo.bookmarkVideo(context, popupMenu.getMenu());
                        return true;
                    case R.id.unbookmark_video:
                        youTubeVideo.unbookmarkVideo(context, popupMenu.getMenu());
                        return true;

					case R.id.report_video:
						reportVideo(view.getContext());
						return true;
					case R.id.recommend_video:
						recommendVideo(view.getContext());
						return true;
//					case R.id.block_channel:
//						youTubeVideo.getChannel().blockChannel();

//						return true;
					case R.id.private_list_video:
						youTubeVideo.privateListVideo(context, popupMenu.getMenu());
						//updateViewsData();
						return true;

					case R.id.unprivate_list_video:
						youTubeVideo.unPrivateListVideo(context, popupMenu.getMenu(), searchQuery);
						//updateViewsData();
						return true;
				}
				return false;
			}
		});
		popupMenu.show();
	}

	/**
	 * Interface to alert a listener that this GridViewHolder has been clicked.
	 */
	public interface GridViewHolderListener {
		void onClick();
	}

	private GridViewHolderListener gridViewHolderListener;

	public void setGridViewHolderListener(GridViewHolderListener gridViewHolderListener) {
		this.gridViewHolderListener = gridViewHolderListener;
	}

	private void recommendVideo(final Context context) {
		new SafetoonsMaterialDialog(context)
				.autoDismiss(true)
				.title(R.string.recommend_video)
				.content(R.string.recommend_video_message)
				.inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE)
				.positiveText(R.string.recommend_video)
				.input(null, null, true, new MaterialDialog.InputCallback() {
					@Override
					public void onInput(@NonNull MaterialDialog dialog, CharSequence recommendComment) {

						DatabaseReference dbRecommendedRef = mDatabase.child(Constants.FIREBASE_DATABASE_TABLE_RECOMMENDED_VIDEOS);

						String key = dbRecommendedRef.push().getKey();

						String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

						// TODO add recommnder
						VideoRecommend recommend = new VideoRecommend(key, youTubeVideo.getVideoUrl(), "", recommendComment.toString(), date);

						dbRecommendedRef.child(key).setValue(recommend);

						Toast.makeText(context, R.string.video_recommended_msg, Toast.LENGTH_LONG).show();
					}
				})
				.show();
	}



	private void reportVideo(final Context context) {
		new SafetoonsMaterialDialog(context)
				.autoDismiss(true)
				.title(R.string.report_video)
				.content(R.string.report_video_message)
				.inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE)
				.positiveText(R.string.report_video)
				.input(null, null, false, new MaterialDialog.InputCallback() {
					@Override
					public void onInput(@NonNull MaterialDialog dialog, CharSequence reportReason) {

						DatabaseReference dbReportedRef = mDatabase.child(Constants.FIREBASE_DATABASE_TABLE_REPORTED_VIDEOS);

						String key = dbReportedRef.push().getKey();

						String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

						// TODO add reporter
						VideoReport report = new VideoReport(key, youTubeVideo.getVideoUrl(), "", reportReason.toString(), date);

						dbReportedRef.child(key).setValue(report);

						Toast.makeText(context, R.string.video_reported_msg, Toast.LENGTH_LONG).show();
					}
				})
				.show();

	}
}
