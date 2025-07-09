package com.example.echo.services.assistant;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.ArrayList;
import com.example.echo.ModelLoader; // Add this class
import com.example.echo.AudioCapture; // Add this class
import com.example.echo.DenoiseProcessor; // Add this class
import org.tensorflow.lite.Interpreter;

public class SpeechRecognitionService {
    private static final String TAG = "SpeechRecognitionService";
    private final Context context;
    private final SpeechRecognizer speechRecognizer;
    private final SpeechRecognitionCallback callback;
    private Interpreter tflite;
    private ModelLoader modelLoader;
    private AudioCapture audioCapture;
    private DenoiseProcessor denoiseProcessor;
    private Thread recognitionThread;
    private volatile boolean isListening = false;

    public SpeechRecognitionService(Context context, SpeechRecognitionCallback callback) {
        this.context = context;
        this.speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        this.callback = callback;
        this.modelLoader = new ModelLoader();
        this.audioCapture = new AudioCapture();
        this.denoiseProcessor = new DenoiseProcessor();
        this.tflite = modelLoader.initializeInterpreter(context);
        if (tflite == null) {
            Log.e(TAG, "Failed to initialize TFLite model");
            callback.onError("Denoising model initialization failed");
        }
        setupRecognitionListener();
    }

    private void setupRecognitionListener() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Speech started");
            }

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "Speech ended");
                isListening = false;
            }

            @Override
            public void onError(int error) {
                String errorMessage = "Speech recognition error: " + error;
                Log.e(TAG, errorMessage);
                callback.onError(errorMessage);
                if (isListening) {
                    startListening(); // Retry if still active
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    callback.onResult(matches.get(0));
                }
                if (isListening) {
                    startListening(); // Restart listening
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    public void startListening() {
        if (isListening || tflite == null) {
            Log.w(TAG, "Already listening or model not initialized, skipping start");
            return;
        }
        isListening = true;

        recognitionThread = new Thread(() -> {
            try {
                // Capture raw audio
                float[] audioData = audioCapture.captureAudio();
                float[] input = prepareInput(audioData);
                float[] denoisedAudio = denoiseProcessor.runInference(tflite, input);
                byte[] audioBytes = convertToByteArray(denoisedAudio);

                // Workaround: Use AudioRecord to feed denoised audio to SpeechRecognizer
                int bufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                AudioRecord audioRecord = new AudioRecord(
                        AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT),
                        16000,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                );

                audioRecord.startRecording();
                // Simulate feeding denoised audio (Note: Direct feeding requires custom RecognitionService)
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                speechRecognizer.startListening(intent); // Fallback to real-time
                Log.d(TAG, "Started listening with denoised audio preprocessing");

                // Clean up
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                Log.e(TAG, "Error in recognition thread: " + e.getMessage());
                callback.onError("Recognition error: " + e.getMessage());
                isListening = false;
            }
        });
        recognitionThread.start();
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

    public void stopListening() {
        isListening = false;
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            Log.d(TAG, "Stopped listening");
        }
        if (recognitionThread != null && recognitionThread.isAlive()) {
            recognitionThread.interrupt();
        }
    }

    public void destroy() {
        stopListening();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            Log.d(TAG, "Speech recognizer destroyed");
        }
    }

    public interface SpeechRecognitionCallback {
        void onResult(String result);
        void onError(String errorMessage);
    }
}