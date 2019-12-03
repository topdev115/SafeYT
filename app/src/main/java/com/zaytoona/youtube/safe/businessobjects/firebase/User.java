package com.zaytoona.youtube.safe.businessobjects.firebase;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String uid;
    public String dateTime;
    public String deviceId;
    public String token;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String dateTime, String deviceId, String token) {
        this.uid = uid;
        this.dateTime = dateTime;
        this.deviceId = deviceId;
        this.token = token;
    }
}
