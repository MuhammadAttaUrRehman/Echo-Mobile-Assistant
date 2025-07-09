package com.example.echo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echo.R;
import com.example.echo.ui.fragments.SettingsFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder> {
    private final List<SettingsFragment.SettingItem> settings;

    public SettingsAdapter(List<SettingsFragment.SettingItem> settings) {
        this.settings = settings;
    }

    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting, parent, false);
        return new SettingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        SettingsFragment.SettingItem item = settings.get(position);
        holder.title.setText(item.title);
        if (item.isSwitch) {
            holder.settingSwitch.setVisibility(View.VISIBLE);
            holder.settingSwitch.setChecked(item.isChecked);
            holder.itemView.setOnClickListener(v -> {
                holder.settingSwitch.setChecked(!holder.settingSwitch.isChecked());
                item.listener.onClick(v);
            });
        } else {
            holder.settingSwitch.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(item.listener);
        }
    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    static class SettingViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        SwitchMaterial settingSwitch;

        SettingViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.setting_title);
            settingSwitch = itemView.findViewById(R.id.setting_switch);
        }
    }
}