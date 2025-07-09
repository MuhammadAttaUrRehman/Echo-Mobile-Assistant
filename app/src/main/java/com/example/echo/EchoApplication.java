package com.example.echo;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;

/**
 * Application class to initialize Firebase
 */
public class EchoApplication extends Application {
    private static final String TAG = "EchoApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Initializing Firebase");
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase: " + e.getMessage(), e);
        }
    }
}