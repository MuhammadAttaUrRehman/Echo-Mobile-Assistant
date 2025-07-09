package com.example.echo.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echo.R;
import com.example.echo.adapters.ReminderAdapter;
import com.example.echo.data.model.Reminder;
import com.example.echo.data.repositories.ReminderRepository;
import com.example.echo.ui.dialogs.LocationReminderDialog;
import com.example.echo.ui.dialogs.TimeReminderDialog;

import java.util.ArrayList;
import java.util.List;

public class RemindersActivity extends AppCompatActivity {
    private static final String TAG = "RemindersActivity";
    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private ReminderRepository reminderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.reminders);
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.reminders_recycler_view);
        reminderRepository = new ReminderRepository(this);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        // Add divider between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                this, layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Fetch reminders from repository
        reminderRepository.getAllReminders(new ReminderRepository.RemindersCallback() {
            @Override
            public void onSuccess(List<Reminder> reminders) {
                adapter = new ReminderAdapter(reminders);
                recyclerView.setAdapter(adapter);
                Log.d(TAG, "RecyclerView setup complete with " + reminders.size() + " reminders");
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading reminders: " + e.getMessage());
                adapter = new ReminderAdapter(new ArrayList<>());
                recyclerView.setAdapter(adapter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reminders_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_add_time_reminder) {
            showTimeReminderDialog();
            return true;
        } else if (item.getItemId() == R.id.action_add_location_reminder) {
            showLocationReminderDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTimeReminderDialog() {
        TimeReminderDialog dialog = new TimeReminderDialog();
        dialog.setOnReminderSaveListener(reminder -> {
            refreshData(); // Refresh reminders
        });
        dialog.show(getSupportFragmentManager(), "TimeReminderDialog");
    }

    private void showLocationReminderDialog() {
        LocationReminderDialog dialog = new LocationReminderDialog();
        dialog.setOnReminderSaveListener(reminder -> {
            refreshData(); // Refresh reminders
        });
        dialog.show(getSupportFragmentManager(), "LocationReminderDialog");
    }

    private void refreshData() {
        reminderRepository.getAllReminders(new ReminderRepository.RemindersCallback() {
            @Override
            public void onSuccess(List<Reminder> reminders) {
                if (adapter != null) {
                    adapter.updateReminders(reminders); // Changed from updateData to updateReminders
                    Log.d(TAG, "Refreshed with " + reminders.size() + " reminders");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing reminders: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up if needed
    }
}