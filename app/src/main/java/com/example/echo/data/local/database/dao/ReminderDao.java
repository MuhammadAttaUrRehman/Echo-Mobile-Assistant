package com.example.echo.data.local.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.echo.data.model.Reminder;
import com.example.echo.database.EchoDbHelper;
import com.example.echo.database.contracts.ReminderContract;

import java.util.ArrayList;
import java.util.List;

public class ReminderDao {
    private static final String TAG = "ReminderDao";
    private final EchoDbHelper dbHelper;

    public ReminderDao(Context context) {
        this.dbHelper = new EchoDbHelper(context);
    }

    public long saveReminder(Reminder reminder) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ReminderContract.ReminderEntry.COLUMN_ID, reminder.getId());
            values.put(ReminderContract.ReminderEntry.COLUMN_TITLE, reminder.getTitle());
            values.put(ReminderContract.ReminderEntry.COLUMN_DESCRIPTION, reminder.getDescription());
            values.put(ReminderContract.ReminderEntry.COLUMN_LATITUDE, reminder.getLatitude());
            values.put(ReminderContract.ReminderEntry.COLUMN_LONGITUDE, reminder.getLongitude());
            values.put(ReminderContract.ReminderEntry.COLUMN_LOCATION_NAME, reminder.getLocationName());
            values.put(ReminderContract.ReminderEntry.COLUMN_RADIUS, reminder.getRadiusInMeters());
            values.put(ReminderContract.ReminderEntry.COLUMN_SCHEDULED_TIME,
                    reminder.getScheduledTime() != null ? reminder.getScheduledTime().getTime() : null);
            values.put(ReminderContract.ReminderEntry.COLUMN_TYPE,
                    reminder.getType() != null ? reminder.getType().name() : null);

            long result = db.insertWithOnConflict(ReminderContract.ReminderEntry.TABLE_NAME, null,
                    values, SQLiteDatabase.CONFLICT_REPLACE);
            Log.d(TAG, "Saved reminder: " + reminder.getId() + ", Result: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error saving reminder: " + e.getMessage(), e);
            throw new RuntimeException("Error saving reminder: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<Reminder> getAllReminders() {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + ReminderContract.ReminderEntry.TABLE_NAME, null);

            if (cursor.moveToFirst()) {
                do {
                    Reminder reminder = cursorToReminder(cursor);
                    reminders.add(reminder);
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "Retrieved " + reminders.size() + " reminders");
            return reminders;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all reminders: " + e.getMessage(), e);
            throw new RuntimeException("Error fetching all reminders: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public Reminder getReminderById(String id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + ReminderContract.ReminderEntry.TABLE_NAME +
                    " WHERE " + ReminderContract.ReminderEntry.COLUMN_ID + " = ?", new String[]{id});
            if (cursor.moveToFirst()) {
                return cursorToReminder(cursor);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching reminder by ID: " + e.getMessage(), e);
            throw new RuntimeException("Error fetching reminder by ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    private Reminder cursorToReminder(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_DESCRIPTION));
        Double latitude = cursor.isNull(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_LATITUDE)) ?
                null : cursor.getDouble(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_LATITUDE));
        Double longitude = cursor.isNull(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_LONGITUDE)) ?
                null : cursor.getDouble(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_LONGITUDE));
        String locationName = cursor.getString(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_LOCATION_NAME));
        Integer radius = cursor.isNull(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_RADIUS)) ?
                null : cursor.getInt(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_RADIUS));
        Long scheduledTime = cursor.isNull(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_SCHEDULED_TIME)) ?
                null : cursor.getLong(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_SCHEDULED_TIME));
        String typeStr = cursor.getString(cursor.getColumnIndexOrThrow(ReminderContract.ReminderEntry.COLUMN_TYPE));

        Reminder reminder = new Reminder(id, title, description);
        if (latitude != null && longitude != null && radius != null) {
            reminder.setLocation(latitude, longitude, locationName, radius);
        }
        if (scheduledTime != null) {
            reminder.setScheduledTime(new java.util.Date(scheduledTime));
            if (typeStr == null || !typeStr.equals(Reminder.ReminderType.LOCATION_BASED.name())) {
                reminder.setType(Reminder.ReminderType.TIME_BASED);
            }
        }
        if (typeStr != null) {
            reminder.setType(Reminder.ReminderType.valueOf(typeStr));
        }
        return reminder;
    }
}