package com.skp.di.rake.client.logger;

import com.skp.di.rake.client.config.RakeUserConfig;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.persistent.RakeDaoMemory;

import java.util.Collections;
import java.util.HashMap;

public final class RakeFactory {

    private static HashMap<String, Rake> loggerMap;

    static {
        loggerMap = new HashMap<>();
        Collections.synchronizedMap(loggerMap);
    }

    public static Rake getLogger(String token, RakeUserConfig config) {

        Rake logger;

        if (loggerMap.containsKey(token)) {
            logger = loggerMap.get(token);
        } else {
            RakeDao dao = new RakeDaoMemory();
            logger = new RakeLogger(dao);
            loggerMap.put(token, logger);
        }

        return logger;
    }
}

