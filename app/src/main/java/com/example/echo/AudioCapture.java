package com.example.echo;

import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.MediaRecorder;

public class AudioCapture {
    private static final int SAMPLE_RATE = 16000;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    public float[] captureAudio() {
        AudioRecord audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE
        );

        audioRecord.startRecording();
        short[] buffer = new short[16000]; // Capture 1 second
        int samplesRead = audioRecord.read(buffer, 0, 16000);
        audioRecord.stop();
        audioRecord.release();

        float[] audioData = new float[samplesRead];
        for (int i = 0; i < samplesRead; i++) {
            audioData[i] = buffer[i] / 32768.0f; // Normalize to [-1, 1]
        }
        return audioData;
    }
}