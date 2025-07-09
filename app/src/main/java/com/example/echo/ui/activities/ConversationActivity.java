package com.example.echo.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echo.R;
import com.example.echo.data.model.ConversationMessage;
import com.example.echo.data.remote.firebase.FirebaseRealtimeDbManager;
import com.example.echo.ui.adapters.ConversationMessageAdapter;
import com.example.echo.data.repositories.UserRepository;
import com.example.echo.data.local.database.dao.UserDao;
import com.example.echo.database.EchoDbHelper;
import java.util.ArrayList;
import java.util.List;

public class ConversationActivity extends AppCompatActivity {
    private RecyclerView conversationRecyclerView;
    private ConversationMessageAdapter adapter;
    private List<ConversationMessage> messages;
    private FirebaseRealtimeDbManager firebaseDbManager;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // Get conversation details from intent
        String conversationId = getIntent().getStringExtra("conversation_id");
        String conversationTitle = getIntent().getStringExtra("conversation_title");

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(conversationTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views and adapters
        conversationRecyclerView = findViewById(R.id.conversation_recycler_view);
        messages = new ArrayList<>();
        adapter = new ConversationMessageAdapter(messages);
        conversationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationRecyclerView.setAdapter(adapter);

        // Initialize repositories
        firebaseDbManager = new FirebaseRealtimeDbManager();
        userRepository = new UserRepository(new UserDao(new EchoDbHelper(this)), this);

        // Load conversation messages
        loadConversationMessages(conversationId);
    }

    private void loadConversationMessages(String conversationId) {
        String userId = userRepository.getCurrentUserId();
        if (userId != null) {
            firebaseDbManager.loadConversationMessages(userId, conversationId, messages -> {
                this.messages.clear();
                this.messages.addAll(messages);
                adapter.notifyDataSetChanged();
                conversationRecyclerView.scrollToPosition(messages.size() - 1);
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}