package com.skp.di.rake.client.api.impl;

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

    public RakeImpl(RakeUserConfig config, RakeCore core) {
        this.config = config;
        this.core = core;
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
//            JSONObject systemInfo = SystemInfomation.getDefaultProperties();
//            ShuttleProtocol.copyProperties(systemInfo, properties);

            willBeTracked.put(ShuttleProtocol.FIELD_NAME_PROPERTIES, properties);

        } catch (JSONException e) {
            Logger.e("Can't build willBeTracked", e);
        } catch (Exception e) {
            Logger.e("Can't build willBeTracked", e);
        }

        if (null != willBeTracked) core.track(willBeTracked);
    }


    @Override
    public void flush() {
        core.flush();
    }
}
