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

    public RakeImpl(RakeUserConfig config, RakeCore core, SystemInformation info) {
        this.config = config;
        this.core = core;
        this.sysInfo = info;
    }

    @Override
    public void track(JSONObject shuttle) {
        if (null == shuttle) return;
        if (shuttle.toString().equals("{\"\":\"\"}")) return;

        // TODO getTIme from here
        JSONObject willBeTracked = null;

        try {
            willBeTracked = ShuttleProtocol.extractSentinelMeta(shuttle);
            JSONObject properties = ShuttleProtocol.extractProperties(shuttle);

            // TODO fill SystemInformation
             JSONObject defaultProperties = sysInfo.getDefaultProperties(config);
             copyProperties(defaultProperties, properties);

            willBeTracked.put(ShuttleProtocol.FIELD_NAME_PROPERTIES, properties);

            // TODO record: token, base_time, local_time

        } catch (JSONException e) {
            Logger.e("Can't build willBeTracked", e);
        } catch (Exception e) {
            Logger.e("Can't build willBeTracked", e);
        }

        if (null != willBeTracked) core.track(willBeTracked);
    }

    private void copyProperties(JSONObject source, JSONObject target) throws JSONException {
        Iterator<String> iter = source.keys();

        while(iter.hasNext()) {
            String key = iter.next();

            target.put(key, source.get(key));
        }
    }


    @Override
    public void flush() {
        core.flush();
    }
}
