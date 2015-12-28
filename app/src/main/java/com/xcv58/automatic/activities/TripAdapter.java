package com.xcv58.automatic.activities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xcv58.automatic.R;
import com.xcv58.automatic.trip.Trip;
import com.xcv58.automatic.trip.TripComparatorByAddress;
import com.xcv58.automatic.trip.TripComparatorByCost;
import com.xcv58.automatic.trip.TripComparatorByDistance;
import com.xcv58.automatic.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by xcv58 on 12/23/15.
 * TripAdapter to hold data and update it.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    public final static int SORT_ADDRESS = 0;
    public final static int SORT_DISTANCE = 1;
    public final static int SORT_COST = 2;

    private ArrayList<Trip> mTrips;
    private TripComparatorByAddress mComparatorByAddress = new TripComparatorByAddress();
    private TripComparatorByCost mComparatorByCost = new TripComparatorByCost();
    private TripComparatorByDistance mComparatorByDistance = new TripComparatorByDistance();
    private Comparator<Trip> mComparator = null;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
        }
    }

    public TripAdapter(ArrayList<Trip> trips) {
        if (trips == null) {
            mTrips = new ArrayList<>();
        } else {
            mTrips = trips;
        }
        notifyDataSetChanged();
    }

    public void addTrips(Trip[] trips) {
        mTrips.addAll(Arrays.asList(trips));
        notifyDataSetChanged();
    }

    @Override
    public TripAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trip_view, parent, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Trip trip = mTrips.get(position);
        updateText(holder, R.id.address_start, trip.start_address.display_name);
        updateText(holder, R.id.address_end, trip.end_address.display_name);
        updateText(holder, R.id.cost, String.format("$%.2f, ", trip.fuel_cost_usd));
        updateText(holder, R.id.duration, getTime(trip.duration_s));
    }

    private void updateText(ViewHolder holder, int id, String text) {
        TextView textView = (TextView) holder.mView.findViewById(id);
        textView.setText(text);
    }

    private String getTime(String seconds) {
        int totalSecs = 0;
        try {
            totalSecs = (int) Float.parseFloat(seconds);
        } catch (NumberFormatException e) {
            return seconds + "seconds";
        }
        StringBuilder stringBuilder = new StringBuilder();

        int hours = totalSecs / 3600;
        if (hours > 0) {
            stringBuilder.append(hours);
            stringBuilder.append("h ");
            totalSecs = totalSecs % 3600;
        }
        int minutes = totalSecs / 60;
        if (minutes > 0) {
            stringBuilder.append(minutes);
            stringBuilder.append("m ");
            totalSecs = totalSecs % 60;
        }
        if (totalSecs > 0) {
            stringBuilder.append(totalSecs);
            stringBuilder.append("s");
        }
        return stringBuilder.toString();
    }

    @Override
    public int getItemCount() {
        return mTrips.size();
    }

    public List<Trip> getData() {
        return mTrips;
    }

    public void sort(int key) {
        switch (key) {
            case SORT_ADDRESS:
                mComparator = mComparatorByAddress;
                break;
            case SORT_COST:
                mComparator = mComparatorByCost;
                break;
            case SORT_DISTANCE:
                mComparator = mComparatorByDistance;
                break;
            default:
                Utils.log("sort key is: " + key + ". It's impossible!");
                break;
        }
        if (mComparator != null) {
            Collections.sort(mTrips, mComparator);
            this.notifyDataSetChanged();
        }
    }
}
