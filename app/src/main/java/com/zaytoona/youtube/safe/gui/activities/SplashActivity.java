package com.zaytoona.youtube.safe.gui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.VideoView;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.common.General;
import com.zaytoona.youtube.safe.gui.businessobjects.fragments.FragmentEx;
import com.zaytoona.youtube.safe.gui.fragments.SafetoonsTutorialFragment;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class SplashActivity extends AppCompatActivity {

    private VideoView videoView = null;

    private static final String TUTORIAL_COMPLETED = "YouTubePlayerActivity.TutorialCompleted";

    private Boolean onCompletionCalled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        General.startService(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        videoView = findViewById(R.id.videoView);

        Uri video = null;

        if(getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
            video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.intro_video_p);
        }
        else {
            video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.intro_video_l);
        }

        videoView.setVideoURI(video);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                // Sometimes onCompletion is called twice which results
                // in two kids activities started
                if(onCompletionCalled == false) {

                    onCompletionCalled = true;

                    // if the tutorial was previously displayed, start KidMainActivity
                    if (wasTutorialDisplayedBefore()) {
                        startActivity(new Intent(SplashActivity.this, KidMainActivity.class));
                        finish();
                    } else {

                        // display the tutorial
                        FragmentEx tutorialFragment = new SafetoonsTutorialFragment().setListener(new SafetoonsTutorialFragment.SafetoonsTutorialListener() {

                            @Override
                            public void onTutorialFinished() {

                                setWasTutorialDisplayedBefore(true);

                                startActivity(new Intent(SplashActivity.this, KidMainActivity.class));
                                finish();
                            }

                        });
                        installFragment(tutorialFragment);
                    }



                }
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
        videoView.start();
    }

    /**
     * "Installs" a fragment inside the {@link FragmentManager}.
     *
     * @param fragment  Fragment to install and that is going to be displayed to the user.
     */
    private void installFragment(FragmentEx fragment) {
        // either use the Safetoons's default video player or the legacy one
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Set that the app tutorial was completed before.
     */
    private void setWasTutorialDisplayedBefore(boolean wasTutorialDisplayedBefore) {

        SharedPreferences preferences = SafetoonsApp.getPreferenceManager();

        preferences.edit().putBoolean(TUTORIAL_COMPLETED, wasTutorialDisplayedBefore).apply();
    }

    /**
     * Will check whether the app tutorial was completed before.  If no, it will return
     * false and will save the value accordingly.
     *
     * @return True if the tutorial was completed in the past.
     */
    private boolean wasTutorialDisplayedBefore() {
        SharedPreferences preferences = SafetoonsApp.getPreferenceManager();

        boolean wasTutorialDisplayedBefore = preferences.getBoolean(TUTORIAL_COMPLETED, false);

        //preferences.edit().putBoolean(TUTORIAL_COMPLETED, true).apply();

        return wasTutorialDisplayedBefore;
        //return false;
    }
}
