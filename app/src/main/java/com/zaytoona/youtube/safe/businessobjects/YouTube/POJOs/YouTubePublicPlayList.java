package com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class YouTubePublicPlayList implements Serializable {

    private String              id;
    private String              categoryId;
    private String              title;
    private int                 orderId;

    public YouTubePublicPlayList() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public YouTubePublicPlayList(String id, String categoryId, String title, int orderId) {

        this.id = id;
        this.title = title;
        this.categoryId = categoryId;
        this.orderId = orderId;
    }

    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
