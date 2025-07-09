package com.example.echo.services.reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.example.echo.R;
import com.example.echo.data.model.Reminder;

import java.util.concurrent.TimeUnit;

public class ReminderNotificationService {
    private static final String TAG = "ReminderNotificationService";
    public static final String CHANNEL_ID = "echo_reminder_channel";
    private static final String CHANNEL_NAME = "Echo Reminders";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created: " + CHANNEL_ID);
        }
    }

    public static void showNotification(Context context, String reminderId, String title, String content) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(reminderId.hashCode(), builder.build());
        Log.d(TAG, "Time-based notification shown for reminder ID: " + reminderId + ", Title: " + title);
    }

    public static void showLocationReminderNotification(Context context, String reminderId, String title, String content) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(reminderId.hashCode(), builder.build());
        Log.d(TAG, "Location-based notification shown for reminder ID: " + reminderId + ", Title: " + title);
    }

    public static void scheduleTimeReminder(Context context, Reminder reminder) {
        if (reminder.getType() != Reminder.ReminderType.TIME_BASED) {
            Log.w(TAG, "Cannot schedule non-time-based reminder: " + reminder.getId());
            return;
        }

        long delayMillis = reminder.getScheduledTime().getTime() - System.currentTimeMillis();
        if (delayMillis <= 0) {
            // Trigger immediately if scheduled time is in the past
            showNotification(
                    context,
                    reminder.getId(),
                    reminder.getTitle(),
                    reminder.getDescription()
            );
            Log.d(TAG, "Immediate notification for past-due reminder: " + reminder.getId());
            return;
        }

        Data inputData = new Data.Builder()
                .putString("reminder_id", reminder.getId())
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(TimeReminderWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(reminder.getId())
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);
        Log.d(TAG, "Scheduled time-based reminder: " + reminder.getId() + ", Delay: " + delayMillis + "ms");
    }
}