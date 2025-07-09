package com.example.echo.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.echo.R;
import com.example.echo.data.model.Reminder;
import com.example.echo.data.repositories.ReminderRepository;
import com.example.echo.services.reminder.ReminderNotificationService;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class TimeReminderDialog extends DialogFragment {

    private EditText titleEditText;
    private EditText descriptionEditText;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private OnReminderSaveListener listener;

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
        View view = inflater.inflate(R.layout.dialog_time_reminder, null);

        // Initialize views
        titleEditText = view.findViewById(R.id.reminder_title);
        descriptionEditText = view.findViewById(R.id.reminder_description);
        datePicker = view.findViewById(R.id.reminder_date_picker);
        timePicker = view.findViewById(R.id.reminder_time_picker);

        // Initialize DatePicker with current date
        Calendar calendar = Calendar.getInstance();
        datePicker.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                null
        );

        // Initialize TimePicker with current time
        timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(calendar.get(Calendar.MINUTE));

        builder.setView(view)
                .setPositiveButton(R.string.save, (dialog, id) -> saveReminder())
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    private void saveReminder() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        // Validate title
        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        // Get selected date and time
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth(),
                timePicker.getHour(),
                timePicker.getMinute(),
                0
        );
        Date scheduledTime = calendar.getTime();

        // Validate future time
        if (scheduledTime.before(new Date())) {
            Toast.makeText(requireContext(), "Please select a future time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create reminder
        Reminder reminder = new Reminder(
                UUID.randomUUID().toString(),
                title,
                description,
                scheduledTime
        );

        // Save reminder
        ReminderRepository repository = new ReminderRepository(requireContext());
        try {
            repository.saveReminder(reminder, new ReminderRepository.Callback() {
                @Override
                public void onSuccess() {
                    // Schedule notification
                    ReminderNotificationService.scheduleTimeReminder(requireContext(), reminder);

                    // Notify listener
                    if (listener != null) {
                        listener.onReminderSaved(reminder);
                    }

                    Toast.makeText(requireContext(), "Reminder saved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(requireContext(), "Error saving reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error saving reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save input state if needed
        outState.putString("title", titleEditText.getText().toString());
        outState.putString("description", descriptionEditText.getText().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            titleEditText.setText(savedInstanceState.getString("title"));
            descriptionEditText.setText(savedInstanceState.getString("description"));
        }
    }
}