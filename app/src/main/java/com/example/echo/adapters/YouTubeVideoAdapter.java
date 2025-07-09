package com.example.echo.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echo.R;
import com.example.echo.ui.activities.YouTubePlayerActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class YouTubeVideoAdapter extends RecyclerView.Adapter<YouTubeVideoAdapter.VideoViewHolder> {

    private List<SearchResult> videoList;

    public YouTubeVideoAdapter(List<SearchResult> videoList) {
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_youtube_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        SearchResult video = videoList.get(position);
        holder.titleTextView.setText(video.getTitle());
        holder.channelTextView.setText(video.getChannelTitle());
        Picasso.get().load(video.getThumbnailUrl()).into(holder.thumbnailImageView);

        // Set click listener to play video
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), YouTubePlayerActivity.class);
            intent.putExtra("VIDEO_ID", video.getVideoId()); // Pass video ID
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return videoList != null ? videoList.size() : 0;
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView titleTextView;
        TextView channelTextView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.video_thumbnail);
            titleTextView = itemView.findViewById(R.id.video_title);
            channelTextView = itemView.findViewById(R.id.channel_title);
        }
    }

    public static class SearchResult {
        private String videoId;
        private String title;
        private String channelTitle;
        private String thumbnailUrl;

        public SearchResult(String videoId, String title, String channelTitle, String thumbnailUrl) {
            this.videoId = videoId;
            this.title = title;
            this.channelTitle = channelTitle;
            this.thumbnailUrl = thumbnailUrl;
        }

        public String getVideoId() { return videoId; }
        public String getTitle() { return title; }
        public String getChannelTitle() { return channelTitle; }
        public String getThumbnailUrl() { return thumbnailUrl; }
    }
}