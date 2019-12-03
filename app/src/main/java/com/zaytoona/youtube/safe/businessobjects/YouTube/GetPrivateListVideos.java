package com.zaytoona.youtube.safe.businessobjects.YouTube;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeVideo;
import com.zaytoona.youtube.safe.businessobjects.db.PrivateListsDb;

public class GetPrivateListVideos extends GetYouTubeVideos {

    private String query;

    @Override
    public void init() throws IOException {
        noMoreVideoPages = false;
    }

    /**
     * Sets user's query.
     */
    @Override
    public void setQuery(String query) {
        if(query != null) {
            this.query = query;
        }
    }

    @Override
    public List<YouTubeVideo> getNextVideos() {

        if (!noMoreVideoPages() && query != null) {
            noMoreVideoPages = true;
            return PrivateListsDb.getPrivateListsDb().getVideosInPrivateList(query);
        }

        return null;
    }

    @Override
    public boolean noMoreVideoPages() {
        return noMoreVideoPages;
    }

}
