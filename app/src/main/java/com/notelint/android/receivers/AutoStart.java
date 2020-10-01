package com.notelint.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            com.notelint.android.database.models.Alarm.reInitAllAlarms();
        }
    }
}