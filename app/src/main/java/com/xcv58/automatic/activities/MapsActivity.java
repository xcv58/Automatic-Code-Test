package com.xcv58.automatic.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.xcv58.automatic.R;

import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public final static String PATH_KEY = "PATH_KEY";

    private GoogleMap mMap;
    private List<String> pathList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            pathList = extras.getStringArrayList(PATH_KEY);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        draw();
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void draw() {
        if (pathList == null) {
            return;
        }
        double latMin = 90.0;
        double latMax = -90.0;
        double lonMin = 180.0;
        double lonMax = -180.0;
        for (String path : pathList) {
            if (path == null) {
                continue;
            }
            List<LatLng> list = PolyUtil.decode(path);
            PolylineOptions options = new PolylineOptions();
            for (LatLng latLng : list) {
                latMax = Math.max(latMax, latLng.latitude);
                latMin = Math.min(latMin, latLng.latitude);
                lonMax = Math.max(lonMax, latLng.longitude);
                lonMin = Math.min(lonMin, latLng.longitude);
                options.add(latLng);
            }
            Random random = new Random();
            int color = Color.argb(255, random.nextInt(255),
                    random.nextInt(255), random.nextInt(255));
            Polyline line = mMap.addPolyline(options
                    .width(16)
                    .color(color));
        }
        LatLngBounds bounds = new LatLngBounds(
                new LatLng(latMin, lonMin),
                new LatLng(latMax, lonMax));

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64));
    }
}
