package com.example.echo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echo.R;
import com.example.echo.data.model.Reminder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private List<Reminder> reminders;
    private final SimpleDateFormat dateFormat;

    public ReminderAdapter(List<Reminder> reminders) {
        this.reminders = reminders;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.titleView.setText(reminder.getTitle() != null ? reminder.getTitle() : "Untitled");
        holder.descriptionView.setText(reminder.getDescription() != null ? reminder.getDescription() : "");

        // Set icon and details based on reminder type
        if (reminder.getType() == Reminder.ReminderType.TIME_BASED) {
            holder.typeIcon.setImageResource(R.drawable.ic_time);
            holder.detailsView.setText(reminder.getScheduledTime() != null ?
                    dateFormat.format(reminder.getScheduledTime()) : "No time set");
        } else if (reminder.getType() == Reminder.ReminderType.LOCATION_BASED) {
            holder.typeIcon.setImageResource(R.drawable.ic_location);
            holder.detailsView.setText(reminder.getLocationName() != null && !reminder.getLocationName().isEmpty() ?
                    reminder.getLocationName() : "Unknown location");
        }
    }

    @Override
    public int getItemCount() {
        return reminders != null ? reminders.size() : 0;
    }

    public void updateReminders(List<Reminder> newReminders) {
        this.reminders = newReminders != null ? newReminders : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        ImageView typeIcon;
        TextView titleView;
        TextView descriptionView;
        TextView detailsView;

        ReminderViewHolder(View itemView) {
            super(itemView);
            typeIcon = itemView.findViewById(R.id.reminder_type_icon);
            titleView = itemView.findViewById(R.id.reminder_title);
            descriptionView = itemView.findViewById(R.id.reminder_description);
            detailsView = itemView.findViewById(R.id.reminder_details);
        }
    }
}