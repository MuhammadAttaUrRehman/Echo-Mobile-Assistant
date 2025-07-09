package com.example.echo.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echo.R;
import com.example.echo.adapters.ReminderAdapter;
import com.example.echo.data.model.Reminder;
import com.example.echo.data.repositories.ReminderRepository;
import com.example.echo.ui.dialogs.LocationReminderDialog;
import com.example.echo.ui.dialogs.TimeReminderDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class RemindersFragment extends Fragment {

    private RecyclerView remindersRecyclerView;
    private ReminderAdapter reminderAdapter;
    private ProgressBar progressBar;
    private ReminderRepository reminderRepository;
    private List<Reminder> reminders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminders, container, false);

        // Initialize views
        remindersRecyclerView = view.findViewById(R.id.reminders_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        FloatingActionButton addReminderButton = view.findViewById(R.id.add_reminder_button);

        // Initialize repository
        reminderRepository = new ReminderRepository(requireContext()); // Assumes constructor exists

        // Set up RecyclerView
        reminderAdapter = new ReminderAdapter(reminders);
        remindersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        remindersRecyclerView.setAdapter(reminderAdapter);

        // Set up FAB click listener
        addReminderButton.setOnClickListener(v -> showReminderTypeDialog());

        // Load reminders
        loadReminders();

        return view;
    }

    private void showReminderTypeDialog() {
        // Create a simple dialog to choose between time-based and location-based reminders
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Reminder Type")
                .setItems(new String[]{"Time-Based", "Location-Based"}, (dialog, which) -> {
                    if (which == 0) {
                        showTimeReminderDialog();
                    } else {
                        showLocationReminderDialog();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showTimeReminderDialog() {
        TimeReminderDialog dialog = new TimeReminderDialog();
        dialog.setOnReminderSaveListener(reminder -> {
            saveReminder(reminder);
            Toast.makeText(requireContext(), "Time-based reminder saved", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "TimeReminderDialog");
    }

    private void showLocationReminderDialog() {
        LocationReminderDialog dialog = new LocationReminderDialog();
        dialog.setOnReminderSaveListener(reminder -> {
            saveReminder(reminder);
            Toast.makeText(requireContext(), "Location-based reminder saved", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "LocationReminderDialog");
    }

    private void saveReminder(Reminder reminder) {
        reminderRepository.saveReminder(reminder, new ReminderRepository.Callback() {
            @Override
            public void onSuccess() {
                loadReminders(); // Refresh the list
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Error saving reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReminders() {
        progressBar.setVisibility(View.VISIBLE);
        reminderRepository.getAllReminders(new ReminderRepository.RemindersCallback() {
            @Override
            public void onSuccess(List<Reminder> loadedReminders) {
                reminders.clear();
                reminders.addAll(loadedReminders);
                reminderAdapter.updateReminders(reminders);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error loading reminders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}