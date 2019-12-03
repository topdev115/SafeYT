/*
 * Safetoons
 * Copyright (C) 2015  Ramon Mifsud
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

package com.zaytoona.youtube.safe.gui.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.support.v7.widget.ShareActionProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zaytoona.pincode.PinCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import hotchemi.android.rate.AppRate;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePlaylist;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePublicPlayList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.VideoBlocker;
import com.zaytoona.youtube.safe.businessobjects.categories.SafetoonsCategory;
import com.zaytoona.youtube.safe.businessobjects.db.DownloadedVideosDb;
import com.zaytoona.youtube.safe.businessobjects.db.PrivateListsDb;
import com.zaytoona.youtube.safe.businessobjects.db.PublicPlayListsDb;
import com.zaytoona.youtube.safe.businessobjects.db.SafetoonsCategoriesDb;
//import com.zaytoona.youtube.safe.businessobjects.db.SearchHistoryDb;
//import com.zaytoona.youtube.safe.businessobjects.db.SearchHistoryTable;
//import com.zaytoona.youtube.safe.businessobjects.db.SubscriptionsDb;
import com.zaytoona.youtube.safe.businessobjects.firebase.MultiCheckPlayList;
//import com.zaytoona.youtube.safe.businessobjects.interfaces.SearchHistoryClickListener;
import com.zaytoona.youtube.safe.common.Constants;
import com.zaytoona.youtube.safe.gui.businessobjects.BlockedVideosDialog;
import com.zaytoona.youtube.safe.gui.businessobjects.MainActivityListener;
//import com.zaytoona.youtube.safe.gui.businessobjects.adapters.SearchHistoryCursorAdapter;
import com.zaytoona.youtube.safe.gui.businessobjects.updates.UpdatesCheckerTask;
import com.zaytoona.youtube.safe.gui.fragments.ChannelBrowserFragment;
import com.zaytoona.youtube.safe.gui.fragments.MainFragment;
import com.zaytoona.youtube.safe.gui.fragments.PlaylistVideosFragment;
import com.zaytoona.youtube.safe.gui.fragments.SafetoonsPlaylistVideosFragment;
//import com.zaytoona.youtube.safe.gui.fragments.SearchVideoGridFragment;
import com.zaytoona.youtube.safe.gui.fragments.preferences.GeneralPreferenceFragment;

/**
 * Main activity (launcher).  This activity holds {@link com.zaytoona.youtube.safe.gui.fragments.VideosGridFragment}.
 */
public class MainActivity extends PinCompatActivity implements MainActivityListener, PrivateListsDb.PrivateListsDbListener, PublicPlayListsDb.PublicPlayListsDbListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	@BindView(R.id.fragment_container)
	protected FrameLayout           fragmentContainer;

	private MainFragment            mainFragment;
//	private SearchVideoGridFragment searchVideoGridFragment;
	private ChannelBrowserFragment  channelBrowserFragment;
	/** Fragment that shows Videos from a specific Playlist */
	private PlaylistVideosFragment  playlistVideosFragment;
	private SafetoonsPlaylistVideosFragment  safetoonsPlaylistVideosFragment;
//	private VideoBlockerPlugin      videoBlockerPlugin;

	private DatabaseReference mDatabase;

	private boolean dontAddToBackStack = false;

	private boolean kidActivityStarted = false;

	/** Set to true of the UpdatesCheckerTask has run; false otherwise. */
	private static boolean updatesCheckerTaskRan = false;

	public static final String ACTION_VIEW_CHANNEL = "MainActivity.ViewChannel";
	public static final String ACTION_VIEW_FEED = "MainActivity.ViewFeed";
	public static final String ACTION_VIEW_PLAYLIST = "MainActivity.ViewPlaylist";
	private static final String MAIN_FRAGMENT   = "MainActivity.MainFragment";
