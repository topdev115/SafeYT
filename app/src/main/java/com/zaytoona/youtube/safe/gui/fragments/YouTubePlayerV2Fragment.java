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

package com.zaytoona.youtube.safe.gui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Locale;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.AsyncTaskParallel;
import com.zaytoona.youtube.safe.businessobjects.Logger;
import com.zaytoona.youtube.safe.businessobjects.YouTube.GetVideosDetailsByIDs;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannelInterface;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeVideo;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetVideoDescriptionTask;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetYouTubeChannelInfoTask;
import com.zaytoona.youtube.safe.businessobjects.YouTube.VideoStream.StreamMetaData;
import com.zaytoona.youtube.safe.businessobjects.db.DownloadedVideosDb;
import com.zaytoona.youtube.safe.businessobjects.db.PlaybackStatusDb;
import com.zaytoona.youtube.safe.businessobjects.db.Tasks.CheckIfUserSubbedToChannelTask;
import com.zaytoona.youtube.safe.businessobjects.db.Tasks.IsVideoBookmarkedTask;
import com.zaytoona.youtube.safe.businessobjects.interfaces.GetDesiredStreamListener;
import com.zaytoona.youtube.safe.businessobjects.interfaces.YouTubePlayerFragmentInterface;
import com.zaytoona.youtube.safe.businessobjects.periods.ViewingManager;
import com.zaytoona.youtube.safe.gui.activities.MainActivity;
import com.zaytoona.youtube.safe.gui.activities.ThumbnailViewerActivity;
import com.zaytoona.youtube.safe.gui.businessobjects.MobileNetworkWarningDialog;
import com.zaytoona.youtube.safe.gui.businessobjects.PlayerViewGestureDetector;
import com.zaytoona.youtube.safe.gui.businessobjects.ResumeVideoTask;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsMaterialDialog;
import com.zaytoona.youtube.safe.gui.businessobjects.fragments.ImmersiveModeFragment;

import static com.zaytoona.youtube.safe.gui.activities.YouTubePlayerActivity.YOUTUBE_VIDEO_OBJ;

/**
 * A fragment that holds a standalone YouTube player (version 2).
 */
public class YouTubePlayerV2Fragment extends ImmersiveModeFragment implements YouTubePlayerFragmentInterface {

	private YouTubeVideo		    youTubeVideo = null;
	private YouTubeChannel          youTubeChannel = null;

	private PlayerView              playerView;
	private SimpleExoPlayer         player;
	private long				    playerInitialPosition = 0;
	private View				    loadingVideoView = null;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		hideNavigationBar();

		// inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_youtube_player_v2, container, false);

		// prevent the device from sleeping while playing
		if (getActivity() != null  &&  (getActivity().getWindow()) != null) {
			getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

//		final View decorView = getActivity().getWindow().getDecorView();
//		decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
//			@Override
//			public void onSystemUiVisibilityChange(int visibility) {
//				hideNavigationBar();
//			}
//		});

		///if (savedInstanceState != null)
		///	videoCurrentPosition = savedInstanceState.getInt(VIDEO_CURRENT_POSITION, 0);

		if (youTubeVideo == null) {
			// initialise the views
			initViews(view);

			// get which video we need to play...
			Bundle bundle = getActivity().getIntent().getExtras();
			if (bundle != null  &&  bundle.getSerializable(YOUTUBE_VIDEO_OBJ) != null) {
				// ... either the video details are passed through the previous activity
				youTubeVideo = (YouTubeVideo) bundle.getSerializable(YOUTUBE_VIDEO_OBJ);
				setUpHUDAndPlayVideo();

			} else {
				// ... or the video URL is passed to Safetoons via another Android app
				new GetVideoDetailsTask().executeInParallel();
			}

		}


		return view;
	}


