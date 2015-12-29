package com.xcv58.automatic.activities;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.xcv58.automatic.R;
import com.xcv58.automatic.trip.Trip;
import com.xcv58.automatic.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private HashMap<String, TripLine> tripPolylineMap = new HashMap<>();
    private List<Trip> mTripList = null;
    public final static String PATH_KEY = "PATH_KEY";

    private final static int LINE_WIDTH_DEFAULT = 16;
    private final static int LINE_WIDTH_FOCUS = 32;
    private final static float ALPHA_DEFAULT = 0.5f;
    private final static float ALPHA_FOCUS = 1.0f;

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
        if (this.mTripList != null && this.mTripList.size() > 0) {
            updateMap(mTripList, 0);
        }
        this.mTripList = null;
    }

    private double latMin = 90.0;
    private double latMax = -90.0;
    private double lonMin = 180.0;
    private double lonMax = -180.0;

    protected void clickTrip(Trip trip) {
        TripLine tripLine = tripPolylineMap.get(trip.id);
        if (tripLine == null) {
            return;
        }
        tripLine.startMaker.setVisible(true);
        tripLine.endMarker.setVisible(true);
        tripLine.endMarker.setAlpha(ALPHA_FOCUS);
        tripLine.startMaker.setAlpha(ALPHA_FOCUS);
        tripLine.polyline.setWidth(LINE_WIDTH_FOCUS);

        loops(tripLine, 0);
    }

    private Location convertLatLngToLocation(LatLng latLng) {
        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    private float bearingBetweenLatLngs(LatLng beginLatLng, LatLng endLatLng) {
        Location beginLocation = convertLatLngToLocation(beginLatLng);
        Location endLocation = convertLatLngToLocation(endLatLng);
        return beginLocation.bearingTo(endLocation);
    }

    private float preBearing = 0.0f;

    private void loops(final TripLine tripLine, final int index) {
        final List<LatLng> points = tripLine.polyline.getPoints();
        if (index + 1 >= points.size()) {
            LatLng start = points.get(index);
            float mapZoom = mMap.getCameraPosition().zoom >= 16 ? mMap.getCameraPosition().zoom : 16;
            CameraPosition cameraPosition =
                    new CameraPosition.Builder()
                            .target(start)
                            .bearing(preBearing + BEARING_OFFSET)
                            .tilt(90)
                            .zoom(mapZoom)
                            .build();
            mMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    ANIMATE_SPEEED_TURN,
                    new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            Utils.log("finish: " + index + "/" + points.size());
                            tripLine.startMaker.setVisible(false);
                            tripLine.endMarker.setVisible(false);
                            tripLine.endMarker.setAlpha(ALPHA_DEFAULT);
                            tripLine.startMaker.setAlpha(ALPHA_DEFAULT);
                            tripLine.polyline.setWidth(LINE_WIDTH_DEFAULT);
                            tripLine.endMarker.setAlpha(ALPHA_DEFAULT);
                            tripLine.startMaker.setAlpha(ALPHA_DEFAULT);
                            tripLine.polyline.setWidth(LINE_WIDTH_DEFAULT);
                        }

                        @Override
                        public void onCancel() {
                            Utils.log("cancel");
                            tripLine.startMaker.setVisible(false);
                            tripLine.endMarker.setVisible(false);
                            tripLine.endMarker.setAlpha(ALPHA_DEFAULT);
                            tripLine.startMaker.setAlpha(ALPHA_DEFAULT);
                            tripLine.polyline.setWidth(LINE_WIDTH_DEFAULT);
                        }
                    }
            );
            return;
        }
        LatLng start = points.get(index);
        LatLng end = points.get(index + 1);

        float mapZoom = mMap.getCameraPosition().zoom >= 16 ? mMap.getCameraPosition().zoom : 16;
        float preBearing = bearingBetweenLatLngs(start, end);
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(start)
                        .bearing(preBearing + BEARING_OFFSET)
                        .tilt(90)
                        .zoom(mapZoom)
                        .build();
        mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition),
                ANIMATE_SPEEED_TURN,
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        Utils.log("finish: " + index + "/" + points.size());
                        loops(tripLine, index + 1);
                    }

                    @Override
                    public void onCancel() {
                        Utils.log("cancel");
                    }
                }
        );
    }

    private static final int ANIMATE_SPEEED = 256;
    private static final int ANIMATE_SPEEED_TURN = 256;
    private static final int BEARING_OFFSET = 0;

    protected void updateMap(List<Trip> tripList, int preSize) {
        if (mMap == null) {
            if (this.mTripList == null) {
                this.mTripList = tripList;
            } else {
                this.mTripList.addAll(tripList);
            }
            return;
        }
        for (int i = preSize; i < tripList.size(); i++) {
            Trip trip = tripList.get(i);
            TripLine tripLine = drawPath(trip);
            if (tripLine != null) {
                tripPolylineMap.put(trip.id, tripLine);
            }
        }
        adjustMapCamera();
    }

    private TripLine drawPath(Trip trip) {
        String path = trip.path;
        if (path == null) {
            return null;
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
        List<LatLng> points = options.getPoints();
        if (points.size() < 2) {
            return null;
        }
        LatLng startPoint = points.get(0);
        LatLng endPoint = points.get(points.size() - 1);
        Marker startMarker = mMap.addMarker(new MarkerOptions()
                .visible(false)
                .position(startPoint)
                .draggable(false)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .alpha(ALPHA_DEFAULT)
                .title("Start Address")
                .snippet(trip.start_address.display_name));
        Marker endMarker = mMap.addMarker(new MarkerOptions()
                .visible(false)
                .position(endPoint)
                .draggable(false)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .alpha(ALPHA_DEFAULT)
                .title("End Address")
                .snippet(trip.end_address.display_name + ", $" + trip.fuel_cost_usd));
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(255),
                random.nextInt(255), random.nextInt(255));
        Polyline line = mMap.addPolyline(options
                .width(LINE_WIDTH_DEFAULT)
                .color(color));
        return new TripLine(startMarker, endMarker, line);
    }

    private void adjustMapCamera() {
        if (mMap == null) {
            return;
        }
        Utils.log("Lat: " + latMax + ", " + latMin + "Lon: " + lonMax + ", " + lonMin);
        LatLngBounds bounds = new LatLngBounds(
                new LatLng(latMin, lonMin),
                new LatLng(latMax, lonMax));

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64));
    }
}
