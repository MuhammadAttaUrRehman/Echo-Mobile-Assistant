package com.example.echo.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echo.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private RecyclerView settingsRecyclerView;
    private SettingsAdapter settingsAdapter;
    private List<SettingItem> settingItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        // Initialize RecyclerView
        settingsRecyclerView = findViewById(R.id.settings_recycler_view);
        settingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        settingItems = new ArrayList<>();
        settingsAdapter = new SettingsAdapter(settingItems);
        settingsRecyclerView.setAdapter(settingsAdapter);

        // Load settings data
        loadSettingsData();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSettingsData() {
        // Dummy settings data reflecting current date and time: 09:03 AM PKT on Sunday, June 01, 2025
        settingItems.clear();
        settingItems.add(new SettingItem("Notifications", true, true, R.drawable.ic_notifications));
        settingItems.add(new SettingItem("Voice Output", true, true, R.drawable.ic_voice));
        settingItems.add(new SettingItem("Language", false, false, "English", R.drawable.ic_language));
        settingItems.add(new SettingItem("Theme", false, false, "Echo Blue", R.drawable.ic_theme));
        settingItems.add(new SettingItem("Data Sync", false, false, "Auto (Last synced: June 01, 2025, 09:03 AM PKT)", R.drawable.ic_sync));
        settingsAdapter.notifyDataSetChanged();
    }

    // SettingItem class for RecyclerView data
    public static class SettingItem {
        private String title;
        private boolean isToggleable;
        private boolean isEnabled;
        private String value;
        private int iconResId;

        public SettingItem(String title, boolean isToggleable, boolean isEnabled, int iconResId) {
            this.title = title;
            this.isToggleable = isToggleable;
            this.isEnabled = isEnabled;
            this.value = isEnabled ? "On" : "Off";
            this.iconResId = iconResId;
        }

        public SettingItem(String title, boolean isToggleable, boolean isEnabled, String value, int iconResId) {
            this.title = title;
            this.isToggleable = isToggleable;
            this.isEnabled = isEnabled;
            this.value = value;
            this.iconResId = iconResId;
        }

        public String getTitle() { return title; }
        public boolean isToggleable() { return isToggleable; }
        public boolean isEnabled() { return isEnabled; }
        public String getValue() { return value; }
        public int getIconResId() { return iconResId; }
        public void setEnabled(boolean enabled) { this.isEnabled = enabled; this.value = enabled ? "On" : "Off"; }
    }

    // SettingsAdapter for RecyclerView
    private class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

        private final List<SettingItem> items;

        public SettingsAdapter(List<SettingItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting, parent, false);
            return new SettingsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
            SettingItem item = items.get(position);
            holder.titleTextView.setText(item.getTitle());
            holder.iconImageView.setImageResource(item.getIconResId());

            if (item.isToggleable()) {
                holder.switchView.setVisibility(View.VISIBLE);
                holder.switchView.setChecked(item.isEnabled());
                holder.valueTextView.setVisibility(View.GONE);
                holder.switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    item.setEnabled(isChecked);
                    // Add logic to save the setting (e.g., to SharedPreferences or Firebase)
                });
                holder.itemView.setOnClickListener(null); // Disable click for toggleable items
            } else {
                holder.switchView.setVisibility(View.GONE);
                holder.valueTextView.setVisibility(View.VISIBLE);
                holder.valueTextView.setText(item.getValue());
                holder.itemView.setOnClickListener(v -> {
                    // Add logic to handle clicks on non-toggleable items (e.g., show dialog for Language or Theme)
                    if (item.getTitle().equals("Language")) {
                        Toast.makeText(SettingsActivity.this, "Language selection not implemented", Toast.LENGTH_SHORT).show();
                    } else if (item.getTitle().equals("Theme")) {
                        Toast.makeText(SettingsActivity.this, "Theme selection not implemented", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class SettingsViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView, valueTextView;
            ImageView iconImageView;
            SwitchMaterial switchView;

            public SettingsViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.setting_title);
                valueTextView = itemView.findViewById(R.id.setting_value);
                iconImageView = itemView.findViewById(R.id.setting_icon);
                switchView = itemView.findViewById(R.id.setting_switch);
            }
        }
    }
}