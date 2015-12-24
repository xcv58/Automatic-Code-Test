package com.xcv58.automatic.trip;

import java.util.Comparator;

/**
 * Created by xcv58 on 12/24/15.
 */
public class TripComparatorByCost implements Comparator<Trip> {
    @Override
    public int compare(Trip lhs, Trip rhs) {
        double left = lhs.fuel_cost_usd;
        double right = rhs.fuel_cost_usd;
        return Double.compare(right, left);
    }
}
