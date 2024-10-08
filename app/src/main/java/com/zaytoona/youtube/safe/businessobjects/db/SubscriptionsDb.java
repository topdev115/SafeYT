/*
 * Safetoons
 * Copyright (C) 2016  Ramon Mifsud
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

package com.zaytoona.youtube.safe.businessobjects.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.Logger;
import com.zaytoona.youtube.safe.businessobjects.YouTube.GetChannelsDetails;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeVideo;

/**
 * A database (DB) that stores user subscriptions (with respect to YouTube channels).
 */
public class SubscriptionsDb extends SQLiteOpenHelperEx {
	private static volatile SubscriptionsDb subscriptionsDb = null;

	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "subs.db";

	private List<SubscriptionsDb.PublicListsDbListener> listeners = new ArrayList<>();

	private SubscriptionsDb(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static synchronized SubscriptionsDb getSubscriptionsDb() {
		if (subscriptionsDb == null) {
			subscriptionsDb = new SubscriptionsDb(SafetoonsApp.getContext());
		}

		return subscriptionsDb;
	}

	@Override
	protected void clearDatabaseInstance() {
		subscriptionsDb = null;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SubscriptionsTable.getCreateStatement());
		db.execSQL(SubscriptionsVideosTable.getCreateStatement());
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Version 2 of the database introduces the SubscriptionsVideosTable, which stores videos found in each subscribed channel
		if(oldVersion == 1 && newVersion == 2) {
			db.execSQL(SubscriptionsVideosTable.getCreateStatement());
		}
	}


