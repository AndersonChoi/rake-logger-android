package com.skp.di.rake.client.utils;

import android.util.Log;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;

public class RakeLogger {

    private String TAG;
    private RakeUserConfig config;

    public RakeLogger(Class clazz, RakeUserConfig config) {
        this.config = config;
        this.TAG = RakeMetaConfig.TAG + " (" + clazz.getName() + ") ";
    }

    static public void e(String message, Throwable e) {
        Log.e(RakeMetaConfig.TAG, message, e);
    }

    static public void e(Throwable e) {
        Log.e(RakeMetaConfig.TAG, e.toString());
    }

    /* print log only in dev */
    public void i(String message) {
        if (null == message) return;
        if (!config.printDebugInfo()) return;

        Log.i(TAG, message);
    }
}
