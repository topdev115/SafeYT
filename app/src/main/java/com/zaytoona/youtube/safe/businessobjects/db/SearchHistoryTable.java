package com.zaytoona.youtube.safe.businessobjects.db;

/**
 * Search History Table
 */
public class SearchHistoryTable {
	public static final String TABLE_NAME = "SearchHistory";
	public static final String COL_SEARCH_ID = "_id";
	public static final String COL_SEARCH_TEXT = "Search_Text";

	public static String getCreateStatement() {
		return "CREATE TABLE " + TABLE_NAME + " (" +
						COL_SEARCH_ID + " INTEGER PRIMARY KEY NOT NULL, " +
						COL_SEARCH_TEXT + " TEXT " +
						" )";
	}

}
