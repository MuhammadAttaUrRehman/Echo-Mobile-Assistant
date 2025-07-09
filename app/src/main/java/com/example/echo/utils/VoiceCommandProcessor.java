package com.example.echo.utils;

import android.content.Context;
import android.util.Log;

import com.example.echo.services.assistant.EchoAssistantService;

public class VoiceCommandProcessor {
    private static final String TAG = "VoiceCommandProcessor";
    private final EchoAssistantService assistantService;

    public VoiceCommandProcessor(Context context) {
        this.assistantService = new EchoAssistantService(context);
    }

    public interface CommandCallback {
        void onResult(String result);
        void onError(String error);
    }

    public void processCommand(String input, CommandCallback callback) {
        Log.d(TAG, "Processing command: " + input);
        assistantService.processQuery(input, new EchoAssistantService.AssistantCallback() {
            @Override
            public void onResponse(String response) {
                callback.onResult(response);
                Log.d(TAG, "Command result: " + response);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
                Log.e(TAG, "Command error: " + errorMessage);
            }
        });
    }
}