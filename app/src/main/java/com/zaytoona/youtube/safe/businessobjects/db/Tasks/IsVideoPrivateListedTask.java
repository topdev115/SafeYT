package com.zaytoona.youtube.safe.businessobjects.db.Tasks;

import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.AsyncTaskParallel;
import com.zaytoona.youtube.safe.businessobjects.VideoCategory;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeVideo;
import com.zaytoona.youtube.safe.businessobjects.db.PrivateListsDb;

public class IsVideoPrivateListedTask extends AsyncTaskParallel<Void, Void, Boolean> {
    private Menu menu;
    private ImageView viewUnPrivateList;
    private YouTubeVideo youTubeVideo;
    private VideoCategory videoCategory;

    public IsVideoPrivateListedTask(YouTubeVideo youTubeVideo, Menu menu, ImageView viewUnPrivateList,VideoCategory videoCategory) {
        this.youTubeVideo = youTubeVideo;
        this.menu = menu;
        this.videoCategory = videoCategory;
        this.viewUnPrivateList = viewUnPrivateList;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return PrivateListsDb.getPrivateListsDb().isPrivateListed(youTubeVideo);
    }

    @Override
    protected void onPostExecute(Boolean videoIsPrivatelisted) {
        // if this video has been private, hide the privatelist option and show the unprivate option.
        if(videoCategory == VideoCategory.PRIVATE_LIST_VIDEOS) {
            //menu.findItem(R.id.private_list_video).setVisible(false);

            if(menu != null) {
                menu.findItem(R.id.unprivate_list_video).setVisible(true);
            }

            if(viewUnPrivateList != null) {
                viewUnPrivateList.setVisibility(View.VISIBLE);
            }
        }
        else {
            //menu.findItem(R.id.private_list_video).setVisible(true);
            if(menu != null) {
                menu.findItem(R.id.unprivate_list_video).setVisible(false);
            }

            if(viewUnPrivateList != null) {
                viewUnPrivateList.setVisibility(View.GONE);
            }
        }
    }
}
