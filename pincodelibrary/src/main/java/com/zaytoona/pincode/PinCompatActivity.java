package com.zaytoona.pincode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.zaytoona.pincode.interfaces.LifeCycleInterface;
import com.zaytoona.pincode.managers.AppLockActivity;

/**
 * Created by callmepeanut on 16-1-14.
 * You must extend this Activity in order to support this library.
 * Then to enable PinCode blocking, you must call
 * {@link com.zaytoona.pincode.managers.LockManager#enableAppLock(android.content.Context, Class)}
 */
public class PinCompatActivity extends AppCompatActivity {
    private static LifeCycleInterface mLifeCycleListener;
    private final BroadcastReceiver mPinCancelledReceiver;

    public PinCompatActivity() {
        super();
        mPinCancelledReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(AppLockActivity.ACTION_CANCEL);
        LocalBroadcastManager.getInstance(this).registerReceiver(mPinCancelledReceiver, filter);
    }

    @Override
    protected void onResume() {
        if (mLifeCycleListener != null) {
            mLifeCycleListener.onActivityResumed(PinCompatActivity.this);
        }
        super.onResume();
    }

    @Override
    public void onUserInteraction() {
        if (mLifeCycleListener != null){
            mLifeCycleListener.onActivityUserInteraction(PinCompatActivity.this);
        }
        super.onUserInteraction();
    }

    @Override
    protected void onPause() {
        if (mLifeCycleListener != null) {
            mLifeCycleListener.onActivityPaused(PinCompatActivity.this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPinCancelledReceiver);
    }

    public static void setListener(LifeCycleInterface listener) {
        if (mLifeCycleListener != null) {
            mLifeCycleListener = null;
        }
        mLifeCycleListener = listener;
    }

    public static void clearListeners() {
        mLifeCycleListener = null;
    }

    public static boolean hasListeners() {
        return (mLifeCycleListener != null);
    }
}
