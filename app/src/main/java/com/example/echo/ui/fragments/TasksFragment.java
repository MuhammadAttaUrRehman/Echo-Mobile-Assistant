package com.example.echo.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.echo.R;
import com.example.echo.data.model.Note;
import com.example.echo.data.model.Reminder;
import com.example.echo.data.repositories.NoteRepository;
import com.example.echo.data.repositories.ReminderRepository;
import com.example.echo.ui.activities.NotesActivity;
import com.example.echo.ui.activities.RemindersActivity;

import java.util.List;

public class TasksFragment extends Fragment {
    private static final String TAG = "TasksFragment";
    private TextView notesCount;
    private TextView remindersCount;
    private CardView notesCard;
    private CardView remindersCard;
    private NoteRepository noteRepository;
    private ReminderRepository reminderRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating view");
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Find all views within the cards
        notesCard = view.findViewById(R.id.notes_card);
        remindersCard = view.findViewById(R.id.reminders_card);

        // Find TextViews within the cards
        View notesCardContent = notesCard.getChildAt(0);
        View remindersCardContent = remindersCard.getChildAt(0);

        if (notesCardContent instanceof ViewGroup) {
            notesCount = ((ViewGroup) notesCardContent).findViewById(R.id.notes_count);
        }

        if (remindersCardContent instanceof ViewGroup) {
            remindersCount = ((ViewGroup) remindersCardContent).findViewById(R.id.reminders_count);
        }

        if (notesCount == null || remindersCount == null) {
            Log.e(TAG, "Failed to find counter TextViews");
            return view;
        }

        // Initialize repositories
        noteRepository = new NoteRepository(requireContext());
        reminderRepository = new ReminderRepository(requireContext());

        setupClickListeners();
        loadData();

        return view;
    }

    private void setupClickListeners() {
        notesCard.setOnClickListener(v -> {
            Log.d(TAG, "Notes card clicked, launching NotesActivity");
            Intent intent = new Intent(requireContext(), NotesActivity.class);
            startActivity(intent);
        });

        remindersCard.setOnClickListener(v -> {
            Log.d(TAG, "Reminders card clicked, launching RemindersActivity");
            Intent intent = new Intent(requireContext(), RemindersActivity.class);
            startActivity(intent);
        });
    }

    private void loadData() {
        noteRepository.getAllNotes(new NoteRepository.NotesCallback() {
            @Override
            public void onSuccess(List<Note> notes) {
                if (notesCount != null) {
                    notesCount.setText(String.format("%d notes", notes.size()));
                    Log.d(TAG, "Loaded " + notes.size() + " notes");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading notes: " + e.getMessage());
                if (notesCount != null) {
                    notesCount.setText("0 notes");
                }
            }
        });

        reminderRepository.getAllReminders(new ReminderRepository.RemindersCallback() {
            @Override
            public void onSuccess(List<Reminder> reminders) {
                if (remindersCount != null) {
                    remindersCount.setText(String.format("%d reminders", reminders.size()));
                    Log.d(TAG, "Loaded " + reminders.size() + " reminders");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading reminders: " + e.getMessage());
                if (remindersCount != null) {
                    remindersCount.setText("0 reminders");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up repositories if needed
    }
}