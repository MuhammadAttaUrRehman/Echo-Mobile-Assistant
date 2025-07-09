package com.example.echo;

import org.tensorflow.lite.Interpreter;

public class DenoiseProcessor {
    public float[] runInference(Interpreter tflite, float[] input) {
        float[][] inputArray = new float[1][16000];
        inputArray[0] = input;
        float[][] outputArray = new float[1][16000];

        tflite.run(inputArray, outputArray);
        return outputArray[0];
    }
}