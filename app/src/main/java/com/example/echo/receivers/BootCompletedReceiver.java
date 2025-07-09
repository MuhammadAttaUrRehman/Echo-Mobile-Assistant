package com.example.echo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Received BOOT_COMPLETED broadcast");
            // Add your boot completion logic here, e.g.:
            // - Start EchoBackgroundService
            // - Reschedule reminders using ReminderNotificationService
            // Example:
            // Intent serviceIntent = new Intent(context, EchoBackgroundService.class);
            // context.startService(serviceIntent);
        } else {
            Log.w(TAG, "Unexpected action: " + intent.getAction());
        }
    }
}