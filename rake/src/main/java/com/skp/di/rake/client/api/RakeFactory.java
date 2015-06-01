package com.skp.di.rake.client.api;

import com.skp.di.rake.client.api.impl.RakeCore;
import com.skp.di.rake.client.api.impl.RakeImpl;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.persistent.RakeDaoMemory;

import java.util.Collections;
import java.util.HashMap;

public class RakeFactory {

    static private HashMap<RakeUserConfig, Rake> loggerMap;

    static {
        loggerMap = new HashMap<RakeUserConfig, Rake>();
        Collections.synchronizedMap(loggerMap);
    }

    static public Rake getLogger(RakeUserConfig config) {

        Rake logger;
        RakeDao dao           = new RakeDaoMemory();
        RakeHttpClient client = new RakeHttpClient();
        RakeCore core         = RakeCore.getInstance(dao, client, config);

        if (loggerMap.containsKey(config)) {
            logger = loggerMap.get(config);
        } else {
            /// TODO remove config
            // logger = new RakeImplWithScheduler(config, RakeCore.getInstance());
            logger = new RakeImpl(config, core);
            loggerMap.put(config, logger);
        }

        return logger;
    }
}

