package com.example.echo.data.repositories;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.example.echo.data.local.database.dao.NoteDao;
import com.example.echo.data.model.Note;

import java.util.List;

public class NoteRepository {
    private static final String TAG = "NoteRepository";
    private final NoteDao noteDao;

    public NoteRepository(Context context) {
        this.noteDao = new NoteDao(context);
    }

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface NotesCallback {
        void onSuccess(List<Note> notes);
        void onError(Exception e);
    }

    public interface NoteCallback {
        void onSuccess(Note note);
        void onError(Exception e);
    }

    public void saveNote(Note note, Callback callback) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                try {
                    return noteDao.saveNote(note);
                } catch (Exception e) {
                    Log.e(TAG, "Error saving note: " + e.getMessage(), e);
                    return -1L;
                }
            }

            @Override
            protected void onPostExecute(Long result) {
                if (result != -1) {
                    callback.onSuccess();
                } else {
                    callback.onError(new Exception("Failed to save note"));
                }
            }
        }.execute();
    }

    public void getAllNotes(NotesCallback callback) {
        new AsyncTask<Void, Void, List<Note>>() {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                try {
                    return noteDao.getAllNotes();
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching all notes: " + e.getMessage(), e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                if (notes != null) {
                    callback.onSuccess(notes);
                } else {
                    callback.onError(new Exception("Failed to fetch notes"));
                }
            }
        }.execute();
    }

    public void getNoteById(String noteId, NoteCallback callback) {
        new AsyncTask<Void, Void, Note>() {
            @Override
            protected Note doInBackground(Void... voids) {
                try {
                    return noteDao.getNoteById(noteId);
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching note by ID: " + e.getMessage(), e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Note note) {
                callback.onSuccess(note);
            }
        }.execute();
    }
}