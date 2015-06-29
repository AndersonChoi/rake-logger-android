package com.skp.di.rake.client.api;

import android.content.Context;

import com.skp.di.rake.client.android.SystemInformation;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.core.RakeCore;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.persistent.RakeDaoMemory;
import com.skp.di.rake.client.persistent.RakeDaoSQLite;
import com.skp.di.rake.client.protocol.RakeProtocolV1;

import java.util.Collections;
import java.util.HashMap;

public class RakeFactory {

    static volatile private RakeCore core;
    static private final HashMap<RakeUserConfig, Rake> loggerMap = new HashMap<RakeUserConfig, Rake>();

    static private RakeDao dao;
    static private RakeHttpClient client;
    static private SystemInformation sysInfo;

    static { Collections.synchronizedMap(loggerMap); }

    synchronized static public Rake getLogger(RakeUserConfig config, Context context) {
        Rake logger;

        if (null == dao) dao = new RakeDaoSQLite(config, context);
        if (null == client) client = new RakeHttpClient(config, new RakeProtocolV1());
        if (null == sysInfo) sysInfo = new SystemInformation(context);
        if (null == core) core = new RakeCore(dao, client, config);

        // TODO 추후에 core per rake instance 가 될 수 있으므로 TBD
        if (loggerMap.containsKey(config)) {
            logger = loggerMap.get(config);
        } else {
            logger = new Rake(config, core, context, new SystemInformation(context));
            loggerMap.put(config, logger);
        }

        return logger;
    }
}

