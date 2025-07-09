package com.example.echo.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echo.R;
import com.example.echo.adapters.NoteAdapter;
import com.example.echo.data.model.Note;
import com.example.echo.data.repositories.NoteRepository;
import com.example.echo.ui.dialogs.NoteDialog;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity {
    private static final String TAG = "NotesActivity";
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private NoteRepository noteRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.notes);
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.notes_recycler_view);
        noteRepository = new NoteRepository(this);
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

        // Fetch notes from repository
        noteRepository.getAllNotes(new NoteRepository.NotesCallback() {
            @Override
            public void onSuccess(List<Note> notes) {
                adapter = new NoteAdapter(notes);
                recyclerView.setAdapter(adapter);
                Log.d(TAG, "RecyclerView setup complete with " + notes.size() + " notes");
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading notes: " + e.getMessage());
                adapter = new NoteAdapter(new ArrayList<>());
                recyclerView.setAdapter(adapter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notes_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_add_note) {
            showNoteDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showNoteDialog() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to add a note", Toast.LENGTH_SHORT).show();
            return;
        }
        NoteDialog dialog = new NoteDialog();
        dialog.setOnNoteSaveListener(note -> {
            refreshData(); // Refresh notes
        });
        dialog.show(getSupportFragmentManager(), "NoteDialog");
    }

    private void refreshData() {
        noteRepository.getAllNotes(new NoteRepository.NotesCallback() {
            @Override
            public void onSuccess(List<Note> notes) {
                if (adapter != null) {
                    adapter.updateNotes(notes);
                    Log.d(TAG, "Refreshed with " + notes.size() + " notes");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing notes: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up if needed
    }
}