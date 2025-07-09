package com.example.echo.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echo.MainActivity;
import com.example.echo.R;
import com.example.echo.database.EchoDbHelper;
import com.example.echo.data.local.database.dao.UserDao;
import com.example.echo.data.repositories.UserRepository;

/**
 * Splash screen activity that shows app logo and checks authentication state
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY_MS = 2000; // 2 seconds
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Inflating layout: R.layout.activity_splash");
        setContentView(R.layout.activity_splash);
        Log.d(TAG, "Layout inflated successfully");

        // Initialize database helper, DAO, and repository
        try {
            EchoDbHelper dbHelper = new EchoDbHelper(this);
            UserDao userDao = new UserDao(dbHelper);
            userRepository = new UserRepository(userDao, this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize repository: " + e.getMessage(), e);
            Toast.makeText(this, "Initialization error, please try again", Toast.LENGTH_LONG).show();
            navigateToLogin();
            return;
        }

        // Delayed navigation to next screen
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthenticationAndNavigate, SPLASH_DELAY_MS);
    }

    /**
     * Check if user is logged in and navigate to appropriate screen
     */
    private void checkAuthenticationAndNavigate() {
        if (userRepository.isUserLoggedIn()) {
            Log.d(TAG, "User is logged in, navigating to MainActivity");
            navigateToMain();
        } else {
            Log.d(TAG, "No user logged in, navigating to LoginActivity");
            navigateToLogin();
        }
    }

    /**
     * Navigate to main activity
     */
    private void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Navigate to login activity
     */
    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}