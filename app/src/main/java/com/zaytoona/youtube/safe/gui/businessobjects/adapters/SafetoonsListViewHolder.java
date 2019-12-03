/*
 * Safetoons
 * Copyright (C) 2017  Ramon Mifsud
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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsListClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A ViewHolder for the playlists grid view.
 */
class SafetoonsListViewHolder extends RecyclerView.ViewHolder {

	@BindView(R.id.thumbnail_image_view)
	ImageView thumbnailImageView;
	@BindView(R.id.title_text_view)
	TextView titleTextView;
	@BindView(R.id.views_text_view)
	TextView videoCountTextView;
	@BindView(R.id.publish_date_text_view)
	TextView publishDateTextView;

	@BindView(R.id.thumbs_up_text_view)
	View thumbsUpView;
	@BindView(R.id.video_duration_text_view)
	View videoDurationTextView;
	@BindView(R.id.channel_text_view)
	View channelTextView;
	@BindView(R.id.options_button)
	View optionsButton;
	@BindView(R.id.video_recommend_button)
	View recommendButton;
	@BindView(R.id.video_report_button)
	View reportButton;
	@BindView(R.id.channel_layout)
	View channelLayout;
	@BindView(R.id.separator_text_view)
	View separatorView;

	private SafetoonsListClickListener safetoonsListClickListener;


	SafetoonsListViewHolder(View view, SafetoonsListClickListener safetoonsListClickListener) {
		super(view);
		ButterKnife.bind(this, view);

		thumbsUpView.setVisibility(View.GONE);
		videoDurationTextView.setVisibility(View.GONE);
		channelTextView.setVisibility(View.GONE);
		optionsButton.setVisibility(View.GONE);
		recommendButton.setVisibility(View.GONE);
		reportButton.setVisibility(View.GONE);
		separatorView.setVisibility(View.GONE);

		this.safetoonsListClickListener = safetoonsListClickListener;
	}

	void setPlaylist(final SafetoonsList playlist, Context context) {
		Glide.with(context)
						.load(playlist.getThumbnailUrl())
						.apply(new RequestOptions().placeholder(R.drawable.thumbnail_default))
						.into(thumbnailImageView);

		titleTextView.setText(playlist.getTitle());

		if(playlist.getType() == SafetoonsList.SAFETOONS_LIST_TYPE_CHANNEL) {
			publishDateTextView.setVisibility(View.GONE);
			videoCountTextView.setVisibility(View.GONE);
			separatorView.setVisibility(View.GONE);
			channelLayout.setVisibility(View.GONE);
		}
		else {
			publishDateTextView.setText(playlist.getPublishDatePretty());
			videoCountTextView.setText(String.format(context.getString(R.string.num_videos), playlist.getVideoCount()));

			publishDateTextView.setVisibility(View.VISIBLE);
			videoCountTextView.setVisibility(View.VISIBLE);
			separatorView.setVisibility(View.VISIBLE);

			channelLayout.setVisibility(View.VISIBLE);
		}

		thumbnailImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				safetoonsListClickListener.onClickPlaylist(playlist);
			}
		});
	}
}
