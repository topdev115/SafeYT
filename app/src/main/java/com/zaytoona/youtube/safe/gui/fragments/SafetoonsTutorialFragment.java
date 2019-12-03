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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.gui.businessobjects.fragments.ImmersiveModeFragment;

/**
 * Fragment that will display the tutorial for the Safetoons App.
 */
public class SafetoonsTutorialFragment extends ImmersiveModeFragment implements ViewPager.OnPageChangeListener {

	private SafetoonsTutorialListener   listener = null;
	private ViewPager                       viewPager;
	private TextView                        nextTextView;
	private TextView                        pageCounterTextView;

	/** Tutorial slides layout resources. */
	private final static int tutorialSlideViews[] = {R.layout.tutorial_app_1, R.layout.tutorial_app_2, R.layout.tutorial_app_3, R.layout.tutorial_app_4, R.layout.tutorial_app_5};



	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		hideNavigationBar();

		// inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_safetoons_tutorial, container, false);

		nextTextView = view.findViewById(R.id.nextButton);
		nextTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (viewPager.getCurrentItem() < tutorialSlideViews.length-1) {
					viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
				} else {
					// the user wants to end the tutorial (by clicking the 'done' button)
					if (listener != null)
						listener.onTutorialFinished();
				}
			}
		});
		view.findViewById(R.id.skipButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// the user wants to end the tutorial (by clicking the 'skip' button)
				if (listener != null)
					listener.onTutorialFinished();
			}
		});
		pageCounterTextView = view.findViewById(R.id.pageCounterTextView);
		viewPager = view.findViewById(R.id.pager);
		viewPager.setAdapter(new TutorialPagerAdapter());
		viewPager.addOnPageChangeListener(this);

		setUp(0);

		return view;
	}


	/**
	 * Setup the {@link SafetoonsTutorialListener} which will be called once the user wants to
	 * quit the tutorial.
	 */
	public SafetoonsTutorialFragment setListener(SafetoonsTutorialListener listener) {
		this.listener = listener;
		return this;
	}


	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}


	/**
	 * Set up the nextTextView and pageCounterTextView's text content according to the slide
	 * position: i.e. if the slide is the last slide, then change the next button text from "next"
	 * to "done".
	 *
	 * @param position  slide position as per {@link #tutorialSlideViews}.
	 */
	private void setUp(int position) {
		nextTextView.setText((position == tutorialSlideViews.length - 1) ? R.string.tut_done : R.string.tut_next);
		pageCounterTextView.setText(getString(R.string.page_counter, position + 1, tutorialSlideViews.length));
	}


	@Override
	public void onPageSelected(int position) {
		setUp(position);
	}


	@Override
	public void onPageScrollStateChanged(int state) {
	}


	////////////////////////////////////////////////////////////////////////////////////////////////

	private class TutorialPagerAdapter extends PagerAdapter {

		@NonNull
		@Override
		public Object instantiateItem(@NonNull ViewGroup container, int position) {
			// inflate the tutorial slide layout resource according to the give position...
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View tutorialSlideView = inflater.inflate(tutorialSlideViews[position], null);

			viewPager.addView(tutorialSlideView);
			return tutorialSlideView;
		}

		@Override
		public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
			viewPager.removeView((View)object);
		}

		@Override
		public int getCount() {
			return tutorialSlideViews.length;
		}

		@Override
		public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
			return view == object;
		}

	}


	////////////////////////////////////////////////////////////////////////////////////////////////

	public interface SafetoonsTutorialListener {

		/**
		 * Will be called once the tutorial is finished.
		 */
		void onTutorialFinished();

	}

}
