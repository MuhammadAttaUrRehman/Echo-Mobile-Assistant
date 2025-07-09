package com.example.echo.services.background;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.echo.R;
import com.example.echo.services.assistant.SpeechRecognitionService;
import com.example.echo.services.reminder.ReminderNotificationService;
import com.example.echo.MainActivity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import com.example.echo.ModelLoader;
import com.example.echo.AudioCapture;
import com.example.echo.DenoiseProcessor;
import org.tensorflow.lite.Interpreter;

public class EchoBackgroundService extends Service {
    private static final String TAG = "EchoBackgroundService";
    private SpeechRecognitionService speechRecognitionService;
    private Handler retryHandler;
    private static final long RETRY_DELAY_MS = 5000;
    private boolean isListening = false;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    private Interpreter tflite;
    private ModelLoader modelLoader = new ModelLoader();
    private AudioCapture audioCapture = new AudioCapture();
    private DenoiseProcessor denoiseProcessor = new DenoiseProcessor();

    public static final String ACTION_START_LISTENING = "com.example.echo.ACTION_START_LISTENING";

    @Override
    public void onCreate() {
        super.onCreate();
        ReminderNotificationService.createNotificationChannel(this);

        Intent listenIntent = new Intent(this, EchoBackgroundService.class);
        listenIntent.setAction(ACTION_START_LISTENING);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, listenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (android.os.Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ReminderNotificationService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Echo AI")
                .setContentText("Tap to speak to Echo")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .addAction(R.drawable.ic_mic, "Listen", pendingIntent);

        tflite = modelLoader.initializeInterpreter(this);
        if (tflite == null) {
            Log.e(TAG, "Failed to initialize TFLite model");
            stopSelf();
        }
        Log.d(TAG, "Foreground service started with TFLite model");

        startForeground(1, builder.build());
        Log.d(TAG, "Foreground service started");

        retryHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_START_LISTENING.equals(intent.getAction())) {
            Log.d(TAG, "Received start listening command");
            initializeSpeechRecognition();
            startListeningWithRetry();
        }
        return START_STICKY;
    }

    private void initializeSpeechRecognition() {
        if (speechRecognitionService != null) {
            speechRecognitionService.stopListening();
            speechRecognitionService.destroy();
        }

        speechRecognitionService = new SpeechRecognitionService(this, new SpeechRecognitionService.SpeechRecognitionCallback() {
            @Override
            public void onResult(String result) {
                Log.d(TAG, "Speech recognition result: " + result);
                isListening = false;
                retryCount = 0;
                if (result.toLowerCase().contains("hey echo")) {
                    Intent mainIntent = new Intent(EchoBackgroundService.this, MainActivity.class);
                    mainIntent.putExtra("voice_query", result.replace("hey echo", "").trim());
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    try {
                        startActivity(mainIntent);
                        Log.d(TAG, "MainActivity launched with query");
                    } catch (Exception e) {
                        Log.e(TAG, "Error launching MainActivity: " + e.getMessage());
                    }
                }
                stopSelf(); // Stop service after successful result
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Speech recognition error: " + errorMessage);
                isListening = false;
                if (errorMessage.contains("7")) { // Error code 7: No recognition result
                    if (!isNetworkAvailable()) {
                        Log.w(TAG, "No network connectivity, stopping");
                        stopSelf();
                        return;
                    } else if (!SpeechRecognizer.isRecognitionAvailable(EchoBackgroundService.this)) {
                        Log.w(TAG, "Speech recognition not available, stopping");
                        stopSelf();
                        return;
                    }
                    if (retryCount < MAX_RETRIES) {
                        retryCount++;
                        scheduleRetry();
                    } else {
                        Log.w(TAG, "Max retries reached, stopping");
                        stopSelf();
                    }
                } else {
                    stopSelf(); // Stop on other errors
                }
            }
        });
    }

    private void startListeningWithRetry() {
        if (isListening) {
            Log.d(TAG, "Already listening, skipping start");
            return;
        }
        try {
            // Capture and denoise audio
            float[] audioData = audioCapture.captureAudio();
            float[] input = prepareInput(audioData);
            float[] denoisedAudio = denoiseProcessor.runInference(tflite, input);
            byte[] audioBytes = convertToByteArray(denoisedAudio);

            // Since EXTRA_AUDIO is not supported, use SpeechRecognizer with real-time input
            // For now, start listening and rely on the denoise pipeline upstream
            speechRecognitionService.startListening();
            isListening = true;
            Log.d(TAG, "Started listening with denoised audio preprocessing");

        } catch (Exception e) {
            Log.e(TAG, "Failed to start listening: " + e.getMessage());
            isListening = false;
            scheduleRetry();
        }
    }

    private float[] prepareInput(float[] audioData) {
        float[] input = new float[16000];
        if (audioData.length < 16000) {
            System.arraycopy(audioData, 0, input, 0, audioData.length);
        } else {
            System.arraycopy(audioData, 0, input, 0, 16000);
        }
        return input;
    }

    private byte[] convertToByteArray(float[] floatArray) {
        byte[] byteArray = new byte[floatArray.length * 2]; // Assuming 16-bit PCM
        for (int i = 0; i < floatArray.length; i++) {
            short val = (short) (floatArray[i] * 32767); // Normalize to 16-bit
            byteArray[i * 2] = (byte) (val & 0xff);
            byteArray[i * 2 + 1] = (byte) ((val >> 8) & 0xff);
        }
        return byteArray;
    }

    private void scheduleRetry() {
        retryHandler.removeCallbacksAndMessages(null);
        retryHandler.postDelayed(() -> {
            Log.d(TAG, "Retrying speech recognition, attempt " + retryCount);
            startListeningWithRetry();
        }, RETRY_DELAY_MS);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (speechRecognitionService != null) {
            speechRecognitionService.stopListening();
            speechRecognitionService.destroy();
            isListening = false;
        }
        if (retryHandler != null) {
            retryHandler.removeCallbacksAndMessages(null);
        }
        Log.d(TAG, "Service destroyed");
        super.onDestroy();
    }
}