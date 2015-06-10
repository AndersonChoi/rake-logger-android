package com.skp.di.rake.client.utils;

import android.util.Log;

import com.skp.di.rake.client.config.RakeMetaConfig;

public class Logger {
    static public void e(String message, Throwable e) {
        Log.e(RakeMetaConfig.TAG, message, e);
    }

    static public void e(Throwable e) {
        Log.e(RakeMetaConfig.TAG, e.toString());
    }

    static public void i(String message) {
        if (null != message)
            Log.i(RakeMetaConfig.TAG, message);
    }
}