	/**
	 * Initialise the views.
	 *
	 * @param view Fragment view.
	 */
	private void initViews(View view) {
		// setup the toolbar / actionbar
		Toolbar toolbar = view.findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		// setup the player
		playerView = view.findViewById(R.id.player_view);
		final PlayerViewGestureHandler playerViewGestureHandler = new PlayerViewGestureHandler();
		playerViewGestureHandler.initView(view);
		playerView.setOnTouchListener(playerViewGestureHandler);
		playerView.requestFocus();

		DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
		DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

		player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);
		player.setPlayWhenReady(true);
		playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);               // ensure that videos are played in their correct aspect ratio
		player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);    // ensure that videos are played in their correct aspect ratio
		playerView.setPlayer(player);

		loadingVideoView = view.findViewById(R.id.loadingVideoView);

	}


	/**
	 * Will setup the HUD's details according to the contents of {@link #youTubeVideo}.  Then it
	 * will try to load and play the video.
	 */
	private void setUpHUDAndPlayVideo() {
		getSupportActionBar().setTitle(youTubeVideo.getTitle());

        new ResumeVideoTask(getContext(), youTubeVideo, new ResumeVideoTask.Callback() {
            @Override
            public void loadVideo(int position) {
                playerInitialPosition = position;
                YouTubePlayerV2Fragment.this.loadVideo();
            }
        }).ask();

	}


	/**
	 * Loads the video specified in {@link #youTubeVideo}.
	 */
	private void loadVideo() {
		loadVideo(false);
	}


	/**
	 * Loads the video specified in {@link #youTubeVideo}.
	 *
	 * @param skipMobileNetworkWarning Set to true to skip the warning displayed when the user is
	 *                                 using mobile network data (i.e. 4g).
	 */
	private void loadVideo(boolean skipMobileNetworkWarning) {
		boolean mobileNetworkWarningDialogDisplayed = false;

		// if the user is using mobile network (i.e. 4g), then warn him
		if (!skipMobileNetworkWarning) {
			mobileNetworkWarningDialogDisplayed = new MobileNetworkWarningDialog(getActivity())
					.onPositive(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							loadVideo(true);
						}
					})
					.onNegative(new MaterialDialog.SingleButtonCallback() {
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
							closeActivity();
						}
					})
					.showAndGetStatus(MobileNetworkWarningDialog.ActionType.STREAM_VIDEO);
		}

		if (!mobileNetworkWarningDialogDisplayed) {
			// if the video is NOT live
			if (!youTubeVideo.isLiveStream()) {
				loadingVideoView.setVisibility(View.VISIBLE);

				if (youTubeVideo.isDownloaded()) {
					Uri uri = youTubeVideo.getFileUri();
					File file = new File(uri.getPath());
					// If the file for this video has gone missing, remove it from the Database and then play remotely.
					if (!file.exists()) {
						DownloadedVideosDb.getVideoDownloadsDb().remove(youTubeVideo);
						Toast.makeText(getContext(),
								getString(R.string.playing_video_file_missing),
								Toast.LENGTH_LONG).show();
						loadVideo();
					} else {
						loadingVideoView.setVisibility(View.GONE);

						Logger.i(this, ">> PLAYING LOCALLY: %s", uri);
						playVideo(uri);
					}
				} else {
					youTubeVideo.getDesiredStream(new GetDesiredStreamListener() {
						@Override
						public void onGetDesiredStream(StreamMetaData desiredStream) {
							// hide the loading video view (progress bar)
							loadingVideoView.setVisibility(View.GONE);

							// Play the video.  Check if this fragment is visible before playing the
							// video.  It might not be visible if the user clicked on the back button
							// before the video streams are retrieved (such action would cause the app
							// to crash if not catered for...).
							if (isVisible()) {
								Logger.i(YouTubePlayerV2Fragment.this, ">> PLAYING: %s", desiredStream.getUri());
								playVideo(desiredStream.getUri());
							}
						}

						@Override
						public void onGetDesiredStreamError(String errorMessage) {
							if (errorMessage != null) {
								new SafetoonsMaterialDialog(getContext())
										.content(errorMessage)
										.title(R.string.error_video_play)
										.cancelable(false)
										.onPositive(new MaterialDialog.SingleButtonCallback() {
											@Override
											public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
												closeActivity();
											}
										})
										.show();
							}
						}
					});
				}
			} else {    // else, if the video is a LIVE STREAM
				// video is live:  ask the user if he wants to play the video using an other app
				new SafetoonsMaterialDialog(getContext())
						.content(R.string.warning_live_video)
						.title(R.string.error_video_play)
						.onNegative(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								closeActivity();
							}
						})
						.onPositive(new MaterialDialog.SingleButtonCallback() {
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								youTubeVideo.playVideoExternally(getContext());
								closeActivity();
							}
						})
						.show();
			}
		}
	}


	/**
	 * Play video.
	 *
	 * @param videoUri  The Uri of the video that is going to be played.
	 */
	private void playVideo(Uri videoUri) {
		DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getContext(), "ST. Agent", new DefaultBandwidthMeter());
		ExtractorMediaSource.Factory extMediaSourceFactory = new ExtractorMediaSource.Factory(dataSourceFactory);
		ExtractorMediaSource mediaSource = extMediaSourceFactory.createMediaSource(videoUri);
		player.prepare(mediaSource);

		if (playerInitialPosition > 0)
			player.seekTo(playerInitialPosition);
	}

	@Override
	public void videoPlaybackStopped() {
		player.stop();

		playerView.setPlayer(null);
		if(!SafetoonsApp.getPreferenceManager().getBoolean(getString(R.string.pref_key_disable_playback_status), false)) {
			PlaybackStatusDb.getVideoDownloadsDb().setVideoPosition(youTubeVideo, player.getCurrentPosition());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// stop the player from playing (when this fragment is going to be destroyed) and clean up

		if(player != null) {
			player.stop();
			player = null;
		}

		if(playerView != null) {
			playerView.setPlayer(null);
		}

	}


	////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * This task will, from the given video URL, get the details of the video (e.g. video name,
	 * likes ...etc).
	 */
	private class GetVideoDetailsTask extends AsyncTaskParallel<Void, Void, YouTubeVideo> {

		private String videoUrl = null;


		@Override
		protected void onPreExecute() {
			String url = getUrlFromIntent(getActivity().getIntent());

			try {
				// YouTube sends subscriptions updates email in which its videos' URL are encoded...
				// Hence we need to decode them first...
				videoUrl = URLDecoder.decode(url, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				Logger.e(this, "UnsupportedEncodingException on " + videoUrl + " encoding = UTF-8", e);
				videoUrl = url;
			}
		}


		/**
		 * The video URL is passed to Safetoons via another Android app (i.e. via an intent).
		 *
		 * @return The URL of the YouTube video the user wants to play.
		 */
		private String getUrlFromIntent(final Intent intent) {
			String url = null;

			if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
				url = intent.getData().toString();
			}

			return url;
		}


		/**
		 * Returns an instance of {@link YouTubeVideo} from the given {@link #videoUrl}.
		 *
		 * @return {@link YouTubeVideo}; null if an error has occurred.
		 */
		@Override
		protected YouTubeVideo doInBackground(Void... params) {
			String videoId = YouTubeVideo.getYouTubeIdFromUrl(videoUrl);
			YouTubeVideo youTubeVideo = null;

			if (videoId != null) {
				try {
					GetVideosDetailsByIDs getVideo = new GetVideosDetailsByIDs();
					getVideo.init(videoId);
					List<YouTubeVideo> youTubeVideos = getVideo.getNextVideos();

					if (youTubeVideos.size() > 0)
						youTubeVideo = youTubeVideos.get(0);
				} catch (IOException ex) {
					Logger.e(this, "Unable to get video details, where id=" + videoId, ex);
				}
			}

			return youTubeVideo;
		}


		@Override
		protected void onPostExecute(YouTubeVideo youTubeVideo) {
			if (youTubeVideo == null) {
				// invalid URL error (i.e. we are unable to decode the URL)
				String err = String.format(getString(R.string.error_invalid_url), videoUrl);
				Toast.makeText(getActivity(), err, Toast.LENGTH_LONG).show();

				// log error
				Logger.e(this, err);

				// close the video player activity
				closeActivity();
			} else {
				YouTubePlayerV2Fragment.this.youTubeVideo = youTubeVideo;

				// setup the HUD and play the video
				setUpHUDAndPlayVideo();

				//getVideoInfoTasks();
			}
		}
	}



	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This will handle any gesture swipe event performed by the user on the player view.
	 */
	private class PlayerViewGestureHandler extends PlayerViewGestureDetector {

		private ImageView           indicatorImageView = null;
		private TextView            indicatorTextView = null;
		private RelativeLayout      indicatorView = null;

		private boolean             isControllerVisible = true;
		private VideoBrightness     videoBrightness;
		private float               startVolumePercent = -1.0f;
		private long                startVideoTime = -1;

		/** Enable/Disable video gestures based on user preferences. */
		//private final boolean       disableGestures = SafetoonsApp.getPreferenceManager().getBoolean(SafetoonsApp.getStr(R.string.pref_key_disable_screen_gestures), false);
		private final boolean       disableGestures = true;

		private static final int    MAX_VIDEO_STEP_TIME = 60 * 1000;


		PlayerViewGestureHandler() {
			super(getContext());

			videoBrightness = new VideoBrightness(getActivity(), disableGestures);
			playerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
				@Override
				public void onVisibilityChange(int visibility) {
					isControllerVisible = (visibility == View.VISIBLE);
				}
			});
		}


		void initView(View view) {
			indicatorView = view.findViewById(R.id.indicatorView);
			indicatorImageView = view.findViewById(R.id.indicatorImageView);
			indicatorTextView = view.findViewById(R.id.indicatorTextView);
		}


		@Override
		public void onCommentsGesture() {
		}


		@Override
		public void onVideoDescriptionGesture() {
		}


		@Override
		public void onDoubleTap() {
			// if the user is playing a video...
			if (player.getPlayWhenReady()) {
				// pause video
				player.setPlayWhenReady(false);
				player.getPlaybackState();
			} else {
				// play video
				player.setPlayWhenReady(true);
				player.getPlaybackState();
			}

			playerView.hideController();
		}


		@Override
		public boolean onSingleTap() {
			return showOrHideHud();
		}


		/**
		 * Hide or display the HUD depending if the HUD is currently visible or not.
		 */
		private boolean showOrHideHud() {
			if (isControllerVisible) {
				playerView.hideController();
				hideNavigationBar();
			} else {
				playerView.showController();
			}

			return false;
		}


		@Override
		public void onGestureDone() {
			videoBrightness.onGestureDone();
			startVolumePercent = -1.0f;
			startVideoTime = -1;
			hideIndicator();
		}


		@Override
		public void adjustBrightness(double adjustPercent) {
			if (disableGestures) {
				return;
			}

			// adjust the video's brightness
			videoBrightness.setVideoBrightness(adjustPercent, getActivity());

			// set indicator
			indicatorImageView.setImageResource(R.drawable.ic_brightness);
			indicatorTextView.setText(videoBrightness.getBrightnessString());

			// Show indicator. It will be hidden once onGestureDone will be called
			showIndicator();
		}


		@Override
		public void adjustVolumeLevel(double adjustPercent) {
			if (disableGestures) {
				return;
			}

			// We are setting volume percent to a value that should be from -1.0 to 1.0. We need to limit it here for these values first
			if (adjustPercent < -1.0f) {
				adjustPercent = -1.0f;
			} else if (adjustPercent > 1.0f) {
				adjustPercent = 1.0f;
			}

			AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
			final int STREAM = AudioManager.STREAM_MUSIC;

			// Max volume will return INDEX of volume not the percent. For example, on my device it is 15
			int maxVolume = audioManager.getStreamMaxVolume(STREAM);
			if (maxVolume == 0) return;

			if (startVolumePercent < 0) {
				// We are getting actual volume index (NOT volume but index). It will be >= 0.
				int curVolume = audioManager.getStreamVolume(STREAM);
				// And counting percents of maximum volume we have now
				startVolumePercent = curVolume * 1.0f / maxVolume;
			}
			// Should be >= 0 and <= 1
			double targetPercent = startVolumePercent + adjustPercent;
			if (targetPercent > 1.0f) {
				targetPercent = 1.0f;
			} else if (targetPercent < 0) {
				targetPercent = 0;
			}

			// Calculating index. Test values are 15 * 0.12 = 1 ( because it's int)
			int index = (int) (maxVolume * targetPercent);
			if (index > maxVolume) {
				index = maxVolume;
			} else if (index < 0) {
				index = 0;
			}
			audioManager.setStreamVolume(STREAM, index, 0);

			indicatorImageView.setImageResource(R.drawable.ic_volume);
			indicatorTextView.setText(index * 100 / maxVolume + "%");

			// Show indicator. It will be hidden once onGestureDone will be called
			showIndicator();
		}

		@Override
		public void adjustVideoPosition(double adjustPercent, boolean forwardDirection) {
			if (disableGestures) {
				return;
			}

			long totalTime = player.getDuration();

			if (adjustPercent < -1.0f) {
				adjustPercent = -1.0f;
			} else if (adjustPercent > 1.0f) {
				adjustPercent = 1.0f;
			}

			if (startVideoTime < 0) {
				startVideoTime = player.getCurrentPosition();
			}
			// adjustPercent: value from -1 to 1.
			double positiveAdjustPercent = Math.max(adjustPercent, -adjustPercent);
			// End of line makes seek speed not linear
			long targetTime = startVideoTime + (long) (MAX_VIDEO_STEP_TIME * adjustPercent * (positiveAdjustPercent / 0.1));
			if (targetTime > totalTime) {
				targetTime = totalTime;
			}
			if (targetTime < 0) {
				targetTime = 0;
			}

			String targetTimeString = formatDuration(targetTime / 1000);

			if (forwardDirection) {
				indicatorImageView.setImageResource(R.drawable.ic_forward);
				indicatorTextView.setText(targetTimeString);
			} else {
				indicatorImageView.setImageResource(R.drawable.ic_rewind);
				indicatorTextView.setText(targetTimeString);
			}

			showIndicator();

			player.seekTo(targetTime);
		}


		@Override
		public Rect getPlayerViewRect() {
			return new Rect(playerView.getLeft(), playerView.getTop(), playerView.getRight(), playerView.getBottom());
		}


		private void showIndicator() {
			indicatorView.setVisibility(View.VISIBLE);
		}


		private void hideIndicator() {
			indicatorView.setVisibility(View.GONE);
		}


		/**
		 * Returns a (localized) string for the given duration (in seconds).
		 *
		 * @param duration
		 * @return  a (localized) string for the given duration (in seconds).
		 */
		private String formatDuration(long duration) {
			long    h = duration / 3600;
			long    m = (duration - h * 3600) / 60;
			long    s = duration - (h * 3600 + m * 60);
			String  durationValue;

			if (h == 0) {
				durationValue = String.format(Locale.getDefault(),"%1$02d:%2$02d", m, s);
			} else {
				durationValue = String.format(Locale.getDefault(),"%1$d:%2$02d:%3$02d", h, m, s);
			}

			return durationValue;
		}

	}



	////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Adjust video's brightness.  Once the brightness is adjust, it is saved in the preferences to
	 * be used when a new video is played.
	 */
	private static class VideoBrightness {

		/** Current video brightness. */
		private float   brightness;
		/** Initial video brightness. */
		private float   initialBrightness;
		private final boolean disableGestures;

		private static final String BRIGHTNESS_LEVEL_PREF = SafetoonsApp.getStr(R.string.pref_key_brightness_level);


		/**
		 * Constructor:  load the previously saved video brightness from the preference and set it.
		 *
		 * @param activity  Activity.
		 */
		public VideoBrightness(final Activity activity, final boolean disableGestures) {
			loadBrightnessFromPreference();
			initialBrightness = brightness;
			this.disableGestures = disableGestures;

			setVideoBrightness(0, activity);
		}


		/**
		 * Set the video brightness.  Once the video brightness is updated, save it in the preference.
		 *
		 * @param adjustPercent Percentage.
		 * @param activity      Activity.
		 */
		public void setVideoBrightness(double adjustPercent, final Activity activity) {
			if (disableGestures) {
				return;
			}

			// We are setting brightness percent to a value that should be from -1.0 to 1.0. We need to limit it here for these values first
			if (adjustPercent < -1.0f) {
				adjustPercent = -1.0f;
			} else if (adjustPercent > 1.0f) {
				adjustPercent = 1.0f;
			}

			// set the brightness instance variable
			setBrightness(initialBrightness + (float) adjustPercent);
			// adjust the video brightness as per this.brightness
			adjustVideoBrightness(activity);
			// save brightness to the preference
			saveBrightnessToPreference();
		}


		/**
		 * Adjust the video brightness.
		 *
		 * @param activity  Current activity.
		 */
		private void adjustVideoBrightness(final Activity activity) {
			WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
			lp.screenBrightness = brightness;
			activity.getWindow().setAttributes(lp);
		}


		/**
		 * Saves {@link #brightness} to preference.
		 */
		private void saveBrightnessToPreference() {
			SharedPreferences.Editor editor = SafetoonsApp.getPreferenceManager().edit();
			editor.putFloat(BRIGHTNESS_LEVEL_PREF, brightness);
			editor.apply();
			Logger.d(this, "BRIGHTNESS: %f", brightness);
		}


		/**
		 * Loads the brightness from preference and set the {@link #brightness} instance variable.
		 */
		private void loadBrightnessFromPreference() {
			final float brightnessPref = SafetoonsApp.getPreferenceManager().getFloat(BRIGHTNESS_LEVEL_PREF, 1);
			setBrightness(brightnessPref);
		}


		/**
		 * Set the {@link #brightness} instance variable.
		 *
		 * @param brightness    Brightness (from 0.0 to 1.0).
		 */
		private void setBrightness(float brightness) {
			if (brightness < 0) {
				brightness = 0;
			} else if (brightness > 1) {
				brightness = 1;
			}

			this.brightness = brightness;
		}


		/**
		 * @return Brightness as string:  e.g. "21%"
		 */
		public String getBrightnessString() {
			return ((int) (brightness * 100)) + "%";
		}


		/**
		 * To be called once the swipe gesture is done/completed.
		 */
		public void onGestureDone() {
			initialBrightness = brightness;
		}

	}

}
