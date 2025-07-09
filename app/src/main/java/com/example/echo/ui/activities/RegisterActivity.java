package com.example.echo.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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
 * Activity handling user registration
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText displayNameEditText;
    private Button registerButton;
    private TextView loginTextView;
    private ProgressBar progressBar;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        displayNameEditText = findViewById(R.id.display_name_edit_text);
        registerButton = findViewById(R.id.register_button);
        loginTextView = findViewById(R.id.login_text_view);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize database helper, DAO, and repository
        EchoDbHelper dbHelper = new EchoDbHelper(this);
        UserDao userDao = new UserDao(dbHelper);
        userRepository = new UserRepository(userDao, this);

        // Set click listeners
        registerButton.setOnClickListener(v -> attemptRegistration());
        loginTextView.setOnClickListener(v -> navigateToLogin());
    }

    /**
     * Attempt to register a new user with provided information
     */
    private void attemptRegistration() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String displayName = displayNameEditText.getText().toString().trim();

        Log.d(TAG, "Attempting registration with email: " + email + ", displayName: " + displayName);

        // Reset errors
        emailEditText.setError(null);
        passwordEditText.setError(null);
        confirmPasswordEditText.setError(null);
        displayNameEditText.setError(null);

        boolean isValid = true;

        // Validate display name
        if (TextUtils.isEmpty(displayName)) {
            displayNameEditText.setError("Display name is required");
            displayNameEditText.requestFocus();
            isValid = false;
        } else if (displayName.length() < 2) {
            displayNameEditText.setError("Display name must be at least 2 characters");
            displayNameEditText.requestFocus();
            isValid = false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            if (isValid) emailEditText.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address");
            if (isValid) emailEditText.requestFocus();
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            if (isValid) passwordEditText.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            if (isValid) passwordEditText.requestFocus();
            isValid = false;
        } else if (!isPasswordStrong(password)) {
            passwordEditText.setError("Password must contain at least one letter and one number");
            if (isValid) passwordEditText.requestFocus();
            isValid = false;
        }

        // Validate password confirmation
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Please confirm your password");
            if (isValid) confirmPasswordEditText.requestFocus();
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            if (isValid) confirmPasswordEditText.requestFocus();
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        showLoading(true);

        // Attempt registration
        userRepository.registerUser(email, password, displayName, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, "Registration successful! Welcome to Echo AI!", Toast.LENGTH_LONG).show();
                    navigateToMain();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Check if password meets strength requirements
     * @param password password to check
     * @return true if password is strong enough
     */
    private boolean isPasswordStrong(String password) {
        boolean hasLetter = false;
        boolean hasNumber = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            }

            if (hasLetter && hasNumber) {
                return true;
            }
        }

        return false;
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);
        passwordEditText.setEnabled(!isLoading);
        confirmPasswordEditText.setEnabled(!isLoading);
        displayNameEditText.setEnabled(!isLoading);
        loginTextView.setEnabled(!isLoading);
    }

    /**
     * Navigate to main activity
     */
    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Navigate back to login activity
     */
    private void navigateToLogin() {
        finish();
    }
}