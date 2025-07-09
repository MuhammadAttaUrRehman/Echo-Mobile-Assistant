package com.example.echo.data.repositories;

import android.content.Context;
import com.example.echo.data.local.database.dao.ConversationDao;
import com.example.echo.data.model.ConversationMessage;
import java.util.List;
import java.util.concurrent.Executors;

public class ConversationRepository {
    private final ConversationDao conversationDao;

    public ConversationRepository(Context context) {
        this.conversationDao = new ConversationDao(context);
    }

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface MessagesCallback {
        void onSuccess(List<ConversationMessage> messages);
        void onError(Exception e);
    }

    public void saveMessage(ConversationMessage message, Callback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                conversationDao.saveMessage(message);
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getMessagesForConversation(String userId, String conversationId, MessagesCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<ConversationMessage> messages = conversationDao.getMessagesForConversation(userId, conversationId);
                if (callback != null) {
                    callback.onSuccess(messages);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
}