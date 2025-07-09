package com.example.echo.data.local.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.echo.data.model.ConversationMessage;
import com.example.echo.database.EchoDbHelper;
import com.example.echo.database.contracts.ConversationContract;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ConversationDao {
    private static final String TAG = "ConversationDao";
    private final EchoDbHelper dbHelper;

    public ConversationDao(Context context) {
        this.dbHelper = new EchoDbHelper(context);
    }

    public void saveMessage(ConversationMessage message) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ConversationContract.ConversationEntry.COLUMN_ID, message.getId());
            values.put(ConversationContract.ConversationEntry.COLUMN_CONVERSATION_ID, message.getConversationId());
            values.put(ConversationContract.ConversationEntry.COLUMN_USER_ID, message.getUserId());
            values.put(ConversationContract.ConversationEntry.COLUMN_TIMESTAMP, message.getTimestamp());
            values.put(ConversationContract.ConversationEntry.COLUMN_CONTENT, message.getContent());
            values.put(ConversationContract.ConversationEntry.COLUMN_IS_USER_MESSAGE, message.isUserMessage() ? 1 : 0);

            db.insertWithOnConflict(ConversationContract.ConversationEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            Log.d(TAG, "Saved message: " + message.getId());
        } catch (Exception e) {
            Log.e(TAG, "Error saving message: " + e.getMessage(), e);
            throw new RuntimeException("Error saving message: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<ConversationMessage> getMessagesForConversation(String userId, String conversationId) {
        List<ConversationMessage> messages = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT * FROM " + ConversationContract.ConversationEntry.TABLE_NAME +
                            " WHERE " + ConversationContract.ConversationEntry.COLUMN_USER_ID + " = ? AND " +
                            ConversationContract.ConversationEntry.COLUMN_CONVERSATION_ID + " = ? ORDER BY " +
                            ConversationContract.ConversationEntry.COLUMN_TIMESTAMP + " ASC",
                    new String[]{userId, conversationId}
            );

            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(ConversationContract.ConversationEntry.COLUMN_ID));
                String convId = cursor.getString(cursor.getColumnIndexOrThrow(ConversationContract.ConversationEntry.COLUMN_CONVERSATION_ID));
                String uId = cursor.getString(cursor.getColumnIndexOrThrow(ConversationContract.ConversationEntry.COLUMN_USER_ID));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(ConversationContract.ConversationEntry.COLUMN_TIMESTAMP));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(ConversationContract.ConversationEntry.COLUMN_CONTENT));
                boolean isUserMessage = cursor.getInt(cursor.getColumnIndexOrThrow(ConversationContract.ConversationEntry.COLUMN_IS_USER_MESSAGE)) == 1;

                messages.add(new ConversationMessage(id, convId, uId, content, timestamp, isUserMessage));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading messages: " + e.getMessage(), e);
            throw new RuntimeException("Error loading messages: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return messages;
    }
}