//	private static final String SEARCH_FRAGMENT = "MainActivity.SearchFragment";
	private static final String CHANNEL_BROWSER_FRAGMENT = "MainActivity.ChannelBrowserFragment";
	private static final String PLAYLIST_VIDEOS_FRAGMENT = "MainActivity.PlaylistVideosFragment";
	private static final String SAFETOONS_PLAYLIST_VIDEOS_FRAGMENT = "MainActivity.SafetoonsPlaylistVideosFragment";
//	private static final String VIDEO_BLOCKER_PLUGIN = "MainActivity.VideoBlockerPlugin";

	// Public play lists downloaded at firt run after install
	private static final String INITIAL_PUBLIC_PLAY_LISTS_DOWNLOADED = "MainActivity.InitialPublicPlayListsDownloaded";

	// Share the app
	private ShareActionProvider mShareActionProvider;
	private String mShareSubject = null;
	private String mShareBody = null;
	private String mShareLink = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check for updates (one time only)
		if (!updatesCheckerTaskRan) {
			new UpdatesCheckerTask(this, false).executeInParallel();
			updatesCheckerTaskRan = true;
		}

		SafetoonsApp.setFeedUpdateInterval();
		// Delete any missing downloaded videos
		//new DownloadedVideosDb.RemoveMissingVideosTask().executeInParallel();

		mDatabase = FirebaseDatabase.getInstance().getReference();

		setContentView(R.layout.activity_fragment_holder);
		ButterKnife.bind(this);

		if (fragmentContainer != null) {
			if (savedInstanceState != null) {
				mainFragment = (MainFragment) getSupportFragmentManager().getFragment(savedInstanceState, MAIN_FRAGMENT);
//				searchVideoGridFragment = (SearchVideoGridFragment) getSupportFragmentManager().getFragment(savedInstanceState, SEARCH_FRAGMENT);
				channelBrowserFragment = (ChannelBrowserFragment) getSupportFragmentManager().getFragment(savedInstanceState, CHANNEL_BROWSER_FRAGMENT);
				playlistVideosFragment = (PlaylistVideosFragment) getSupportFragmentManager().getFragment(savedInstanceState, PLAYLIST_VIDEOS_FRAGMENT);
				safetoonsPlaylistVideosFragment = (SafetoonsPlaylistVideosFragment) getSupportFragmentManager().getFragment(savedInstanceState, SAFETOONS_PLAYLIST_VIDEOS_FRAGMENT);

				// Because other fragments are pointing to the old activity instance
				// Make them look at the new activity instance
				if (mainFragment != null) {
					mainFragment.updateMainActivityListeners();
				}
			}

			// If this Activity was called to view a particular channel, display that channel via ChannelBrowserFragment, instead of MainFragment
			String action = getIntent().getAction();
			if (ACTION_VIEW_CHANNEL.equals(action)) {
				dontAddToBackStack = true;
				YouTubeChannel channel = (YouTubeChannel) getIntent().getSerializableExtra(ChannelBrowserFragment.CHANNEL_OBJ);
				onChannelClick(channel);
			} else if (ACTION_VIEW_PLAYLIST.equals(action)) {
				dontAddToBackStack = true;
				YouTubePlaylist playlist = (YouTubePlaylist) getIntent().getSerializableExtra(PlaylistVideosFragment.PLAYLIST_OBJ);
				onPlaylistClick(playlist);
			} else if (ACTION_VIEW_PLAYLIST.equals(action)) { // Something wrong???
				dontAddToBackStack = true;
				SafetoonsList playlist = (SafetoonsList) getIntent().getSerializableExtra(SafetoonsPlaylistVideosFragment.PLAYLIST_OBJ);
				onSafetoonsListClick(playlist);
			} else {
				if (mainFragment == null) {
					mainFragment = new MainFragment();
					// If we're coming here via a click on the Notification that new videos for subscribed channels have been found, make sure to
					// select the Feed tab.
					if (action != null && action.equals(ACTION_VIEW_FEED)) {
						Bundle args = new Bundle();
						args.putBoolean(MainFragment.SHOULD_SELECTED_FEED_TAB, true);
						mainFragment.setArguments(args);
					}
					getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mainFragment).commit();
				}


				PrivateListsDb.getPrivateListsDb().addListener(this);
				PublicPlayListsDb.getPublicPlayListsDb().addListener(this);
			}
		}

		/*
		if (savedInstanceState != null) {
			// restore the video blocker plugin
			this.videoBlockerPlugin = (VideoBlockerPlugin) savedInstanceState.getSerializable(VIDEO_BLOCKER_PLUGIN);
			this.videoBlockerPlugin.setActivity(this);
		} else {
			this.videoBlockerPlugin = new VideoBlockerPlugin(this);
		}
		*/


		if(wasInitialPublicPlayListsDownloaded() == false) {
			final DatabaseReference mDbPublicPlayListsRef = mDatabase.child(Constants.FIREBASE_DATABASE_TABLE_PUBLIC_PLAY_LISTS);

			// Remove Public Play Lists which are not found on Safetoons Server anymore
			ValueEventListener publicPlayListsListener = new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {

					if (dataSnapshot != null) {

						for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {

							String categoryTitle = snapshot.getKey();

							String categoryId = categoryTitle.substring(0, 2);

							// Category title after removing the Id above
							categoryTitle = categoryTitle.substring(5);

							String description = null;

							if (snapshot.child("description").getValue() != null) {
								description = snapshot.child("description").getValue().toString();
							}

							String avatarURL = null;

							if (snapshot.child("avatarURL").getValue() != null) {
								avatarURL = snapshot.child("avatarURL").getValue().toString();
							}

							boolean bCategoryCreated = false;

							for (DataSnapshot playListsSnapshot : snapshot.child(Constants.FIREBASE_DATABASE_TABLE_PUBLIC_PLAY_LISTS_PUBLIC_LISTS).getChildren()) {

								String publicPlayListsKey = playListsSnapshot.getKey();

								if (bCategoryCreated == false) {

									// Create the category if not exists
									SafetoonsCategoriesDb.getSafetoonsCategoriesDb().addOrUpdate(new SafetoonsCategory(categoryId, categoryTitle, description, avatarURL));

									bCategoryCreated = true;
								}

								String name = playListsSnapshot.child("title").getValue().toString();

								int orderIdInt = Integer.MAX_VALUE;

								if (name.length() > 5) {

									// List orderId, first two numerical characters
									String orderId = name.substring(0, 2);

									try {
										orderIdInt = Integer.parseInt(orderId);
										// List name after removing the orderId above
										name = name.substring(5);
									} catch (NumberFormatException | NullPointerException nfe) {
									}
								}

								YouTubePublicPlayList publicPlayList = new YouTubePublicPlayList(publicPlayListsKey, categoryId, name, orderIdInt);

								PublicPlayListsDb.getPublicPlayListsDb().addOrUpdate(publicPlayList);
							}

						}
						// Remove empty categories
						SafetoonsCategoriesDb.getSafetoonsCategoriesDb().clearEmptySafetoonsCategories();

						setWasInitialPublicPlayListsDownloaded(true);
					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {
					// Getting Post failed, log a message
					Log.w(TAG, "Initial Download Public Play Lists : onCancelled", databaseError.toException());
				}
			};

			mDbPublicPlayListsRef.addListenerForSingleValueEvent(publicPlayListsListener);
		}

		// Read values from settings
		mDatabase.child("/settings/").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

				if(dataSnapshot.exists()) {
					mShareSubject = dataSnapshot.child("shareSubject").getValue().toString();
					mShareBody = dataSnapshot.child("shareBody").getValue().toString();
					mShareLink = dataSnapshot.child("shareLink").getValue().toString();

					setShareIntent();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		AppRate.with(this)
				.setInstallDays(5)    // Number of days after install, default value is 10
				.setLaunchTimes(5)	  // Number of times the app should have been started, default value is 10
				.setRemindInterval(2) // How long to wait after user clicks on remind me later, default value is 1
				.monitor();

		AppRate.showRateDialogIfMeetsConditions(this);
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if(mainFragment != null)
			getSupportFragmentManager().putFragment(outState, MAIN_FRAGMENT, mainFragment);
//		if(searchVideoGridFragment != null && searchVideoGridFragment.isVisible())
//			getSupportFragmentManager().putFragment(outState, SEARCH_FRAGMENT, searchVideoGridFragment);
		if(channelBrowserFragment != null && channelBrowserFragment.isVisible())
			getSupportFragmentManager().putFragment(outState, CHANNEL_BROWSER_FRAGMENT, channelBrowserFragment);
		if(playlistVideosFragment != null && playlistVideosFragment.isVisible())
			getSupportFragmentManager().putFragment(outState, PLAYLIST_VIDEOS_FRAGMENT, playlistVideosFragment);
		if(safetoonsPlaylistVideosFragment != null && safetoonsPlaylistVideosFragment.isVisible())
			getSupportFragmentManager().putFragment(outState, SAFETOONS_PLAYLIST_VIDEOS_FRAGMENT, safetoonsPlaylistVideosFragment);
		// save the video blocker plugin
		//outState.putSerializable(VIDEO_BLOCKER_PLUGIN, videoBlockerPlugin);

	}


	@Override
	protected void onResume() {
		super.onResume();

		//Toast.makeText(this, "onResume", Toast.LENGTH_LONG).show();

		// Activity may be destroyed when the devices is rotated, so we need to make sure that the
		// channel play list is holding a reference to the activity being currently in use...
		if (channelBrowserFragment != null)
			channelBrowserFragment.getChannelPlaylistsFragment().setMainActivityListener(this);

		((SafetoonsApp) getApplication()).setKidMode(false);
	}

	@Override
	protected void onDestroy() {

		if(kidActivityStarted == false) {
			// This is when the back is pressed when changing the pin in the preferences
			startActivity(new Intent(this, KidMainActivity.class));
		}

		super.onDestroy();

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main_activity_menu, menu);

		// setup the video blocker notification icon which will be displayed in the tool bar
		//videoBlockerPlugin.setupIconForToolBar(menu);

		// setup the SearchView (actionbar)
//		final MenuItem searchItem = menu.findItem(R.id.menu_search);
//		final SearchView searchView = (SearchView) searchItem.getActionView();
//
//		searchView.setQueryHint(getString(R.string.search_videos));
//
//		SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
//		searchAutoComplete.setHintTextColor(getResources().getColor(android.R.color.black));
//		searchAutoComplete.setTextColor(getResources().getColor(android.R.color.black));
//
//		// set the query hints to be equal to the previously searched text
//		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//			@Override
//			public boolean onQueryTextChange(final String newText) {
//				// if the user does not want to have the search string saved, then skip the below...
//				if (SafetoonsApp.getPreferenceManager().getBoolean(getString(R.string.pref_key_disable_search_history), false)
//						||  newText == null  ||  newText.length() <= 1) {
//					return false;
//				}
//
//				SearchHistoryCursorAdapter searchHistoryCursorAdapter = (SearchHistoryCursorAdapter) searchView.getSuggestionsAdapter();
//				Cursor cursor = SearchHistoryDb.getSearchHistoryDb().getSearchCursor(newText);
//
//				// if the adapter has not been created, then create it
//				if (searchHistoryCursorAdapter == null) {
//					searchHistoryCursorAdapter = new SearchHistoryCursorAdapter(getBaseContext(),
//							R.layout.search_hint,
//							cursor,
//							new String[]{SearchHistoryTable.COL_SEARCH_TEXT},
//							new int[]{android.R.id.text1},
//							0);
//					searchHistoryCursorAdapter.setSearchHistoryClickListener(new SearchHistoryClickListener() {
//						@Override
//						public void onClick(String query) {
//							displaySearchResults(query, searchView);
//						}
//					});
//					searchView.setSuggestionsAdapter(searchHistoryCursorAdapter);
//				} else {
//					// else just change the cursor...
//					searchHistoryCursorAdapter.changeCursor(cursor);
//				}
//
//				// update the current search string
//				searchHistoryCursorAdapter.setSearchBarString(newText);
//
//				return true;
//			}
//
//			@Override
//			public boolean onQueryTextSubmit(String query) {
//				if(!SafetoonsApp.getPreferenceManager().getBoolean(SafetoonsApp.getStr(R.string.pref_key_disable_search_history), false)) {
//					// Save this search string into the Search History Database (for Suggestions)
//					SearchHistoryDb.getSearchHistoryDb().insertSearchText(query);
//				}
//
//				displaySearchResults(query, searchView);
//
//				return true;
//			}
//		});

		// ShareActionProvider
		final MenuItem shareItem = menu.findItem(R.id.menu_item_share);

		//shareItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		//shareItem.setIcon(R.drawable.abc_ic_menu_share_mtrl_alpha);

		// To remove the extra entry of the last used share method (extra icon for history)
		mShareActionProvider = new ShareActionProvider(this) {
			@Override
			public View onCreateActionView() {
				return null;
			}
		};

		MenuItemCompat.setActionProvider(shareItem, mShareActionProvider);

		setShareIntent();

		return true;
	}

	// Call to update the share intent
	private void setShareIntent() {

		Intent shareIntent = new Intent(Intent.ACTION_SEND);

		shareIntent.setType("text/plain");

		shareIntent.putExtra(Intent.EXTRA_SUBJECT, mShareSubject);
		shareIntent.putExtra(Intent.EXTRA_TEXT, mShareSubject + "\n\n" + mShareBody + "\n\n" + mShareLink);

		if (mShareActionProvider != null) {
			mShareActionProvider.setShareIntent(shareIntent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
//			case R.id.menu_blocker:
//				videoBlockerPlugin.onMenuBlockerIconClicked();
//				return true;
			case R.id.menu_preferences:
				//Intent i = new Intent(this, PreferencesActivity.class);

				Intent intent = new Intent( this, PreferencesActivity.class );
				intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, GeneralPreferenceFragment.class.getName() );
				intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );

				startActivity(intent);
				return true;
//			case R.id.menu_locker:
//				onBackPressed();
//				return true;
			case android.R.id.home:
				//if(mainFragment == null || !mainFragment.isVisible()) {
					onBackPressed();
					return true;
				//}
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

//	@Override
//	public void onPublicListsDbUpdated(String channelId) {
//
//		MainFragment mainFragmentNew = new MainFragment();
//
//		if(channelId != null) {
//			// Open the last added Public List
//			Bundle args = new Bundle();
//			args.putString(MainFragment.SHOULD_SELECT_PUBLIC_LIST_TAB, channelId);
//			mainFragmentNew.setArguments(args);
//		}
//
//		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mainFragmentNew).commitAllowingStateLoss();
//
//		mainFragment = mainFragmentNew;
//	}

	@Override
	public void onPrivateListsDbUpdated() {

		MainFragment mainFragmentNew = new MainFragment();

		// Open the last added Private List
		Bundle args = new Bundle();
		args.putBoolean(MainFragment.SHOULD_SELECT_LAST_PRIVATE_LIST_TAB, true);
		mainFragmentNew.setArguments(args);

		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mainFragmentNew).commitAllowingStateLoss();

		mainFragment = mainFragmentNew;
	}

	@Override
	public void onPrivateListItemsDbUpdated() {
		if(safetoonsPlaylistVideosFragment != null) {
			safetoonsPlaylistVideosFragment.onRefresh();
		}
	}

	@Override
	public void onPublicPlayListsDbUpdated() {
		MainFragment mainFragmentNew = new MainFragment();

		Log.w("SHADI", "onPublicPlayListsDbUpdated");

		// Open the last added Private List
		Bundle args = new Bundle();
		args.putBoolean(MainFragment.SHOULD_SELECT_LAST_PUBLIC_PLAY_LIST_TAB, true);
		mainFragmentNew.setArguments(args);

		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mainFragmentNew).commitAllowingStateLoss();

		mainFragment = mainFragmentNew;
	}

	/**
	 * Return the last item stored in the clipboard.
	 *
	 * @return	{@link String}
	 */
	private String getClipboardItem() {
		String              clipboardText    = "";
		ClipboardManager    clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

		// if the clipboard contain data ...
		if (clipboardManager != null  &&  clipboardManager.hasPrimaryClip()) {
			ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);

			// gets the clipboard as text.
			clipboardText = item.getText().toString();
		}

		return clipboardText;
	}


	@Override
	public void onBackPressed() {
		if (mainFragment != null  &&  mainFragment.isVisible()) {
			// If the Subscriptions Drawer is open, close it instead of minimizing the app.

			if(mainFragment.isDrawerOpen()) {
				mainFragment.closeDrawer();
			}
			else if(mainFragment.isPublicFragmentOpenedForPlayLists()) {
				mainFragment.openPublicFragmentForCategories();
			}
			else {
				// On Android, when the user presses back button, the Activity is destroyed and will be
				// recreated when the user relaunches the app.
				// We do not want that behaviour, instead then the back button is pressed, the app will
				// be **minimized**.
				//Intent startMain = new Intent(Intent.ACTION_MAIN);
				//startMain.addCategory(Intent.CATEGORY_HOME);
				//startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//startActivity(startMain);
				//finish();

				// This is when back is pressed when entering\creating the pin
				// We need to go back to kids
				startActivity(new Intent(this, KidMainActivity.class));

				// To avoid flickering
				kidActivityStarted = true;

				finish();
				//super.onBackPressed();
			}
		} else {
			super.onBackPressed();
		}
	}


	private void switchToFragment(Fragment fragment) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		transaction.replace(R.id.fragment_container, fragment);
		if(!dontAddToBackStack)
			transaction.addToBackStack(null);
		else
			dontAddToBackStack = false;
		transaction.commit();
	}


	@Override
	public void onChannelClick(YouTubeChannel channel) {
		Bundle args = new Bundle();
		args.putSerializable(ChannelBrowserFragment.CHANNEL_OBJ, channel);
		switchToChannelBrowserFragment(args);
	}


	@Override
	public void onChannelClick(String channelId) {
		Bundle args = new Bundle();
		args.putString(ChannelBrowserFragment.CHANNEL_ID, channelId);
		switchToChannelBrowserFragment(args);
	}


	private void switchToChannelBrowserFragment(Bundle args) {
		channelBrowserFragment = new ChannelBrowserFragment();
		channelBrowserFragment.getChannelPlaylistsFragment().setMainActivityListener(this);
		channelBrowserFragment.setArguments(args);
		switchToFragment(channelBrowserFragment);
	}


	@Override
	public void onPlaylistClick(YouTubePlaylist playlist) {
		playlistVideosFragment = new PlaylistVideosFragment();
		Bundle args = new Bundle();
		args.putSerializable(PlaylistVideosFragment.PLAYLIST_OBJ, playlist);
		playlistVideosFragment.setArguments(args);
		switchToFragment(playlistVideosFragment);
	}

	@Override
	public void onSafetoonsListClick(SafetoonsList playlist) {
		safetoonsPlaylistVideosFragment = new SafetoonsPlaylistVideosFragment();
		Bundle args = new Bundle();
		args.putSerializable(SafetoonsPlaylistVideosFragment.PLAYLIST_OBJ, playlist);
		safetoonsPlaylistVideosFragment.setArguments(args);
		safetoonsPlaylistVideosFragment.setDisplayMode(true);
		safetoonsPlaylistVideosFragment.setCategory(playlist.getCategory());
		switchToFragment(safetoonsPlaylistVideosFragment);
	}

	/**
	 * Hide the virtual keyboard and then switch to the Search Video Grid Fragment with the selected
	 * query to search for videos.
	 *
	 * @param query Query text submitted by the user.
	 */
