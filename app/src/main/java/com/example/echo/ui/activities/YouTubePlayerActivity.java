package com.example.echo.ui.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.echo.R;

public class YouTubePlayerActivity extends AppCompatActivity {

    private static final String TAG = "YouTubePlayerActivity";
    private ExoPlayer player;
    private PlayerView playerView;
    private String videoUrl;
    private String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);

        playerView = findViewById(R.id.player_view);

        // Get video ID from intent
        videoId = getIntent().getStringExtra("VIDEO_ID");
        if (videoId == null) {
            Toast.makeText(this, "No video ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch stream URL asynchronously
        new FetchStreamUrlTask().execute(videoId);
    }

    private void initializePlayer(String url) {
        if (url == null) {
            Toast.makeText(this, "Failed to load video stream", Toast.LENGTH_SHORT).show();
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"; // Fallback
        } else {
            videoUrl = url;
        }

        // Create ExoPlayer instance
        player = new ExoPlayer.Builder(this).build();

        // Bind player to view
        playerView.setPlayer(player);

        // Create media item
        MediaItem mediaItem = MediaItem.fromUri(videoUrl);

        // Set media item and prepare
        player.setMediaItem(mediaItem);
        player.prepare();

        // Start playback
        player.setPlayWhenReady(true);
    }

    private class FetchStreamUrlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String videoId = params[0];
            try {
                // Placeholder: Replace with actual stream URL fetching logic
                // This could involve:
                // 1. YouTube Data API with OAuth to get a DASH/HLS manifest
                // 2. A third-party service or library (e.g., youtube-dl) with legal compliance
                // 3. Backend API call to fetch stream URL
                // For now, return a placeholder or null to trigger fallback
                Log.d(TAG, "Fetching stream URL for videoId: " + videoId);
                // Simulate delay for demo
                Thread.sleep(1000);
                // TODO: Implement real stream URL fetching here
                return null; // Replace with actual URL (e.g., "https://example.com/stream.mp4")
            } catch (InterruptedException e) {
                Log.e(TAG, "Stream URL fetch interrupted: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String url) {
            initializePlayer(url);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}