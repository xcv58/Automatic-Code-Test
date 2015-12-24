package com.xcv58.automatic.trip;

/**
 * Created by xcv58 on 12/23/15.
 */
public class Trip {
    public String url;
    public String id;
    public String driver;
    public String user;
    public String started_at;
    public String ended_at;
    public double distance_m;
    public String duration_s;
    public String vehicle;
    public Location location;
    public Address start_address;
    public Location end_location;
    public Address end_address;
    public String path;
    public double fuel_cost_usd;
    public float fuel_volume_l;
    public float average_kmpl;
    public float average_from_epa_kmpl;
    public double score_events;
    public double score_speeding;
    public int hard_brakes;
    public int hard_accels;
    public int duration_over_70_s;
    public int duration_over_75_s;
    public int duration_over_80_s;
    public Event[] vehicle_events;
    public String start_timezone;
    public String end_timezone;
    public float city_fraction;
    public float highway_fraction;
    public float night_driving_fraction;
    public int idling_time_s;
    public String[] tags;
}
