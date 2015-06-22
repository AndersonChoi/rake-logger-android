package com.skp.di.rake.client.config;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.protocol.exception.RakeProtocolBrokenException;

public class RakeMetaConfig {
    /* fields and enums */
    private RakeUserConfig.RUNNING_ENV runningMode;
    static public final String TAG = "RAKE";

    /* settings which Rake-Client developer (not user) should provide */
    static public final String RAKE_CLIENT_VERSION = "r0.5.0_c0.4.0";
    /* end provided settings */

    public RakeMetaConfig(RakeUserConfig config) {
        this.runningMode = config.getRunningMode();

    }

}
