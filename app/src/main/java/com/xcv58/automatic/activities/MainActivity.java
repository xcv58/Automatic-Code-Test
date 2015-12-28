package com.xcv58.automatic.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
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
import com.xcv58.automatic.trip.Trip;
import com.xcv58.automatic.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private List<String> pathList;
    public final static String PATH_KEY = "PATH_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sort();
//                map();
//                test();
            }
        });
    }

    private void sort() {
        final TripFragment fragment = (TripFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_main);
        new MaterialDialog.Builder(this)
                .title(R.string.first_time_title)
                .items(R.array.sort_keys)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        Utils.log("which: " + which + ", text: " + text);
                        fragment.sort(which);
                        return true;
                    }
                })
                .positiveText(R.string.choose)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (this.pathList != null) {
            drawPaths(this.pathList);
        }
        this.pathList = null;
    }

    private double latMin = 90.0;
    private double latMax = -90.0;
    private double lonMin = 180.0;
    private double lonMax = -180.0;

    protected void updateMap(List<Trip> tripList, int preSize) {
        ArrayList<String> pathList = new ArrayList<>();

        for (int i = preSize; i < tripList.size(); i++) {
            Trip trip = tripList.get(i);
            pathList.add(trip.path);
        }

        drawPaths(pathList);
    }

    private void drawPaths(List<String> pathList) {
        if (pathList == null) {
            return;
        }
        if (mMap == null) {
            if (this.pathList == null) {
                this.pathList = pathList;
            } else {
                this.pathList.addAll(pathList);
            }
            return;
        }
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
        Utils.log("Lat: " + latMax + ", " + latMin + "Lon: " + lonMax + ", " + lonMin);
        LatLngBounds bounds = new LatLngBounds(
                new LatLng(latMin, lonMin),
                new LatLng(latMax, lonMax));

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64));
    }
}
