package com.example.echo.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.echo.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private LatLng selectedLocation;
    private Marker selectedMarker;
    private EditText locationNameEditText;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        locationNameEditText = findViewById(R.id.location_name_edit_text);
        confirmButton = findViewById(R.id.confirm_button);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        confirmButton.setOnClickListener(v -> {
            if (selectedLocation != null) {
                Intent result = new Intent();
                result.putExtra("latitude", selectedLocation.latitude);
                result.putExtra("longitude", selectedLocation.longitude);
                String locationName = locationNameEditText.getText().toString().trim();
                if (locationName.isEmpty()) {
                    locationName = "Selected Location";
                }
                result.putExtra("location_name", locationName);
                result.putExtra("radius", 100);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        LatLng defaultLocation = new LatLng(24.8607, 67.0011);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));

        googleMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            if (selectedMarker != null) {
                selectedMarker.remove();
            }
            selectedMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            confirmButton.setEnabled(true);
        });
    }
}