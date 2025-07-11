// Generated by view binder compiler. Do not edit!
package com.example.echo.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.echo.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemTaskBinding implements ViewBinding {
  @NonNull
  private final CardView rootView;

  @NonNull
  public final TextView taskDetails;

  @NonNull
  public final TextView taskTime;

  @NonNull
  public final TextView taskTitle;

  private ItemTaskBinding(@NonNull CardView rootView, @NonNull TextView taskDetails,
      @NonNull TextView taskTime, @NonNull TextView taskTitle) {
    this.rootView = rootView;
    this.taskDetails = taskDetails;
    this.taskTime = taskTime;
    this.taskTitle = taskTitle;
  }

  @Override
  @NonNull
  public CardView getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemTaskBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemTaskBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_task, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemTaskBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.task_details;
      TextView taskDetails = ViewBindings.findChildViewById(rootView, id);
      if (taskDetails == null) {
        break missingId;
      }

      id = R.id.task_time;
      TextView taskTime = ViewBindings.findChildViewById(rootView, id);
      if (taskTime == null) {
        break missingId;
      }

      id = R.id.task_title;
      TextView taskTitle = ViewBindings.findChildViewById(rootView, id);
      if (taskTitle == null) {
        break missingId;
      }

      return new ItemTaskBinding((CardView) rootView, taskDetails, taskTime, taskTitle);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
