package com.example.echo.services.assistant;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class TextToSpeechService {
    private TextToSpeech tts = null;
    private boolean isInitialized = false;

    public TextToSpeechService(Context context, TextToSpeech.OnInitListener listener) {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                isInitialized = true;
            }
            listener.onInit(status);
        });
    }

    public void speak(String text) {
        if (isInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}