// Generated by view binder compiler. Do not edit!
package com.example.echo.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.echo.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityRegisterBinding implements ViewBinding {
  @NonNull
  private final ScrollView rootView;

  @NonNull
  public final TextView appTitle;

  @NonNull
  public final View bottomLeftDecoration;

  @NonNull
  public final View bottomRightDecoration;

  @NonNull
  public final TextInputEditText confirmPasswordEditText;

  @NonNull
  public final TextInputLayout confirmPasswordInputLayout;

  @NonNull
  public final TextInputEditText displayNameEditText;

  @NonNull
  public final TextInputLayout displayNameInputLayout;

  @NonNull
  public final TextInputEditText emailEditText;

  @NonNull
  public final TextInputLayout emailInputLayout;

  @NonNull
  public final LinearLayout loginSection;

  @NonNull
  public final TextView loginTextView;

  @NonNull
  public final TextInputEditText passwordEditText;

  @NonNull
  public final TextInputLayout passwordInputLayout;

  @NonNull
  public final TextView passwordRequirements;

  @NonNull
  public final ProgressBar progressBar;

  @NonNull
  public final MaterialButton registerButton;

  @NonNull
  public final CardView registerCard;

  @NonNull
  public final ImageView registerLogo;

  @NonNull
  public final TextView registerSubtitle;

  @NonNull
  public final View topLeftDecoration;

  @NonNull
  public final View topRightDecoration;

  private ActivityRegisterBinding(@NonNull ScrollView rootView, @NonNull TextView appTitle,
      @NonNull View bottomLeftDecoration, @NonNull View bottomRightDecoration,
      @NonNull TextInputEditText confirmPasswordEditText,
      @NonNull TextInputLayout confirmPasswordInputLayout,
      @NonNull TextInputEditText displayNameEditText,
      @NonNull TextInputLayout displayNameInputLayout, @NonNull TextInputEditText emailEditText,
      @NonNull TextInputLayout emailInputLayout, @NonNull LinearLayout loginSection,
      @NonNull TextView loginTextView, @NonNull TextInputEditText passwordEditText,
      @NonNull TextInputLayout passwordInputLayout, @NonNull TextView passwordRequirements,
      @NonNull ProgressBar progressBar, @NonNull MaterialButton registerButton,
      @NonNull CardView registerCard, @NonNull ImageView registerLogo,
      @NonNull TextView registerSubtitle, @NonNull View topLeftDecoration,
      @NonNull View topRightDecoration) {
    this.rootView = rootView;
    this.appTitle = appTitle;
    this.bottomLeftDecoration = bottomLeftDecoration;
    this.bottomRightDecoration = bottomRightDecoration;
    this.confirmPasswordEditText = confirmPasswordEditText;
    this.confirmPasswordInputLayout = confirmPasswordInputLayout;
    this.displayNameEditText = displayNameEditText;
    this.displayNameInputLayout = displayNameInputLayout;
    this.emailEditText = emailEditText;
    this.emailInputLayout = emailInputLayout;
    this.loginSection = loginSection;
    this.loginTextView = loginTextView;
    this.passwordEditText = passwordEditText;
    this.passwordInputLayout = passwordInputLayout;
    this.passwordRequirements = passwordRequirements;
    this.progressBar = progressBar;
    this.registerButton = registerButton;
    this.registerCard = registerCard;
    this.registerLogo = registerLogo;
    this.registerSubtitle = registerSubtitle;
    this.topLeftDecoration = topLeftDecoration;
    this.topRightDecoration = topRightDecoration;
  }

  @Override
  @NonNull
  public ScrollView getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityRegisterBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityRegisterBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_register, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityRegisterBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.app_title;
      TextView appTitle = ViewBindings.findChildViewById(rootView, id);
      if (appTitle == null) {
        break missingId;
      }

      id = R.id.bottom_left_decoration;
      View bottomLeftDecoration = ViewBindings.findChildViewById(rootView, id);
      if (bottomLeftDecoration == null) {
        break missingId;
      }

      id = R.id.bottom_right_decoration;
      View bottomRightDecoration = ViewBindings.findChildViewById(rootView, id);
      if (bottomRightDecoration == null) {
        break missingId;
      }

      id = R.id.confirm_password_edit_text;
      TextInputEditText confirmPasswordEditText = ViewBindings.findChildViewById(rootView, id);
      if (confirmPasswordEditText == null) {
        break missingId;
      }

      id = R.id.confirm_password_input_layout;
      TextInputLayout confirmPasswordInputLayout = ViewBindings.findChildViewById(rootView, id);
      if (confirmPasswordInputLayout == null) {
        break missingId;
      }

      id = R.id.display_name_edit_text;
      TextInputEditText displayNameEditText = ViewBindings.findChildViewById(rootView, id);
      if (displayNameEditText == null) {
        break missingId;
      }

      id = R.id.display_name_input_layout;
      TextInputLayout displayNameInputLayout = ViewBindings.findChildViewById(rootView, id);
      if (displayNameInputLayout == null) {
        break missingId;
      }

      id = R.id.email_edit_text;
      TextInputEditText emailEditText = ViewBindings.findChildViewById(rootView, id);
      if (emailEditText == null) {
        break missingId;
      }

      id = R.id.email_input_layout;
      TextInputLayout emailInputLayout = ViewBindings.findChildViewById(rootView, id);
      if (emailInputLayout == null) {
        break missingId;
      }

      id = R.id.login_section;
      LinearLayout loginSection = ViewBindings.findChildViewById(rootView, id);
      if (loginSection == null) {
        break missingId;
      }

      id = R.id.login_text_view;
      TextView loginTextView = ViewBindings.findChildViewById(rootView, id);
      if (loginTextView == null) {
        break missingId;
      }

      id = R.id.password_edit_text;
      TextInputEditText passwordEditText = ViewBindings.findChildViewById(rootView, id);
      if (passwordEditText == null) {
        break missingId;
      }

      id = R.id.password_input_layout;
      TextInputLayout passwordInputLayout = ViewBindings.findChildViewById(rootView, id);
      if (passwordInputLayout == null) {
        break missingId;
      }

      id = R.id.password_requirements;
      TextView passwordRequirements = ViewBindings.findChildViewById(rootView, id);
      if (passwordRequirements == null) {
        break missingId;
      }

      id = R.id.progress_bar;
      ProgressBar progressBar = ViewBindings.findChildViewById(rootView, id);
      if (progressBar == null) {
        break missingId;
      }

      id = R.id.register_button;
      MaterialButton registerButton = ViewBindings.findChildViewById(rootView, id);
      if (registerButton == null) {
        break missingId;
      }

      id = R.id.register_card;
      CardView registerCard = ViewBindings.findChildViewById(rootView, id);
      if (registerCard == null) {
        break missingId;
      }

      id = R.id.register_logo;
      ImageView registerLogo = ViewBindings.findChildViewById(rootView, id);
      if (registerLogo == null) {
        break missingId;
      }

      id = R.id.register_subtitle;
      TextView registerSubtitle = ViewBindings.findChildViewById(rootView, id);
      if (registerSubtitle == null) {
        break missingId;
      }

      id = R.id.top_left_decoration;
      View topLeftDecoration = ViewBindings.findChildViewById(rootView, id);
      if (topLeftDecoration == null) {
        break missingId;
      }

      id = R.id.top_right_decoration;
      View topRightDecoration = ViewBindings.findChildViewById(rootView, id);
      if (topRightDecoration == null) {
        break missingId;
      }

      return new ActivityRegisterBinding((ScrollView) rootView, appTitle, bottomLeftDecoration,
          bottomRightDecoration, confirmPasswordEditText, confirmPasswordInputLayout,
          displayNameEditText, displayNameInputLayout, emailEditText, emailInputLayout,
          loginSection, loginTextView, passwordEditText, passwordInputLayout, passwordRequirements,
          progressBar, registerButton, registerCard, registerLogo, registerSubtitle,
          topLeftDecoration, topRightDecoration);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
