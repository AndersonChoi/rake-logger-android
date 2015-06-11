package com.skp.di.rake.client.utils;

import com.skp.di.rake.client.api.RakeUserConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class RakeLoggerFactory {

    static private HashMap<Class, RakeLogger> loggerMap =
            new LinkedHashMap<Class, RakeLogger>();

    static { Collections.synchronizedMap(loggerMap); }

    static public RakeLogger getLogger(Class clazz, RakeUserConfig config) {
        synchronized (loggerMap) {
            if (null == loggerMap.get(clazz))
                loggerMap.put(clazz, new RakeLogger(clazz, config));

            return loggerMap.get(clazz);
        }
    }
}
