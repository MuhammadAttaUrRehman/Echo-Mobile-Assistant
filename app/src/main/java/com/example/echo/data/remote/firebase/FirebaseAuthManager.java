package com.example.echo.data.remote.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.echo.data.model.User;

/**
 * Manager class for Firebase Authentication operations
 * Handles user authentication, registration, and profile management
 */
public class FirebaseAuthManager {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;

    public FirebaseAuthManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    /**
     * Login a user with email and password
     * @param email user's email
     * @param password user's password
     * @param callback callback to report result
     */
    public void loginUser(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            callback.onSuccess(firebaseUser.getUid());
                        } else {
                            callback.onError("No user found after successful login");
                        }
                    } else {
                        callback.onError(task.getException() != null ? task.getException().getMessage() : "Login failed");
                    }
                });
    }

    /**
     * Register a new user with email and password
     * @param email user's email
     * @param password user's password
     * @param callback callback to report result
     */
    public void registerUser(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            callback.onSuccess(firebaseUser.getUid());
                        } else {
                            callback.onError("No user found after successful registration");
                        }
                    } else {
                        callback.onError(task.getException() != null ? task.getException().getMessage() : "Registration failed");
                    }
                });
    }

    /**
     * Logout the current user
     */
    public void logoutUser() {
        firebaseAuth.signOut();
    }

    /**
     * Get user details from Firebase
     * @param userId ID of the user
     * @param callback callback to return the user
     */
    public void getUserDetails(String userId, UserCallback callback) {
        databaseReference.child(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        User user = task.getResult().getValue(User.class);
                        callback.onUserLoaded(user);
                    } else {
                        callback.onUserLoaded(null);
                    }
                });
    }

    /**
     * Save user details to Firebase
     * @param user user to save
     * @param callback callback to report result
     */
    public void saveUserDetails(User user, SaveCallback callback) {
        if (user == null || user.getUid() == null) {
            callback.onSaveComplete(false);
            return;
        }
        databaseReference.child(user.getUid()).setValue(user)
                .addOnCompleteListener(task -> callback.onSaveComplete(task.isSuccessful()));
    }

    /**
     * Check if a user is currently authenticated
     * @return true if a user is signed in
     */
    public boolean isUserAuthenticated() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Callback interface for authentication operations
     */
    public interface AuthCallback {
        void onSuccess(String userId);
        void onError(String errorMessage);
    }

    /**
     * Callback interface for user operations
     */
    public interface UserCallback {
        void onUserLoaded(User user);
    }

    /**
     * Callback interface for save operations
     */
    public interface SaveCallback {
        void onSaveComplete(boolean success);
    }
}