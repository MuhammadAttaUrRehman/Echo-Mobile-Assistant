package com.example.echo.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echo.MainActivity;
import com.example.echo.R;
import com.example.echo.database.EchoDbHelper;
import com.example.echo.data.local.database.dao.UserDao;
import com.example.echo.data.remote.firebase.FirebaseAuthManager;
import com.example.echo.data.repositories.UserRepository;

/**
 * Activity handling user login
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private ProgressBar progressBar;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        registerTextView = findViewById(R.id.register_text_view);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize database helper, DAO, and repository
        EchoDbHelper dbHelper = new EchoDbHelper(this);
        UserDao userDao = new UserDao(dbHelper);
        userRepository = new UserRepository(userDao, this);

        // Set click listeners
        loginButton.setOnClickListener(v -> attemptLogin());
        registerTextView.setOnClickListener(v -> navigateToRegister());
    }

    /**
     * Attempt to log in with provided credentials
     */
    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        Log.d(TAG, "Attempting login with email: " + email);

        // Simple validation
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        showLoading(true);

        // Attempt login
        userRepository.loginUser(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Log.d(TAG, "Login successful, userId: " + userId);
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_LONG).show();
                    navigateToMain();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Log.e(TAG, "Login failed: " + errorMessage);
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);
        passwordEditText.setEnabled(!isLoading);
        registerTextView.setEnabled(!isLoading);
    }

    /**
     * Navigate to main activity
     */
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Navigate to register activity
     */
    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}