//	private void displaySearchResults(String query, @NotNull final View searchView) {
//		// hide the keyboard
//		searchView.clearFocus();
//
//		// open SearchVideoGridFragment and display the results
//		searchVideoGridFragment = new SearchVideoGridFragment();
//		Bundle bundle = new Bundle();
//		bundle.putString(SearchVideoGridFragment.QUERY, query);
//		searchVideoGridFragment.setArguments(bundle);
//		switchToFragment(searchVideoGridFragment);
//	}



	//////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * A module/"plugin"/icon that displays the total number of blocked videos.
	 */
	private static class VideoBlockerPlugin implements VideoBlocker.VideoBlockerListener,
			BlockedVideosDialog.BlockedVideosDialogListener,
			Serializable {

		private ArrayList<VideoBlocker.BlockedVideo> blockedVideos = new ArrayList<>();
		private transient AppCompatActivity activity = null;


		VideoBlockerPlugin(AppCompatActivity activity) {
			// notify this class whenever a video is blocked...
			VideoBlocker.setVideoBlockerListener(this);
			this.activity = activity;
		}


		public void setActivity(AppCompatActivity activity) {
			this.activity = activity;
		}


		@Override
		public void onVideoBlocked(VideoBlocker.BlockedVideo blockedVideo) {
			blockedVideos.add(blockedVideo);
			activity.invalidateOptionsMenu();
		}


		/**
		 * Setup the video blocker notification icon which will be displayed in the tool bar.
 		 */
/*
		void setupIconForToolBar(final Menu menu) {
			if (getTotalBlockedVideos() > 0) {
				// display a red bubble containing the number of blocked videos
				ActionItemBadge.update(activity,
						menu.findItem(R.id.menu_blocker),
						ContextCompat.getDrawable(activity, R.drawable.ic_video_blocker_black),
						ActionItemBadge.BadgeStyles.RED,
						getTotalBlockedVideos());
			} else {
				// Else, set the bubble to transparent.  This is required so that when the user
				// clicks on the icon, the app will be able to detect such click and displays the
				// BlockedVideosDialog (otherwise, the ActionItemBadge would just ignore such clicks.
				ActionItemBadge.update(activity,
						menu.findItem(R.id.menu_blocker),
						ContextCompat.getDrawable(activity, R.drawable.ic_video_blocker_black),
						new BadgeStyle(BadgeStyle.Style.DEFAULT, com.mikepenz.actionitembadge.library.R.layout.menu_action_item_badge, Color.TRANSPARENT, Color.TRANSPARENT, Color.WHITE),
						"");
			}
		}
*/

		void onMenuBlockerIconClicked() {
			new BlockedVideosDialog(activity, this, blockedVideos).show();
		}


		@Override
		public void onClearBlockedVideos() {
			blockedVideos.clear();
			activity.invalidateOptionsMenu();
		}


		/**
		 * @return Total number of blocked videos.
		 */
		private int getTotalBlockedVideos() {
			return blockedVideos.size();
		}

	}

	/**
	 * Set that the public play lists was imported before
	 */
	private void setWasInitialPublicPlayListsDownloaded(boolean wasInitialPublicPlayListsDownloaded) {

		SharedPreferences preferences = SafetoonsApp.getPreferenceManager();

		preferences.edit().putBoolean(INITIAL_PUBLIC_PLAY_LISTS_DOWNLOADED, wasInitialPublicPlayListsDownloaded).apply();
	}

	/**
	 * Will check whether the public play lists was imported before
	 *
	 * @return True if the public play lists was downloaded before.
	 */
	private boolean wasInitialPublicPlayListsDownloaded() {
		SharedPreferences preferences = SafetoonsApp.getPreferenceManager();

		boolean wasInitialPublicPlayListsDownloaded = preferences.getBoolean(INITIAL_PUBLIC_PLAY_LISTS_DOWNLOADED, false);

		return wasInitialPublicPlayListsDownloaded;
	}

}
