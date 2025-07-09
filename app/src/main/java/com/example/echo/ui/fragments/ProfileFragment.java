package com.example.echo.ui.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.echo.R;
import com.example.echo.data.model.User;
import com.example.echo.data.repositories.UserRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.example.echo.BuildConfig;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ImageView ivProfileImage;
    private Button btnUploadImage;
    private TextView profileNameTextView;
    private TextView profileEmailTextView;
    private TextView profileLanguageTextView;
    private TextView profileNotificationsTextView;
    private TextView profileActiveStatusTextView;
    private TextView profileLastLoginTextView;

    private DatabaseReference userRef;
    private Uri profileImageUri;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        btnUploadImage = view.findViewById(R.id.btnUploadImage);
        profileNameTextView = view.findViewById(R.id.profile_name);
        profileEmailTextView = view.findViewById(R.id.profile_email);
        profileLanguageTextView = view.findViewById(R.id.profile_language);
        profileNotificationsTextView = view.findViewById(R.id.profile_notifications);
        profileActiveStatusTextView = view.findViewById(R.id.profile_active_status);
        profileLastLoginTextView = view.findViewById(R.id.profile_last_login);

        // Initialize Firebase and UserRepository
        userRef = FirebaseDatabase.getInstance().getReference("users");
        userRepository = new UserRepository(new com.example.echo.data.local.database.dao.UserDao(new com.example.echo.database.EchoDbHelper(requireContext())), requireContext());

        // Initialize Cloudinary
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
        config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
        config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);
        MediaManager.init(requireContext(), config);

        // Set listeners
        setListeners();

        // Load user data from Firebase
        loadUserData();

        return view;
    }

    private void setListeners() {
        btnUploadImage.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .crop() // Optional: Crop the image
                    .start();
        });
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String displayName = snapshot.child("displayName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String preferredLanguage = snapshot.child("preferredLanguage").getValue(String.class);
                    Boolean notificationsEnabled = snapshot.child("notificationsEnabled").getValue(Boolean.class);
                    Boolean active = snapshot.child("active").getValue(Boolean.class);
                    Long lastLoginAt = snapshot.child("lastLoginAt").getValue(Long.class);
                    String photoUrl = snapshot.child("photoUrl").getValue(String.class);

                    profileNameTextView.setText(displayName != null ? displayName : "Unknown");
                    profileEmailTextView.setText(email != null ? email : "No email");
                    profileLanguageTextView.setText("Preferred Language: " + (preferredLanguage != null ? preferredLanguage.toUpperCase() : "Not set"));
                    profileNotificationsTextView.setText("Notifications: " + (notificationsEnabled != null && notificationsEnabled ? "Enabled" : "Disabled"));
                    profileActiveStatusTextView.setText("Status: " + (active != null && active ? "Active" : "Inactive"));

                    if (lastLoginAt != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy, hh:mm a", java.util.Locale.getDefault());
                        String lastLogin = sdf.format(new java.util.Date(lastLoginAt));
                        profileLastLoginTextView.setText("Last Login: " + lastLogin);
                    } else {
                        profileLastLoginTextView.setText("Last Login: Unknown");
                    }

                    // Load profile image
                    if (photoUrl != null) {
                        Glide.with(requireContext()).load(photoUrl).into(ivProfileImage);
                    } else {
                        ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder);
                    }

                    // Add click listener to view image
                    ivProfileImage.setOnClickListener(v -> {
                        if (photoUrl != null) {
                            Intent intent = new Intent(requireContext(), com.example.echo.ui.activities.ImageViewActivity.class); // Adjust package if needed
                            intent.putExtra("screenshotUrl", photoUrl);
                            startActivity(intent);
                        }
                    });
                } else {
                    setFallbackData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setFallbackData();
            }
        });
    }

    private void setFallbackData() {
        profileNameTextView.setText("Not logged in");
        profileEmailTextView.setText("Please log in to view your profile");
        profileLanguageTextView.setText("Preferred Language: Not set");
        profileNotificationsTextView.setText("Notifications: Disabled");
        profileActiveStatusTextView.setText("Status: Inactive");
        profileLastLoginTextView.setText("Last Login: Unknown");
        ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ImagePicker.REQUEST_CODE) {
            profileImageUri = data.getData();
            if (ivProfileImage != null) {
                ivProfileImage.setImageURI(profileImageUri);
                uploadProfileImage();
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfileImage() {
        if (profileImageUri != null) {
            MediaManager.get().upload(profileImageUri)
                    .option("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d("ProfileFragment", "Upload started: " + requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Optional: Show progress if needed
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = (String) resultData.get("secure_url");
                            updateProfileImageUrl(imageUrl);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Toast.makeText(requireContext(), "Image upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.w("ProfileFragment", "Upload rescheduled: " + requestId + " due to " + error.getDescription());
                        }
                    }).dispatch();
        }
    }

    private void updateProfileImageUrl(String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("photoUrl", imageUrl);
        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Glide.with(requireContext()).load(imageUrl).into(ivProfileImage);
                    Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show();
                    // Update local database via UserRepository
                    userRepository.getCurrentUser(user -> {
                        if (user != null) {
                            user.setPhotoUrl(imageUrl);
                            userRepository.saveUser(user, updatedUser -> {
                                if (updatedUser != null) {
                                    Toast.makeText(requireContext(), "User saved successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to save user", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}