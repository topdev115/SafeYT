package com.zaytoona.youtube.safe.businessobjects.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePlaylist;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePublicPlayList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeVideo;
import com.zaytoona.youtube.safe.businessobjects.interfaces.OrderableDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * A database (DB) that stores user's public playlists.
 */

public class PublicPlayListsDb extends SQLiteOpenHelperEx implements OrderableDatabase {
    private static volatile PublicPlayListsDb publicPlayListsDb = null;
    private static boolean hasUpdated = false;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "publicplaylists.db";

    private List<PublicPlayListsDb.PublicPlayListsDbListener> listeners = new ArrayList<>();

    private PublicPlayListsDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public static synchronized PublicPlayListsDb getPublicPlayListsDb() {
        if (publicPlayListsDb == null) {
            publicPlayListsDb = new PublicPlayListsDb(SafetoonsApp.getContext());
        }

        return publicPlayListsDb;
    }


    @Override
    protected void clearDatabaseInstance() {
        publicPlayListsDb = null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PublicPlayListsTable.getCreateStatement());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Add the specified play list to database.
     * @param playList playlist to be added
     *
     * @return True if the playList was successfully saved to the DB.
     */
    public boolean add(YouTubePublicPlayList playList) {

        ContentValues values = new ContentValues();
        values.put(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ID, playList.getId());
        values.put(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID, new Integer(playList.getCategoryId()).intValue());
        values.put(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_TITLE, playList.getTitle());
        values.put(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ORDER_ID, playList.getOrderId());
        values.put(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_SHOW, 1); // By default the list is shown

        boolean addSuccessful = getWritableDatabase().insert(PublicPlayListsTable.TABLE_NAME, null, values) != -1;

        onUpdated();

        return addSuccessful;
    }

    /**
     * Add the specified play list to database if not exist, else update
     * it with new data.
     * @param playList playlist to be added
     *
     * @return True if the playList was added to the DB, false otherwise.
     */
    public boolean addOrUpdate(YouTubePublicPlayList playList) {

        Cursor cursor = getReadableDatabase().query(
                PublicPlayListsTable.TABLE_NAME,
                new String[]{PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ID},
                PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ID + " = ? AND " + PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID + " = ?",
                new String[]{playList.getId(), playList.getCategoryId()}, null, null, null);

        boolean	listExists = cursor.moveToNext();

        cursor.close();

        if(!listExists) {
            return add(playList);
        }

        ContentValues values = new ContentValues();
        //values.put(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID, playList.getCategoryId());
        values.put(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_TITLE, playList.getTitle());
        values.put(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ORDER_ID, playList.getOrderId());

        getWritableDatabase().update(PublicPlayListsTable.TABLE_NAME, values, PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ID + " = ? AND " + PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID + " = ?", new String[]{playList.getId(), playList.getCategoryId()});

        //onUpdated();

        return false;
    }

    /**
     * Remove the specified public play list.
     *
     * @param categoryId playlist category
     * @param publicPlayList play list to remove
     *
     * @return True if the public play list has been removed; false otherwise.
     */
    public boolean remove(String categoryId, String publicPlayList) {

        // Delete the List
        getWritableDatabase().delete(PublicPlayListsTable.TABLE_NAME,
                PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ID + " = ? AND " + PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID + " = ?",
                new String[]{publicPlayList, categoryId});

        onUpdated();

        return true;
    }

    /**
     * Remove all public play lists in a cetegory.
     *
     * @param categoryId playlist category
     *
     * @return True if the public play lists have been removed; false otherwise.
     */
    public boolean remove(String categoryId) {

        // Delete the List
        getWritableDatabase().delete(PublicPlayListsTable.TABLE_NAME,
                PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID + " = ?",
                new String[]{categoryId});

        onUpdated();

        return true;
    }


