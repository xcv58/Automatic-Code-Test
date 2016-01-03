package com.xcv58.automatic;

import com.xcv58.automatic.trip.Address;
import com.xcv58.automatic.trip.Trip;
import com.xcv58.automatic.trip.TripComparatorByAddress;
import com.xcv58.automatic.trip.TripComparatorByCost;
import com.xcv58.automatic.trip.TripComparatorByDistance;

import org.junit.Test;

import java.lang.Double;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class UnitTest {
    @Test
    public void sortTest() {
        List<Trip> list;
        Comparator<Trip> comparator;
        comparator = new TripComparatorByAddress();
        list = sortTest(comparator);
        for (int i = 0; i < list.size() - 1; i++) {
            Trip t1 = list.get(i);
            Trip t2 = list.get(i + 1);
            String destination1 = t1.end_address.display_name;
            String destination2 = t2.end_address.display_name;
            int compareRes = destination1.compareTo(destination2);
            assertTrue("Destination address alphabetical", compareRes <= 0);
        }

        comparator = new TripComparatorByDistance();
        list = sortTest(comparator);
        for (int i = 0; i < list.size() - 1; i++) {
            Trip t1 = list.get(i);
            Trip t2 = list.get(i + 1);
            int compareRes = Double.compare(t1.distance_m, t2.distance_m);
            assertTrue("Distance descending", compareRes >= 0);
        }

        comparator = new TripComparatorByCost();
        list = sortTest(comparator);
        for (int i = 0; i < list.size() - 1; i++) {
            Trip t1 = list.get(i);
            Trip t2 = list.get(i + 1);
            int compareRes = Double.compare(t1.fuel_cost_usd, t2.fuel_cost_usd);
            assertTrue("Cost descending", compareRes >= 0);
        }
    }

    public List<Trip> sortTest(Comparator<Trip> comparator) {
        List<Trip> list = this.getRandomTrip(100);
        Collections.sort(list, comparator);
        return list;
    }

    private List<Trip> getRandomTrip(int n) {
        List<Trip> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            Trip trip = new Trip();
            trip.distance_m = random.nextDouble();
            trip.fuel_cost_usd = random.nextDouble();
            trip.end_address = new Address();
            trip.end_address.display_name = getString(random, 10);
            list.add(trip);
        }
        return list;
    }

    private String getString(Random random, int len) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int num = random.nextInt();
            num %= 26;
            stringBuilder.append(num + 'a');
        }
        return stringBuilder.toString();
    }
}