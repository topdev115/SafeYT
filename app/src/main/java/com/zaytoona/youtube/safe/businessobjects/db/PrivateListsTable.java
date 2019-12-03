package com.zaytoona.youtube.safe.businessobjects.db;


/**
 * Private List Items Table
 */
public class PrivateListsTable {
    public static final String TABLE_NAME = "PrivateLists";
    public static final String COL_PRIVATE_LIST_ID = "Private_List_Id";
    public static final String COL_PRIVATE_LIST_SHOW = "Private_List_Show"; // 1 to show, hide otherwise
    public static final String COL_PRIVATE_LIST_THUMBNAIL_URL = "Private_List_Thumbnail_Url";
    public static final String COL_PRIVATE_LIST_CREATE_DATE = "Private_List_Create_Date";
    public static final String COL_PRIVATE_LIST_UPDATE_DATE = "Private_List_Update_Date";

    public static String getCreateStatement() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                COL_PRIVATE_LIST_ID + " TEXT PRIMARY KEY NOT NULL, " +
                COL_PRIVATE_LIST_SHOW + " INTEGER, " +
                COL_PRIVATE_LIST_THUMBNAIL_URL + " TEXT, " +
                COL_PRIVATE_LIST_CREATE_DATE + " TEXT, " +
                COL_PRIVATE_LIST_UPDATE_DATE + " TEXT " +
                " )";
    }
}
