package com.skp.di.rake.client.utils;

import com.skp.di.rake.client.api.RakeUserConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RakeLoggerFactory {

    static private Map<Class, RakeLogger> loggerMap = new ConcurrentHashMap<Class, RakeLogger>();

    static public RakeLogger getLogger(Class clazz, RakeUserConfig config) {
        synchronized (loggerMap) {
            if (null == loggerMap.get(clazz))
                loggerMap.put(clazz, new RakeLogger(clazz, config));

            return loggerMap.get(clazz);
        }
    }
}
