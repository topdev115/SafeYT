package com.zaytoona.youtube.safe.gui.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ImageViewCompat;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.AsyncTaskParallel;
import com.zaytoona.youtube.safe.businessobjects.VideoCategory;
import com.zaytoona.youtube.safe.businessobjects.db.PrivateListsDb;
import com.zaytoona.youtube.safe.gui.businessobjects.SafetoonsMaterialDialog;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.OrderableVideoGridAdapter;
import com.zaytoona.youtube.safe.gui.businessobjects.fragments.OrderableVideosGridFragment;

public class PrivateListFragment extends OrderableVideosGridFragment implements PrivateListsDb.PrivateListsDbListener {
    @BindView(R.id.noPrivateListVideosText)
    View noPrivateListVideosText;
    @BindView(R.id.private_list_header)
    View privateListHeaderLayout;
    @BindView(R.id.private_list_delete_image)
    ImageView deleteImage;
    @BindView(R.id.private_list_show_hide_image)
    ImageView showHideImage;

    public String fragmentName = null;

    private Handler hideHudTimerHandler = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoGridAdapter = new OrderableVideoGridAdapter(getActivity(), PrivateListsDb.getPrivateListsDb(), displayMode);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Show header if parent mode
        if (displayMode == true) {
            privateListHeaderLayout.setVisibility(View.VISIBLE);
        }

        int showHide = PrivateListsDb.getPrivateListsDb().getPrivateListShowHide(fragmentName);

        if( showHide == 1) {
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

                int showHide = PrivateListsDb.getPrivateListsDb().getPrivateListShowHide(fragmentName);

                //Toast.makeText(getActivity(), "" + showHide, Toast.LENGTH_LONG).show();

                if( showHide == 1) {
                    PrivateListsDb.getPrivateListsDb().setPrivateListShow(fragmentName, 0);
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            ImageViewCompat.setImageTintList(showHideImage, ColorStateList.valueOf(getResources().getColor(R.color.green_color)));
                            showHideImage.setImageResource(R.drawable.ic_show_black);
                        }
                    });
                }
                else {
                    PrivateListsDb.getPrivateListsDb().setPrivateListShow(fragmentName, 1);

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

                        int showHide = PrivateListsDb.getPrivateListsDb().getPrivateListShowHide(fragmentName);
                        if( showHide == 1) {
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

        swipeRefreshLayout.setEnabled(false);
        populateList();
    }


    private void populateList() {
        //new PrivateListFragment.PopulatePrivateListsTask(fragmentName).executeInParallel();
        new PrivateListFragment.PopulatePrivateListsTask(fragmentName).executeInParallel();
    }

/*
    @Override
    public void onFragmentSelected() {
        super.onFragmentSelected();

        //if (PrivateListsDb.getPrivateListsDb().isHasUpdatedItems()) {
            populateList();
            PrivateListsDb.getPrivateListsDb().setHasUpdatedItem(false);
        //}
    }
*/

    @Override
    public void onPrivateListsDbUpdated() {
        // Not used
    }

    @Override
    public void onPrivateListItemsDbUpdated() {

        // Because it will reload
        populateList();

        if(videoGridAdapter != null)
            videoGridAdapter.refresh(true);
    }
    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_private_lists;
    }


    @Override
    protected VideoCategory getVideoCategory() {
        return VideoCategory.PRIVATE_LIST_VIDEOS;
    }


    @Override
    public String getFragmentName() {
        return fragmentName; //SafetoonsApp.getStr(R.string.privatelists);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * A task that:
     *   1. gets the current total number of videos in this private list
     *   2. updated the UI accordingly (wrt step 1)
     *   3. get the private listed videos asynchronously.
     */
    private class PopulatePrivateListsTask extends AsyncTaskParallel<Void, Void, Integer> {

        private String fragmentName = null;

        PopulatePrivateListsTask(String fragmentName) {
            this.fragmentName = fragmentName;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return PrivateListsDb.getPrivateListsDb().getTotalVideosInList(this.fragmentName);
        }

        @Override
        protected void onPostExecute(Integer numVideosPrivateListed) {
            // If no videos have been private listed, show the text notifying the user, otherwise
            // show the swipe refresh layout that contains the actual video grid.
            //Toast.makeText(getContext(),
                    //"" + numVideosPrivateListed,
                    //Toast.LENGTH_LONG).show();

            if (numVideosPrivateListed <= 0) {
                swipeRefreshLayout.setVisibility(View.GONE);
                noPrivateListVideosText.setVisibility(View.VISIBLE);
            } else {
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                noPrivateListVideosText.setVisibility(View.GONE);

                // set video category and get the private listed videos asynchronously
                videoGridAdapter.setVideoCategory(VideoCategory.PRIVATE_LIST_VIDEOS, this.fragmentName);
                videoGridAdapter.refresh(true);
            }
        }
    }
}
