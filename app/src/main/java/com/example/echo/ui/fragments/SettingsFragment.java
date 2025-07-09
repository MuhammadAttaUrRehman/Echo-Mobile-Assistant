package com.example.echo.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echo.R;
import com.example.echo.adapters.SettingsAdapter;
import com.example.echo.data.local.preferences.AppPreferences;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {
    private RecyclerView settingsRecyclerView;
    private SettingsAdapter settingsAdapter;
    private AppPreferences appPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_settings, container, false);

        settingsRecyclerView = view.findViewById(R.id.settings_recycler_view);
        settingsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        settingsAdapter = new SettingsAdapter(getSettingsItems());
        settingsRecyclerView.setAdapter(settingsAdapter);

        appPreferences = new AppPreferences(getContext());

        return view;
    }

    private List<SettingItem> getSettingsItems() {
        List<SettingItem> items = new ArrayList<>();
        items.add(new SettingItem("Dark Mode", appPreferences.isDarkMode(), true, v -> {
            appPreferences.setDarkMode(!appPreferences.isDarkMode());
            // Recreate activity to apply theme
            getActivity().recreate();
        }));
        items.add(new SettingItem("Notifications", appPreferences.areNotificationsEnabled(), true, v -> {
            appPreferences.setNotificationsEnabled(!appPreferences.areNotificationsEnabled());
        }));
        items.add(new SettingItem("Voice Assistant", appPreferences.isVoiceAssistantEnabled(), true, v -> {
            appPreferences.setVoiceAssistantEnabled(!appPreferences.isVoiceAssistantEnabled());
        }));
        return items;
    }

    public static class SettingItem {
        public String title;
        public boolean isChecked;
        public boolean isSwitch;
        public View.OnClickListener listener;

        SettingItem(String title, boolean isChecked, boolean isSwitch, View.OnClickListener listener) {
            this.title = title;
            this.isChecked = isChecked;
            this.isSwitch = isSwitch;
            this.listener = listener;
        }
    }
}