	/**
	 * Add a Listener that will be notified when a subscription is updated.
	 *
	 * @param listener The Listener (which implements PublicListsDbListener) to add.
	 */
	public void addListener(SubscriptionsDb.PublicListsDbListener listener) {
		if(!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Saves the given channel into the subscriptions DB.
	 *
	 * @param channel Channel the user wants to subscribe to.
	 *
	 * @return True if the operation was successful; false otherwise.
	 */
	public boolean subscribe(YouTubeChannel channel) {
		saveChannelVideos(channel);

		boolean bRet = subscribe(channel.getId(), channel.getTitle());

		//onUpdated();

		return bRet;
	}


	/**
	 * Saves the given channel into the subscriptions DB.
	 *
	 * @param channelId The channel ID the user wants to subscribe to.
	 *
	 * @return True if the operation was successful; false otherwise.
	 */
	public boolean subscribe(String channelId, String title) {
		ContentValues values = new ContentValues();
		values.put(SubscriptionsTable.COL_CHANNEL_ID, channelId);
		values.put(SubscriptionsTable.COL_CHANNEL_TITLE, title);
		values.put(SubscriptionsTable.COL_LAST_VISIT_TIME, System.currentTimeMillis());
		values.put(SubscriptionsTable.COL_CHANNEL_ALLOWED, 0);
		values.put(SubscriptionsTable.COL_CHANNEL_HIDDEN, 1);

		boolean bRet = getWritableDatabase().insert(SubscriptionsTable.TABLE_NAME, null, values) != -1;

		//onUpdated();

		return bRet;
	}


	/**
	 * Removes the given channel from the subscriptions DB.
	 *
	 * @param channel Channel the user wants to unsubscribe to.
	 *
	 * @return True if the operation was successful; false otherwise.
	 */
	public boolean unsubscribe(YouTubeChannel channel) {
		// delete any feed videos pertaining to this channel
		getWritableDatabase().delete(SubscriptionsVideosTable.TABLE_NAME,
				SubscriptionsVideosTable.COL_CHANNEL_ID + " = ?",
				new String[]{channel.getId()});

		// remove this channel from the subscriptions DB
		int rowsDeleted = getWritableDatabase().delete(SubscriptionsTable.TABLE_NAME,
				SubscriptionsTable.COL_CHANNEL_ID + " = ?",
				new String[]{channel.getId()});

		onUpdated(null);

		return (rowsDeleted >= 0);
	}


	/**
	 * Returns a list of channels that the user subscribed to and will check each channel whether
	 * new videos have been uploaded since last channel visit
	 *
	 * @return A list of channels that the user subscribed to.
	 *
	 * @throws IOException
	 */
	public List<YouTubeChannel> getSubscribedChannels() throws IOException {
		return getSubscribedChannels(true);
	}


	/**
	 * Returns a list of channels that the user subscribed to.
	 *
	 * @param shouldCheckForNewVideos  If true it will check if new videos have been uploaded to the
	 *                                 subscribed channels since last channel visit.
	 *
	 * @return A list of channels that the user subscribed to.
	 * @throws IOException
	 */
	public List<YouTubeChannel> getSubscribedChannels(boolean shouldCheckForNewVideos) throws IOException {
		List<YouTubeChannel> subsChannels = new ArrayList<>();
		Cursor cursor = getReadableDatabase().query(SubscriptionsTable.TABLE_NAME,
													new String[]{SubscriptionsTable.COL_CHANNEL_ID},
													null, null,
													null, null,
													SubscriptionsTable.COL_ID + " ASC");

		if (cursor.moveToNext()) {
			int             colChannelIdNum = cursor.getColumnIndexOrThrow(SubscriptionsTable.COL_CHANNEL_ID);
			List<String>    channelIdsList = new ArrayList<>();

			do {
				channelIdsList.add(cursor.getString(colChannelIdNum));
			} while (cursor.moveToNext());

			// Initialize the channel.  If the initialization is successful, then add the channel
			// to the subsChannel list...
			subsChannels = new GetChannelsDetails().getYouTubeChannels(channelIdsList, true /* = user is subscribed to this channel*/, shouldCheckForNewVideos);
		}

		cursor.close();
		return subsChannels;
	}


    /**
     * Updates the given channel's allowed status.
     *
     * @param channelId	Channel ID
     * @param allowed	1 allowed, 0 not allowed
     *
     */
    public void updateChannelAllowedStatus(String channelId, int allowed) {
        SQLiteDatabase	db = getWritableDatabase();
        long			currentTime = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(SubscriptionsTable.COL_CHANNEL_ALLOWED, allowed);

		if(allowed == 1) {
        	values.put(SubscriptionsTable.COL_CHANNEL_HIDDEN, 0);
		}
		else {
			values.put(SubscriptionsTable.COL_CHANNEL_HIDDEN, 1);
		}

        int count = db.update(
                SubscriptionsTable.TABLE_NAME,
                values,
                SubscriptionsTable.COL_CHANNEL_ID + " = ?",
                new String[]{channelId});

        if(allowed == 1) {
			onUpdated(channelId);
		}
		else {
			onUpdated(null);
		}
    }


    /**
	 * Returns a list of channels Ids that the user subscribed to.
	 *
	 *
	 * @return A list of channel Ids that the user subscribed to.
	 */
	public List<String> getAllowedChannelIds() {

		List<String>    channelIdsList = new ArrayList<>();

		Cursor cursor = getReadableDatabase().query(SubscriptionsTable.TABLE_NAME,
				new String[]{SubscriptionsTable.COL_CHANNEL_ID},
				SubscriptionsTable.COL_CHANNEL_ALLOWED + " = ?",
				new String[]{"1"},
				null, null,
				SubscriptionsTable.COL_ID + " ASC");

		if (cursor.moveToNext()) {

			int colChannelIdNum = cursor.getColumnIndexOrThrow(SubscriptionsTable.COL_CHANNEL_ID);

			do {
				channelIdsList.add(cursor.getString(colChannelIdNum));
			} while (cursor.moveToNext());
		}

		cursor.close();

		return channelIdsList;
	}

	/**
	 * Check if a channel is allowed.
	 *
	 *
	 * @return True if allowed, false otherwise.
	 */
	public boolean isdChannelAllowed(String channelId) {

		boolean isAllowed = false;

		Cursor cursor = getReadableDatabase().query(SubscriptionsTable.TABLE_NAME,
				new String[]{SubscriptionsTable.COL_CHANNEL_ID},
				SubscriptionsTable.COL_CHANNEL_ID + " = ? AND " + SubscriptionsTable.COL_CHANNEL_ALLOWED + " = ?",
				new String[]{channelId, "1"},
				null, null,
				SubscriptionsTable.COL_ID + " ASC");

		if (cursor.moveToNext()) {
			isAllowed = true;
		}

		cursor.close();

		return isAllowed;
	}

	/**
	 * Updates the given channel's hidden status.
	 *
	 * @param channelId	Channel ID
	 * @param hidden	1 hidden, 0 not hidden
	 *
	 */
	public void updateChannelHiddenStatus(String channelId, int hidden) {
		SQLiteDatabase	db = getWritableDatabase();
		long			currentTime = System.currentTimeMillis();

		ContentValues values = new ContentValues();
		values.put(SubscriptionsTable.COL_CHANNEL_HIDDEN, hidden);

		int count = db.update(
				SubscriptionsTable.TABLE_NAME,
				values,
				SubscriptionsTable.COL_CHANNEL_ID + " = ?",
				new String[]{channelId});
	}


	/**
	 * Returns a list of channels Ids that are visible to kid.
	 *
	 *
	 * @return A list of channel Ids that the kid is allowed to see.
	 */
	public List<String> getShowChannelIds() {

		List<String>    channelIdsList = new ArrayList<>();

		Cursor cursor = getReadableDatabase().query(SubscriptionsTable.TABLE_NAME,
				new String[]{SubscriptionsTable.COL_CHANNEL_ID},
				SubscriptionsTable.COL_CHANNEL_HIDDEN + " = ?",
				new String[]{"0"},
				null, null,
				SubscriptionsTable.COL_ID + " ASC");

		if (cursor.moveToNext()) {

			int colChannelIdNum = cursor.getColumnIndexOrThrow(SubscriptionsTable.COL_CHANNEL_ID);

			do {
				channelIdsList.add(cursor.getString(colChannelIdNum));
			} while (cursor.moveToNext());
		}

		cursor.close();

		return channelIdsList;
	}


	/**
	 * Check if a channel is hidden.
	 *
	 *
	 * @return True if hidden, false otherwise.
	 */
	public boolean isdChannelHidden(String channelId) {

		boolean isHidden = false;

		Cursor cursor = getReadableDatabase().query(SubscriptionsTable.TABLE_NAME,
				new String[]{SubscriptionsTable.COL_CHANNEL_ID},
				SubscriptionsTable.COL_CHANNEL_ID + " = ? AND " + SubscriptionsTable.COL_CHANNEL_HIDDEN + " = ?",
				new String[]{channelId, "1"},
				null, null,
				SubscriptionsTable.COL_ID + " ASC");

		if (cursor.moveToNext()) {
			isHidden = true;
		}

		cursor.close();

		return isHidden;
	}


	/**
	 * Get the title of a given channel.
	 *
	 * @param channelId	Channel ID
	 * @return Channel title.
	 */
	public String getChannelTitle(String channelId) {

		String title = null;

		Cursor cursor = getReadableDatabase().query(
				SubscriptionsTable.TABLE_NAME,
				new String[]{SubscriptionsTable.COL_ID, SubscriptionsTable.COL_CHANNEL_TITLE},
				SubscriptionsTable.COL_CHANNEL_ID + " = ?",
				new String[]{channelId}, null, null, null);

		if (cursor.moveToNext()) {

			int colChannelTitleNum = cursor.getColumnIndexOrThrow(SubscriptionsTable.COL_CHANNEL_TITLE);
			title = cursor.getString(colChannelTitleNum);
		}

		cursor.close();

		return title;
	}

	/**
	 * @return The total number of subscribed channels.
	 */
	public int getTotalSubscribedChannels() {
		String	query  = String.format("SELECT COUNT(*) FROM %s", SubscriptionsTable.TABLE_NAME);
		Cursor	cursor = SubscriptionsDb.getSubscriptionsDb().getReadableDatabase().rawQuery(query, null);
		int		totalSubbedChannels = 0;

		if (cursor.moveToFirst())
			totalSubbedChannels = cursor.getInt(0);

		cursor.close();
		return totalSubbedChannels;
	}


	/**
	 * Checks if the user is subscribed to the given channel.
	 *
	 * @param channelId	Channel ID
	 * @return True if the user is subscribed; false otherwise.
	 * @throws IOException
	 */
	public boolean isUserSubscribedToChannel(String channelId) {
		Cursor cursor = getReadableDatabase().query(
				SubscriptionsTable.TABLE_NAME,
				new String[]{SubscriptionsTable.COL_ID},
				SubscriptionsTable.COL_CHANNEL_ID + " = ?",
				new String[]{channelId}, null, null, null);
		boolean	isUserSubbed = cursor.moveToNext();

		cursor.close();
		return isUserSubbed;
	}


	/**
	 * Updates the given channel's last visit time.
	 *
	 * @param channelId	Channel ID
	 *
	 * @return	last visit time, if the update was successful;  -1 otherwise.
	 */
	public long updateLastVisitTime(String channelId) {
		SQLiteDatabase	db = getWritableDatabase();
		long			currentTime = System.currentTimeMillis();

		ContentValues values = new ContentValues();
		values.put(SubscriptionsTable.COL_LAST_VISIT_TIME, currentTime);

		int count = db.update(
				SubscriptionsTable.TABLE_NAME,
				values,
				SubscriptionsTable.COL_CHANNEL_ID + " = ?",
				new String[]{channelId});

		return (count > 0 ? currentTime : -1);
	}


	/**
	 * Returns the last time the user has visited this channel.
	 *
	 * @param channel
	 *
	 * @return	last visit time, if the update was successful;  -1 otherwise.
	 * @throws IOException
	 */
	public long getLastVisitTime(YouTubeChannel channel) {
		Cursor	cursor = getReadableDatabase().query(
							SubscriptionsTable.TABLE_NAME,
							new String[]{SubscriptionsTable.COL_LAST_VISIT_TIME},
							SubscriptionsTable.COL_CHANNEL_ID + " = ?",
							new String[]{channel.getId()}, null, null, null);
		long	lastVisitTime = -1;

		if (cursor.moveToNext()) {
			int colLastVisitTIme = cursor.getColumnIndexOrThrow(SubscriptionsTable.COL_LAST_VISIT_TIME);
			lastVisitTime = cursor.getLong(colLastVisitTIme);
		}

		cursor.close();
		return lastVisitTime;
	}


	private boolean hasVideo(YouTubeVideo video) {
		String query = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", SubscriptionsVideosTable.TABLE_NAME, SubscriptionsVideosTable.COL_YOUTUBE_VIDEO_ID);
		Cursor cursor = null;
		try {
			cursor = SubscriptionsDb.getSubscriptionsDb().getReadableDatabase().rawQuery(query, new String[]{video.getId()});
			if (cursor.moveToFirst()) {
				return cursor.getInt(0) > 0;
			}
			return false;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}


	/**
	 * Check if the given channel has new videos (by looking into the {@link SubscriptionsVideosTable}
	 * [i.e. video cache table]).
	 *
	 * @param channel Channel to check.
	 *
	 * @return True if the user hasn't visited the channel and new videos have been uploaded in the
	 * meantime; false otherwise.
	 */
	public boolean channelHasNewVideos(YouTubeChannel channel) {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		String query = String.format("SELECT COUNT(*) FROM %s WHERE %s = ? AND %s > ?", SubscriptionsVideosTable.TABLE_NAME, SubscriptionsVideosTable.COL_CHANNEL_ID, SubscriptionsVideosTable.COL_YOUTUBE_VIDEO_DATE);
		Cursor cursor = SubscriptionsDb.getSubscriptionsDb().getReadableDatabase().rawQuery(
							query,
							new String[]{channel.getId(), fmt.parseDateTime(new DateTime(new Date(channel.getLastVisitTime())).toString()).toString()});
		boolean channelHasNewVideos = false;

		if (cursor.moveToFirst())
			channelHasNewVideos = cursor.getInt(0) > 0;

		cursor.close();
		return channelHasNewVideos;
	}

	/**
	 * Loop through each video saved in the passed {@link YouTubeChannel} and save it into the database, if it's not already been saved
	 * @param channel
	 */
	public void saveChannelVideos(YouTubeChannel channel) {
		Gson gson = new Gson();
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		for (YouTubeVideo video : channel.getYouTubeVideos()) {
			if(!hasVideo(video)) {
				ContentValues values = new ContentValues();
				values.put(SubscriptionsVideosTable.COL_CHANNEL_ID, channel.getId());
				values.put(SubscriptionsVideosTable.COL_YOUTUBE_VIDEO_ID, video.getId());
				values.put(SubscriptionsVideosTable.COL_YOUTUBE_VIDEO, gson.toJson(video).getBytes());
				values.put(SubscriptionsVideosTable.COL_YOUTUBE_VIDEO_DATE, fmt.parseDateTime(video.getPublishDate().toStringRfc3339()).toString());

				getWritableDatabase().insert(SubscriptionsVideosTable.TABLE_NAME, null, values);
			}
		}
	}

	/**
	 * Delete any videos stored in the database (for subscribed channels) that are over a month old.
	 * @return
	 */
	public boolean trimSubscriptionVideos() {
		int result = getWritableDatabase().delete(SubscriptionsVideosTable.TABLE_NAME, String.format("%s < DATETIME('now', '-1 month')", SubscriptionsVideosTable.COL_YOUTUBE_VIDEO_DATE), null);
		return result > 0;
	}

	/**
	 * Query the database to retrieve all videos for subscribed channels.
	 * @return
	 */
	public List<YouTubeVideo> getSubscriptionVideos() {
		Cursor	cursor = getReadableDatabase().query(
							SubscriptionsVideosTable.TABLE_NAME,
							new String[]{SubscriptionsVideosTable.COL_YOUTUBE_VIDEO},
							null, null, null, null,
							SubscriptionsVideosTable.COL_YOUTUBE_VIDEO_DATE + " DESC");
		List<YouTubeVideo> videos = new ArrayList<>();

		if (cursor.moveToNext()) {
			do {
				final byte[]    blob = cursor.getBlob(cursor.getColumnIndex(SubscriptionsVideosTable.COL_YOUTUBE_VIDEO));
				final String    videoJson = new String(blob);

				// convert JSON into YouTubeVideo
				YouTubeVideo video = new Gson().fromJson(videoJson, new TypeToken<YouTubeVideo>(){}.getType());

				// due to upgrade to YouTubeVideo (by changing channel{Id,Name} to YouTubeChannel)
				// from version 2.82 to 2.90
				if (video.getChannel() == null) {
					try {
						JSONObject videoJsonObj = new JSONObject(videoJson);
						final String channelId   = videoJsonObj.get("channelId").toString();
						final String channelName = videoJsonObj.get("channelName").toString();
						video.setChannel(new YouTubeChannel(channelId, channelName));
					} catch (JSONException e) {
						Logger.e(this, "Error occurred while extracting channel{Id,Name} from JSON", e);
					}
				}
				// regenerate the video's PublishDatePretty (e.g. 5 hours ago)
				video.forceRefreshPublishDatePretty();
				// add the video to the list
				videos.add(video);
			} while(cursor.moveToNext());
		}

		cursor.close();
		return videos;
	}

	/**
	 * Called when the PrivateLists DB is updated by either a private list insertion or deletion.
	 */
	private void onUpdated(String channelId) {
		for (SubscriptionsDb.PublicListsDbListener listener : listeners)
			listener.onPublicListsDbUpdated(channelId);
	}

	public interface PublicListsDbListener {
		/**
		 * Will be called once the PublicLists DB is updated (by either a insertion or
		 * deletion).
		 */
		void onPublicListsDbUpdated(String channelId);
	}
}
