package com.example.echo.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.echo.R;
import com.example.echo.data.model.Reminder;
import com.example.echo.data.repositories.ReminderRepository;
import com.example.echo.ui.activities.MapActivity;
import com.example.echo.utils.GeofenceUtils;

import java.util.UUID;

public class LocationReminderDialog extends DialogFragment {

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText locationNameEditText;
    private Button selectLocationButton;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private int radius = 100;
    private OnReminderSaveListener listener;
    private static final int REQUEST_LOCATION = 100;

    public interface OnReminderSaveListener {
        void onReminderSaved(Reminder reminder);
    }

    public void setOnReminderSaveListener(OnReminderSaveListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_location_reminder, null);

        // Initialize views
        titleEditText = view.findViewById(R.id.reminder_title);
        descriptionEditText = view.findViewById(R.id.reminder_description);
        locationNameEditText = view.findViewById(R.id.location_name);
        selectLocationButton = view.findViewById(R.id.select_location_button);

        // Set up location picker button
        selectLocationButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MapActivity.class);
            startActivityForResult(intent, REQUEST_LOCATION);
        });

        builder.setView(view)
                .setPositiveButton(R.string.save, (dialog, id) -> saveReminder())
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION && resultCode == Activity.RESULT_OK && data != null) {
            latitude = data.getDoubleExtra("latitude", 0.0);
            longitude = data.getDoubleExtra("longitude", 0.0);
            radius = data.getIntExtra("radius", 100);
            String locationName = data.getStringExtra("location_name");
            if (locationName != null && !locationName.isEmpty()) {
                locationNameEditText.setText(locationName);
            }
        }
    }

    private void saveReminder() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String locationName = locationNameEditText.getText().toString().trim();

        // Validate inputs
        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }
        if (locationName.isEmpty()) {
            locationNameEditText.setError("Location name is required");
            return;
        }
        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(requireContext(), "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create reminder
        Reminder reminder = new Reminder(
                UUID.randomUUID().toString(),
                title,
                description,
                latitude,
                longitude,
                locationName,
                radius
        );

        // Save reminder
        ReminderRepository repository = new ReminderRepository(requireContext());
        repository.saveReminder(reminder, new ReminderRepository.Callback() {
            @Override
            public void onSuccess() {
                // Add geofence
                GeofenceUtils.addGeofence(requireContext(), reminder);

                // Notify listener
                if (listener != null) {
                    listener.onReminderSaved(reminder);
                }

                Toast.makeText(requireContext(), "Location-based reminder saved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Error saving reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", titleEditText.getText().toString());
        outState.putString("description", descriptionEditText.getText().toString());
        outState.putString("location_name", locationNameEditText.getText().toString());
        outState.putDouble("latitude", latitude);
        outState.putDouble("longitude", longitude);
        outState.putInt("radius", radius);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            titleEditText.setText(savedInstanceState.getString("title"));
            descriptionEditText.setText(savedInstanceState.getString("description"));
            locationNameEditText.setText(savedInstanceState.getString("location_name"));
            latitude = savedInstanceState.getDouble("latitude");
            longitude = savedInstanceState.getDouble("longitude");
            radius = savedInstanceState.getInt("radius");
        }
    }
}