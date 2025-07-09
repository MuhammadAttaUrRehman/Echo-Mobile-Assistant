package com.example.echo.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echo.R;
import com.example.echo.adapters.ConversationAdapter;
import com.example.echo.data.local.database.dao.ConversationDao;
import com.example.echo.data.local.database.dao.UserDao;
import com.example.echo.data.model.ConversationMessage;
import com.example.echo.data.remote.firebase.FirebaseRealtimeDbManager;
import com.example.echo.data.repositories.ConversationRepository;
import com.example.echo.data.repositories.UserRepository;
import com.example.echo.database.EchoDbHelper;
import com.example.echo.services.assistant.EchoAssistantService;
import com.example.echo.services.assistant.TextToSpeechService;
import com.example.echo.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private EditText queryEditText;
    private ImageButton microphoneButton;
    private MaterialButton sendButton;
    private MaterialButton newConversationButton;
    private RecyclerView conversationRecyclerView;
    private ProgressBar progressBar;
    private ConversationAdapter conversationAdapter;
    private List<ConversationMessage> conversationMessages;
    private EchoAssistantService assistantService;
    private ConversationRepository conversationRepository;
    private UserRepository userRepository;
    private ConversationDao conversationDao;
    private FirebaseRealtimeDbManager firebaseDbManager;
    private PreferenceManager preferenceManager;
    private TextToSpeechService ttsService;
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;
    private ActivityResultLauncher<Intent> speechRecognitionLauncher;
    private ActivityResultLauncher<Intent> mapActivityLauncher;
    private String currentConversationId;
    private String currentConversationTitle;
    private EchoAssistantService echoAssistantService;

    public interface PermissionRequestListener {
        void onPermissionRequired(String permission, String command);
    }

    private PermissionRequestListener permissionRequestListener;

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        if (activity instanceof PermissionRequestListener) {
            permissionRequestListener = (PermissionRequestListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement PermissionRequestListener");
        }

        mapActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            assistantService.handleMapActivityResult(result, new EchoAssistantService.AssistantCallback() {
                @Override
                public void onResponse(String response) {
                    processAssistantResponse(response);
                }

                @Override
                public void onError(String errorMessage) {
                    processAssistantError(errorMessage);
                }
            });
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        echoAssistantService = new EchoAssistantService(requireContext(), mapActivityLauncher);

        // Initialize views
        queryEditText = view.findViewById(R.id.query_edit_text);
        microphoneButton = view.findViewById(R.id.microphone_button);
        sendButton = view.findViewById(R.id.send_button);
        newConversationButton = view.findViewById(R.id.new_conversation_button);
        conversationRecyclerView = view.findViewById(R.id.conversation_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize dependencies
        EchoDbHelper dbHelper = new EchoDbHelper(requireContext());
        userRepository = new UserRepository(new UserDao(dbHelper), requireContext());
        conversationDao = new ConversationDao(requireContext());
        conversationRepository = new ConversationRepository(requireContext());
        firebaseDbManager = new FirebaseRealtimeDbManager();
        preferenceManager = new PreferenceManager(requireContext());
        assistantService = new EchoAssistantService(requireContext(), mapActivityLauncher);
        conversationMessages = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversationMessages);
        conversationRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        conversationRecyclerView.setAdapter(conversationAdapter);

        // Initialize Text-to-Speech asynchronously
        initializeTtsAsync();

        // Set up permissions launcher
        requestPermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            Boolean micGranted = permissions.getOrDefault(Manifest.permission.RECORD_AUDIO, false);
            Boolean smsGranted = permissions.getOrDefault(Manifest.permission.SEND_SMS, false);
            Boolean callGranted = permissions.getOrDefault(Manifest.permission.CALL_PHONE, false);
            Boolean locationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);

            if (micGranted) {
                startSpeechRecognition();
            } else {
                Toast.makeText(requireContext(), R.string.mic_permission_denied, Toast.LENGTH_LONG).show();
            }
            if (!smsGranted) {
                Toast.makeText(requireContext(), "SMS permission needed for sending messages", Toast.LENGTH_SHORT).show();
            }
            if (!callGranted) {
                Toast.makeText(requireContext(), "Call permission needed for making calls", Toast.LENGTH_SHORT).show();
            }
            if (!locationGranted) {
                Toast.makeText(requireContext(), "Location permission needed for location-based reminders", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up speech recognition launcher
        speechRecognitionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (matches != null && !matches.isEmpty()) {
                    String query = matches.get(0);
                    queryEditText.setText(query);
                    processQuery(query);
                }
            } else {
                Log.w(TAG, "Speech recognition result not successful");
            }
        });

        // Set click listeners
        microphoneButton.setOnClickListener(v -> checkPermissions());
        sendButton.setOnClickListener(v -> {
            String query = queryEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(query)) {
                processQuery(query);
            }
        });

        newConversationButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.new_conversation);
            final EditText input = new EditText(requireContext());
            input.setHint(R.string.start_conversation_prompt);
            builder.setView(input);
            builder.setPositiveButton(R.string.start_conversation, (dialog, which) -> {
                String title = input.getText().toString().trim();
                if (!TextUtils.isEmpty(title)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                    String dateStr = sdf.format(new Date());
                    currentConversationId = title + "_" + dateStr;
                    currentConversationTitle = title;
                    conversationMessages.clear();
                    conversationAdapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), "New conversation: " + title, Toast.LENGTH_SHORT).show();

                    String userId = userRepository.getCurrentUserId();
                    if (userId != null) {
                        firebaseDbManager.saveConversationTitle(userId, currentConversationId, title);
                        preferenceManager.setCurrentConversationId(currentConversationId);
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.no_conversation_title, Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });

        // Load existing conversation
        loadConversationAsync();

        return view;
    }

    private void initializeTtsAsync() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ttsService = new TextToSpeechService(requireContext(), status -> {
                    if (status != android.speech.tts.TextToSpeech.SUCCESS) {
                        Log.e(TAG, "TTS initialization failed with status: " + status);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), R.string.tts_init_failed, Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        Log.d(TAG, "TTS initialized successfully");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error initializing TTS: " + e.getMessage(), e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), R.string.tts_init_failed, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void loadConversationAsync() {
        currentConversationId = preferenceManager.getCurrentConversationId();
        if (currentConversationId == null) {
            Log.w(TAG, "No current conversation ID found");
            return;
        }

        String userId = userRepository.getCurrentUserId();
        if (userId == null) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), R.string.login_required, Toast.LENGTH_SHORT).show();
                });
            }
            Log.e(TAG, "User ID is null");
            return;
        }

        Log.d(TAG, "Loading conversation: userId=" + userId + ", conversationId=" + currentConversationId);
        firebaseDbManager.loadConversationMessages(userId, currentConversationId, messages -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (messages != null && !messages.isEmpty()) {
                        Log.d(TAG, "Loaded " + messages.size() + " messages from Firebase");
                        conversationMessages.clear();
                        conversationMessages.addAll(messages);
                        conversationAdapter.notifyDataSetChanged();
                        conversationRecyclerView.scrollToPosition(conversationMessages.size() - 1);
                    } else {
                        Log.w(TAG, "No messages loaded from Firebase");
                        conversationMessages.clear();
                        conversationAdapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "No messages found", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        firebaseDbManager.getConversationTitle(userId, currentConversationId, title -> {
            if (title != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    currentConversationTitle = title;
                    Log.d(TAG, "Loaded conversation title: " + title);
                });
            } else {
                Log.w(TAG, "No title loaded for conversationId: " + currentConversationId);
            }
        });
    }

    private void checkPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CALL_PHONE);
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!permissionsToRequest.isEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            startSpeechRecognition();
        }
    }

    private void startSpeechRecognition() {
        Log.d(TAG, "Checking if speech recognition is available");
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            Log.w(TAG, "Speech recognition not available on this device");
            Toast.makeText(requireContext(), R.string.speech_not_available, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Echo...");
        try {
            speechRecognitionLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Speech recognition error: " + e.getMessage(), e);
            Toast.makeText(requireContext(), R.string.speech_error, Toast.LENGTH_SHORT).show();
        }
    }

    public void processVoiceQuery(String query) {
        if (!TextUtils.isEmpty(query)) {
            queryEditText.setText(query);
            processQuery(query);
        }
    }

    private void processQuery(String query) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String userId = userRepository.getCurrentUserId();
            if (userId == null) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), R.string.login_required, Toast.LENGTH_SHORT).show();
                    });
                }
                return;
            }

            if (currentConversationId == null) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), R.string.conversation_required, Toast.LENGTH_SHORT).show();
                    });
                }
                return;
            }

            ConversationMessage userMessage = new ConversationMessage(
                    UUID.randomUUID().toString(),
                    currentConversationId,
                    userId,
                    query,
                    System.currentTimeMillis(),
                    true
            );

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    conversationMessages.add(userMessage);
                    conversationAdapter.notifyItemInserted(conversationMessages.size() - 1);
                    conversationRecyclerView.scrollToPosition(conversationMessages.size() - 1);
                    queryEditText.setText("");
                    showLoading(true);
                });
            }

            assistantService.processQuery(query, new EchoAssistantService.AssistantCallback() {
                @Override
                public void onResponse(String response) {
                    processAssistantResponse(response);
                }

                @Override
                public void onError(String errorMessage) {
                    processAssistantError(errorMessage);
                }
            });
        });
    }

    private void processAssistantResponse(String response) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                String userId = userRepository.getCurrentUserId();
                ConversationMessage aiMessage = new ConversationMessage(
                        UUID.randomUUID().toString(),
                        currentConversationId,
                        userId,
                        response,
                        System.currentTimeMillis(),
                        false
                );
                conversationMessages.add(aiMessage);
                conversationAdapter.notifyItemInserted(conversationMessages.size() - 1);
                conversationRecyclerView.scrollToPosition(conversationMessages.size() - 1);

                conversationRepository.saveMessage(aiMessage, new ConversationRepository.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Saved AI message: " + aiMessage.getId());
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error saving AI message: " + e.getMessage(), e);
                    }
                });

                firebaseDbManager.saveConversationMessage(userId, currentConversationId, aiMessage);

                if (ttsService != null) {
                    ttsService.speak(response);
                }

                showLoading(false);
            });
        }
    }

    private void processAssistantError(String errorMessage) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (errorMessage.equals("Please grant permission to read contacts")) {
                    permissionRequestListener.onPermissionRequired(Manifest.permission.READ_CONTACTS, queryEditText.getText().toString());
                    Toast.makeText(requireContext(), "Contacts permission needed", Toast.LENGTH_SHORT).show();
                } else if (errorMessage.equals("Please grant permission to send SMS")) {
                    permissionRequestListener.onPermissionRequired(Manifest.permission.SEND_SMS, queryEditText.getText().toString());
                    Toast.makeText(requireContext(), "SMS permission needed", Toast.LENGTH_SHORT).show();
                } else if (errorMessage.equals("Please grant permission to make calls")) {
                    permissionRequestListener.onPermissionRequired(Manifest.permission.CALL_PHONE, queryEditText.getText().toString());
                    Toast.makeText(requireContext(), "Call permission needed", Toast.LENGTH_SHORT).show();
                } else if (errorMessage.equals("Please grant location permission")) {
                    permissionRequestListener.onPermissionRequired(Manifest.permission.ACCESS_FINE_LOCATION, queryEditText.getText().toString());
                    Toast.makeText(requireContext(), "Location permission needed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
                showLoading(false);
            });
        }
    }

    private void showLoading(boolean isLoading) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                microphoneButton.setEnabled(!isLoading);
                sendButton.setEnabled(!isLoading);
                queryEditText.setEnabled(!isLoading);
                newConversationButton.setEnabled(!isLoading);
            });
        }
    }

    @Override
    public void onDestroy() {
        if (ttsService != null) {
            ttsService.shutdown();
        }
        super.onDestroy();
    }
}