package com.example.echo.data.repositories;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.echo.data.local.database.dao.UserDao;
import com.example.echo.data.local.preferences.AppPreferences;
import com.example.echo.data.model.User;
import com.example.echo.data.remote.firebase.FirebaseAuthManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Repository that manages user-related data operations
 * Acts as a single source of truth for user data by coordinating between
 * local storage (database, preferences) and remote sources (Firebase)
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private final UserDao userDao;
    private final AppPreferences appPreferences;
    private final FirebaseAuthManager firebaseAuthManager;
    private final Handler mainHandler;

    public UserRepository(UserDao userDao, Context context) {
        this.userDao = userDao;
        this.appPreferences = new AppPreferences(context);
        this.firebaseAuthManager = new FirebaseAuthManager();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Get the current user's ID
     * @return Current user's ID if logged in, null otherwise
     */
    public String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : appPreferences.getUserId();
        Log.d(TAG, "Current user ID: " + userId);
        return userId;
    }

    /**
     * Check if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        return appPreferences.isLoggedIn() && firebaseAuthManager.isUserAuthenticated();
    }

    /**
     * Login a user with email and password
     * @param email user's email
     * @param password user's password
     * @param callback callback to return result
     */
    public void loginUser(String email, String password, FirebaseAuthManager.AuthCallback callback) {
        firebaseAuthManager.loginUser(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                firebaseAuthManager.getUserDetails(userId, user -> {
                    new AsyncTask<Void, Void, Long>() {
                        @Override
                        protected Long doInBackground(Void... voids) {
                            try {
                                if (user != null) {
                                    return userDao.saveUser(user);
                                }
                                return -1L;
                            } catch (Exception e) {
                                Log.e(TAG, "Error saving user during login: " + e.getMessage(), e);
                                return -1L;
                            }
                        }

                        @Override
                        protected void onPostExecute(Long result) {
                            mainHandler.post(() -> {
                                Log.d(TAG, "Login success, userId: " + userId + ", Saved user to local DB, result: " + result);
                                appPreferences.setLoggedIn(true);
                                appPreferences.setUserId(userId);
                                appPreferences.setUserEmail(email);
                                callback.onSuccess(userId);
                            });
                        }
                    }.execute();
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    Log.e(TAG, "Login error: " + errorMessage);
                    callback.onError(errorMessage);
                });
            }
        });
    }

    /**
     * Register a new user with email, password, and display name
     * @param email user's email
     * @param password user's password
     * @param displayName user's display name
     * @param callback callback to return result
     */
    public void registerUser(String email, String password, String displayName, FirebaseAuthManager.AuthCallback callback) {
        firebaseAuthManager.registerUser(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                User newUser = new User(userId, email, displayName);
                firebaseAuthManager.saveUserDetails(newUser, success -> {
                    if (success) {
                        new AsyncTask<Void, Void, Long>() {
                            @Override
                            protected Long doInBackground(Void... voids) {
                                try {
                                    return userDao.saveUser(newUser);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error saving user during registration: " + e.getMessage(), e);
                                    return -1L;
                                }
                            }

                            @Override
                            protected void onPostExecute(Long result) {
                                mainHandler.post(() -> {
                                    Log.d(TAG, "Registration success, userId: " + userId + ", Saved user to local DB, result: " + result);
                                    appPreferences.setLoggedIn(true);
                                    appPreferences.setUserId(userId);
                                    appPreferences.setUserEmail(email);
                                    callback.onSuccess(userId);
                                });
                            }
                        }.execute();
                    } else {
                        mainHandler.post(() -> {
                            Log.e(TAG, "Failed to save user details to Firebase");
                            callback.onError("Failed to save user details");
                        });
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    Log.e(TAG, "Registration error: " + errorMessage);
                    callback.onError(errorMessage);
                });
            }
        });
    }

    /**
     * Logout the current user
     */
    public void logoutUser() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                try {
                    return userDao.clearAllUsers();
                } catch (Exception e) {
                    Log.e(TAG, "Error clearing users during logout: " + e.getMessage(), e);
                    return 0;
                }
            }

            @Override
            protected void onPostExecute(Integer rowsDeleted) {
                mainHandler.post(() -> {
                    firebaseAuthManager.logoutUser();
                    appPreferences.setLoggedIn(false);
                    appPreferences.setUserId(null);
                    appPreferences.setUserEmail(null);
                    Log.d(TAG, "Cleared local user data, rows deleted: " + rowsDeleted);
                });
            }
        }.execute();
    }

    /**
     * Get the currently logged in user
     * @param callback callback to return result
     */
    public void getCurrentUser(UserCallback callback) {
        String userId = appPreferences.getUserId();
        if (userId == null || !firebaseAuthManager.isUserAuthenticated()) {
            mainHandler.post(() -> callback.onUserLoaded(null));
            return;
        }

        new AsyncTask<Void, Void, User>() {
            @Override
            protected User doInBackground(Void... voids) {
                try {
                    return userDao.getUserByUid(userId);
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching user by UID: " + e.getMessage(), e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(User localUser) {
                if (localUser != null) {
                    mainHandler.post(() -> callback.onUserLoaded(localUser));
                } else {
                    firebaseAuthManager.getUserDetails(userId, user -> mainHandler.post(() -> callback.onUserLoaded(user)));
                }
            }
        }.execute();
    }

    /**
     * Save a user to both local database and Firebase
     * @param user User object to save
     * @param callback Callback to return the result
     */
    public void saveUser(User user, UserCallback callback) {
        if (user == null || user.getUid() == null) {
            mainHandler.post(() -> callback.onUserLoaded(null));
            Log.e(TAG, "Cannot save null user or user without UID");
            return;
        }

        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                try {
                    long localResult = userDao.saveUser(user);
                    if (localResult != -1) {
                        firebaseAuthManager.saveUserDetails(user, success -> {
                            if (success) {
                                Log.d(TAG, "User saved to Firebase successfully for UID: " + user.getUid());
                            } else {
                                Log.e(TAG, "Failed to save user to Firebase for UID: " + user.getUid());
                            }
                        });
                    }
                    return localResult;
                } catch (Exception e) {
                    Log.e(TAG, "Error saving user: " + e.getMessage(), e);
                    return -1L;
                }
            }

            @Override
            protected void onPostExecute(Long result) {
                mainHandler.post(() -> {
                    if (result != -1) {
                        Log.d(TAG, "User saved to local DB with result: " + result + " for UID: " + user.getUid());
                        callback.onUserLoaded(user);
                    } else {
                        Log.e(TAG, "Failed to save user for UID: " + user.getUid());
                        callback.onUserLoaded(null);
                    }
                });
            }
        }.execute();
    }

    /**
     * Callback interface for user operations
     */
    public interface UserCallback {
        void onUserLoaded(User user);
    }
}