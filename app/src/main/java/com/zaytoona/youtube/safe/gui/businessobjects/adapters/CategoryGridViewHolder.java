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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.VideoCategory;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeVideo;
import com.zaytoona.youtube.safe.businessobjects.categories.SafetoonsCategory;
import com.zaytoona.youtube.safe.businessobjects.db.PlaybackStatusDb;
import com.zaytoona.youtube.safe.businessobjects.db.Tasks.IsVideoBookmarkedTask;
import com.zaytoona.youtube.safe.businessobjects.db.Tasks.IsVideoPrivateListedTask;
import com.zaytoona.youtube.safe.businessobjects.db.Tasks.IsVideoWatchedTask;
import com.zaytoona.youtube.safe.businessobjects.firebase.VideoRecommend;
import com.zaytoona.youtube.safe.businessobjects.firebase.VideoReport;
import com.zaytoona.youtube.safe.common.Constants;
import com.zaytoona.youtube.safe.gui.businessobjects.MainActivityListener;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsListClickListener;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsMaterialDialog;
import com.zaytoona.youtube.safe.gui.businessobjects.YouTubePlayer;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A ViewHolder for the playlists grid view.
 */
class CategoryGridViewHolder extends RecyclerView.ViewHolder {

	@BindView(R.id.thumbnail_image_view)
	ImageView thumbnailImageView;
	@BindView(R.id.title_text_view)
	TextView titleTextView;

	private SafetoonsListClickListener safetoonsListClickListener;


	CategoryGridViewHolder(View view, SafetoonsListClickListener safetoonsListClickListener) {
		super(view);
		ButterKnife.bind(this, view);

		this.safetoonsListClickListener = safetoonsListClickListener;
	}

	void setSafetoonsCategory(final SafetoonsCategory category, Context context) {

		if(isActivityDestroyed(context)) {
			return;
		}

		Glide.with(context.getApplicationContext())
				.load(category.getAvatarURL())
				.apply(new RequestOptions().placeholder(R.drawable.thumbnail_default))
				.into(thumbnailImageView);

		titleTextView.setText(category.getTitle());

		thumbnailImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				safetoonsListClickListener.onClickCategory(category);
			}
		});
	}

	private boolean isActivityDestroyed(Context context) {

		if (context == null) {
			return true;
		} else if (context instanceof Application) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				if (context instanceof FragmentActivity) {
					if (((FragmentActivity) context).isDestroyed()) {
						return true;
					}
				} else if (context instanceof Activity) {
					if (((Activity) context).isDestroyed()) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
