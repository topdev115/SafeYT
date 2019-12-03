package com.zaytoona.youtube.safe.businessobjects.db;

/**
 * Private List Items Table
 */
public class PrivateListItemsTable {
    public static final String TABLE_NAME = "PrivateListItems";
    public static final String COL_YOUTUBE_VIDEO_ID = "YouTube_Video_Id";
    public static final String COL_YOUTUBE_VIDEO = "YouTube_Video";
    public static final String COL_PRIVATE_LIST_ID = "Private_List_Id";
    public static final String COL_ORDER = "Order_Index";

    public static String getCreateStatement() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                COL_YOUTUBE_VIDEO_ID + " TEXT NOT NULL, " +
                COL_PRIVATE_LIST_ID + " TEXT NOT NULL, " +
                COL_YOUTUBE_VIDEO + " BLOB, " +
                COL_ORDER + " INTEGER, " +
                "PRIMARY KEY ( " + COL_YOUTUBE_VIDEO_ID + ", " + COL_PRIVATE_LIST_ID + " )" +
                " )";
    }
}
