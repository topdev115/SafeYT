package com.zaytoona.youtube.safe.gui.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.Logger;
//import com.zaytoona.youtube.safe.businessobjects.db.BookmarksDb;
//import com.zaytoona.youtube.safe.businessobjects.db.DownloadedVideosDb;
import com.zaytoona.youtube.safe.gui.businessobjects.MainActivityListener;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.SubsAdapter;
import com.zaytoona.youtube.safe.gui.businessobjects.fragments.FragmentEx;

public class MainFragment extends FragmentEx {

	private RecyclerView				subsListView = null;
	private SubsAdapter					subsAdapter  = null;
	private ActionBarDrawerToggle		subsDrawerToggle;
	private TextView 					parentPageHeader;
	private TabLayout                   tabLayout = null;
	private DrawerLayout 				subsDrawerLayout = null;

	/** List of fragments that will be displayed as tabs. */
	private List<VideosGridFragment>	 			videoGridFragmentsList = new ArrayList<>();
//	private WelcomeFragment							welcomeFragment = null;
//	private DownloadedVideosFragment    			downloadedVideosFragment = null;
//	private BookmarksFragment						bookmarksFragment = null;
	private SafetoonsPublicPlayListsFragment 		safetoonsPublicPlayListsFragment = null;
	private SafetoonsPrivatePlayListsFragment 	    safetoonsPrivatePlayListsFragment = null;
	//private SafetoonsRecommendedChannelsFragment 	safetoonsRecommendedChannelsFragment = null;
//	private List<PublicListFragment>				publicListFragmentsList = new ArrayList<>();
//	private List<PublicPlayListFragment>			publicPlayListFragmentsList = new ArrayList<>();
//	private List<PrivateListFragment>				privateListFragmentsList = new ArrayList<>();

	// Constants for saving the state of this Fragment's child Fragments
//	public static final String WELCOME_FRAGMENT = "MainFragment.welcomeFragment";
//	public static final String DOWNLOADED_VIDEOS_FRAGMENT = "MainFragment.downloadedVideosFragment";
//	public static final String BOOKMARKS_FRAGMENT = "MainFragment.bookmarksFragment";
	public static final String PUBLIC_PLAY_LISTS_FRAGMENT = "MainFragment.safetoonsPublicPlayListsFragment.";
	public static final String PRIVATE_PLAY_LISTS_FRAGMENT = "MainFragment.safetoonsPrivatePlayListsFragment.";
//	public static final String RECOMMENDED_CHANNELS_FRAGMENT = "MainFragment.safetoonsRecommendedChannelsFragment.";
//	public static final String PUBLIC_PLAY_LIST_FRAGMENT = "MainFragment.publicPlayListFragment.";
//	public static final String PUBLIC_PLAY_LIST_FRAGMENT_COUNT = "MainFragment.publicPlayListFragment.Count";
//	public static final String PUBLIC_LIST_FRAGMENT = "MainFragment.publicListFragment.";
//	public static final String PUBLIC_LIST_FRAGMENT_COUNT = "MainFragment.publicListFragment.Count";
//	public static final String PRIVATE_LIST_FRAGMENT = "MainFragment.privateListFragment.";
//	public static final String PRIVATE_LIST_FRAGMENT_COUNT = "MainFragment.privateListFragment.Count";

	private VideosPagerAdapter			videosPagerAdapter = null;
	private ViewPager					viewPager;

