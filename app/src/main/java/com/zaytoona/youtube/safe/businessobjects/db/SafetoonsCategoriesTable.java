package com.zaytoona.youtube.safe.businessobjects.db;

public class SafetoonsCategoriesTable {
    public static final String TABLE_NAME = "SafetoonsCategories";
    public static final String COL_CATEGORY_ID = "Category_Id";
    public static final String COL_CATEGORY_TITLE = "Category_Title";;
    public static final String COL_CATEGORY_DESCRIPTION = "Category_Description";;
    public static final String COL_CATEGORY_AVATAR_URL = "Category_Avatar_URL";;

    public static String getCreateStatement() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                COL_CATEGORY_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                COL_CATEGORY_TITLE + " TEXT NOT NULL, " +
                COL_CATEGORY_DESCRIPTION + " TEXT, " +
                COL_CATEGORY_AVATAR_URL + " TEXT " +
                " )";
    }
}
