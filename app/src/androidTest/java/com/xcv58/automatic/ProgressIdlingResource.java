package com.xcv58.automatic;

import android.support.test.espresso.IdlingResource;

import com.xcv58.automatic.activities.TripFragment;

/**
 * Created by xcv58 on 1/3/16.
 */
public class ProgressIdlingResource implements IdlingResource {
    private ResourceCallback resourceCallback;
    private TripFragment tripFragment;

    public ProgressIdlingResource(TripFragment fragment) {
        tripFragment = fragment;

        TripFragment.ProgressListener progressListener = new TripFragment.ProgressListener() {
            @Override
            public void onProgressShown() {
            }

            @Override
            public void onProgressDismissed() {
                if (resourceCallback == null) {
                    return;
                }
                resourceCallback.onTransitionToIdle();
            }
        };
        tripFragment.setProgressListener(progressListener);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean isIdleNow() {
        return !tripFragment.isInProgress();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        resourceCallback = callback;
    }
}
