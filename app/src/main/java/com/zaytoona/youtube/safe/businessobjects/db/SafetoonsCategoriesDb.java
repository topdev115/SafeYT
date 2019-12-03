package com.zaytoona.youtube.safe.businessobjects.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePublicPlayList;
import com.zaytoona.youtube.safe.businessobjects.categories.SafetoonsCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * A database (DB) that stores safetoons categories.
 */

public class SafetoonsCategoriesDb extends SQLiteOpenHelperEx{
    private static volatile SafetoonsCategoriesDb safetoonsCategoriesDb = null;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "safetoonscategories.db";

    private SafetoonsCategoriesDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public static synchronized SafetoonsCategoriesDb getSafetoonsCategoriesDb() {
        if (safetoonsCategoriesDb == null) {
            safetoonsCategoriesDb = new SafetoonsCategoriesDb(SafetoonsApp.getContext());
        }

        return safetoonsCategoriesDb;
    }


    @Override
    protected void clearDatabaseInstance() {
        safetoonsCategoriesDb = null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SafetoonsCategoriesTable.getCreateStatement());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Add the specified safetoons category to database.
     * @param category safetoons category to be added
     *
     * @return True if the Safetoons Category was successfully saved to the DB.
     */
    public boolean add(SafetoonsCategory category) {

        ContentValues values = new ContentValues();
        values.put(SafetoonsCategoriesTable.COL_CATEGORY_ID, category.getId());
        values.put(SafetoonsCategoriesTable.COL_CATEGORY_TITLE, category.getTitle());
        values.put(SafetoonsCategoriesTable.COL_CATEGORY_DESCRIPTION, category.getDescription());
        values.put(SafetoonsCategoriesTable.COL_CATEGORY_AVATAR_URL, category.getAvatarURL());

        boolean addSuccessful = getWritableDatabase().insert(SafetoonsCategoriesTable.TABLE_NAME, null, values) != -1;

        //onUpdated();

        return addSuccessful;
    }

    /**
     * Add the specified category to database if not exist, else update
     * it with new data.
     * @param category safetoons category to be added
     *
     * @return True if the Safetoons Category was successfully saved to the DB, false otherwise.
     */
    public boolean addOrUpdate(SafetoonsCategory category) {

        Cursor cursor = getReadableDatabase().query(
                SafetoonsCategoriesTable.TABLE_NAME,
                new String[]{SafetoonsCategoriesTable.COL_CATEGORY_ID},
                SafetoonsCategoriesTable.COL_CATEGORY_ID + " = ?",
                new String[]{category.getId()}, null, null, null);

        boolean	categoryExists = cursor.moveToNext();

        cursor.close();

        if(!categoryExists) {
            return add(category);
        }

        ContentValues values = new ContentValues();
        values.put(SafetoonsCategoriesTable.COL_CATEGORY_TITLE, category.getTitle());
        values.put(SafetoonsCategoriesTable.COL_CATEGORY_DESCRIPTION, category.getDescription());
        values.put(SafetoonsCategoriesTable.COL_CATEGORY_AVATAR_URL, category.getAvatarURL());

        getWritableDatabase().update(SafetoonsCategoriesTable.TABLE_NAME, values, SafetoonsCategoriesTable.COL_CATEGORY_ID + " = ?", new String[]{category.getId()});

        //onUpdated();

        return false;
    }

    /**
     * Remove the specified safetoons category.
     *
     * @param category safetoons category to be added
     *
     * @return True if the safetoons category has been removed; false otherwise.
     */
    public boolean remove(String category) {

        // Delete safetoons category
        getWritableDatabase().delete(SafetoonsCategoriesTable.TABLE_NAME,
                SafetoonsCategoriesTable.COL_CATEGORY_ID + " = ?",
                new String[]{category});

        //onUpdated();

        return true;
    }

    /**
     * Get if the Safetoons Category Exists.
     *
     * @param category safetoons category to check if added
     *
     * @return : true if the Safetoons category is added, false otherwise.
     *
     */
    public boolean isSafetoonsCategoryAdded(String category) {

        boolean exists = false;

        Cursor	cursor = getReadableDatabase().query(
                SafetoonsCategoriesTable.TABLE_NAME,
                new String[]{SafetoonsCategoriesTable.COL_CATEGORY_ID},
                SafetoonsCategoriesTable.COL_CATEGORY_ID + " = ?",
                new String[]{category}, null, null, null);

        if(cursor.moveToNext()) {
            exists = true;
        }

        cursor.close();

        return exists;
    }


    /**
     * Get all safetoons categories.
     *
     *
     * @return List of safetoons categories Ids
     */
    public List<SafetoonsCategory> getSafetoonsCategories() {

        Cursor cursor = getReadableDatabase().query(
                SafetoonsCategoriesTable.TABLE_NAME,
                new String[]{SafetoonsCategoriesTable.COL_CATEGORY_ID,
                        SafetoonsCategoriesTable.COL_CATEGORY_TITLE,
                        SafetoonsCategoriesTable.COL_CATEGORY_DESCRIPTION,
                        SafetoonsCategoriesTable.COL_CATEGORY_AVATAR_URL},
                null,
                null, null, null, SafetoonsCategoriesTable.COL_CATEGORY_ID);

        List<SafetoonsCategory> list = new ArrayList<>();

        if(cursor.moveToNext()) {
            do {

                String id = cursor.getString(cursor.getColumnIndex(SafetoonsCategoriesTable.COL_CATEGORY_ID));
                String title = cursor.getString(cursor.getColumnIndex(SafetoonsCategoriesTable.COL_CATEGORY_TITLE));
                String description = cursor.getString(cursor.getColumnIndex(SafetoonsCategoriesTable.COL_CATEGORY_DESCRIPTION));
                String avatarURL = cursor.getString(cursor.getColumnIndex(SafetoonsCategoriesTable.COL_CATEGORY_AVATAR_URL));

                // add the category to the list
                list.add(new SafetoonsCategory(id, title, description, avatarURL));

            } while(cursor.moveToNext());
        }

        cursor.close();

        return list;
    }

    /**
     * Clear empty Safetoons Categories
     *
     */
    public void clearEmptySafetoonsCategories() {

        Cursor cursor = getReadableDatabase().query(
                SafetoonsCategoriesTable.TABLE_NAME,
                new String[]{SafetoonsCategoriesTable.COL_CATEGORY_ID},
                null,
                null, null, null, null);

        if(cursor.moveToNext()) {
            do {

                String id = cursor.getString(cursor.getColumnIndex(SafetoonsCategoriesTable.COL_CATEGORY_ID));

                List<YouTubePublicPlayList> list = PublicPlayListsDb.getPublicPlayListsDb().getPublicPlayListsInCategory(id);

                if(list.isEmpty()) {
                    remove(id);
                }

            } while(cursor.moveToNext());
        }
    }

}
