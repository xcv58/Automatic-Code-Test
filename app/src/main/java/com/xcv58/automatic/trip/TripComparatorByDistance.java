package com.xcv58.automatic.trip;

import java.util.Comparator;

/**
 * Created by xcv58 on 12/24/15.
 */
public class TripComparatorByDistance implements Comparator<Trip> {
    @Override
    public int compare(Trip lhs, Trip rhs) {
        double left = lhs.distance_m;
        double right = rhs.distance_m;
        return Double.compare(right, left);
    }
}
