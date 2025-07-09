package com.example.echo.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.echo.database.contracts.UserContract;

public class EchoDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "EchoDbHelper";
    private static final String DATABASE_NAME = "echo.db";
    private static final int DATABASE_VERSION = 5; // Increment to force recreation

    // SQL for creating users table
    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + UserContract.UserEntry.TABLE_NAME + " (" +
                    UserContract.UserEntry.COLUMN_UID + " TEXT PRIMARY KEY," +
                    UserContract.UserEntry.COLUMN_EMAIL + " TEXT," +
                    UserContract.UserEntry.COLUMN_DISPLAY_NAME + " TEXT," +
                    UserContract.UserEntry.COLUMN_PHOTO_URL + " TEXT," +
                    UserContract.UserEntry.COLUMN_PHONE_NUMBER + " TEXT," +
                    UserContract.UserEntry.COLUMN_EMAIL_VERIFIED + " INTEGER," +
                    UserContract.UserEntry.COLUMN_PROVIDER + " TEXT," +
                    UserContract.UserEntry.COLUMN_CREATED_AT + " INTEGER," +
                    UserContract.UserEntry.COLUMN_LAST_LOGIN_AT + " INTEGER," +
                    UserContract.UserEntry.COLUMN_UPDATED_AT + " INTEGER," +
                    UserContract.UserEntry.COLUMN_DEVICE_TOKEN + " TEXT," +
                    UserContract.UserEntry.COLUMN_IS_ACTIVE + " INTEGER," +
                    UserContract.UserEntry.COLUMN_PREFERRED_LANGUAGE + " TEXT," +
                    UserContract.UserEntry.COLUMN_NOTIFICATIONS_ENABLED + " INTEGER)";

    // SQL for creating conversations table
    private static final String SQL_CREATE_CONVERSATIONS_TABLE =
            "CREATE TABLE conversations (" +
                    "id TEXT PRIMARY KEY," +
                    "user_id TEXT," +
                    "title TEXT," +
                    "created_at INTEGER," +
                    "updated_at INTEGER)";

    // SQL for creating reminders table with type column
    private static final String SQL_CREATE_REMINDERS_TABLE =
            "CREATE TABLE reminders (" +
                    "id TEXT PRIMARY KEY," +
                    "title TEXT," +
                    "description TEXT," +
                    "latitude REAL," +
                    "longitude REAL," +
                    "location_name TEXT," +
                    "radius INTEGER," +
                    "scheduled_time INTEGER," +
                    "type TEXT)";

    // SQL for creating notes table
    private static final String SQL_CREATE_NOTES_TABLE =
            "CREATE TABLE notes (" +
                    "id TEXT PRIMARY KEY," +
                    "user_id TEXT," +
                    "title TEXT," +
                    "content TEXT," +
                    "created_at INTEGER)";

    public EchoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "EchoDbHelper initialized with version: " + DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");
        try {
            db.execSQL(SQL_CREATE_USERS_TABLE);
            Log.d(TAG, "Created users table successfully");
            db.execSQL(SQL_CREATE_CONVERSATIONS_TABLE);
            Log.d(TAG, "Created conversations table successfully");
            db.execSQL(SQL_CREATE_REMINDERS_TABLE);
            Log.d(TAG, "Created reminders table successfully");
            db.execSQL(SQL_CREATE_NOTES_TABLE);
            Log.d(TAG, "Created notes table successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating tables: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion < 2) {
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{UserContract.UserEntry.TABLE_NAME});
            if (cursor != null && !cursor.moveToFirst()) {
                db.execSQL(SQL_CREATE_USERS_TABLE);
                Log.d(TAG, "Created users table during upgrade");
            }
            if (cursor != null) {
                cursor.close();
            }
            db.execSQL(SQL_CREATE_CONVERSATIONS_TABLE);
            Log.d(TAG, "Created conversations table during upgrade");
            db.execSQL(SQL_CREATE_REMINDERS_TABLE);
            Log.d(TAG, "Created reminders table during upgrade");
        }

        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE reminders ADD COLUMN type TEXT");
                Log.d(TAG, "Added type column to reminders table during upgrade");
            } catch (Exception e) {
                Log.e(TAG, "Error adding type column: " + e.getMessage(), e);
            }
        }

        if (oldVersion < 4) {
            db.execSQL(SQL_CREATE_NOTES_TABLE);
            Log.d(TAG, "Created notes table during upgrade");
        }

        if (oldVersion < 5) {
            // Force recreate tables if needed
            db.execSQL("DROP TABLE IF EXISTS " + UserContract.UserEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS conversations");
            db.execSQL("DROP TABLE IF EXISTS reminders");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
            Log.d(TAG, "Recreated all tables due to version 5 upgrade");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Downgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + UserContract.UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS conversations");
        db.execSQL("DROP TABLE IF EXISTS reminders");
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
    }
}