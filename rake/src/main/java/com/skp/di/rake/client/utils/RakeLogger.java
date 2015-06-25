package com.skp.di.rake.client.utils;

import android.util.Log;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;

import java.io.PrintWriter;
import java.io.StringWriter;

public class RakeLogger {

    private String TAG;
    private RakeUserConfig config;

    public RakeLogger(Class clazz, RakeUserConfig config) {
        this.config = config;
        this.TAG = RakeMetaConfig.TAG + " (" + clazz.getName() + ") ";
    }

    // `e` provide detailed error messages than `error`
    public void e(String message, Throwable t) {
        Log.e(TAG, message + "\n" + getStacktrace(t));
    }

    public void e(Throwable t) {
        e("", t);
    }

    // for static methods
    static public void error(String message, Throwable t) {
        Log.e(RakeMetaConfig.TAG, message + "\n" + getStacktrace(t));
    }

    static public void error(Throwable t) {
       error("", t);
    }

    static public String getStacktrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /* print log only in dev */
    public void i(String message) {
        if (null == message) return;
        if (!config.getPrintInfoFlag()) return;

        Log.i(TAG, message);
    }
}
