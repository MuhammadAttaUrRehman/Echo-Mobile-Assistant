package com.example.echo.ui.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echo.R;
import com.example.echo.adapters.YouTubeVideoAdapter;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YouTubeSearchActivity extends AppCompatActivity {

    private static final String TAG = "YouTubeSearchActivity";
    private RecyclerView recyclerView;
    private YouTubeVideoAdapter adapter;
    private String apiKey = "AIzaSyCKL4SpNgKfS-iXvXZrFu1Ln4IyDgIjkmM"; // Replace with your YouTube Data API v3 key
    private YouTube youtubeService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_search);

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Results");
        }

        recyclerView = findViewById(R.id.youtube_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Get search query from intent
        String query = getIntent().getStringExtra("query");
        if (query != null) {
            initializeYouTubeService();
            new SearchVideosTask().execute(query);
        } else {
            Toast.makeText(this, "No search query provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeYouTubeService() {
        try {
            youtubeService = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    null)
                    .setApplicationName("Echo-App")
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to initialize YouTube service: " + e.getMessage());
        }
    }

    private class SearchVideosTask extends AsyncTask<String, Void, List<YouTubeVideoAdapter.SearchResult>> {
        @Override
        protected List<YouTubeVideoAdapter.SearchResult> doInBackground(String... queries) {
            List<YouTubeVideoAdapter.SearchResult> results = new ArrayList<>();
            if (youtubeService == null) return results;

            try {
                // Use Arrays.asList to create a List<String> for the part and type parameters
                YouTube.Search.List search = youtubeService.search()
                        .list(Arrays.asList("id", "snippet"))
                        .setQ(queries[0])
                        .setType(Arrays.asList("video")) // Changed from "video" to List<String>
                        .setMaxResults(10L)
                        .setKey(apiKey);

                SearchListResponse response = search.execute();
                for (SearchResult result : response.getItems()) {
                    String videoId = result.getId().getVideoId();
                    String title = result.getSnippet().getTitle();
                    String channelTitle = result.getSnippet().getChannelTitle();
                    String thumbnailUrl = result.getSnippet().getThumbnails().getDefault().getUrl();
                    results.add(new YouTubeVideoAdapter.SearchResult(videoId, title, channelTitle, thumbnailUrl));
                }
            } catch (IOException e) {
                Log.e(TAG, "Search failed: " + e.getMessage());
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<YouTubeVideoAdapter.SearchResult> results) {
            if (results.isEmpty()) {
                Toast.makeText(YouTubeSearchActivity.this, "No videos found", Toast.LENGTH_SHORT).show();
            } else {
                adapter = new YouTubeVideoAdapter(results);
                recyclerView.setAdapter(adapter);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}