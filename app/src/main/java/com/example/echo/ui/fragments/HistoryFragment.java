package com.example.echo.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echo.R;
import com.example.echo.data.remote.firebase.FirebaseRealtimeDbManager;
import com.example.echo.data.repositories.UserRepository;
import com.example.echo.data.local.database.dao.UserDao;
import com.example.echo.database.EchoDbHelper;
import com.example.echo.ui.activities.ConversationActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyItems;
    private FirebaseRealtimeDbManager firebaseDbManager;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Initialize dependencies
        firebaseDbManager = new FirebaseRealtimeDbManager();
        userRepository = new UserRepository(new UserDao(new EchoDbHelper(requireContext())), requireContext());

        // Initialize RecyclerView
        historyRecyclerView = view.findViewById(R.id.history_recycler_view);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyItems = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyItems, this::onConversationClick);
        historyRecyclerView.setAdapter(historyAdapter);

        // Load conversation history
        loadHistoryData();

        return view;
    }

    private void onConversationClick(HistoryItem item) {
        Intent intent = new Intent(requireContext(), ConversationActivity.class);
        intent.putExtra("conversation_id", item.getConversationId());
        intent.putExtra("conversation_title", item.getTitle());
        startActivity(intent);
    }

    private void loadHistoryData() {
        String userId = userRepository.getCurrentUserId();
        if (userId != null) {
            firebaseDbManager.getConversationTitles(userId, conversations -> {
        historyItems.clear();
                for (FirebaseRealtimeDbManager.ConversationInfo conversation : conversations) {
                    historyItems.add(new HistoryItem(
                        conversation.getId(),
                        conversation.getTitle(),
                        formatDate(conversation.getTimestamp())
                    ));
                }
        historyAdapter.notifyDataSetChanged();
            });
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // HistoryItem class for RecyclerView data
    public static class HistoryItem {
        private String conversationId;
        private String title;
        private String date;

        public HistoryItem(String conversationId, String title, String date) {
            this.conversationId = conversationId;
            this.title = title;
            this.date = date;
        }

        public String getConversationId() { return conversationId; }
        public String getTitle() { return title; }
        public String getDate() { return date; }
    }

    // HistoryAdapter for RecyclerView
    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

        private final List<HistoryItem> items;
        private final OnConversationClickListener listener;

        public interface OnConversationClickListener {
            void onConversationClick(HistoryItem item);
        }

        public HistoryAdapter(List<HistoryItem> items, OnConversationClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            HistoryItem item = items.get(position);
            holder.titleTextView.setText(item.getTitle());
            holder.dateTextView.setText(item.getDate());
            holder.itemView.setOnClickListener(v -> listener.onConversationClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class HistoryViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView;
            TextView dateTextView;

            public HistoryViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.history_title);
                dateTextView = itemView.findViewById(R.id.history_date);
            }
        }
    }
}