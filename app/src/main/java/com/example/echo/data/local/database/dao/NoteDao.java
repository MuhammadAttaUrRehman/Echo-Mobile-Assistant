package com.example.echo.data.local.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.echo.data.model.Note;
import com.example.echo.database.EchoDbHelper;
import com.example.echo.database.contracts.NoteContract;

import java.util.ArrayList;
import java.util.List;

public class NoteDao {
    private static final String TAG = "NoteDao";
    private final EchoDbHelper dbHelper;

    public NoteDao(Context context) {
        this.dbHelper = new EchoDbHelper(context);
    }

    public long saveNote(Note note) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(NoteContract.NoteEntry.COLUMN_ID, note.getId());
            values.put(NoteContract.NoteEntry.COLUMN_USER_ID, note.getUserId());
            values.put(NoteContract.NoteEntry.COLUMN_TITLE, note.getTitle());
            values.put(NoteContract.NoteEntry.COLUMN_CONTENT, note.getContent());
            values.put(NoteContract.NoteEntry.COLUMN_CREATED_AT, note.getTimestamp());

            long result = db.insertWithOnConflict(NoteContract.NoteEntry.TABLE_NAME, null,
                    values, SQLiteDatabase.CONFLICT_REPLACE);
            Log.d(TAG, "Saved note: " + note.getId() + ", Result: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error saving note: " + e.getMessage(), e);
            throw new RuntimeException("Error saving note: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + NoteContract.NoteEntry.TABLE_NAME, null);

            if (cursor.moveToFirst()) {
                do {
                    Note note = cursorToNote(cursor);
                    notes.add(note);
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "Retrieved " + notes.size() + " notes");
            return notes;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all notes: " + e.getMessage(), e);
            throw new RuntimeException("Error fetching all notes: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public Note getNoteById(String id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + NoteContract.NoteEntry.TABLE_NAME +
                    " WHERE " + NoteContract.NoteEntry.COLUMN_ID + " = ?", new String[]{id});
            if (cursor.moveToFirst()) {
                return cursorToNote(cursor);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching note by ID: " + e.getMessage(), e);
            throw new RuntimeException("Error fetching note by ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    private Note cursorToNote(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_ID));
        String userId = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_USER_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_TITLE));
        String content = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_CONTENT));
        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_CREATED_AT));

        return new Note(id, userId, title, content, timestamp);
    }
}