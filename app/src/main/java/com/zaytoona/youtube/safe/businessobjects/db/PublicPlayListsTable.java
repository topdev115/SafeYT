package com.zaytoona.youtube.safe.businessobjects.db;

public class PublicPlayListsTable {
    public static final String TABLE_NAME = "PublicPlayLists";
    public static final String COL_PUBLIC_PLAY_LIST_ID = "Public_Play_List_Id";
    public static final String COL_PUBLIC_PLAY_LIST_CATEGORY_ID = "Public_Play_List_Category_Id";
    public static final String COL_PUBLIC_PLAY_LIST_TITLE = "Public_Play_List_Title";;
    public static final String COL_PUBLIC_PLAY_LIST_SHOW = "Public_List_Show"; // 1 to show, hide otherwise
    public static final String COL_PUBLIC_PLAY_LIST_ORDER_ID = "Public_List_Order_Id";

    public static String getCreateStatement() {
        return "CREATE TABLE " + TABLE_NAME + " (" +
                COL_PUBLIC_PLAY_LIST_ID + " TEXT NOT NULL, " +
                COL_PUBLIC_PLAY_LIST_CATEGORY_ID + " INTEGER, " +
                COL_PUBLIC_PLAY_LIST_TITLE + " TEXT NOT NULL, " +
                COL_PUBLIC_PLAY_LIST_SHOW + " INTEGER, " +
                COL_PUBLIC_PLAY_LIST_ORDER_ID + " INTEGER, " +
                " PRIMARY KEY ( " + COL_PUBLIC_PLAY_LIST_ID + " , " + COL_PUBLIC_PLAY_LIST_CATEGORY_ID + " )" +
                " )";
    }
}
