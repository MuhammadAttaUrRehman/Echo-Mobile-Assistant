package com.example.echo.services.assistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import android.util.Log;

import com.example.echo.data.model.Reminder;
import com.example.echo.data.repositories.ReminderRepository;
import com.example.echo.ui.activities.MapActivity;
import com.example.echo.utils.GeofenceUtils;
import com.example.echo.data.remote.openrouter.OpenRouterApiService;

public class EchoAssistantService {
    private static final String TAG = "EchoAssistantService";
    private final Context context;
    private final OpenRouterApiService apiService;
    private final CommandHandler commandHandler;
    private AssistantCallback pendingCallback; // To store callback for retry

    // Constructor for contexts requiring MapActivity (e.g., HomeFragment)
    public EchoAssistantService(Context context, ActivityResultLauncher<Intent> mapActivityLauncher) {
        this.context = context;
        this.apiService = new OpenRouterApiService();
        this.commandHandler = new CommandHandler(context, mapActivityLauncher);
        Log.d(TAG, "EchoAssistantService initialized with MapActivity launcher");
    }

    // Constructor for contexts not requiring MapActivity (e.g., VoiceActivationDialog, VoiceCommandProcessor)
    public EchoAssistantService(Context context) {
        this(context, null);
        Log.d(TAG, "EchoAssistantService initialized without MapActivity launcher");
    }

    public interface AssistantCallback {
        void onResponse(String response);
        void onError(String errorMessage);
    }

    public void processQuery(String query, AssistantCallback callback) {
        String lowerQuery = query.toLowerCase().trim();

        if (commandHandler.isCommand(lowerQuery)) {
            commandHandler.handleCommand(lowerQuery, new CommandHandler.CommandCallback() {
                @Override
                public void onResult(String result) {
                    callback.onResponse(result);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        } else {
            apiService.sendQuery(query, new OpenRouterApiService.OpenRouterCallback() {
                @Override
                public void onSuccess(String response) {
                    callback.onResponse(response);
                }

                @Override
                public void onError(String errorMessage) {
                    callback.onError(errorMessage);
                }
            });
        }
    }

    public void handleMapActivityResult(ActivityResult result, AssistantCallback callback) {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            double latitude = data.getDoubleExtra("latitude", 0.0);
            double longitude = data.getDoubleExtra("longitude", 0.0);
            String locationName = data.getStringExtra("location_name");
            int radius = data.getIntExtra("radius", 100);

            if (latitude == 0.0 || longitude == 0.0) {
                callback.onError("Invalid location data received from MapActivity");
                return;
            }

            // Create reminder
            Reminder reminder = new Reminder();
            reminder.setId(java.util.UUID.randomUUID().toString());
            reminder.setType(Reminder.ReminderType.LOCATION_BASED);
            reminder.setLatitude(latitude);
            reminder.setLongitude(longitude);
            reminder.setRadiusInMeters(radius);
            reminder.setTitle(locationName != null ? locationName : "Location Reminder");
            reminder.setDescription("Location-based reminder set for " + (locationName != null ? locationName : "selected location"));

            // Save reminder asynchronously
            ReminderRepository repository = new ReminderRepository(context);
            repository.saveReminder(reminder, new ReminderRepository.Callback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Reminder saved successfully: " + reminder.getId());
                    addGeofence(reminder, callback);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Failed to save reminder: " + e.getMessage(), e);
                    callback.onError("Failed to save reminder: " + e.getMessage());
                }
            });
        } else {
            callback.onError("Failed to set location-based reminder due to invalid result");
        }
    }

    private void addGeofence(Reminder reminder, AssistantCallback callback) {
        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            GeofenceUtils.addGeofence(context, reminder);
            Log.d(TAG, "Geofence added for reminder: " + reminder.getId());
            callback.onResponse("Location-based reminder set for " + (reminder.getLocationName() != null ? reminder.getLocationName() : "selected location"));
        } else {
            Log.e(TAG, "Location permission not granted, geofence not added for reminder: " + reminder.getId());
            pendingCallback = callback; // Store callback for retry
            callback.onError("Please grant location permission to set geofence");
            // Optionally, trigger permission request if context is an Activity
            if (context instanceof Activity) {
                ((Activity) context).requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 104);
            }
        }
    }

    // Method to retry geofence addition after permission is granted
    public void retryGeofenceAddition(Reminder reminder) {
        if (pendingCallback != null) {
            addGeofence(reminder, pendingCallback);
            pendingCallback = null; // Clear after retry
        }
    }
}