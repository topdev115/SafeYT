package com.zaytoona.pincode.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.zaytoona.pincode.PinActivity;
import com.zaytoona.pincode.PinCompatActivity;
import com.zaytoona.pincode.PinFragmentActivity;
import com.zaytoona.pincode.PinPreferenceActivity;
import com.zaytoona.pincode.encryption.Encryptor;
import com.zaytoona.pincode.enums.Algorithm;
import com.zaytoona.pincode.interfaces.LifeCycleInterface;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AppLockImpl<T extends AppLockActivity> extends AppLock implements LifeCycleInterface {

    public static final String TAG = "AppLockImpl";

    private FirebaseAuth mAuth;
    private FirebaseFunctions mFunctions;

    /**
     * The {@link android.content.SharedPreferences} key used to store the password
     */
    private static final String PASSWORD_PREFERENCE_KEY = "PASSCODE";
    /**
     * The {@link android.content.SharedPreferences} key used to store the {@link Algorithm}
     */
    private static final String PASSWORD_ALGORITHM_PREFERENCE_KEY = "ALGORITHM";
    /**
     * The {@link android.content.SharedPreferences} key used to store the last active time
     */
    private static final String LAST_ACTIVE_MILLIS_PREFERENCE_KEY = "LAST_ACTIVE_MILLIS";
    /**
     * The {@link android.content.SharedPreferences} key used to store the timeout
     */
    private static final String TIMEOUT_MILLIS_PREFERENCE_KEY = "TIMEOUT_MILLIS_PREFERENCE_KEY";
    /**
     * The {@link android.content.SharedPreferences} key used to store the logo resource id
     */
    private static final String LOGO_ID_PREFERENCE_KEY = "LOGO_ID_PREFERENCE_KEY";
    /**
     * The {@link android.content.SharedPreferences} key used to store the forgot option
     */
    private static final String SHOW_FORGOT_PREFERENCE_KEY = "SHOW_FORGOT_PREFERENCE_KEY";

    /**
     * The {@link android.content.SharedPreferences} key used to store the only background timeout option
     */
    private static final String ONLY_BACKGROUND_TIMEOUT_PREFERENCE_KEY = "ONLY_BACKGROUND_TIMEOUT_PREFERENCE_KEY";
    /**
     * The {@link SharedPreferences} key used to store whether the user has backed out of the {@link AppLockActivity}
     */
    private static final String PIN_CHALLENGE_CANCELLED_PREFERENCE_KEY = "PIN_CHALLENGE_CANCELLED_PREFERENCE_KEY";
    /**
     * The {@link android.content.SharedPreferences} key used to store the dynamically generated password salt
     */
    private static final String PASSWORD_SALT_PREFERENCE_KEY = "PASSWORD_SALT_PREFERENCE_KEY";
    /**
     * The {@link SharedPreferences} key used to store whether the caller has enabled fingerprint authentication.
     * This value defaults to true for backwards compatibility.
     */
    private static final String FINGERPRINT_AUTH_ENABLED_PREFERENCE_KEY = "FINGERPRINT_AUTH_ENABLED_PREFERENCE_KEY";
    /**
     * The default password salt
     */
    private static final String DEFAULT_PASSWORD_SALT = "7xn7@c$";
    /**
     * The key algorithm used to generating the dynamic salt
     */
    private static final String KEY_ALGORITHM = "PBEWithMD5AndDES";
    /**
     * The key length of the salt
     */
    private static final int KEY_LENGTH = 256;
    /**
     * The number of iterations used to generate a dynamic salt
     */
    private static final int KEY_ITERATIONS = 20;

    /**
     * The {@link android.content.SharedPreferences} used to store the password, the last active time etc...
     */
    private SharedPreferences mSharedPreferences;

    /**
     * The activity class that extends {@link com.zaytoona.pincode.managers.AppLockActivity}
     */
    private Class<T> mActivityClass;

    /**
     * Static instance of {@link AppLockImpl}
     */
    private static AppLockImpl mInstance;

    /**
     * Static method that allows to get back the current static Instance of {@link AppLockImpl}
     *
     * @param context       The current context of the {@link Activity}
     * @param activityClass The activity extending {@link AppLockActivity}
     * @return The instance.
     */
    public static AppLockImpl getInstance(Context context, Class<? extends AppLockActivity> activityClass) {
        synchronized (LockManager.class) {
            if (mInstance == null) {
                mInstance = new AppLockImpl<>(context, activityClass);
            }
        }
        return mInstance;
    }

    private AppLockImpl(Context context, Class<T> activityClass) {
        super();
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mActivityClass = activityClass;

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Functions
        mFunctions = FirebaseFunctions.getInstance();
    }

    @Override
    public void setTimeout(long timeout) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(TIMEOUT_MILLIS_PREFERENCE_KEY, timeout);
        editor.apply();
    }

    public String getSalt() {
        String salt = mSharedPreferences.getString(PASSWORD_SALT_PREFERENCE_KEY, null);
        if (salt == null) {
            salt = generateSalt();
            setSalt(salt);
        }
        return salt;
    }

    private void setSalt(String salt) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PASSWORD_SALT_PREFERENCE_KEY, salt);
        editor.apply();
    }

    private String generateSalt() {
        byte[] salt = new byte[KEY_LENGTH];
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(System.currentTimeMillis());
            sr.nextBytes(salt);
            return Arrays.toString(salt);
        } catch (Exception e) {
            salt = DEFAULT_PASSWORD_SALT.getBytes();
        }
        return Base64.encodeToString(salt, Base64.DEFAULT);
    }

    @Override
    public long getTimeout() {
        return mSharedPreferences.getLong(TIMEOUT_MILLIS_PREFERENCE_KEY, DEFAULT_TIMEOUT);
    }

    @Override
    public void setLogoId(int logoId) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(LOGO_ID_PREFERENCE_KEY, logoId);
        editor.apply();
    }

    @Override
    public int getLogoId() {
        return mSharedPreferences.getInt(LOGO_ID_PREFERENCE_KEY, LOGO_ID_NONE);
    }

    @Override
    public void setShouldShowForgot(boolean showForgot) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(SHOW_FORGOT_PREFERENCE_KEY, showForgot);
        editor.apply();
    }

    @Override
    public boolean pinChallengeCancelled() {
        return mSharedPreferences.getBoolean(PIN_CHALLENGE_CANCELLED_PREFERENCE_KEY, false);
    }

    @Override
    public void setPinChallengeCancelled(boolean backedOut) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(PIN_CHALLENGE_CANCELLED_PREFERENCE_KEY, backedOut);
        editor.apply();
    }

    @Override
    public boolean shouldShowForgot(int appLockType) {
        return mSharedPreferences.getBoolean(SHOW_FORGOT_PREFERENCE_KEY, true)
                && appLockType != AppLock.ENABLE_PINLOCK && appLockType != AppLock.ENABLE_PINLOCK_FOR_CHANGE && appLockType != AppLock.CONFIRM_PIN && appLockType != AppLock.CONFIRM_PIN_FOR_CHANGE;
    }

    @Override
    public boolean onlyBackgroundTimeout() {
        return mSharedPreferences.getBoolean(ONLY_BACKGROUND_TIMEOUT_PREFERENCE_KEY, false);
    }

    @Override
    public void setOnlyBackgroundTimeout(boolean onlyBackgroundTimeout) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(ONLY_BACKGROUND_TIMEOUT_PREFERENCE_KEY, onlyBackgroundTimeout);
        editor.apply();
    }

    @Override
    public void enable() {
        PinActivity.setListener(this);
        PinCompatActivity.setListener(this);
        PinFragmentActivity.setListener(this);
        PinPreferenceActivity.setListener(this);
    }

    @Override
    public void disable() {
        PinActivity.clearListeners();
        PinCompatActivity.clearListeners();
        PinFragmentActivity.clearListeners();
        PinPreferenceActivity.clearListeners();
    }

    @Override
    public void disableAndRemoveConfiguration() {
        PinActivity.clearListeners();
        PinCompatActivity.clearListeners();
        PinFragmentActivity.clearListeners();
        PinPreferenceActivity.clearListeners();

        mSharedPreferences.edit().remove(PASSWORD_PREFERENCE_KEY)
                .remove(LAST_ACTIVE_MILLIS_PREFERENCE_KEY)
                .remove(PASSWORD_ALGORITHM_PREFERENCE_KEY)
                .remove(TIMEOUT_MILLIS_PREFERENCE_KEY)
                .remove(LOGO_ID_PREFERENCE_KEY)
                .remove(SHOW_FORGOT_PREFERENCE_KEY)
                .remove(FINGERPRINT_AUTH_ENABLED_PREFERENCE_KEY)
                .remove(ONLY_BACKGROUND_TIMEOUT_PREFERENCE_KEY)
                .apply();
    }

    @Override
    public long getLastActiveMillis() {
        return mSharedPreferences.getLong(LAST_ACTIVE_MILLIS_PREFERENCE_KEY, 0);
    }

    @Override
    public boolean isFingerprintAuthEnabled() {
        return mSharedPreferences.getBoolean(FINGERPRINT_AUTH_ENABLED_PREFERENCE_KEY, true);
    }

    @Override
    public void setFingerprintAuthEnabled(boolean enabled) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(FINGERPRINT_AUTH_ENABLED_PREFERENCE_KEY, enabled);
        editor.apply();
    }

    @Override
    public void setLastActiveMillis() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(LAST_ACTIVE_MILLIS_PREFERENCE_KEY, System.currentTimeMillis());
        editor.apply();
    }

    @Override
    public void resetLastActiveMillis() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(LAST_ACTIVE_MILLIS_PREFERENCE_KEY);
        editor.commit();
    }

    @Override
    public boolean checkPasscode(String passcode) {
        Algorithm algorithm = Algorithm.getFromText(mSharedPreferences.getString(PASSWORD_ALGORITHM_PREFERENCE_KEY, ""));

        String salt = getSalt();
        passcode = salt + passcode + salt;
        passcode = Encryptor.getSHA(passcode, algorithm);
        String storedPasscode = "";

        if (mSharedPreferences.contains(PASSWORD_PREFERENCE_KEY)) {
            storedPasscode = mSharedPreferences.getString(PASSWORD_PREFERENCE_KEY, "");
        }

        if (storedPasscode.equalsIgnoreCase(passcode)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean setPasscode(String passcode) {
        String salt = getSalt();
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        if (passcode == null) {
            editor.remove(PASSWORD_PREFERENCE_KEY);
            editor.apply();
            this.disable();

            // Should we clear custom claim??

        } else {

            String pincode = passcode;

            passcode = salt + passcode + salt;
            setAlgorithm(Algorithm.SHA256);
            passcode = Encryptor.getSHA(passcode, Algorithm.SHA256);
            editor.putString(PASSWORD_PREFERENCE_KEY, passcode);
            editor.apply();

            this.enable();

            // Store as custom claim
            updateUserPinCode(mAuth.getCurrentUser(), pincode);

        }

        return true;
    }

    /**
     * Set the algorithm used in {@link #setPasscode(String)}
     */
    private void setAlgorithm(Algorithm algorithm) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PASSWORD_ALGORITHM_PREFERENCE_KEY, algorithm.getValue());
        editor.apply();
    }

    @Override
    public boolean isPasscodeSet() {
        if (mSharedPreferences.contains(PASSWORD_PREFERENCE_KEY)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isIgnoredActivity(Activity activity) {
        String clazzName = activity.getClass().getName();

        // ignored activities
        if (mIgnoredActivities.contains(clazzName)) {
            Log.d(TAG, "ignore activity " + clazzName);
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldLockSceen(Activity activity) {
        Log.d(TAG, "Lollipin shouldLockSceen() called");

        // previously backed out of pin screen
        if (pinChallengeCancelled()) {
            return true;
        }

        // already unlock
        if (activity instanceof AppLockActivity) {
            AppLockActivity ala = (AppLockActivity) activity;
            if (ala.getType() == AppLock.UNLOCK_PIN) {
                Log.d(TAG, "already unlock activity");
                return false;
            }
        }

        // no pass code set
        if (!isPasscodeSet()) {
            Log.d(TAG, "lock passcode not set.");
            return false;
        }

        // no enough timeout
        long lastActiveMillis = getLastActiveMillis();
        long passedTime = System.currentTimeMillis() - lastActiveMillis;
        long timeout = getTimeout();
        if (lastActiveMillis > 0 && passedTime <= timeout) {
            Log.d(TAG, "no enough timeout " + passedTime + " for "
                    + timeout);
            return false;
        }

        return true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (isIgnoredActivity(activity)) {
            return;
        }

        String clazzName = activity.getClass().getName();
        Log.d(TAG, "onActivityPaused " + clazzName);

        if (!shouldLockSceen(activity) && !(activity instanceof AppLockActivity)) {
            setLastActiveMillis();
        }
    }

    @Override
    public void onActivityUserInteraction(Activity activity) {
        if (onlyBackgroundTimeout() && !shouldLockSceen(activity) && !(activity instanceof AppLockActivity)) {
            setLastActiveMillis();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isIgnoredActivity(activity)) {
            return;
        }

        String clazzName = activity.getClass().getName();
        Log.d(TAG, "onActivityResumed " + clazzName);

        if (shouldLockSceen(activity)) {
            Log.d(TAG, "mActivityClass.getClass() " + mActivityClass);
            Intent intent = new Intent(activity.getApplicationContext(),
                    mActivityClass);
            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.getApplication().startActivity(intent);
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            return;
        }

        if (!shouldLockSceen(activity) && !(activity instanceof AppLockActivity)) {
            setLastActiveMillis();
        }
    }

    private Task<String> updateUserPinCode(final FirebaseUser user, String pincode) {

        // When authentication fails, usually no internet connection.
        if(user == null){
            return null;
        }

        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();

        data.put("uid", user.getUid());
        data.put("prev_uid", "");
        data.put("pincode", pincode);

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

                        // Refresh custom claims
                        user.getIdToken(true);

                        return result.toString();
                    }
                });
    }

}
