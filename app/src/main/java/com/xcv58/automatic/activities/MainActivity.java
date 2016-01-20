package com.xcv58.automatic.activities;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
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
import com.optimizely.Optimizely;
import com.xcv58.automatic.R;
import com.xcv58.automatic.trip.Trip;
import com.xcv58.automatic.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private HashMap<String, TripLine> tripPolylineMap = new HashMap<>();
    private List<Trip> mTripList = null;
    private boolean isVertical = true;

    public final static String PATH_KEY = "PATH_KEY";
    private int drawPathNum = 0;

    private final static int MIN_HEIGHT = 500;
    private final static int MIN_WIDTH = 500;

    private final static int LINE_WIDTH_DEFAULT = 16;
    private final static int LINE_WIDTH_FOCUS = 32;
    private final static float ALPHA_DEFAULT = 0.5f;
    private final static float ALPHA_FOCUS = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        Optimizely.startOptimizelyWithAPIToken(getString(R.string.com_optimizely_api_key), getApplication());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_map);
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

        final LinearLayoutCompat linearLayout = (LinearLayoutCompat) findViewById(R.id.main_wrapper);
        isVertical = linearLayout.getOrientation() == LinearLayoutCompat.VERTICAL;

        minimizeListWidth();

        View dividerView = findViewById(R.id.divider);
        dividerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_map);
                View mapView = mapFragment.getView();
                final TripFragment tripFragment = (TripFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_list);
                View listView = tripFragment.getView();
                final View dividerView = findViewById(R.id.divider);
                if (mapView == null || listView == null) {
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        int top = linearLayout.getTop();
                        int bottom = linearLayout.getBottom();
                        int left = linearLayout.getLeft();
                        int right = linearLayout.getRight();
                        int height = linearLayout.getHeight();
                        int width = linearLayout.getWidth();
                        float x = event.getX();
                        float y = event.getY();
                        float rawX = event.getRawX();
                        float rawY = event.getRawY();
                        ViewGroup.LayoutParams mapParams = mapView.getLayoutParams();
                        ViewGroup.LayoutParams listParams = listView.getLayoutParams();
                        if (isVertical) {
                            float current = rawY + y;
                            if (hitBorder(current, top, bottom, MIN_HEIGHT)) {
                                return true;
                            }
                            mapParams.height += y;
                            mapView.setLayoutParams(mapParams);
                        } else {
                            float current = rawX + x;
                            if (hitBorder(current, left, right, MIN_WIDTH)) {
                                return true;
                            }
                            listParams.width += x;
                            mapParams.width -= x;
                            mapView.setLayoutParams(mapParams);
                            listView.setLayoutParams(listParams);
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        dividerView.setBackgroundColor(ContextCompat
                                .getColor(getBaseContext(),R.color.colorHover));
                        break;
                    case MotionEvent.ACTION_UP:
                        dividerView.setBackgroundColor(ContextCompat
                                .getColor(getBaseContext(), R.color.colorStatic));
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private boolean hitBorder(float current, int start, int end, int limit) {
        return current < start + limit || current > end - limit;
    }

    private void minimizeListWidth() {
        if (!isVertical) {
            final TripFragment listFragment = (TripFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_list);
            divideHalf(listFragment.getView(), 10);
        }
    }

    private void divideListMap() {
        final LinearLayoutCompat linearLayout = (LinearLayoutCompat) findViewById(R.id.main_wrapper);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_map);
        final TripFragment listFragment = (TripFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_list);
        final View dividerView = findViewById(R.id.divider);
        ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                linearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = linearLayout.getHeight();
                int width = linearLayout.getWidth();
                if (height == 0) {
                    return;
                }
                int dividerWidth = dividerView.getWidth();
                int dividerHeight = dividerView.getHeight();
                int size = isVertical ? (height - dividerHeight) : (width - dividerWidth);
                if (isVertical) {
                    divideHalf(mapFragment.getView(), size);
                } else {
                    divideHalf(listFragment.getView(), size);
                    divideHalf(mapFragment.getView(), size);
                }
                Utils.log("size: " + size);
                Utils.log("Linear: " + describeView(linearLayout));
                Utils.log("Map: " + describeView(mapFragment.getView()));
                Utils.log("Divider: " + describeView(dividerView));
                Utils.log("List: " + describeView(listFragment.getView()));
            }
        };

        linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }

    private String describeView(View view) {
        int top = view.getTop();
        int bottom = view.getBottom();
        int height = view.getHeight();
        int left = view.getLeft();
        int right = view.getRight();
        int width = view.getWidth();
        return top + " " + bottom + " " + height + "; "
                + left + " " + right + " " + width;
    }

    private void divideHalf(View view, int num) {
        if (view == null) {
            return;
        }
        if (num <= 0) {
            return;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (isVertical) {
            params.height = num / 2;
        } else {
            params.width = num / 2;
        }
        Utils.log("set for " + num / 2 + " " + view);
        view.setLayoutParams(params);
    }

    private void sort() {
        final TripFragment fragment = (TripFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_list);
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
        divideListMap();
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
        Utils.log("onMapReady");
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

    private float baseBearing = 0.0f;

    private void loops(final TripLine tripLine, final int index) {
        final List<LatLng> points = tripLine.polyline.getPoints();
        if (index + 1 >= points.size()) {
            LatLng start = points.get(index);
            float mapZoom = mMap.getCameraPosition().zoom >= 16 ? mMap.getCameraPosition().zoom : 16;
            CameraPosition cameraPosition =
                    new CameraPosition.Builder()
                            .target(start)
                            .bearing(baseBearing + BEARING_OFFSET)
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
        baseBearing = bearingBetweenLatLngs(start, end);
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(start)
                        .bearing(baseBearing + BEARING_OFFSET)
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
            if (this.mTripList == null || this.mTripList == tripList) {
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
        drawPathNum += 1;
        return new TripLine(startMarker, endMarker, line);
    }

    private void adjustMapCamera() {
        if (mMap == null) {
            return;
        }
        Utils.log("Lat: " + latMax + ", " + latMin + "Lon: " + lonMax + ", " + lonMin);
        if (drawPathNum <= 0) {
            return;
        }
        LatLngBounds bounds = new LatLngBounds(
                new LatLng(latMin, lonMin),
                new LatLng(latMax, lonMax));

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64));
    }
}
