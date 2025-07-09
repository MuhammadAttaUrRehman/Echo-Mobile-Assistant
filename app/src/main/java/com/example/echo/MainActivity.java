package com.example.echo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.echo.R;
import com.example.echo.data.model.Reminder;
import com.example.echo.database.EchoDbHelper;
import com.example.echo.data.local.database.dao.UserDao;
import com.example.echo.data.repositories.UserRepository;
import com.example.echo.services.assistant.EchoAssistantService;
import com.example.echo.services.background.EchoBackgroundService;
import com.example.echo.ui.activities.LoginActivity;
import com.example.echo.ui.activities.SettingsActivity;
import com.example.echo.ui.fragments.HomeFragment;
import com.example.echo.ui.fragments.HistoryFragment;
import com.example.echo.ui.fragments.ProfileFragment;
import com.example.echo.ui.fragments.TasksFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements HomeFragment.PermissionRequestListener {
    private static final String TAG = "MainActivity";
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private static final int CONTACTS_PERMISSION_CODE = 101;
    private static final int SMS_PERMISSION_CODE = 102;
    private static final int PHONE_PERMISSION_CODE = 103;
    private static final int LOCATION_PERMISSION_CODE = 104;
    private UserRepository userRepository;
    private String lastCommand;
    private EchoAssistantService echoAssistantService;
    private static Reminder pendingReminder; // Static field to store the last reminder needing geofence

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize repository and assistant service
        EchoDbHelper dbHelper = new EchoDbHelper(this);
        UserDao userDao = new UserDao(dbHelper);
        userRepository = new UserRepository(userDao, this);
        echoAssistantService = new EchoAssistantService(this); // Initialize with context only if no MapActivity needed initially
        Log.d(TAG, "Initialized UserRepository and EchoAssistantService");

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
                Log.d(TAG, "Requested POST_NOTIFICATIONS permission");
            }
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            Log.d(TAG, "Toolbar initialized");
        } else {
            Log.e(TAG, "Toolbar not found in layout");
        }

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = getFragmentForItemId(item.getItemId());
            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                Log.d(TAG, "Navigated to fragment: " + selectedFragment.getClass().getSimpleName());
                return true;
            }
            Log.w(TAG, "No fragment found for item ID: " + item.getItemId());
            return false;
        });

        // Check user login status and start service asynchronously
        checkUserLoginAndStartServiceAsync(savedInstanceState);

        // Handle voice query from EchoBackgroundService
        handleVoiceQuery(getIntent());
    }

    private void checkUserLoginAndStartServiceAsync(Bundle savedInstanceState) {
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean isLoggedIn = userRepository.isUserLoggedIn();
            Log.d(TAG, "Checked login status: isLoggedIn=" + isLoggedIn);
            runOnUiThread(() -> {
                if (!isLoggedIn) {
                    navigateToLogin();
                } else {
                    // Load default fragment
                    if (savedInstanceState == null) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, new HomeFragment())
                                .commit();
                        Log.d(TAG, "Loaded default HomeFragment");
                    }
                    // Start EchoBackgroundService
                    Intent serviceIntent = new Intent(this, EchoBackgroundService.class);
                    startForegroundService(serviceIntent);
                    Log.d(TAG, "Started EchoBackgroundService");
                }
            });
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleVoiceQuery(intent);
        Log.d(TAG, "Handled new intent");
    }

    private void handleVoiceQuery(Intent intent) {
        if (intent != null && intent.hasExtra("voice_query")) {
            String query = intent.getStringExtra("voice_query");
            if (query != null) {
                Log.d(TAG, "Received voice query: " + query);
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (fragment instanceof HomeFragment) {
                    ((HomeFragment) fragment).processVoiceQuery(query);
                    BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                    bottomNavigationView.setSelectedItemId(R.id.nav_assistant);
                    Log.d(TAG, "Processed voice query in HomeFragment");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        Log.d(TAG, "Inflated action menu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_logout) {
            Executors.newSingleThreadExecutor().execute(() -> {
                userRepository.logoutUser();
                runOnUiThread(() -> {
                    navigateToLogin();
                    Log.d(TAG, "User logged out");
                });
            });
            return true;
        } else if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            Log.d(TAG, "Navigated to SettingsActivity");
            return true;
        }
        Log.w(TAG, "Unhandled menu item: " + itemId);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case NOTIFICATION_PERMISSION_CODE:
                    Log.d(TAG, "Notification permission granted");
                    break;
                case CONTACTS_PERMISSION_CODE:
                    Log.d(TAG, "Contacts permission granted");
                    retryLastCommand();
                    break;
                case SMS_PERMISSION_CODE:
                    Log.d(TAG, "SMS permission granted");
                    retryLastCommand();
                    break;
                case PHONE_PERMISSION_CODE:
                    Log.d(TAG, "Phone permission granted");
                    retryLastCommand();
                    break;
                case LOCATION_PERMISSION_CODE:
                    Log.d(TAG, "Location permission granted");
                    if (echoAssistantService != null && pendingReminder != null) {
                        echoAssistantService.retryGeofenceAddition(pendingReminder);
                        Log.d(TAG, "Retried geofence addition for reminder: " + pendingReminder.getId());
                        pendingReminder = null; // Clear after retry
                    }
                    retryLastCommand();
                    break;
                default:
                    Log.w(TAG, "Unknown permission request code: " + requestCode);
                    // Forward to fragments if not handled
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (fragment != null) {
                        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    }
            }
        } else {
            Log.w(TAG, "Permission denied for request code: " + requestCode);
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Log.d(TAG, "Navigated to LoginActivity");
    }

    private Fragment getFragmentForItemId(int itemId) {
        if (itemId == R.id.nav_assistant) {
            return new HomeFragment();
        } else if (itemId == R.id.nav_history) {
            return new HistoryFragment();
        } else if (itemId == R.id.nav_tasks) {
            return new TasksFragment();
        } else if (itemId == R.id.nav_profile) {
            return new ProfileFragment();
        }
        Log.w(TAG, "No fragment for itemId: " + itemId);
        return null;
    }

    @Override
    public void onPermissionRequired(String permission, String command) {
        lastCommand = command;
        switch (permission) {
            case Manifest.permission.READ_CONTACTS:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        CONTACTS_PERMISSION_CODE);
                Log.d(TAG, "Requested READ_CONTACTS permission");
                break;
            case Manifest.permission.SEND_SMS:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_CODE);
                Log.d(TAG, "Requested SEND_SMS permission");
                break;
            case Manifest.permission.CALL_PHONE:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        PHONE_PERMISSION_CODE);
                Log.d(TAG, "Requested CALL_PHONE permission");
                break;
            case Manifest.permission.ACCESS_FINE_LOCATION:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_CODE);
                Log.d(TAG, "Requested ACCESS_FINE_LOCATION permission");
                break;
            default:
                Log.w(TAG, "Unknown permission requested: " + permission);
        }
    }

    private void retryLastCommand() {
        if (lastCommand != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment instanceof HomeFragment) {
                ((HomeFragment) fragment).processVoiceQuery(lastCommand);
                Log.d(TAG, "Retried command: " + lastCommand);
            }
            lastCommand = null;
        }
    }
}