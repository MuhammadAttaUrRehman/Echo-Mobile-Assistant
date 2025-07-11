// Generated by view binder compiler. Do not edit!
package com.example.echo.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.echo.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemSettingBinding implements ViewBinding {
  @NonNull
  private final CardView rootView;

  @NonNull
  public final ImageView settingIcon;

  @NonNull
  public final SwitchMaterial settingSwitch;

  @NonNull
  public final TextView settingTitle;

  @NonNull
  public final TextView settingValue;

  private ItemSettingBinding(@NonNull CardView rootView, @NonNull ImageView settingIcon,
      @NonNull SwitchMaterial settingSwitch, @NonNull TextView settingTitle,
      @NonNull TextView settingValue) {
    this.rootView = rootView;
    this.settingIcon = settingIcon;
    this.settingSwitch = settingSwitch;
    this.settingTitle = settingTitle;
    this.settingValue = settingValue;
  }

  @Override
  @NonNull
  public CardView getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemSettingBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemSettingBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_setting, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemSettingBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.setting_icon;
      ImageView settingIcon = ViewBindings.findChildViewById(rootView, id);
      if (settingIcon == null) {
        break missingId;
      }

      id = R.id.setting_switch;
      SwitchMaterial settingSwitch = ViewBindings.findChildViewById(rootView, id);
      if (settingSwitch == null) {
        break missingId;
      }

      id = R.id.setting_title;
      TextView settingTitle = ViewBindings.findChildViewById(rootView, id);
      if (settingTitle == null) {
        break missingId;
      }

      id = R.id.setting_value;
      TextView settingValue = ViewBindings.findChildViewById(rootView, id);
      if (settingValue == null) {
        break missingId;
      }

      return new ItemSettingBinding((CardView) rootView, settingIcon, settingSwitch, settingTitle,
          settingValue);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
