package com.example.echo.data.remote.firebase;

import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.echo.data.model.ConversationMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseRealtimeDbManager {
    private static final String TAG = "FirebaseRealtimeDbManager";
    private final DatabaseReference databaseReference;

    public FirebaseRealtimeDbManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference("conversations");
    }

    public void saveConversationMessage(String userId, String conversationId, ConversationMessage message) {
        databaseReference.child(userId).child("messages").child(conversationId).child(message.getId())
                .setValue(message)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Saved message to Firebase: " + message.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving message to Firebase: " + e.getMessage(), e));
    }

    public void saveConversationTitle(String userId, String conversationId, String title) {
        Map<String, Object> titleData = new HashMap<>();
        titleData.put("id", conversationId);
        titleData.put("title", title);
        titleData.put("timestamp", System.currentTimeMillis());
        databaseReference.child(userId).child("titles").child(conversationId)
                .setValue(titleData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Saved conversation title: " + conversationId))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving conversation title: " + e.getMessage(), e));
    }

    public void loadConversationMessages(String userId, String conversationId, Callback<List<ConversationMessage>> callback) {
        databaseReference.child(userId).child("messages").child(conversationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<ConversationMessage> messages = new ArrayList<>();
                        for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                            try {
                                ConversationMessage message = messageSnapshot.getValue(ConversationMessage.class);
                                if (message != null) {
                                    messages.add(message);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing message: " + e.getMessage(), e);
                            }
                        }
                        if (callback != null) {
                            callback.onResult(messages);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Firebase load messages cancelled: " + error.getMessage());
                        if (callback != null) {
                            callback.onResult(new ArrayList<>());
                        }
                    }
                });
    }

    public void getConversationTitle(String userId, String conversationId, Callback<String> callback) {
        databaseReference.child(userId).child("titles").child(conversationId).child("title")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String title = snapshot.getValue(String.class);
                        if (callback != null) {
                            callback.onResult(title);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Firebase get title cancelled: " + error.getMessage());
                        if (callback != null) {
                            callback.onResult(null);
                        }
                    }
                });
    }

    public void getConversationTitles(String userId, ConversationTitlesCallback callback) {
        databaseReference.child(userId).child("titles").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<ConversationInfo> conversations = new ArrayList<>();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    try {
                        ConversationInfo info = snapshot.getValue(ConversationInfo.class);
                        if (info != null) {
                            conversations.add(info);
                        } else {
                            Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                            if (map != null) {
                                String id = (String) map.get("id");
                                String title = (String) map.get("title");
                                Object timestamp = map.get("timestamp");
                                long time = timestamp instanceof Long ? (Long) timestamp : System.currentTimeMillis();
                                conversations.add(new ConversationInfo(id, title, time));
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing conversation title: " + e.getMessage(), e);
                        continue;
                    }
                }
                if (callback != null) {
                    callback.onConversationTitlesLoaded(conversations);
                }
            } else {
                Log.e(TAG, "Error loading conversation titles: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                if (callback != null) {
                    callback.onConversationTitlesLoaded(new ArrayList<>());
                }
            }
        });
    }

    public static class ConversationInfo {
        private String id;
        private String title;
        private long timestamp;

        public ConversationInfo() {
        }

        public ConversationInfo(String id, String title, long timestamp) {
            this.id = id;
            this.title = title;
            this.timestamp = timestamp;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public interface Callback<T> {
        void onResult(T result);
    }

    public interface ConversationTitlesCallback {
        void onConversationTitlesLoaded(List<ConversationInfo> conversations);
    }
}