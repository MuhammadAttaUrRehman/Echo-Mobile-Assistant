package com.example.echo.data.remote.openrouter;

import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenRouterApiService {
    private static final String BASE_URL = "https://openrouter.ai/api/v1";
    private static final String API_KEY = "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";
    private static final String MODEL = "deepseek/deepseek-r1-0528:free";
    private static final String SITE_URL = "YOUR_SITE_URL"; // Replace with your site URL
    private static final String SITE_NAME = "Echo AI"; // Replace with your app name
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public static class ChatRequest {
        @SerializedName("model")
        String model;
        @SerializedName("messages")
        List<Message> messages;

        public ChatRequest(String model, List<Message> messages) {
            this.model = model;
            this.messages = messages;
        }
    }

    public static class Message {
        @SerializedName("role")
        String role;
        @SerializedName("content")
        String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class ChatResponse {
        @SerializedName("choices")
        List<Choice> choices;

        public static class Choice {
            @SerializedName("message")
            Message message;

            public static class Message {
                @SerializedName("content")
                String content;
            }
        }
    }

    public void sendQuery(String query, OpenRouterCallback callback) {
        ChatRequest requestBody = new ChatRequest(
                MODEL,
                Collections.singletonList(new Message("user", query))
        );
        String json = gson.toJson(requestBody);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/chat/completions")
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("HTTP-Referer", SITE_URL)
                .addHeader("X-Title", SITE_NAME)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = gson.fromJson(response.body().string(), ChatResponse.class);
                    if (chatResponse.choices != null && !chatResponse.choices.isEmpty()) {
                        String content = chatResponse.choices.get(0).message.content;
                        callback.onSuccess(content);
                    } else {
                        callback.onError("No response content");
                    }
                } else {
                    callback.onError("Request failed: " + response.message());
                }
            }
        });
    }

    public interface OpenRouterCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }
}