package com.xcv58.automatic.trip;

import java.util.Comparator;

/**
 * Created by xcv58 on 12/24/15.
 */
public class TripComparatorByAddress implements Comparator<Trip> {
    @Override
    public int compare(Trip lhs, Trip rhs) {
        String left = lhs.end_address.display_name;
        String right = rhs.end_address.display_name;
        left = nullToEmpty(left);
        right = nullToEmpty(right);
        return left.compareTo(right);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
