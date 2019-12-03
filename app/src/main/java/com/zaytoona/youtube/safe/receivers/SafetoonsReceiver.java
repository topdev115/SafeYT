package com.zaytoona.youtube.safe.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zaytoona.youtube.safe.common.General;

public class SafetoonsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent i) {

        General.startService(context);
    }
}