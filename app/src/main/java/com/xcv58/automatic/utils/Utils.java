package com.xcv58.automatic.utils;

import android.util.Log;

/**
 * Created by xcv58 on 12/26/15.
 */
public class Utils {
    public final static String TAG = "automatic_code_test";

    public static void log() {
        log("");
    }

    public static void log(Throwable e) {
        log(e.getMessage());
    }

    public static void log(String message) {
        Log.d(TAG, message);
    }
}
