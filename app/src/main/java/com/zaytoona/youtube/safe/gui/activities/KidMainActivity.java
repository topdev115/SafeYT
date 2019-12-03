package com.zaytoona.youtube.safe.gui.activities;

import android.app.ProgressDialog;
import android.arch.lifecycle.Lifecycle;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.zaytoona.pincode.managers.AppLock;
import com.zaytoona.pincode.managers.LockManager;

import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.app.SafetoonsApp;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.SafetoonsList;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubeChannel;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePlaylist;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePublicPlayList;
import com.zaytoona.youtube.safe.businessobjects.categories.SafetoonsCategory;
import com.zaytoona.youtube.safe.businessobjects.db.DownloadedVideosDb;
import com.zaytoona.youtube.safe.businessobjects.db.PublicPlayListsDb;
import com.zaytoona.youtube.safe.businessobjects.db.SafetoonsCategoriesDb;
import com.zaytoona.youtube.safe.businessobjects.firebase.User;
import com.zaytoona.youtube.safe.common.Constants;
import com.zaytoona.youtube.safe.common.General;
import com.zaytoona.youtube.safe.gui.businessobjects.MainActivityListener;
import com.zaytoona.youtube.safe.gui.businessobjects.updates.UpdatesCheckerTask;
import com.zaytoona.youtube.safe.gui.fragments.ChannelBrowserFragment;
import com.zaytoona.youtube.safe.gui.fragments.KidMainFragment;
import com.zaytoona.youtube.safe.gui.fragments.PlaylistVideosFragment;
import com.zaytoona.youtube.safe.gui.fragments.SafetoonsPlaylistVideosFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main activity (launcher).  This activity holds {@link com.zaytoona.youtube.safe.gui.fragments.VideosGridFragment}.
 */
public class KidMainActivity extends AppCompatActivity implements MainActivityListener {

    @BindView(R.id.fragment_container)
    protected FrameLayout fragmentContainer;

    private ProgressDialog mProgressDialog;

    private static final String TAG = KidMainActivity.class.getSimpleName();

    private KidMainFragment kidMainFragment;
    private SafetoonsPlaylistVideosFragment  safetoonsPlaylistVideosFragment;

    private boolean dontAddToBackStack = false;

//    public static final String ACTION_VIEW_CHANNEL = "KidMainActivity.ViewChannel";
    private static final String KID_MAIN_FRAGMENT   = "KidMainActivity.KidMainFragment";
    private static final String SAFETOONS_PLAYLIST_VIDEOS_FRAGMENT = "KidMainActivity.SafetoonsPlaylistVideosFragment";

    private static final int REQUEST_CODE_ENABLE = 11;

    // Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseFunctions mFunctions;

    private LockManager<CustomPinActivity> mLockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Crashlytics USAGE
        //Crashlytics.log("Testing Crashlytics"); // Crash report only
        //Crashlytics.log(1, "Crashlytics", "Testing Crashlytics Again"); // Crash report and Log.println

        // Set key\value pairs
        //void setBool(String key, boolean value);
        //void setDouble(String key, double value);
        //void setFloat(String key, float value);
        //void setInt(String key, int value);
        //void setString(String key, String value);

        //Crashlytics.setString("key1", "Value 1");

        // provide an ID number, token, or hashed value that uniquely identifies the end-user of your application without disclosing or transmitting any of their personal information.
        //void Crashlytics.setUserIdentifier(String identifier);
        //void Crashlytics.setUserName(String name);
        //void Crashlytics.setUserEmail(String email);


        SafetoonsApp.setFeedUpdateInterval();

        // Delete any missing downloaded videos
        new DownloadedVideosDb.RemoveMissingVideosTask().executeInParallel();

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Functions
        mFunctions = FirebaseFunctions.getInstance();

        mLockManager = LockManager.getInstance();

        setContentView(R.layout.activity_fragment_holder);
        ButterKnife.bind(this);


