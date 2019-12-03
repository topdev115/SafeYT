package com.zaytoona.youtube.safe.businessobjects.firebase;

import android.os.Parcel;
import android.os.Parcelable;

public class MultiCheckPlayList implements Parcelable {

    private String name;
    private String key;

    public MultiCheckPlayList(String name, String key) {
        this.name = name;
        this.key = key;
    }

    protected MultiCheckPlayList(Parcel in) {
        name = in.readString();
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultiCheckPlayList)) return false;

        MultiCheckPlayList child = (MultiCheckPlayList) o;

        return getName() != null ? getName().equals(child.getName()) : child.getName() == null;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MultiCheckPlayList> CREATOR = new Creator<MultiCheckPlayList>() {
        @Override
        public MultiCheckPlayList createFromParcel(Parcel in) {
            return new MultiCheckPlayList(in);
        }

        @Override
        public MultiCheckPlayList[] newArray(int size) {
            return new MultiCheckPlayList[size];
        }
    };
}

