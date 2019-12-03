package com.zaytoona.youtube.safe.businessobjects.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeVideo;
import com.zaytoona.youtube.safe.businessobjects.db.model.PrivateListInfo;
import com.zaytoona.youtube.safe.businessobjects.interfaces.OrderableDatabase;

/**
 * A database (DB) that stores user's private lists.
 */
public class PrivateListsDb extends SQLiteOpenHelperEx implements OrderableDatabase {
    private static volatile PrivateListsDb privateListsDb = null;
    private static boolean hasUpdated = false;
    private static boolean hasUpdatedItem = false;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "privatelists.db";

    private List<PrivateListsDbListener> listeners = new ArrayList<>();


    private PrivateListsDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public static synchronized PrivateListsDb getPrivateListsDb() {
        if (privateListsDb == null) {
            privateListsDb = new PrivateListsDb(SafetoonsApp.getContext());
        }

        return privateListsDb;
    }


    @Override
    protected void clearDatabaseInstance() {
        privateListsDb = null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PrivateListsTable.getCreateStatement());
        db.execSQL(PrivateListItemsTable.getCreateStatement());
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Add the specified Private List.
     *
     * @param privateList private list to add
     *
     * @return True if the private list was successfully saved to the DB.
     */
    public boolean add(String privateList) {
        Gson gson = new Gson();

        ContentValues values = new ContentValues();
        values.put(PrivateListsTable.COL_PRIVATE_LIST_ID, privateList);
        values.put(PrivateListsTable.COL_PRIVATE_LIST_SHOW, 1); // By default the list is shown
        values.put(PrivateListsTable.COL_PRIVATE_LIST_CREATE_DATE, new DateTime(new Date()).toString());
        values.put(PrivateListsTable.COL_PRIVATE_LIST_UPDATE_DATE, new DateTime(new Date()).toString());


        boolean addSuccessful = getWritableDatabase().insert(PrivateListsTable.TABLE_NAME, null, values) != -1;

        // Possibly have one for Lists (the current one is for List Items)
        onUpdated();

        return addSuccessful;
    }

    /**
     * Remove the specified private list and all items added to it.
     *
     * @param privateList private list to remove
     *
     * @return True if the private list has been removed; false otherwise.
     */
    public boolean remove(String privateList) {

        // Delete the List Items
        int rowsDeleted = getWritableDatabase().delete(PrivateListItemsTable.TABLE_NAME,
                PrivateListItemsTable.COL_PRIVATE_LIST_ID + " = ?",
                new String[]{privateList});

        // Delete the List
        rowsDeleted = getWritableDatabase().delete(PrivateListsTable.TABLE_NAME,
                PrivateListsTable.COL_PRIVATE_LIST_ID + " = ?",
                new String[]{privateList});

        onUpdated();

        return true;
    }

    /**
     * Get all private lists.
     *
     *
     * @return List of Private Lists
     */
    public List<String> getPrivateLists() {

        Cursor	cursor = getReadableDatabase().query(
                PrivateListsTable.TABLE_NAME,
                new String[]{PrivateListsTable.COL_PRIVATE_LIST_ID},
                null,
                null, null, null, null);

        List<String> lists = new ArrayList<>();

        if(cursor.moveToNext()) {
            do {
                String privateList = cursor.getString(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_ID));

                // add the private list to the list
                lists.add(privateList);

            } while(cursor.moveToNext());
        }

        cursor.close();

