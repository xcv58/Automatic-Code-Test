package com.xcv58.automatic.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by xcv58 on 12/23/15.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
