package com.example.echo.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.echo.data.model.Reminder;
import com.example.echo.services.reminder.GeofenceTransitionsJobIntentService;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

public class GeofenceUtils {
    private static final String TAG = "GeofenceUtils";

    public static void addGeofence(Context context, Reminder reminder) {
        if (reminder.getType() != Reminder.ReminderType.LOCATION_BASED) {
            Log.w(TAG, "Attempted to add geofence for non-location-based reminder: " + reminder.getId());
            return;
        }

        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(context);

        Geofence geofence = new Geofence.Builder()
                .setRequestId(reminder.getId())
                .setCircularRegion(
                        reminder.getLatitude(),
                        reminder.getLongitude(),
                        reminder.getRadiusInMeters()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();

        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        Intent intent = new Intent(context, GeofenceTransitionsJobIntentService.class);
        intent.putExtra("reminder_id", reminder.getId());
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        try {
            geofencingClient.addGeofences(request, pendingIntent)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofence added for reminder: " + reminder.getId()))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add geofence: " + e.getMessage()));
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted: " + e.getMessage());
        }
    }
}