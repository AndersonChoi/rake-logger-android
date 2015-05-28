package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.api.RakeFactory;
import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.api.Rake;
import com.skp.di.rake.client.api.impl.RakeImpl;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.persistent.RakeDaoMemory;

import java.util.Collections;
import java.util.HashMap;

public class MockRakeFactory extends RakeFactory {
    private static HashMap<RakeUserConfig, Rake> loggerMap;

    static {
        loggerMap = new HashMap<RakeUserConfig, Rake>();
        Collections.synchronizedMap(loggerMap);
    }

    public static Rake getLogger(RakeUserConfig config) {

        Rake logger;

        if (loggerMap.containsKey(config)) {
            logger = loggerMap.get(config);
        } else {
            RakeDao dao           = new RakeDaoMemory();
            RakeHttpClient client = new MockRakeHttpClient();

            logger = new RakeImpl(dao, client); /* use TestRakeLogger */
            loggerMap.put(config, logger);
        }

        return logger;
    }
}


