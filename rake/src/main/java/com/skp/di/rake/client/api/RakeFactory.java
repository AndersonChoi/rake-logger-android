package com.skp.di.rake.client.api;

import android.content.Context;

import com.skp.di.rake.client.android.SystemInformation;
import com.skp.di.rake.client.api.impl.RakeCore;
import com.skp.di.rake.client.api.impl.RakeImpl;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.persistent.RakeDaoMemory;

import java.util.Collections;
import java.util.HashMap;

public class RakeFactory {


    static volatile private RakeCore core;
    static private final HashMap<RakeUserConfig, Rake> loggerMap = new HashMap<RakeUserConfig, Rake>();

    static { Collections.synchronizedMap(loggerMap); }

    synchronized static public Rake getLogger(RakeUserConfig config, Context context) {
        Rake logger;

        // TODO remove config: 추후에 core per rake instance 가 될 수 있으므로 TBD
        if (null == core) {
            RakeDao dao           = new RakeDaoMemory();
            RakeHttpClient client = new RakeHttpClient(new RakeMetaConfig(config));
            core = new RakeCore(dao, client, config);
        }

        if (loggerMap.containsKey(config)) {
            logger = loggerMap.get(config);
        } else {
            logger = new RakeImpl(config, core, new SystemInformation(context));
            loggerMap.put(config, logger);
        }

        return logger;
    }
}

