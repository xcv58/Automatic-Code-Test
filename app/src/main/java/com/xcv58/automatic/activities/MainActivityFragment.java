package com.xcv58.automatic.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.xcv58.automatic.R;
import com.xcv58.automatic.rest.AutomaticRESTService;
import com.xcv58.automatic.rest.ServiceFactory;
import com.xcv58.automatic.trip.Trip;
import com.xcv58.automatic.trip.TripResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by xcv58 on 12/23/15.
 */
public class MainActivityFragment extends Fragment {

    public final static String TAG = "automatic_code_test";
    private final static String BASE_URL = "https://api.automatic.com/";

    private final static String TRIPS_KEY = "TRIPS_KEY";
    private final static String NEXT_URL_KEY = "NEXT_URL_KEY";

    private RecyclerView mRecyclerView;
    private TripAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipyRefreshLayout mSwipeRefreshLayout;

    private AutomaticRESTService restService =
            ServiceFactory.createRetrofitService(AutomaticRESTService.class, BASE_URL);
    private String nextUrl = null;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayList<Trip> tripList = null;
        if (savedInstanceState != null) {
            tripList = getTrips(savedInstanceState.getStringArray(TRIPS_KEY));
            nextUrl = savedInstanceState.getString(NEXT_URL_KEY);
        }

        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(false);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new TripAdapter(tripList);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipyRefreshLayout) getActivity().findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                load();
            }
        });

        if (tripList == null) {
            load();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);

        List<Trip> trips = mAdapter.getData();
        String[] values = new String[trips.size()];
        Gson gson = new Gson();
        for (int i = 0; i < trips.size(); i++) {
            values[i] = gson.toJson(trips.get(i));
        }
        savedState.putStringArray(TRIPS_KEY, values);

        savedState.putString(NEXT_URL_KEY, nextUrl);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter.getItemCount() == 0) {
            load();
        }
    }

    private void load() {
        if (!hasActiveNetwork()) {
            View view = getView();
            String alert = "No Internet Connection!";
            if (view != null) {
                Snackbar.make(view, alert, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                Toast.makeText(getActivity(), alert, LENGTH_LONG).show();
            }
            loadFinish();
            return;
        }

        mSwipeRefreshLayout.setRefreshing(true);

        Map<String, String> queryMap = getQueryMap(nextUrl);

        String token = getToken();

        Observable<TripResponse> tripResponseObservable =
                restService.getTrip(token, queryMap)
                        .onErrorReturn(new Func1<Throwable, TripResponse>() {
                            @Override
                            public TripResponse call(Throwable throwable) {
                                throwable.printStackTrace();
                                // TODO: handle network error including 401 and no internet
                                Log.d(TAG, throwable.getMessage());
                                Log.d(TAG, throwable.getClass().getName());
                                return null;
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());

        tripResponseObservable.subscribe(new Subscriber<TripResponse>() {
            @Override
            public void onCompleted() {
                loadFinish();
            }

            @Override
            public void onError(Throwable e) {
                loadFinish();
            }

            @Override
            public void onNext(TripResponse tripResponse) {
                if (tripResponse == null) {
                    Log.d(TAG, "response is null!");
                } else {
                    mAdapter.addTrips(tripResponse.results);
                    nextUrl = tripResponse._metadata.next;
                }
            }
        });
    }

    private void loadFinish() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private Map<String, String> getQueryMap(String url) {
        Map<String, String> map = new ArrayMap<>();
        if (url == null) {
//            map.put("limit", "1");
            return map;
        }

        Uri uri = Uri.parse(url);
        Set<String> names = uri.getQueryParameterNames();
        for (String name : names) {
            String value = uri.getQueryParameter(name);
            map.put(name, value);
        }
        return map;
    }

    private ArrayList<Trip> getTrips(String[] values) {
        ArrayList<Trip> trips = new ArrayList<>();
        if (values == null || values.length == 0) {
            return trips;
        }
        Gson gson = new Gson();
        for (String json : values) {
            trips.add(gson.fromJson(json, Trip.class));
        }
        return trips;
    }

    private String getToken() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        String type = sharedPreferences.getString(SettingsFragment.TYPE,
                SettingsFragment.TYPE_DEFAULT);
        String token = sharedPreferences.getString(SettingsFragment.TOKEN, "");
        if ("".equals(token)) {
            // TODO: ask user to input token
            Log.d(TAG, "No token!");
        }
        return type + " " + token;
    }

    private boolean hasActiveNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
