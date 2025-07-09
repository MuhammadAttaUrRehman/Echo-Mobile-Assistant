package com.example.echo.ui.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.echo.R;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        ImageView ivFullScreenImage = findViewById(R.id.ivFullScreenImage);
        Button btnClose = findViewById(R.id.btnClose);

        String screenshotUrl = getIntent().getStringExtra("screenshotUrl");
        if (screenshotUrl != null) {
            Glide.with(this).load(screenshotUrl).into(ivFullScreenImage);
        } else {
            finish(); // Close if URL is invalid
        }

        btnClose.setOnClickListener(v -> finish());
    }
}