	public static final String SHOULD_SELECTED_FEED_TAB = "MainFragment.SHOULD_SELECTED_FEED_TAB";
	public static final String SHOULD_SELECT_PUBLIC_LIST_TAB = "MainFragment.SHOULD_SELECT_PUBLIC_LIST_TAB";
	public static final String SHOULD_SELECT_LAST_PUBLIC_PLAY_LIST_TAB = "MainFragment.SHOULD_SELECT_LAST_PUBLIC_PLAY_LIST_TAB";
	public static final String SHOULD_SELECT_LAST_PRIVATE_LIST_TAB = "MainFragment.SHOULD_SELECT_LAST_PRIVATE_LIST_TAB";


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {

//			welcomeFragment = (WelcomeFragment) getChildFragmentManager().getFragment(savedInstanceState, WELCOME_FRAGMENT);

			safetoonsPublicPlayListsFragment = (SafetoonsPublicPlayListsFragment) getChildFragmentManager().getFragment(savedInstanceState, PUBLIC_PLAY_LISTS_FRAGMENT);
			safetoonsPrivatePlayListsFragment = (SafetoonsPrivatePlayListsFragment) getChildFragmentManager().getFragment(savedInstanceState, PRIVATE_PLAY_LISTS_FRAGMENT);
//			safetoonsRecommendedChannelsFragment = (SafetoonsRecommendedChannelsFragment) getChildFragmentManager().getFragment(savedInstanceState, RECOMMENDED_CHANNELS_FRAGMENT);


//			downloadedVideosFragment = (DownloadedVideosFragment) getChildFragmentManager().getFragment(savedInstanceState, DOWNLOADED_VIDEOS_FRAGMENT);
//			bookmarksFragment = (BookmarksFragment) getChildFragmentManager().getFragment(savedInstanceState, BOOKMARKS_FRAGMENT);
/*
			int count = savedInstanceState.getInt(PUBLIC_PLAY_LIST_FRAGMENT_COUNT);

			for(int i=0; i<count; i++) {
				publicPlayListFragmentsList.add((PublicPlayListFragment) getChildFragmentManager().getFragment(savedInstanceState, PUBLIC_PLAY_LIST_FRAGMENT + i));
			}

			count = savedInstanceState.getInt(PUBLIC_LIST_FRAGMENT_COUNT);

			for(int i=0; i<count; i++) {
				publicListFragmentsList.add((PublicListFragment) getChildFragmentManager().getFragment(savedInstanceState, PUBLIC_LIST_FRAGMENT + i));
			}

			count = savedInstanceState.getInt(PRIVATE_LIST_FRAGMENT_COUNT);

			for(int i=0; i<count; i++) {
				privateListFragmentsList.add((PrivateListFragment) getChildFragmentManager().getFragment(savedInstanceState, PRIVATE_LIST_FRAGMENT + i));
			}
			*/
		}
	}


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);

		// setup the toolbar / actionbar
		Toolbar toolbar = view.findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// indicate that this fragment has an action bar menu
		setHasOptionsMenu(true);

		subsDrawerLayout = view.findViewById(R.id.subs_drawer_layout);
		subsDrawerToggle = new ActionBarDrawerToggle(
						getActivity(),
						subsDrawerLayout,
						R.string.app_name,
						R.string.app_name
		);

		// Disable drawer
		subsDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		subsDrawerToggle.setDrawerIndicatorEnabled(false);

		//subsDrawerToggle.setDrawerIndicatorEnabled(true);
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);
		}


		subsListView = view.findViewById(R.id.subs_drawer);
		if (subsAdapter == null) {
			subsAdapter = SubsAdapter.get(getActivity(), view.findViewById(R.id.subs_drawer_progress_bar));
		} else {
			subsAdapter.setContext(getActivity());
		}
		subsAdapter.setListener((MainActivityListener)getActivity());

		subsListView.setLayoutManager(new LinearLayoutManager(getActivity()));
		subsListView.setAdapter(subsAdapter);

		videosPagerAdapter = new VideosPagerAdapter(getChildFragmentManager());
		viewPager = view.findViewById(R.id.pager);
		viewPager.setOffscreenPageLimit(videoGridFragmentsList.size() - 1);
		viewPager.setAdapter(videosPagerAdapter);

		parentPageHeader = view.findViewById(R.id.parent_page_header);
		parentPageHeader.setVisibility(View.VISIBLE);

		tabLayout = view.findViewById(R.id.tab_layout);
		tabLayout.setupWithViewPager(viewPager);

		tabLayout.setTabTextColors(Color.GRAY, Color.BLACK);

		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				viewPager.setCurrentItem(tab.getPosition());
				videoGridFragmentsList.get(tab.getPosition()).onFragmentSelected();
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
				videoGridFragmentsList.get(tab.getPosition()).onFragmentUnselected();
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
			}
		});

		// select the default tab:  the default tab is defined by the user through the Preferences
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

		// If the app is being opened via the Notification that new videos from Subscribed channels have been found, select the Subscriptions Feed Fragment

		Bundle args = getArguments();
		/*if(args != null && args.getString(SHOULD_SELECT_PUBLIC_LIST_TAB, null) != null) {
			viewPager.setCurrentItem(videoGridFragmentsList.indexOf(safetoonsRecommendedChannelsFragment));
		}
		else*/ if(args != null && args.getBoolean(SHOULD_SELECT_LAST_PUBLIC_PLAY_LIST_TAB, false)) {
			viewPager.setCurrentItem(videoGridFragmentsList.indexOf(safetoonsPublicPlayListsFragment));
		}
		else if(args != null && args.getBoolean(SHOULD_SELECT_LAST_PRIVATE_LIST_TAB, false)) {
			viewPager.setCurrentItem(videoGridFragmentsList.indexOf(safetoonsPrivatePlayListsFragment));
		}
		else{
			viewPager.setCurrentItem(0);
		}

		// Set the current viewpager fragment as selected, as when the Activity is recreated, the Fragment
		// won't know that it's selected. When the Feeds fragment is the default tab, this will prevent the
		// refresh dialog from showing when an automatic refresh happens.
		videoGridFragmentsList.get(viewPager.getCurrentItem()).onFragmentSelected();

		return view;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		subsDrawerToggle.syncState();
	}


	@Override
	public void onResume() {
		super.onResume();
		// when the MainFragment is resumed (e.g. after Preferences is minimized), inform the
		// current fragment that it is selected.
		if (videoGridFragmentsList != null  &&  tabLayout != null) {
			Logger.d(this, "MAINFRAGMENT RESUMED " + tabLayout.getSelectedTabPosition());
			videoGridFragmentsList.get(tabLayout.getSelectedTabPosition()).onFragmentSelected();
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns true, then it has handled the app
		// icon touch event
		if (subsDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle your other action bar items...
		return super.onOptionsItemSelected(item);
	}


	private class VideosPagerAdapter extends FragmentPagerAdapter {

		public VideosPagerAdapter(FragmentManager fm) {
			super(fm);

			// initialise fragments

//			if (welcomeFragment == null) {
//				welcomeFragment = new WelcomeFragment();
//			}

			if (safetoonsPublicPlayListsFragment == null) {
				safetoonsPublicPlayListsFragment = new SafetoonsPublicPlayListsFragment();
				safetoonsPublicPlayListsFragment.setDisplayMode(true);
			}

			safetoonsPublicPlayListsFragment.setMainActivityListener((MainActivityListener)getActivity());

			if (safetoonsPrivatePlayListsFragment == null) {
				safetoonsPrivatePlayListsFragment = new SafetoonsPrivatePlayListsFragment();
				safetoonsPrivatePlayListsFragment.setDisplayMode(true);
			}

			safetoonsPrivatePlayListsFragment.setMainActivityListener((MainActivityListener)getActivity());

//			if (safetoonsRecommendedChannelsFragment == null) {
//				safetoonsRecommendedChannelsFragment = new SafetoonsRecommendedChannelsFragment();
//				safetoonsRecommendedChannelsFragment.setDisplayMode(true);
//			}

//			safetoonsRecommendedChannelsFragment.setMainActivityListener((MainActivityListener)getActivity());

/*
			if(downloadedVideosFragment == null) {
				downloadedVideosFragment = new DownloadedVideosFragment();
				DownloadedVideosDb.getVideoDownloadsDb().setListener(downloadedVideosFragment);
			}
*/
//			if (bookmarksFragment == null) {
//				bookmarksFragment = new BookmarksFragment();
//				BookmarksDb.getBookmarksDb().addListener(bookmarksFragment);
//			}
/*
			if (publicListFragmentsList.isEmpty()) {

				List<String> publicListsList = SubscriptionsDb.getSubscriptionsDb().getAllowedChannelIds();

				for (int i = 0; i < publicListsList.size(); i++) {

					PublicListFragment publicListFragment = new PublicListFragment();

					publicListFragment.setYouTubeChannelId(publicListsList.get(i));

					publicListFragmentsList.add(publicListFragment);
				}
			}

			if (publicPlayListFragmentsList.isEmpty()) {

				List<YouTubePublicPlayList> publicPlayListsList = PublicPlayListsDb.getPublicPlayListsDb().getPublicPlayLists();

				for (int i = 0; i < publicPlayListsList.size(); i++) {

					PublicPlayListFragment publicPlayListFragment = new PublicPlayListFragment();

					publicPlayListFragment.fragmentName = publicPlayListsList.get(i).getId();
					publicPlayListFragment.title = publicPlayListsList.get(i).getTitle();

					PublicPlayListsDb.getPublicPlayListsDb().addListener(publicPlayListFragment);

					publicPlayListFragmentsList.add(publicPlayListFragment);
				}
			}

			if (privateListFragmentsList.isEmpty()) {

				List<String> privateListsList = PrivateListsDb.getPrivateListsDb().getPrivateLists();

				for (int i = 0; i < privateListsList.size(); i++) {

					PrivateListFragment privateListFragment = new PrivateListFragment();

					privateListFragment.fragmentName = privateListsList.get(i);

					PrivateListsDb.getPrivateListsDb().addListener(privateListFragment);

					privateListFragmentsList.add(privateListFragment);
				}
			}
*/
			// add fragments to list:  do NOT forget to ***UPDATE*** @string/default_tab and @string/default_tab_values
			videoGridFragmentsList.clear();

			// Add welcome only if no tabs added already
//			if(publicListFragmentsList.size() == 0 && privateListFragmentsList.size() == 0) {
//				videoGridFragmentsList.add(welcomeFragment);
//			}

			videoGridFragmentsList.add(safetoonsPublicPlayListsFragment);
//			videoGridFragmentsList.add(safetoonsRecommendedChannelsFragment);
			videoGridFragmentsList.add(safetoonsPrivatePlayListsFragment);
/*
			for(PublicPlayListFragment publicPlayListFragment : publicPlayListFragmentsList) {
				videoGridFragmentsList.add(publicPlayListFragment);
			}

			for(PublicListFragment publicListFragment : publicListFragmentsList) {
				videoGridFragmentsList.add(publicListFragment);
			}

			for(PrivateListFragment privateListFragment : privateListFragmentsList) {
				videoGridFragmentsList.add(privateListFragment);
			}
*/
//			videoGridFragmentsList.add(bookmarksFragment);
			//videoGridFragmentsList.add(downloadedVideosFragment);
		}

		@Override
		public int getCount() {
			return videoGridFragmentsList.size();
		}

		@Override
		public Fragment getItem(int position) {
			return videoGridFragmentsList.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return videoGridFragmentsList.get(position).getFragmentName();
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

//		if(welcomeFragment != null && welcomeFragment.isAdded())
//			getChildFragmentManager().putFragment(outState, WELCOME_FRAGMENT, welcomeFragment);

		if(safetoonsPublicPlayListsFragment != null && safetoonsPublicPlayListsFragment.isAdded())
			getChildFragmentManager().putFragment(outState, PUBLIC_PLAY_LISTS_FRAGMENT, safetoonsPublicPlayListsFragment);

		if(safetoonsPrivatePlayListsFragment != null && safetoonsPrivatePlayListsFragment.isAdded())
			getChildFragmentManager().putFragment(outState, PRIVATE_PLAY_LISTS_FRAGMENT, safetoonsPrivatePlayListsFragment);

//		if(safetoonsRecommendedChannelsFragment != null && safetoonsRecommendedChannelsFragment.isAdded())
//			getChildFragmentManager().putFragment(outState, RECOMMENDED_CHANNELS_FRAGMENT, safetoonsRecommendedChannelsFragment);

/*
		if(downloadedVideosFragment != null && downloadedVideosFragment.isAdded())
			getChildFragmentManager().putFragment(outState, DOWNLOADED_VIDEOS_FRAGMENT, downloadedVideosFragment);
*/
//		if(bookmarksFragment != null && bookmarksFragment.isAdded())
//			getChildFragmentManager().putFragment(outState, BOOKMARKS_FRAGMENT, bookmarksFragment);
/*
		if(publicPlayListFragmentsList.isEmpty() == false) {

			int count = 0;

			for(PublicPlayListFragment publicPlayListFragment : publicPlayListFragmentsList) {

				if(publicPlayListFragment != null && publicPlayListFragment.isAdded())
					getChildFragmentManager().putFragment(outState, PUBLIC_PLAY_LIST_FRAGMENT + count++, publicPlayListFragment);

			}

			// Stroe count
			outState.putInt(PUBLIC_PLAY_LIST_FRAGMENT_COUNT, count);
		}

		if(publicListFragmentsList.isEmpty() == false) {

			int count = 0;

			for(PublicListFragment publicListFragment : publicListFragmentsList) {

				if(publicListFragment != null && publicListFragment.isAdded())
					getChildFragmentManager().putFragment(outState, PUBLIC_LIST_FRAGMENT + count++, publicListFragment);

			}

			// Stroe count
			outState.putInt(PUBLIC_LIST_FRAGMENT_COUNT, count);
		}

		if(privateListFragmentsList.isEmpty() == false) {

			int count = 0;

			for(PrivateListFragment privateListFragment : privateListFragmentsList) {

				if(privateListFragment != null && privateListFragment.isAdded())
					getChildFragmentManager().putFragment(outState, PRIVATE_LIST_FRAGMENT + count++, privateListFragment);

			}

			// Stroe count
			outState.putInt(PRIVATE_LIST_FRAGMENT_COUNT, count);
		}
*/
		super.onSaveInstanceState(outState);
	}


	/**
	 * Returns true if the subscriptions drawer is opened.
	 */
	public boolean isDrawerOpen() {
		return subsDrawerLayout.isDrawerOpen(GravityCompat.START);
	}

	/**
	 * Close the subscriptions drawer.
	 */
	public void closeDrawer() {
		subsDrawerLayout.closeDrawer(GravityCompat.START);
	}

	public void updateSelectedTab() {
		videoGridFragmentsList.get(viewPager.getCurrentItem()).onFragmentSelected();
	}

	public boolean isPublicFragmentOpenedForPlayLists() {
		if(videoGridFragmentsList.get(viewPager.getCurrentItem()) instanceof SafetoonsPublicPlayListsFragment) {
			return ((SafetoonsPublicPlayListsFragment) videoGridFragmentsList.get(viewPager.getCurrentItem())).category != null;
		}
		return false;
	}

	public void openPublicFragmentForCategories() {
		if(videoGridFragmentsList.get(viewPager.getCurrentItem()) instanceof SafetoonsPublicPlayListsFragment) {
			((SafetoonsPublicPlayListsFragment) videoGridFragmentsList.get(viewPager.getCurrentItem())).openForCategories();
		}
	}

	public void updateMainActivityListeners() {

		if (safetoonsPublicPlayListsFragment != null) {
			safetoonsPublicPlayListsFragment.setMainActivityListener((MainActivityListener)getActivity());
		}

		if (safetoonsPrivatePlayListsFragment != null) {
			safetoonsPrivatePlayListsFragment.setMainActivityListener((MainActivityListener)getActivity());
		}

//		if (safetoonsRecommendedChannelsFragment != null) {
//			safetoonsRecommendedChannelsFragment.setMainActivityListener((MainActivityListener)getActivity());
//		}
	}
}
