package com.example.echo.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.echo.R;
import com.example.echo.data.model.Note;
import com.example.echo.data.repositories.NoteRepository;

import java.util.UUID;

public class NoteDialog extends DialogFragment {

    private EditText titleEditText;
    private EditText contentEditText;
    private OnNoteSaveListener listener;

    public interface OnNoteSaveListener {
        void onNoteSaved(Note note);
    }

    public void setOnNoteSaveListener(OnNoteSaveListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_note, null);

        // Initialize views
        titleEditText = view.findViewById(R.id.note_title_input);
        contentEditText = view.findViewById(R.id.note_content_input);

        builder.setView(view)
                .setPositiveButton(R.string.save, (dialog, id) -> saveNote())
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());

        return builder.create();
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        // Validate inputs
        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        // Create note with a random ID and current timestamp
        Note note = new Note(
                UUID.randomUUID().toString(),
                "user123", // Replace with actual user ID if available
                title,
                content,
                System.currentTimeMillis()
        );

        // Save note
        NoteRepository repository = new NoteRepository(requireContext());
        repository.saveNote(note, new NoteRepository.Callback() {
            @Override
            public void onSuccess() {
                // Notify listener
                if (listener != null) {
                    listener.onNoteSaved(note);
                }
                Toast.makeText(requireContext(), "Note saved", Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Error saving note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", titleEditText.getText().toString());
        outState.putString("content", contentEditText.getText().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            titleEditText.setText(savedInstanceState.getString("title"));
            contentEditText.setText(savedInstanceState.getString("content"));
        }
    }
}