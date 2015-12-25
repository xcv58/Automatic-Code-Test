package com.xcv58.automatic.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.xcv58.automatic.R;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        String path = "uioeFxycjVxDMvAGBjATlJXrLaIZHvCFhCHpCaI^i@@";
        List<LatLng> list = PolyUtil.decode(path);
        PolylineOptions options = new PolylineOptions();
        double lat = 0.0;
        double lon = 0.0;
        int count = 0;
        for (LatLng latLng : list) {
            Log.d(MainActivityFragment.TAG, "latlng: " + latLng.toString());
            lat += latLng.latitude;
            lon += latLng.longitude;
            count++;
            options.add(latLng);
        }
//        mMap.addPolyline(options);
        Polyline line = mMap.addPolyline(options
                .width(5)
                .color(Color.RED));
        LatLng sydney = new LatLng(lat / count, lon / count);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15.0f));

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
