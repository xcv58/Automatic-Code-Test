package com.xcv58.automatic.activities;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

public class TripLine {
    public Polyline polyline;
    public Marker startMaker;
    public Marker endMarker;

    public TripLine(Marker s, Marker e, Polyline line) {
        startMaker = s;
        endMarker = e;
        polyline = line;
    }
}
