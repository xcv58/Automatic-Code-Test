package com.xcv58.automatic.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.xcv58.automatic.R;
import com.xcv58.automatic.utils.Utils;

/**
 * Created by xcv58 on 12/23/15.
 */
public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    protected final static String TOKEN = "token";
    protected final static String TYPE = "type";
    protected final static String TYPE_DEFAULT = "Bearer";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(getActivity());
        updateSummary(sharedPreferences, TYPE, TYPE_DEFAULT);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Utils.log("onSharedPreferenceChanged: " + key);
        if (!key.equals(TOKEN)) {
            updateSummary(sharedPreferences, key);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key, String def) {
        Preference preference = findPreference(key);
        preference.setSummary(sharedPreferences.getString(key, def));
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        updateSummary(sharedPreferences, key, "");
    }
}

