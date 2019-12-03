package com.zaytoona.youtube.safe.businessobjects.firebase;

import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;

import java.util.List;

public class MultiCheckCategory extends MultiCheckExpandableGroup {

    private int iconResId;
    private String description = null;
    private String avatarURL = null;

    public MultiCheckCategory(String title, List<MultiCheckPlayList> items, int iconResId, String description, String avatarURL) {
        super(title, items);
        this.iconResId = iconResId;
        this.description = description;
        this.avatarURL = avatarURL;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getDescription() {
        return description;
    }

    public String getAvatarURL() {
        return avatarURL;
    }
}