        return lists;
    }

    /**
     * Get a private list Info.
     *
     *
     * @return Info of a PrivateList
     */
    public PrivateListInfo getPrivateListInfo(String privateList) {

        PrivateListInfo privateListInfo = null;

        Cursor	cursor = getReadableDatabase().query(
                PrivateListsTable.TABLE_NAME,
                new String[]{PrivateListsTable.COL_PRIVATE_LIST_ID, PrivateListsTable.COL_PRIVATE_LIST_SHOW, PrivateListsTable.COL_PRIVATE_LIST_THUMBNAIL_URL,
                        PrivateListsTable.COL_PRIVATE_LIST_CREATE_DATE, PrivateListsTable.COL_PRIVATE_LIST_UPDATE_DATE},
                PrivateListsTable.COL_PRIVATE_LIST_ID + " = ?",
                new String[]{privateList}, null, null, null);

        if(cursor.moveToNext()) {

            int show = cursor.getInt(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_SHOW));
            String thumbNailUrl = cursor.getString(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_THUMBNAIL_URL));

            String createDate = cursor.getString(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_CREATE_DATE));
            String updateDate = cursor.getString(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_UPDATE_DATE));

            privateListInfo = new PrivateListInfo(privateList, show, thumbNailUrl, getTotalVideosInList(privateList), new DateTime(createDate), new DateTime(updateDate));
        }

        cursor.close();

        return privateListInfo;
    }


    /**
     * Get all private lists.
     *
     *
     * @return List of Private Lists
     */
    public List<PrivateListInfo> getPrivateListsInfo() {

        Cursor	cursor = getReadableDatabase().query(
                PrivateListsTable.TABLE_NAME,
                new String[]{PrivateListsTable.COL_PRIVATE_LIST_ID, PrivateListsTable.COL_PRIVATE_LIST_SHOW, PrivateListsTable.COL_PRIVATE_LIST_THUMBNAIL_URL,
                        PrivateListsTable.COL_PRIVATE_LIST_CREATE_DATE, PrivateListsTable.COL_PRIVATE_LIST_UPDATE_DATE},
                null,
                null, null, null, null);

        List<PrivateListInfo> lists = new ArrayList<>();

        if(cursor.moveToNext()) {
            do {
                String privateList = cursor.getString(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_ID));
                int show = cursor.getInt(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_SHOW));
                String thumbNailUrl = cursor.getString(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_THUMBNAIL_URL));
                int count = getTotalVideosInList(privateList);

                String createDate = cursor.getString(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_CREATE_DATE));
                String updateDate = cursor.getString(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_UPDATE_DATE));

                PrivateListInfo privateListInfo = new PrivateListInfo(privateList, show, thumbNailUrl, count, new DateTime(createDate), new DateTime(updateDate));

                // add the private list to the list
                lists.add(privateListInfo);

            } while(cursor.moveToNext());
        }

        cursor.close();

        return lists;
    }

    /**
     * Set Private List show/hide.
     *
     * @param privateList : The list to hide\show
     * @param show: 1 to make the Private List shown, 0 otherwise.
     *
     */
    public void setPrivateListShow(String privateList, int show) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(PrivateListsTable.COL_PRIVATE_LIST_SHOW, show);

        getWritableDatabase().update(PrivateListsTable.TABLE_NAME, contentValues, PrivateListsTable.COL_PRIVATE_LIST_ID + " = ?",
                new String[]{privateList});
    }

    /**
     * Get if the Private List Show/Hide.
     *
     * @param privateList : The list to get show\hide for
     *
     * @return : 1 if the Private List is shown, 0 otherwise.
     *
     */
    public int getPrivateListShowHide(String privateList) {

        int show = 0;

        Cursor	cursor = getReadableDatabase().query(
                PrivateListsTable.TABLE_NAME,
                new String[]{PrivateListsTable.COL_PRIVATE_LIST_SHOW},
                PrivateListsTable.COL_PRIVATE_LIST_ID + " = ?",
                new String[]{privateList}, null, null, null);

        if(cursor.moveToNext()) {
            show = cursor.getInt(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_SHOW));
        }

        cursor.close();

        return show;
    }

    /**
     * Set Private List ThumbNailUrl.
     *
     * @param privateList : The list to set thumbnamil for
     * @param thumbNailUrl: The thumbNailUrl to set.
     *
     */
    public void setPrivateListThumbNailUrl(String privateList, String thumbNailUrl) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(PrivateListsTable.COL_PRIVATE_LIST_THUMBNAIL_URL, thumbNailUrl);

        getWritableDatabase().update(PrivateListsTable.TABLE_NAME, contentValues, PrivateListsTable.COL_PRIVATE_LIST_ID + " = ?",
                new String[]{privateList});
    }


    /**
     * Get the Private List thumbNailUrl.
     *
     * @param privateList : The list to get thumbNailUrl for
     *
     * @return : The thumbNailUrl of the List
     *
     */
    public String getPrivateListThumbNailUrl(String privateList) {

        String thumbNailUrl = null;

        Cursor	cursor = getReadableDatabase().query(
                PrivateListsTable.TABLE_NAME,
                new String[]{PrivateListsTable.COL_PRIVATE_LIST_THUMBNAIL_URL},
                PrivateListsTable.COL_PRIVATE_LIST_ID + " = ?",
                new String[]{privateList}, null, null, null);

        if(cursor.moveToNext()) {
            thumbNailUrl = cursor.getString(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_THUMBNAIL_URL));
        }

        cursor.close();

        return thumbNailUrl;
    }

    /**
     * Set Private List Update Date.
     *
     * @param privateList : The list to set update date for
     * @param updateDate: The date to set.
     *
     */
    public void setPrivateListUpdateDate(String privateList, DateTime updateDate) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(PrivateListsTable.COL_PRIVATE_LIST_UPDATE_DATE, updateDate.toString());

        getWritableDatabase().update(PrivateListsTable.TABLE_NAME, contentValues, PrivateListsTable.COL_PRIVATE_LIST_ID + " = ?",
                new String[]{privateList});
    }


    /**
     * Get the Private List thumbNailUrl.
     *
     * @param privateList : The list to get thumbNailUrl for
     *
     * @return : The thumbNailUrl of the List
     *
     *//*
    public String getPrivateListThumbNailUrl(String privateList) {

        String thumbNailUrl = null;

        Cursor	cursor = getReadableDatabase().query(
                PrivateListsTable.TABLE_NAME,
                new String[]{PrivateListsTable.COL_PRIVATE_LIST_THUMBNAIL_URL},
                PrivateListsTable.COL_PRIVATE_LIST_ID + " = ?",
                new String[]{privateList}, null, null, null);

        if(cursor.moveToNext()) {
            thumbNailUrl = cursor.getString(cursor.getColumnIndex(PrivateListsTable.COL_PRIVATE_LIST_THUMBNAIL_URL));
        }

        cursor.close();

        return thumbNailUrl;
    }
*/

    /**
     * Add the specified video to the private list specified by privateList. The video will
     * appear at the top of the list (when displayed in the grid, videos will be ordered
     * by the Order field, descending.
     *
     * @param video Video to add
     * @param privateList list to add the video to
     *
     * @return True if the video was successfully saved to the DB.
     */
    public boolean add(YouTubeVideo video, String privateList) {
        Gson gson = new Gson();

        ContentValues values = new ContentValues();
        values.put(PrivateListItemsTable.COL_YOUTUBE_VIDEO_ID, video.getId());
        values.put(PrivateListItemsTable.COL_YOUTUBE_VIDEO, gson.toJson(video).getBytes());
        values.put(PrivateListItemsTable.COL_PRIVATE_LIST_ID, privateList);

        int order = getTotalVideos();

        order++;
        values.put(PrivateListItemsTable.COL_ORDER, order);

        boolean addSuccessful = getWritableDatabase().insert(PrivateListItemsTable.TABLE_NAME, null, values) != -1;

        if(addSuccessful) {
            setPrivateListThumbNailUrl(privateList, video.getThumbnailUrl());
            setPrivateListUpdateDate(privateList, new DateTime(new Date()));
        }

        onUpdatedItems();

        return addSuccessful;
    }

    /**
     * Add the list of videos to the private list specified by privateList.
     *
     * @param iterator Video List to add
     * @param privateList list to add the video list to
     *
     * @return True if the video was successfully saved to the DB.
     */
    public boolean add(Iterator<YouTubeVideo> iterator, String privateList) {

        boolean addSuccessful = true;
        YouTubeVideo video = null;
        ContentValues values = null;
        Gson gson = new Gson();

        while(iterator.hasNext()) {
            video = iterator.next();

            values = new ContentValues();

            values.put(PrivateListItemsTable.COL_YOUTUBE_VIDEO_ID, video.getId());
            values.put(PrivateListItemsTable.COL_YOUTUBE_VIDEO, gson.toJson(video).getBytes());
            values.put(PrivateListItemsTable.COL_PRIVATE_LIST_ID, privateList);

            int order = getTotalVideos();

            order++;
            values.put(PrivateListItemsTable.COL_ORDER, order);

            addSuccessful = getWritableDatabase().insert(PrivateListItemsTable.TABLE_NAME, null, values) != -1;

            if(addSuccessful) {
                setPrivateListThumbNailUrl(privateList, video.getThumbnailUrl());
                setPrivateListUpdateDate(privateList, new DateTime(new Date()));
            }
        }

        onUpdatedItems();

        return addSuccessful;
    }

    /**
     * Remove the specified video from the private list.
     *
     * @param video Video to remove.
     *
     * @return True if the video has been removed; false otherwise.
     */
    public boolean remove(String searchQuery, YouTubeVideo video) {

        int rowsDeleted = getWritableDatabase().delete(PrivateListItemsTable.TABLE_NAME,
                PrivateListItemsTable.COL_PRIVATE_LIST_ID + " = ? AND " + PrivateListItemsTable.COL_YOUTUBE_VIDEO_ID + " = ? ",
                new String[]{searchQuery, video.getId()});

        boolean successful = false;

        if(rowsDeleted >= 0) {
            // Since we've removed a video, we will need to update the order column for all the videos.
            int order = 1;

            Cursor cursor = getReadableDatabase().query(
                    PrivateListItemsTable.TABLE_NAME,
                    new String[]{PrivateListItemsTable.COL_PRIVATE_LIST_ID, PrivateListItemsTable.COL_YOUTUBE_VIDEO, PrivateListItemsTable.COL_ORDER},
                    null,
                    null, null, null, PrivateListItemsTable.COL_ORDER + " ASC");
            if(cursor.moveToNext()) {
                do {
                    byte[] blob = cursor.getBlob(cursor.getColumnIndex(PrivateListItemsTable.COL_YOUTUBE_VIDEO));
                    YouTubeVideo uvideo = new Gson().fromJson(new String(blob), new TypeToken<YouTubeVideo>(){}.getType());
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(PrivateListItemsTable.COL_ORDER, order++);

                    getWritableDatabase().update(PrivateListItemsTable.TABLE_NAME, contentValues, PrivateListItemsTable.COL_YOUTUBE_VIDEO_ID + " = ?",
                            new String[]{uvideo.getId()});
                } while(cursor.moveToNext());
            }

            // Get videos in the private list to update the thumbnail url as it could be deleted
            cursor = getReadableDatabase().query(
                    PrivateListItemsTable.TABLE_NAME,
                    new String[]{PrivateListItemsTable.COL_PRIVATE_LIST_ID, PrivateListItemsTable.COL_YOUTUBE_VIDEO},
                    PrivateListItemsTable.COL_PRIVATE_LIST_ID + " = ? ",
                    new String[]{searchQuery}, null, null, PrivateListItemsTable.COL_ORDER + " DESC");

            // Update the List if the removed video's thumbnail was the one set to for the Private Play List
            if(cursor.moveToFirst()) {
                // Last video in that Private List
                byte[] blob = cursor.getBlob(cursor.getColumnIndex(PrivateListItemsTable.COL_YOUTUBE_VIDEO));

                YouTubeVideo uvideo = new Gson().fromJson(new String(blob), new TypeToken<YouTubeVideo>() {}.getType());
                String thumbNailUrl = uvideo.getThumbnailUrl();

                setPrivateListThumbNailUrl(searchQuery, thumbNailUrl);
            }

            cursor.close();

            onUpdatedItems();
            successful = true;
        }

        return successful;
    }


    /**
     * When a Video in the Private List tab is drag & dropped to a new position, this will be
     * called with the new updated list of videos. Since the videos are displayed in descending order,
     * the first video in the list will have the highest number.
     *
     * @param videos List of Videos to update their order.
     */
    @Override
    public void updateOrder(List<YouTubeVideo> videos) {

        int order = videos.size();

        for(YouTubeVideo video : videos) {
            ContentValues cv = new ContentValues();
            cv.put(PrivateListItemsTable.COL_ORDER, order--);
            getWritableDatabase().update(PrivateListItemsTable.TABLE_NAME, cv, PrivateListItemsTable.COL_YOUTUBE_VIDEO_ID + " = ?", new String[]{video.getId()});
        }
    }


    /**
     * Check if the specified Video has been private listed.
     *
     * @param video Video to check
     * @param privateList private list to check if video is in
     *
     * @return True if it has been private listed in privateList, false if not.
     */
    public boolean isPrivateListed(String privateList ,YouTubeVideo video) {

        Cursor cursor = getReadableDatabase().query(
                PrivateListItemsTable.TABLE_NAME,
                new String[]{PrivateListItemsTable.COL_YOUTUBE_VIDEO_ID},
                PrivateListItemsTable.COL_YOUTUBE_VIDEO_ID + " = ? AND " + PrivateListItemsTable.COL_PRIVATE_LIST_ID + " = ?",
                new String[]{video.getId(), privateList}, null, null, null);
        boolean	hasVideo = cursor.moveToNext();

        cursor.close();
        return hasVideo;
    }

    /**
     * Check if the specified Video has been private listed in a certain list.
     *
     * @param video Video to check
     *
     * @return True if it has been private listed, false if not.
     */
    public boolean isPrivateListed(YouTubeVideo video) {

        if(video == null) {
            return false;
        }

        Cursor cursor = getReadableDatabase().query(
                PrivateListItemsTable.TABLE_NAME,
                new String[]{PrivateListItemsTable.COL_YOUTUBE_VIDEO_ID},
                PrivateListItemsTable.COL_YOUTUBE_VIDEO_ID + " = ?",
                new String[]{video.getId()}, null, null, null);

        boolean	hasVideo = cursor.moveToNext();

        cursor.close();
        return hasVideo;
    }

    /**
     * @return The total number of videos (all lists).
     */
    public int getTotalVideos() {
        String	query = String.format("SELECT COUNT(*) FROM %s", PrivateListItemsTable.TABLE_NAME);
        Cursor	cursor = PrivateListsDb.getPrivateListsDb().getReadableDatabase().rawQuery(query, null);

        int totalVideos = 0;

        if (cursor.moveToFirst()) {
            totalVideos = cursor.getInt(0);
        }

        cursor.close();

        return totalVideos;
    }

    /**
     * @param privateList private list for which the total number is found
     * @return The total number of videos in a private list.
     */
    public int getTotalVideosInList(String privateList) {
        String	query = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", PrivateListItemsTable.TABLE_NAME, PrivateListItemsTable.COL_PRIVATE_LIST_ID);

        Cursor	cursor = PrivateListsDb.getPrivateListsDb().getReadableDatabase().rawQuery(query, new String[]{privateList});

        int	totalVideosInList = 0;

        if (cursor.moveToFirst()) {
            totalVideosInList = cursor.getInt(0);
        }

        cursor.close();
        return totalVideosInList;
    }


    /**
     * Get the list of Videos in a private list.
     *
     * @param privateList private list of the returned videos
     *
     * @return List of Videos
     */
    public List<YouTubeVideo> getVideosInPrivateList(String privateList) {

        Cursor	cursor = getReadableDatabase().query(
                PrivateListItemsTable.TABLE_NAME,
                new String[]{PrivateListItemsTable.COL_YOUTUBE_VIDEO, PrivateListItemsTable.COL_ORDER},
                PrivateListItemsTable.COL_PRIVATE_LIST_ID + " = ?",
                new String[]{privateList}, null, null, PrivateListItemsTable.COL_ORDER + " DESC");

        List<YouTubeVideo> videos = new ArrayList<>();

        if(cursor.moveToNext()) {
            do {
                final byte[] blob = cursor.getBlob(cursor.getColumnIndex(PrivateListItemsTable.COL_YOUTUBE_VIDEO));

                final String videoJson = new String(blob);

                // convert JSON into YouTubeVideo
                YouTubeVideo video = new Gson().fromJson(new String(blob), new TypeToken<YouTubeVideo>(){}.getType());

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
     * Add a Listener that will be notified when a Video is added or removed from private list. This will
     * allow the Video Grid to be redrawn in order to remove the video from display.
     *
     * @param listener The Listener (which implements PrivateListsDbListener) to add.
     */
    public void addListener(PrivateListsDbListener listener) {
        if(!listeners.contains(listener))
            listeners.add(listener);
    }


    /**
     * Called when the PrivateLists DB is updated by either a private list insertion or deletion.
     */
    private void onUpdated() {
        hasUpdated = true;

        for (PrivateListsDbListener listener : listeners)
            listener.onPrivateListsDbUpdated();
    }

    public static boolean isHasUpdated() {
        return hasUpdated;
    }

    public static void setHasUpdated(boolean hasUpdated) {
        PrivateListsDb.hasUpdated = hasUpdated;
    }


    /**
     * Called when the PrivateLists DB is updated by either a video insertion or deletion.
     */
    private void onUpdatedItems() {
        hasUpdatedItem = true;

        for (PrivateListsDbListener listener : listeners)
            listener.onPrivateListItemsDbUpdated();
    }

    public static boolean isHasUpdatedItems() {
        return hasUpdatedItem;
    }

    public static void setHasUpdatedItem(boolean hasUpdatedItem) {
        PrivateListsDb.hasUpdatedItem = hasUpdatedItem;
    }

    public interface PrivateListsDbListener {
        /**
         * Will be called once the PrivateLists DB is updated (by either a playlist insertion or
         * deletion).
         */
        void onPrivateListsDbUpdated();

        /**
         * Will be called once the PrivateLists DB is updated (by either a video insertion or
         * deletion to a playlist ).
         */
        void onPrivateListItemsDbUpdated();
    }
}
