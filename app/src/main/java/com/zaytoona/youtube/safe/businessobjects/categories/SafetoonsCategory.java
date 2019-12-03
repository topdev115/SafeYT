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

package com.zaytoona.youtube.safe.businessobjects.categories;

import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.zaytoona.youtube.safe.BuildConfig;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.FileDownloader;
import com.zaytoona.youtube.safe.businessobjects.Logger;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.PrettyTimeEx;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.VideoDuration;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.Tasks.GetVideoStreamTask;
import com.zaytoona.youtube.safe.businessobjects.YouTube.VideoStream.StreamMetaData;
import com.zaytoona.youtube.safe.businessobjects.db.BookmarksDb;
import com.zaytoona.youtube.safe.businessobjects.db.DownloadedVideosDb;
import com.zaytoona.youtube.safe.businessobjects.db.PrivateListsDb;
import com.zaytoona.youtube.safe.businessobjects.interfaces.GetDesiredStreamListener;

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zaytoona.youtube.safe.app.SafetoonsApp.getContext;
import static com.zaytoona.youtube.safe.app.SafetoonsApp.getStr;

/**
 * Represents a YouTube video.
 */
public class SafetoonsCategory implements Serializable {

	/**
	 * YouTube video ID.
	 */
	private String id;
	/**
	 * Video title.
	 */
	private String title;

	/**
	 * The description of the category.
	 */
	private String description;

	/**
	 * The avatar of the category.
	 */
	private String avatarURL;


	/**
	 * Constructor.
	 */
	public SafetoonsCategory(String id, String title, String description, String avatarURL) {

		this.id = id;
		this.title = title;
		this.description = description;
		this.avatarURL = avatarURL;

	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getAvatarURL() {
		return avatarURL;
	}

}
