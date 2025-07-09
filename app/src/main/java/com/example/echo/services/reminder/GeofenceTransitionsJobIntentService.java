package com.example.echo.services.reminder;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import com.example.echo.data.model.Reminder;
import com.example.echo.data.repositories.ReminderRepository;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceTransitionsJobIntentService extends JobIntentService {
    private static final String TAG = "GeofenceJobService";

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofenceTransitionsJobIntentService.class, 1000, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            Log.e(TAG, "Geofencing error: " + event.getErrorCode());
            return;
        }

        int transition = event.getGeofenceTransition();
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            for (Geofence geofence : event.getTriggeringGeofences()) {
                String reminderId = geofence.getRequestId();
                ReminderRepository repository = new ReminderRepository(this);
                repository.getReminderById(reminderId, new ReminderRepository.ReminderCallback() {
                    @Override
                    public void onSuccess(Reminder reminder) {
                        if (reminder != null) {
                            ReminderNotificationService.showLocationReminderNotification(
                                    getApplicationContext(),
                                    reminder.getId(),
                                    reminder.getTitle(),
                                    reminder.getDescription()
                            );
                            Log.d(TAG, "Geofence entered for reminder: " + reminderId);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error fetching reminder: " + e.getMessage());
                    }
                });
            }
        }
    }
}