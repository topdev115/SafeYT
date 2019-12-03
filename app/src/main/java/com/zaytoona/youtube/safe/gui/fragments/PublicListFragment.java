package com.zaytoona.youtube.safe.gui.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ImageViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.AsyncTaskParallel;
import com.zaytoona.youtube.safe.businessobjects.VideoCategory;
import com.zaytoona.youtube.safe.businessobjects.YouTube.GetChannelsDetails;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.db.SubscriptionsDb;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.VideoGridAdapter;

import butterknife.BindView;

public class PublicListFragment extends VideosGridFragment {

    private String channelId;
    /** YouTube Channel */
    private YouTubeChannel channel;

    @BindView(R.id.public_list_header)
    View publicListHeaderLayout;
    @BindView(R.id.public_list_show_hide_image)
    ImageView showHideImage;

    private Handler hideHudTimerHandler = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // create and return the view
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Show header if parent mode
        if (displayMode == true) {
            publicListHeaderLayout.setVisibility(View.VISIBLE);
        }

        boolean showHide = SubscriptionsDb.getSubscriptionsDb().isdChannelHidden(this.channelId);

        if( showHide == false) {
            showHideImage.setImageResource(R.drawable.ic_show_black);
            ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
        }
        else {
            showHideImage.setImageResource(R.drawable.ic_hide_black);
            ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.red_color)));
        }

        showHideImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean showHide = SubscriptionsDb.getSubscriptionsDb().isdChannelHidden(channel.getId());

                //Toast.makeText(getActivity(), "" + showHide, Toast.LENGTH_LONG).show();

                if( showHide == false) {
                    SubscriptionsDb.getSubscriptionsDb().updateChannelHiddenStatus(channel.getId(), 1);

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
                            showHideImage.setImageResource(R.drawable.ic_show_black);
                        }
                    });
                }
                else {
                    SubscriptionsDb.getSubscriptionsDb().updateChannelHiddenStatus(channel.getId(), 0);

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            showHideImage.setImageResource(R.drawable.ic_hide_black);
                            ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.red_color)));
                        }
                    });
                }

                // To force a refresh...
                hideHudTimerHandler = new Handler();
                hideHudTimerHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideHudTimerHandler = null;

                        boolean showHide = SubscriptionsDb.getSubscriptionsDb().isdChannelHidden(PublicListFragment.this.channelId);

                        if( showHide == false) {
                            ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
                            showHideImage.setImageResource(R.drawable.ic_show_black);
                        }
                        else {
                            ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.red_color)));
                            showHideImage.setImageResource(R.drawable.ic_hide_black);
                        }
                    }
                }, 100);
            }
        });
/*
        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SafetoonsMaterialDialog(getActivity())
                        .content(R.string.private_list_confirm_delete_msg)
                        .positiveText(R.string.delete_private_list)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                PrivateListsDb.getPrivateListsDb().remove(fragmentName);
                            }
                        })
                        .negativeText(R.string.no)
                        .show();
            }
        });
*/
        swipeRefreshLayout.setEnabled(false);
    }


    public void setYouTubeChannel(YouTubeChannel youTubeChannel) {
        channel = youTubeChannel;
        videoGridAdapter.setYouTubeChannel(youTubeChannel);
    }

    public void setYouTubeChannelId(String youTubeChannelId) {

        channelId = youTubeChannelId;
        new PublicListFragment.PopulatePublicListsTask(channelId).executeInParallel();
    }

    public VideoGridAdapter getVideoGridAdapter() {
        return videoGridAdapter;
    }


    @Override
    protected VideoCategory getVideoCategory() {
        return VideoCategory.CHANNEL_VIDEOS;
    }


    @Override
    protected String getSearchString() {
        return channelId;
    }


    @Override
    public String getFragmentName() {
        if (channel != null) {
            return channel.getTitle();
        }
        else {
            return SubscriptionsDb.getSubscriptionsDb().getChannelTitle(channelId);
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_public_lists;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * A task that:
     *   1. gets the current total number of videos in this private list
     *   2. updated the UI accordingly (wrt step 1)
     *   3. get the private listed videos asynchronously.
     */
    private class PopulatePublicListsTask extends AsyncTaskParallel<Void, Void, Integer> {

        private final String TAG = PopulatePublicListsTask.class.getSimpleName();

        private String channelId = null;

        PopulatePublicListsTask(String channelId) {
            this.channelId = channelId;
        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                channel = new GetChannelsDetails().getYouTubeChannel(channelId);

                if (videoGridAdapter != null) {
                    videoGridAdapter.setYouTubeChannel(channel);
                }

            } catch (Throwable tr) {
                Log.e(TAG, "An error has occurred while getting subbed channels", tr);
            }

            return channel != null? channel.getYouTubeVideos().size() : 0;
        }

        @Override
        protected void onPostExecute(Integer numVideosPrivateListed) {
            // If no videos have been private listed, show the text notifying the user, otherwise
            // show the swipe refresh layout that contains the actual video grid.
            //Toast.makeText(getContext(),
            //"" + numVideosPrivateListed,
            //Toast.LENGTH_LONG).show();

            if (numVideosPrivateListed <= 0) {
                //swipeRefreshLayout.setVisibility(View.GONE);
                //noPrivateListVideosText.setVisibility(View.VISIBLE);
            } else {
                //swipeRefreshLayout.setVisibility(View.VISIBLE);
                //noPrivateListVideosText.setVisibility(View.GONE);

                // set video category and get the private listed videos asynchronously
                videoGridAdapter.setVideoCategory(VideoCategory.CHANNEL_VIDEOS, getFragmentName());
            }
        }
    }

}
