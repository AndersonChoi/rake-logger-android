package com.skp.di.rake.client.api;

import android.content.Context;

import com.skp.di.rake.client.android.SystemInformation;
import com.skp.di.rake.client.api.impl.RakeCore;
import com.skp.di.rake.client.api.impl.RakeImpl;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.persistent.RakeDaoMemory;

import java.util.Collections;
import java.util.HashMap;

public class RakeFactory {

    static private HashMap<RakeUserConfig, Rake> loggerMap;
    static private RakeCore core;

    static {
        loggerMap = new HashMap<RakeUserConfig, Rake>();
        Collections.synchronizedMap(loggerMap);
    }

    static public Rake getLogger(RakeUserConfig config, Context context) {
        Rake logger;

        // TODO remove config: 추후에 core per rake instance 가 될 수 있으므로 TBD
        if (null == core) {
            RakeDao dao           = new RakeDaoMemory();
            RakeHttpClient client = new RakeHttpClient();
            core = new RakeCore(dao, client, config);
        }

        if (loggerMap.containsKey(config)) {
            logger = loggerMap.get(config);
        } else {
            logger = new RakeImpl(config, core, SystemInformation.getInstance(context));
            loggerMap.put(config, logger);
        }

        return logger;
    }
}

