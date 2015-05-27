package com.skp.di.rake.api;

import com.skp.di.rake.api.Rake;
import com.skp.di.rake.api.RakeFactory;
import com.skp.di.rake.api.RakeUserConfig;
import com.skp.di.rake.api.impl.RakeImpl;
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

        if (loggerMap.containsKey(config)) {
            logger = loggerMap.get(config);
        } else {
            RakeDao dao           = new RakeDaoMemory();
            RakeHttpClient client = new RakeHttpClient();

            logger = new RakeImpl(dao, client);
            loggerMap.put(config, logger);
        }

        return logger;
    }
}

