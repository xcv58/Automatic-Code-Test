package com.xcv58.automatic.rest;

import com.xcv58.automatic.trip.TripResponse;

import java.util.Map;

import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.QueryMap;
import rx.Observable;

/**
 * Created by xcv58 on 12/23/15.
 * REST API for retrofit
 */
public interface AutomaticRESTService {
    @GET("trip/")
    Observable<TripResponse> getTrip(@Header("Authorization") String token, @QueryMap Map<String, String> options);
}
