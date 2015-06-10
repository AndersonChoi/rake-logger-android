package com.skp.di.rake.client.api.impl;

import com.skp.di.rake.client.android.SystemInformation;
import com.skp.di.rake.client.api.Rake;
import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.protocol.RakeProtocol;
import com.skp.di.rake.client.protocol.ShuttleProtocol;
import com.skp.di.rake.client.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class RakeImpl implements Rake {
    private RakeCore core;
    private RakeUserConfig config;
    private SystemInformation sysInfo;

    private JSONObject superProperties;

    public RakeImpl(RakeUserConfig config, RakeCore core, SystemInformation info) {
        this.config = config;
        this.core = core;
        this.sysInfo = info;
        this.superProperties = new JSONObject();
    }

    @Override
    synchronized public void track(JSONObject shuttle) {
        if (null == shuttle) return;
        if (shuttle.toString().equals("{\"\":\"\"}")) return;

        JSONObject trackableShuttle = null;

        try {
            JSONObject defaultProperties = sysInfo.getDefaultProperties(config);
            trackableShuttle = ShuttleProtocol.getTrackableShuttle(shuttle, defaultProperties);
        } catch (JSONException e) {
            Logger.e("Can't build willBeTracked", e);
        } catch (Exception e) {
            Logger.e("Can't build willBeTracked", e);
        }

        if (null != trackableShuttle) core.track(trackableShuttle);
    }

    @Override
    public void flush() {
        core.flush();
    }
}
