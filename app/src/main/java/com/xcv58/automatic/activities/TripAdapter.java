package com.xcv58.automatic.activities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xcv58.automatic.R;
import com.xcv58.automatic.trip.Trip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xcv58 on 12/23/15.
 * TripAdapter to hold data and update it.
 * TODO: add sort functions
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private ArrayList<Trip> mTrips;

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
        this.notifyDataSetChanged();
    }

    public void addTrips(Trip[] trips) {
        mTrips.addAll(Arrays.asList(trips));
        this.notifyDataSetChanged();
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
        updateText(holder, R.id.cost, String.valueOf(trip.fuel_cost_usd));
        updateText(holder, R.id.duration, String.valueOf(trip.duration_s));
    }

    private void updateText(ViewHolder holder, int id, String text) {
        TextView textView = (TextView) holder.mView.findViewById(id);
        textView.setText(text);
    }

    @Override
    public int getItemCount() {
        return mTrips.size();
    }

    public List<Trip> getData() {
        return mTrips;
    }
}
