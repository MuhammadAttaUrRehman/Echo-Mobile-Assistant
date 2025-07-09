package com.example.echo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echo.R;
import com.example.echo.data.model.ConversationMessage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;
    private List<ConversationMessage> messages;
    private final SimpleDateFormat dateFormat;

    public ConversationAdapter(List<ConversationMessage> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUserMessage() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_message, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ai_message, parent, false);
            return new AIMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ConversationMessage message = messages.get(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else {
            ((AIMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ConversationMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timestampText;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
        }

        void bind(ConversationMessage message) {
            messageText.setText(message.getContent());
            timestampText.setText(dateFormat.format(message.getTimestamp()));
        }
    }

    class AIMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timestampText;

        AIMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
        }

        void bind(ConversationMessage message) {
            messageText.setText(message.getContent());
            timestampText.setText(dateFormat.format(message.getTimestamp()));
        }
    }
}