package com.example.echo.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echo.R;
import com.example.echo.data.model.ConversationMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationMessageAdapter extends RecyclerView.Adapter<ConversationMessageAdapter.MessageViewHolder> {

    private List<ConversationMessage> messages;

    public ConversationMessageAdapter(List<ConversationMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                viewType == 0 ? R.layout.item_user_message : R.layout.item_ai_message,
                parent,
                false
        );
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ConversationMessage message = messages.get(position);
        holder.messageTextView.setText(message.getContent());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.timestampTextView.setText(sdf.format(new Date(message.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUserMessage() ? 0 : 1;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView timestampTextView;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text);
            timestampTextView = itemView.findViewById(R.id.timestamp_text);
        }
    }
}