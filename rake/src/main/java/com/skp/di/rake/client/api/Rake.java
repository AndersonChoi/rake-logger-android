package com.skp.di.rake.client.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.skp.di.rake.client.android.SystemInformation;
import com.skp.di.rake.client.core.RakeCore;
import com.skp.di.rake.client.protocol.ShuttleProtocol;
import com.skp.di.rake.client.utils.RakeLogger;
import com.skp.di.rake.client.utils.RakeLoggerFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class Rake {
    private RakeCore core;
    private RakeUserConfig config;
    private SystemInformation sysInfo;

    private RakeLogger debugLogger;

    public Rake(RakeUserConfig config, RakeCore core, Context context, SystemInformation sysInfo) {
        this.config = config;
        this.core = core;
        this.sysInfo = sysInfo;
        this.superProperties = new JSONObject();
        this.debugLogger = RakeLoggerFactory.getLogger(this.getClass(), config);

        legacySupport(context, config);
    }

    // no synchronized needed. since caller doesn't care consistency thanks to Um.
    public void track(JSONObject shuttle) {
        if (null == shuttle) return;
        if (shuttle.toString().equals("{\"\":\"\"}")) return;

        debugLogger.i("`track` called, inserted shuttle: \n" + shuttle.toString());

        JSONObject trackable = null;

        try {
            // TODO sync with this.sysInfo?
            JSONObject defaultProperties = sysInfo.getDefaultProperties(config);
            trackable = ShuttleProtocol.getTrackable(shuttle, superProperties, defaultProperties);

            debugLogger.i("Tracked: \n" + trackable.toString());

        } catch (JSONException e) {
            RakeLogger.e("Can't build trackable", e);
        } catch (Exception e) {
            RakeLogger.e("Can't build trackable. Due to invalid shuttle", e);
        }

        if (null != trackable) core.track(trackable);
    }

    public void flush() {

        debugLogger.i("flush called");
        core.flush();
    }


























    /* Legacy API */
    private JSONObject superProperties;

    public void setFlushInterval(Context context, long milliseconds) {}
    public void setDebug(Boolean debug) {}
    public void setRakeServer(Context onctext, String server) {}

    public boolean hasSuperProperty(String superPropertyName) {
        synchronized (this.superProperties) {
            return this.superProperties.has(superPropertyName);
        }
    }

    public Object getSuperPropertyValue(String superPropertyName) throws JSONException {
        synchronized (this.superProperties) {
            return this.superProperties.get(superPropertyName);
        }
    }

    public void registerSuperProperties(JSONObject superProperties) {
        debugLogger.i("add super-properties: \n" + superProperties);
        addSuperProperties(superProperties, false);
    }

    public void registerSuperPropertiesOnce(JSONObject superProperties) {
        debugLogger.i("add super-properties once: \n" + superProperties);
        addSuperProperties(superProperties, true);
    }

    private void addSuperProperties(JSONObject superProps, boolean once) {
        synchronized (this.superProperties) {
            try {
                Iterator<String> iter = superProps.keys();

                while(iter.hasNext()) {
                    String key = iter.next();

                    if (once && this.superProperties.has(key)) continue;

                    this.superProperties.put(key, superProps.get(key));
                }
            } catch (JSONException e) {
                RakeLogger.e("Can't add super property", e);
            }
        }

        debugLogger.i("total super-properties: \n" + this.superProperties);
        savePrefenrences();
    }

    public void unregisterSuperProperties(String superPropertyName) {
        synchronized (this.superProperties) {
            this.superProperties.remove(superPropertyName);
        }

        debugLogger.i("unregister super-property: " + superPropertyName);
        debugLogger.i("total super-properties: \n" + this.superProperties);
        savePrefenrences();
    }

    public void clearSuperProperties() {
        synchronized (this.superProperties) {
            this.superProperties = new JSONObject();
        }

        debugLogger.i("clear all super-properties, now super-properties: \n" + this.superProperties);
        clearPreferences();
    }

    private SharedPreferences sharedPref;

    private void legacySupport(Context context, RakeUserConfig config) {
        String path = "com.skp.di.rake.client.api.Rake_" + config.getToken();
        sharedPref = context.getSharedPreferences(path, Context.MODE_PRIVATE);

        readPreferences();
    }

    private void readPreferences() {
        String props = sharedPref.getString("super_properties", "{}");

        synchronized (this.superProperties) {
            try {
                superProperties = new JSONObject(props);
            } catch (JSONException e) {
                RakeLogger.e("Cannot parse stored superProperties", e);
                superProperties = new JSONObject();
                clearPreferences();
            }
        }

        debugLogger.i("read super-properties from SharedPref, now super-properties: \n" + this.superProperties);
    }

    private void savePrefenrences() {
        synchronized (this.superProperties) {
            String props = superProperties.toString();
            SharedPreferences.Editor prefsEditor = sharedPref.edit();
            prefsEditor.putString("super_properties", props);
            prefsEditor.commit();
        }
    }

    private void clearPreferences() {
        SharedPreferences.Editor prefsEdit = sharedPref.edit();
        prefsEdit.clear().commit();
    }
}