    /**
     * Set Public Play List show/hide.
     *
     * @param categoryId playlist category
     * @param publicPlayList : The list to hide\show
     * @param show: 1 to make the Public List shown, 0 otherwise.
     *
     */
    public void setPublicPlayListShow(String categoryId, String publicPlayList, int show) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_SHOW, show);

        getWritableDatabase().update(PublicPlayListsTable.TABLE_NAME, contentValues, PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ID + " = ? AND " + PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID + " = ?",
                new String[]{publicPlayList, categoryId});
    }

    /**
     * Get if the Public Play List Show/Hide.
     *
     * @param categoryId playlist category
     * @param publicPlayList : The list to get show\hide for
     *
     * @return : 1 if the Public List is shown, 0 otherwise.
     *
     */
    public int getPublicPlayListShowHide(String categoryId, String publicPlayList) {

        int show = 0;

        Cursor	cursor = getReadableDatabase().query(
                PublicPlayListsTable.TABLE_NAME,
                new String[]{PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_SHOW},
                PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ID + " = ? AND " + PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID + " = ?",
                new String[]{publicPlayList, categoryId}, null, null, null);

        if(cursor.moveToNext()) {
            show = cursor.getInt(cursor.getColumnIndex(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_SHOW));
        }

        cursor.close();

        return show;
    }

    /**
     * Get if the Public Play List Exists.
     *
     * @param categoryId playlist category
     * @param publicPlayList : The list to find if exists
     *
     * @return : true if the Public List is added, false otherwise.
     *
     */
    public boolean isPublicPlayListAdded(String categoryId, String publicPlayList) {

        boolean exists = false;

        Cursor	cursor = getReadableDatabase().query(
                PublicPlayListsTable.TABLE_NAME,
                new String[]{PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_SHOW},
                PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ID + " = ? AND " + PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID + " = ?",
                new String[]{publicPlayList, categoryId}, null, null, null);

        if(cursor.moveToNext()) {
            exists = true;
        }

        cursor.close();

        return exists;
    }


    /**
     * Get all public playlists.
     *
     *
     * @return List of Public PlayLists Ids
     */
    public List<YouTubePublicPlayList> getPublicPlayLists() {

        Cursor cursor = getReadableDatabase().query(
                PublicPlayListsTable.TABLE_NAME,
                new String[]{PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ID,
                        PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID,
                        PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_TITLE,
                        PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ORDER_ID},
                null,
                null, null, null, PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID);

        List<YouTubePublicPlayList> list = new ArrayList<>();

        if(cursor.moveToNext()) {
            do {

                String id = cursor.getString(cursor.getColumnIndex(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ID));
                String categoryId = cursor.getString(cursor.getColumnIndex(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID));
                String title = cursor.getString(cursor.getColumnIndex(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_TITLE));
                int orderId = cursor.getInt(cursor.getColumnIndex(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ORDER_ID));

                // add the public play list to the list
                list.add(new YouTubePublicPlayList(id, categoryId, title, orderId));

            } while(cursor.moveToNext());
        }

        cursor.close();

        return list;
    }

    /**
     * Get all public playlists in a category.
     *
     * @param category : The category to get lists inside to get show\hide for
     *
     * @return List of Public PlayLists Ids in a category
     */
    public List<YouTubePublicPlayList> getPublicPlayListsInCategory(String category) {

        Cursor cursor = getReadableDatabase().query(
                PublicPlayListsTable.TABLE_NAME,
                new String[]{PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ID,
                        PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID,
                        PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_TITLE,
                        PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ORDER_ID},
                PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID + " = ?",
                new String[]{category}, null, null, PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ORDER_ID);

        List<YouTubePublicPlayList> list = new ArrayList<>();

        if(cursor.moveToNext()) {
            do {

                String id = cursor.getString(cursor.getColumnIndex(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ID));
                String categoryId = cursor.getString(cursor.getColumnIndex(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_CATEGORY_ID));
                String title = cursor.getString(cursor.getColumnIndex(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_TITLE));
                int orderId = cursor.getInt(cursor.getColumnIndex(PublicPlayListsTable.COL_PUBLIC_PLAY_LIST_ORDER_ID));

                // add the public play list to the list
                list.add(new YouTubePublicPlayList(id, categoryId, title, orderId));

            } while(cursor.moveToNext());
        }

        cursor.close();

        return list;
    }

    public void removeNotInList(List<YouTubePublicPlayList> validLists) {

        List<YouTubePublicPlayList> lists = getPublicPlayLists();

        for(int i = 0; i < lists.size(); i++) {

            String id = lists.get(i).getId();
            int categoryId = new Integer(lists.get(i).getCategoryId()).intValue();

            boolean found = false;

            for(int j = 0; j < validLists.size(); j++) {

                int catId = new Integer(validLists.get(j).getCategoryId()).intValue();

                if(id.equals(validLists.get(j).getId()) && categoryId == catId) {
                    found = true;
                    break;
                }
            }

            if(found == false) {
                remove(lists.get(i).getCategoryId(), id);
            }
        }

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
/*
        int order = videos.size();

        for(YouTubeVideo video : videos) {
            ContentValues cv = new ContentValues();
            cv.put(PrivateListItemsTable.COL_ORDER, order--);
            getWritableDatabase().update(PrivateListItemsTable.TABLE_NAME, cv, PrivateListItemsTable.COL_YOUTUBE_VIDEO_ID + " = ?", new String[]{video.getId()});
        }
*/
    }

    /**
     * Add a Listener that will be notified when a Video is added or removed from private list. This will
     * allow the Video Grid to be redrawn in order to remove the video from display.
     *
     * @param listener The Listener (which implements PrivateListsDbListener) to add.
     */
    public void addListener(PublicPlayListsDb.PublicPlayListsDbListener listener) {
        if(!listeners.contains(listener))
            listeners.add(listener);
    }


    /**
     * Called when the PrivateLists DB is updated by either a private list insertion or deletion.
     */
    private void onUpdated() {
        hasUpdated = true;

        for (PublicPlayListsDb.PublicPlayListsDbListener listener : listeners)
            listener.onPublicPlayListsDbUpdated();
    }

    public interface PublicPlayListsDbListener {
        /**
         * Will be called once the PublicPlayLists DB is updated (by either a playlist insertion or
         * deletion).
         */
        void onPublicPlayListsDbUpdated();
    }
}
