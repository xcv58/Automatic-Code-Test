package com.xcv58.automatic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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

import retrofit.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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


    private MaterialDialog.SingleButtonCallback error401Callback =
            new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                    final Intent settingsIntent = new Intent(getContext(), SettingsActivity.class);
                    if (which == DialogAction.POSITIVE || which == DialogAction.NEUTRAL) {
                        getContext().startActivity(settingsIntent);
                    } else {
                        alert(getString(R.string.auth_fail_title));
                    }
                }
            };

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
        if (mAdapter.getItemCount() == 0 && !mSwipeRefreshLayout.isRefreshing()) {
            load();
        }
    }

    private void alert(String alert) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, alert, Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), alert, Toast.LENGTH_LONG).show();
        }
    }

    private void load() {
        if (!hasActiveNetwork()) {
            alert("No Internet Connection!");
            loadFinish();
            return;
        }

        mSwipeRefreshLayout.setRefreshing(true);

        Map<String, String> queryMap = getQueryMap(nextUrl);

        String token = getToken();

        Observable<TripResponse> tripResponseObservable =
                restService.getTrip(token, queryMap)
//                        .onErrorReturn(new Func1<Throwable, TripResponse>() {
//                            @Override
//                            public TripResponse call(Throwable throwable) {
//                                return null;
//                            }
//                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());

        tripResponseObservable.subscribe(new Subscriber<TripResponse>() {
            @Override
            public void onCompleted() {
                loadFinish();
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError");
                Log.d(TAG, e.getClass().getName());
                if (e instanceof retrofit.HttpException) {
                    HttpException exception = (retrofit.HttpException) e;
                    int code = exception.code();
                    if (code == 401) {
                        onError401();
                    }
                }
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

    private void onError401() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.auth_fail_title)
                .content(R.string.auth_fail_content)
                .positiveText(R.string.auth_fail_positive)
                .negativeText(R.string.auth_fail_negative)
                .cancelable(false)
                .onPositive(error401Callback)
                .onNegative(error401Callback)
                .onNeutral(error401Callback)
                .show();
    }

    private boolean hasActiveNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
