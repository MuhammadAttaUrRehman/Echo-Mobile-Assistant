package com.example.echo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echo.R;
import com.example.echo.data.model.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes;
    private final SimpleDateFormat dateFormat;

    public NoteAdapter(List<Note> notes) {
        this.notes = notes;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.titleView.setText(note.getTitle() != null ? note.getTitle() : "Untitled");
        holder.contentView.setText(note.getContent() != null ? note.getContent() : "");
        holder.dateView.setText(dateFormat.format(new java.util.Date(note.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

    public void updateNotes(List<Note> newNotes) {
        this.notes = newNotes != null ? newNotes : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView contentView;
        TextView dateView;

        NoteViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.note_title);
            contentView = itemView.findViewById(R.id.note_content);
            dateView = itemView.findViewById(R.id.note_date);
        }
    }
}