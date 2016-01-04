package com.xcv58.automatic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.ArrayMap;
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
import com.xcv58.automatic.rest.User;
import com.xcv58.automatic.trip.Trip;
import com.xcv58.automatic.trip.TripResponse;
import com.xcv58.automatic.utils.Utils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by xcv58 on 12/23/15.
 */
public class TripFragment extends Fragment {
    private final static String BASE_URL = "https://api.automatic.com/";

    private final static String TRIPS_KEY = "TRIPS_KEY";
    private final static String NEXT_URL_KEY = "NEXT_URL_KEY";

    private RecyclerView mRecyclerView;
    private TripAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private SwipyRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Trip> mTripList;

    private boolean firstLoad = false;
    private MaterialDialog firstLoadDialog;

    private AutomaticRESTService restService =
            ServiceFactory.createRetrofitService(AutomaticRESTService.class, BASE_URL);
    private String nextUrl = null;

    private Handler mHandler;
    private final static long MIN_PROGRESS_APPEAR_TIME = 512L;
    private Runnable setNotRefreshing = new Runnable() {
        @Override
        public void run() {
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(false);
                notifyListener(mProgressListener, false);
            }
        }
    };
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private MaterialDialog.SingleButtonCallback settingsCallback =
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
                    dialog.dismiss();
                }
            };


    private Subscription mSubscription;

    private ProgressListener mProgressListener;

    public interface ProgressListener {
        void onProgressShown();
        void onProgressDismissed();
    }

    public TripFragment() {
    }

    public void setProgressListener(ProgressListener listener) {
        mProgressListener = listener;
    }

    public boolean isInProgress() {
        return mSwipeRefreshLayout.isRefreshing();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mTripList = getTrips(savedInstanceState.getStringArray(TRIPS_KEY));
            nextUrl = savedInstanceState.getString(NEXT_URL_KEY);
            updateMap(mTripList, 0);
        } else {
            mTripList = new ArrayList<>();
        }

        mSwipeRefreshLayout = (SwipyRefreshLayout) getActivity()
                .findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                load();
            }
        });

        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.addItemDecoration(new
                HorizontalDividerItemDecoration.Builder(getContext()).build());

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new TripAdapter(mTripList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mSwipeRefreshLayout.isRefreshing()) {
                    return;
                }
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                        load();
                    }
                }
            }
        });
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(),
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Utils.log("click: " + position);
                                MainActivity mainActivity = (MainActivity) getActivity();
                                mainActivity.clickTrip(mTripList.get(position));
                            }
                        })
        );

        mHandler = new Handler();
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);

        List<Trip> trips = mTripList;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Utils.log("onAttach Main Fragment");
    }

    public List<Trip> getTripList() {
        return mTripList;
    }

    private void alert(String alert) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, alert, Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), alert, Toast.LENGTH_LONG).show();
        }
    }

    protected int getVisibleItemCount() {
        return mLayoutManager.getChildCount();
    }

    private void load() {
        if (!hasActiveNetwork()) {
            alert("No Internet Connection!");
            return;
        }

        Map<String, String> queryMap = getQueryMap(nextUrl);

        String token = getToken();
        if (token == null) {
            return;
        }

        loadProgress();

        Observable<TripResponse> tripResponseObservable =
                restService.getTrip(token, queryMap)
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());

        mSubscription = tripResponseObservable.subscribe(new Subscriber<TripResponse>() {
            @Override
            public void onCompleted() {
                loadFinish();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
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
                    Utils.log("response is null!");
                } else {
                    int preSize = mTripList.size();
                    nextUrl = tripResponse._metadata.next;

                    mTripList.addAll(Arrays.asList(tripResponse.results));
                    mAdapter.notifyDataSetChanged();

                    updateMap(mTripList, preSize);
                }
            }
        });
    }

    private void updateMap(List<Trip> tripList, int preSize) {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.updateMap(tripList, preSize);
    }

    private void loadProgress() {
        if (mAdapter.getItemCount() == 0) {
            firstLoad = true;
            firstLoadDialog = new MaterialDialog.Builder(getContext())
                    .title(R.string.load_more_data)
                    .content(R.string.load_more_data)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
        }
        mSwipeRefreshLayout.setRefreshing(true);
        notifyListener(mProgressListener, true);
    }

    private void loadFinish() {
        if (firstLoad) {
            firstLoad = false;
            firstLoadDialog.dismiss();
            mHandler.postDelayed(setNotRefreshing, 0);
            return;
        }
        long startTime = mSwipeRefreshLayout.getDrawingTime();
        long currentTime = SystemClock.uptimeMillis();
        long remain = startTime + MIN_PROGRESS_APPEAR_TIME - currentTime;
        mHandler.postDelayed(setNotRefreshing, remain);
    }

    private void notifyListener(ProgressListener listener, boolean isProgress) {
        if (listener == null){
            return;
        }
        if (isProgress) {
            listener.onProgressShown();
        } else {
            listener.onProgressDismissed();
        }
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
            askToken(R.string.first_time_title, R.string.first_time_content);
            return null;
        }
        return type + " " + token;
    }

    private void askToken(int title, int content) {
        new MaterialDialog.Builder(getContext())
                .title(title)
                .content(content)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .cancelable(false)
                .input(R.string.first_time_hint, R.string.first_time_fill,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                SharedPreferences sharedPreferences =
                                        PreferenceManager.getDefaultSharedPreferences(getActivity());
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                String token = input.toString();
                                editor.putString(SettingsFragment.TOKEN, token);
                                if (editor.commit()) {
                                    checkToken();
                                } else {
                                    Utils.log("Commit token to SharedPreferences failed!");
                                }
                            }
                        }).show();
    }

    private void checkToken() {
        final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .title(R.string.check_token_title)
                .content(R.string.check_token_content)
                .progress(true, 0)
                .cancelable(false)
                .show();

        String token = getToken();
        Map<String, String> queryMap = getQueryMap(nextUrl);

        Observable<User> userObservable = restService.getUser(token, queryMap)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        mSubscription = userObservable.subscribe(new Subscriber<User>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                askToken(R.string.auth_fail_title, R.string.first_time_content);
                dialog.dismiss();
            }

            @Override
            public void onNext(User user) {
                dialog.dismiss();
                if (user != null) {
                    load();
                } else {
                    askToken(R.string.auth_fail_title, R.string.first_time_content);
                }
            }
        });
    }

    private void onError401() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.auth_fail_title)
                .content(R.string.auth_fail_content)
                .positiveText(R.string.auth_fail_positive)
                .negativeText(R.string.auth_fail_negative)
                .cancelable(false)
                .onPositive(settingsCallback)
                .onNegative(settingsCallback)
                .onNeutral(settingsCallback)
                .show();
    }

    private boolean hasActiveNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    protected void sort(int sortKey) {
        Utils.log("sort by: " + sortKey);
        mAdapter.sort(sortKey);
    }
}
