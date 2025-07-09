package com.example.echo.services.reminder;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.echo.data.model.Reminder;
import com.example.echo.data.repositories.ReminderRepository;

public class TimeReminderWorker extends Worker {
    private static final String TAG = "TimeReminderWorker";

    public TimeReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String reminderId = getInputData().getString("reminder_id");
        if (reminderId == null) {
            Log.e(TAG, "No reminder ID provided in input data");
            return Result.failure();
        }

        ReminderRepository repository = new ReminderRepository(getApplicationContext());
        repository.getReminderById(reminderId, new ReminderRepository.ReminderCallback() {
            @Override
            public void onSuccess(Reminder reminder) {
                if (reminder == null) {
                    Log.e(TAG, "Reminder not found for ID: " + reminderId);
                    return;
                }

                Log.d(TAG, "Processing reminder: " + reminder.getId() + ", Type: " + reminder.getType());
                if (reminder.getType() == Reminder.ReminderType.TIME_BASED) {
                    ReminderNotificationService.showNotification(
                            getApplicationContext(),
                            reminder.getId(),
                            reminder.getTitle(),
                            reminder.getDescription()
                    );
                    Log.d(TAG, "Notification triggered for time-based reminder: " + reminder.getTitle());
                } else {
                    Log.w(TAG, "Reminder is not time-based: " + reminderId + ", Type: " + reminder.getType());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching reminder: " + e.getMessage());
            }
        });

        return Result.success();
    }
}