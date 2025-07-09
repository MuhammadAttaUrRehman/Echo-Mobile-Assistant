package com.example.echo.data.repositories;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.echo.data.local.database.dao.ReminderDao;
import com.example.echo.data.model.Reminder;

import java.util.List;

public class ReminderRepository {
    private static final String TAG = "ReminderRepository";
    private final ReminderDao reminderDao;

    public ReminderRepository(Context context) {
        this.reminderDao = new ReminderDao(context);
        Log.d(TAG, "ReminderRepository initialized");
    }

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface RemindersCallback {
        void onSuccess(List<Reminder> reminders);
        void onError(Exception e);
    }

    public interface ReminderCallback {
        void onSuccess(Reminder reminder);
        void onError(Exception e);
    }

    public void saveReminder(Reminder reminder, Callback callback) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                try {
                    return reminderDao.saveReminder(reminder);
                } catch (Exception e) {
                    Log.e(TAG, "Error saving reminder: " + e.getMessage(), e);
                    return -1L;
                }
            }

            @Override
            protected void onPostExecute(Long result) {
                if (result != -1) {
                    callback.onSuccess();
                    Log.d(TAG, "Reminder saved successfully");
                } else {
                    callback.onError(new Exception("Failed to save reminder"));
                }
            }
        }.execute();
    }

    public void getAllReminders(RemindersCallback callback) {
        new AsyncTask<Void, Void, List<Reminder>>() {
            @Override
            protected List<Reminder> doInBackground(Void... voids) {
                try {
                    return reminderDao.getAllReminders();
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching all reminders: " + e.getMessage(), e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Reminder> reminders) {
                if (reminders != null) {
                    callback.onSuccess(reminders);
                    Log.d(TAG, "Fetched all reminders successfully");
                } else {
                    callback.onError(new Exception("Failed to fetch reminders"));
                }
            }
        }.execute();
    }

    public void getReminderById(String reminderId, ReminderCallback callback) {
        new AsyncTask<Void, Void, Reminder>() {
            @Override
            protected Reminder doInBackground(Void... voids) {
                try {
                    return reminderDao.getReminderById(reminderId);
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching reminder by ID: " + e.getMessage(), e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Reminder reminder) {
                if (reminder != null) {
                    callback.onSuccess(reminder);
                    Log.d(TAG, "Fetched reminder by ID: " + reminderId);
                } else {
                    callback.onError(new Exception("Reminder not found for ID: " + reminderId));
                    Log.w(TAG, "No reminder found for ID: " + reminderId);
                }
            }
        }.execute();
    }
}