        if(fragmentContainer != null) {
            if(savedInstanceState != null) {
                kidMainFragment = (KidMainFragment)getSupportFragmentManager().getFragment(savedInstanceState, KID_MAIN_FRAGMENT);
                safetoonsPlaylistVideosFragment = (SafetoonsPlaylistVideosFragment) getSupportFragmentManager().getFragment(savedInstanceState, SAFETOONS_PLAYLIST_VIDEOS_FRAGMENT);

                // Because other fragments are pointing to the old activity instance
                // Make them look at the new activity instance
                if(kidMainFragment != null) {
                    kidMainFragment.updateMainActivityListeners();
                }
            }

            if(kidMainFragment == null) {

                kidMainFragment = new KidMainFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, kidMainFragment).commit();
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(kidMainFragment != null)
            getSupportFragmentManager().putFragment(outState, KID_MAIN_FRAGMENT, kidMainFragment);

        if(safetoonsPlaylistVideosFragment != null && safetoonsPlaylistVideosFragment.isVisible())
            getSupportFragmentManager().putFragment(outState, SAFETOONS_PLAYLIST_VIDEOS_FRAGMENT, safetoonsPlaylistVideosFragment);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser() == null) {

            mProgressDialog = new ProgressDialog(this);

            mProgressDialog.setTitle(getString(R.string.connecting));
            mProgressDialog.setMessage(null);
            mProgressDialog.setCancelable(false);

            mProgressDialog.show();

            signInAnonymously();
        }
        else {
            // This refreshes the custom claims
            mAuth.getCurrentUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult result) {
                    //Log.d(TAG, "result.pincode: " + result.getClaims().get("pincode"));
                    if(result.getClaims() != null) {
                        Object pincode = result.getClaims().get("pincode");

                        if((pincode == null || pincode.toString().equals("null")) && mLockManager.getAppLock().isPasscodeSet()) {
                            mLockManager.getAppLock().setPasscode(null);
                        }

                        if(!mLockManager.getAppLock().isPasscodeSet()) {
                            // User cancelled setting new passcode at first install
                            // Setup pin code then go to MainActivity in onActivityResults

                            Intent intent = new Intent(KidMainActivity.this, CustomPinActivity.class);
                            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                            startActivityForResult(intent, REQUEST_CODE_ENABLE);
                        }

                    }
                }
            });

            // This could be called without Sign in yet, but that is rare at initial use
            // When no playlists are added yet
            final DatabaseReference mDbPublicPlayListsRef = mDatabase.child(Constants.FIREBASE_DATABASE_TABLE_PUBLIC_PLAY_LISTS);

            // Remove Public Play Lists which are not found on Safetoons Server anymore
            ValueEventListener publicPlayListsListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot != null) {
                        final List<String> publicPlayListsIds = new ArrayList<>();
                        final List<Integer> publicPlayListsCategoryIds = new ArrayList<>();

                        boolean restartApp = false;

                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            String categoryTitle = snapshot.getKey();

                            String categoryId = categoryTitle.substring(0, 2);

                            publicPlayListsCategoryIds.add(new Integer(categoryId).intValue());

                            for (DataSnapshot playListsSnapshot : snapshot.child(Constants.FIREBASE_DATABASE_TABLE_PUBLIC_PLAY_LISTS_PUBLIC_LISTS).getChildren()) {
                                publicPlayListsIds.add(playListsSnapshot.getKey());
                            }

                            List<YouTubePublicPlayList> publicPlayListsList = PublicPlayListsDb.getPublicPlayListsDb().getPublicPlayListsInCategory(categoryId);

                            for (int i = 0; i < publicPlayListsList.size(); i++) {

                                String publicPlayList = publicPlayListsList.get(i).getId();

                                if (new Integer(categoryId).intValue() == new Integer(publicPlayListsList.get(i).getCategoryId()).intValue() && publicPlayListsIds.contains(publicPlayList) == false) {
                                    // Remove publicPlayList
                                    PublicPlayListsDb.getPublicPlayListsDb().remove(categoryId, publicPlayList);

                                    restartApp = true;
                                }
                            }
                        }

                        // Are categories in the app still exist on server
                        List<SafetoonsCategory> categories = SafetoonsCategoriesDb.getSafetoonsCategoriesDb().getSafetoonsCategories();

                        for(SafetoonsCategory category : categories) {

                            if(publicPlayListsCategoryIds.contains(new Integer(category.getId()).intValue()) == false) {

                                PublicPlayListsDb.getPublicPlayListsDb().remove(category.getId());

                                restartApp = true;
                            }
                        }

                        // Remove empty categories
                        SafetoonsCategoriesDb.getSafetoonsCategoriesDb().clearEmptySafetoonsCategories();

                        if(restartApp == true) {
                            SafetoonsApp.restartApp();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "Clean Public Play Lists : onCancelled", databaseError.toException());
                }
            };

            mDbPublicPlayListsRef.addListenerForSingleValueEvent(publicPlayListsListener);
        }
    }

    @Override
    protected void onResume() {
        /*KidMainFragment kidMainFragmentNew = new KidMainFragment();

        // Open the last added Private List
        Bundle args = new Bundle();
        args.putBoolean(MainFragment.SHOULD_SELECT_LAST_PRIVATE_LIST_TAB, true);
        kidMainFragmentNew.setArguments(args);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, kidMainFragmentNew).commit();

        kidMainFragment = kidMainFragmentNew;*/

        // Once we are back to kid activity reset last time so we require
        // a passcode if the kid try to get back to locked activities
        mLockManager.getAppLock().resetLastActiveMillis();

        ((SafetoonsApp) getApplication()).setKidMode(true);

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.kid_main_activity_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_unlocker:
                if (isPasscodeSet() == false) {
                    Intent intent = new Intent(KidMainActivity.this, CustomPinActivity.class);
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                    startActivityForResult(intent, REQUEST_CODE_ENABLE);
                } else {
                    Intent intent = new Intent(KidMainActivity.this, MainActivity.class);

                    startActivity(intent);

                    finish();
                }

                return true;

            case android.R.id.home:
                if(kidMainFragment == null || !kidMainFragment.isVisible()) {
                    onBackPressed();
                    return true;
                }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CODE_ENABLE:

                if(mLockManager.getAppLock().pinChallengeCancelled() == false) {

                    Intent intent = new Intent(KidMainActivity.this, MainActivity.class);

                    startActivity(intent);

                    finish();
                }
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (kidMainFragment != null  &&  kidMainFragment.isVisible()) {

            if(kidMainFragment.isPublicFragmentOpenedForPlayLists()) {
                kidMainFragment.openPublicFragmentForCategories();
            }
            else {
                super.onBackPressed();
            }
            // On Android, when the user presses back button, the Activity is destroyed and will be
            // recreated when the user relaunches the app.
            // We do not want that behaviour, instead then the back button is pressed, the app will
            // be **minimized**.
            //Intent startMain = new Intent(Intent.ACTION_MAIN);
            //startMain.addCategory(Intent.CATEGORY_HOME);
            //startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startActivity(startMain);

            //super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    private void signInAnonymously() {
        //showProgressDialog();

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");

                            onAuthSuccess(task.getResult().getUser());

                        } else {

                            if (KidMainActivity.this.isFinishing() == false && mProgressDialog != null) {
                                mProgressDialog.dismiss();
                            }
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());

                            Toast.makeText(KidMainActivity.this, R.string.sign_in_failed,
                                    Toast.LENGTH_LONG).show();
                        }

                        // [START_EXCLUDE]
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END signin_anonymously]
    }

    private void signOut() {
        mAuth.signOut();
    }

    private void onAuthSuccess(final FirebaseUser user) {

        final String deviceId = General.getDeviceId(this);
        final String messagingToken = FirebaseInstanceId.getInstance().getToken();

        final DatabaseReference mDbDeviceUsersRef = mDatabase.child(Constants.FIREBASE_DATABASE_TABLE_DEVICE_USERS).child(deviceId);

        // If new installation, check if pincode exists at server
        // If so set the local pincode to the one on the server,
        // This is to prevent the kid from reinstalling our app and
        // specify a new pinecode.

        ValueEventListener deviceUsersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot != null && dataSnapshot.exists()) {

                    // There is a record for this machine.
                    // Likley from a previous install

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        // Disable lock button

                        // addd a record in the device-users
                        // Each sign-in has a new UID
                        // Group them by deviceId (which is constant on the device)
                        User newUser = new User(user.getUid(), getCurrentDateTime(), deviceId, messagingToken);

                        mDbDeviceUsersRef.setValue(newUser);

                        User oldUser = dataSnapshot.getValue(User.class);

                        updateUserPinCode(user, oldUser.uid /* This is the previous record (uid) , extract the pincode from it (from custom claim) */)
                                .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {

                                if (!task.isSuccessful()) {

                                    if (KidMainActivity.this.isFinishing() == false && mProgressDialog != null) {
                                        mProgressDialog.dismiss();
                                    }

                                    Exception e = task.getException();

                                    if (e instanceof FirebaseFunctionsException) {

                                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;

                                        FirebaseFunctionsException.Code code = ffe.getCode();

                                        Object details = ffe.getDetails();

                                        Log.i(TAG, "FirebaseFunctionsException: " + ffe.getMessage());

                                        Log.i(TAG, "FirebaseFunctionsException code: " + code);

                                        //Log.i(TAG, details.toString());

                                    }
                                }
                                else {

                                    // This is to refresh the custom claim.
                                    mAuth.getCurrentUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                                        @Override
                                        public void onSuccess(GetTokenResult result) {

                                            mLockManager.getAppLock().setPasscode(result.getClaims().get("pincode").toString());

                                            Log.i(TAG, "updateUserPinCode.OnComplete().getIdToken().onSuccess(): " + result.toString());

                                            if (KidMainActivity.this.isFinishing() == false && mProgressDialog != null) {
                                                // Enable lock button
                                                mProgressDialog.dismiss();
                                            }

                                            // The device already had a passcode, go to MainActivity to get the lists refreshed
                                            // the user will be challanged with pincode
                                            Intent intent = new Intent(KidMainActivity.this, MainActivity.class);

                                            startActivity(intent);

                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {

                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Log.i(TAG, "updateUserPinCode.OnComplete().getIdToken().onFailure(): " + e.getMessage());

                                            if (KidMainActivity.this.isFinishing() == false && mProgressDialog != null) {
                                                // Enable lock button
                                                mProgressDialog.dismiss();
                                            }
                                        }

                                    });
                                }
                            }
                        });

                        //user.getIdToken(true);

                        break; // Got the first one, ordered by dateTime
                    }
                }
                else {
                    // A new onAuthSuccess and no records => Brand new install.
                    // No pinecode yet.

                    // Each sign-in has a new UID
                    // Group them by deviceId (which is constant on the device)
                    User newUser = new User(user.getUid(), getCurrentDateTime(), deviceId, messagingToken);

                    mDbDeviceUsersRef.setValue(newUser);

                    if (KidMainActivity.this.isFinishing() == false && mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }

                    // New installation
                    // Setup pin code then go to MainActivity in onActivityResults

                    Intent intent = new Intent(KidMainActivity.this, CustomPinActivity.class);
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                    startActivityForResult(intent, REQUEST_CODE_ENABLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "Read Prev. User Info : onCancelled", databaseError.toException());
            }
        };
        mDbDeviceUsersRef.addListenerForSingleValueEvent(deviceUsersListener);



    }

    private Task<String> updateUserPinCode(final FirebaseUser user, String prev_uid) {

        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();

        data.put("uid", user.getUid());
        data.put("pincode", "");
        data.put("prev_uid", prev_uid);

        return mFunctions.getHttpsCallable("updateUserPinCode")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {

                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.

                        Object result = task.getResult().getData();

                        //
                        Log.i(TAG, "Return from then() of updateUserPinCode Function: " + result.toString());

                        return result.toString();
                    }
                });
    }

    protected String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Date dateTime = new Date();

        String strDate = dateFormat.format(dateTime).toString();

        return strDate;
    }


    private void switchToFragment(Fragment fragment) {
        Lifecycle.State x = getLifecycle().getCurrentState();

        if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, fragment);
            if (!dontAddToBackStack)
                transaction.addToBackStack(null);
            else
                dontAddToBackStack = false;
            transaction.commit();
        }
    }


    @Override
    public void onChannelClick(YouTubeChannel channel) {
        Bundle args = new Bundle();
        args.putSerializable(ChannelBrowserFragment.CHANNEL_OBJ, channel);
        switchToChannelBrowserFragment(args);
    }


    @Override
    public void onChannelClick(String channelId) {
        Bundle args = new Bundle();
        args.putString(ChannelBrowserFragment.CHANNEL_ID, channelId);
        switchToChannelBrowserFragment(args);
    }

    @Override
    public void onSafetoonsListClick(SafetoonsList playlist) {
        safetoonsPlaylistVideosFragment = new SafetoonsPlaylistVideosFragment();
        Bundle args = new Bundle();
        args.putSerializable(SafetoonsPlaylistVideosFragment.PLAYLIST_OBJ, playlist);
        safetoonsPlaylistVideosFragment.setArguments(args);
        safetoonsPlaylistVideosFragment.setDisplayMode(false);
        safetoonsPlaylistVideosFragment.setCategory(playlist.getCategory());

        switchToFragment(safetoonsPlaylistVideosFragment);
    }

    private void switchToChannelBrowserFragment(Bundle args) {
        //channelBrowserFragment = new ChannelBrowserFragment();
        //channelBrowserFragment.getChannelPlaylistsFragment().setMainActivityListener(this);
        //channelBrowserFragment.setArguments(args);
        //switchToFragment(channelBrowserFragment);
    }


    @Override
    public void onPlaylistClick(YouTubePlaylist playlist) {
        //playlistVideosFragment = new PlaylistVideosFragment();
        Bundle args = new Bundle();
        args.putSerializable(PlaylistVideosFragment.PLAYLIST_OBJ, playlist);
        //playlistVideosFragment.setArguments(args);
        //switchToFragment(playlistVideosFragment);
    }

    public boolean isPasscodeSet() {
        if (SafetoonsApp.getPreferenceManager().contains(SafetoonsApp.PASSWORD_PREFERENCE_KEY)) {
            return true;
        }
        return false;
    }
}
