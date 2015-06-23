package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.api.RakeUserConfig;

public class SampleLiveConfig extends RakeUserConfig {
    @Override
    public RUNNING_ENV provideRunningMode() {
        return RUNNING_ENV.LIVE;
    }

    @Override
    public String provideDevToken() {
        return "dev_token_SampleLiveConfig";
    }

    @Override
    public String provideLiveToken() {
        return "live_token_SampleLiveConfig";
    }

    @Override
    public int provideFlushIntervalAsMilliseconds() {
        return 10; /* seconds */
    }

    @Override
    public int provideMaxLogTrackCount() {
        return 5;
    }

    @Override
    public boolean printDebugInfo() {
        return false;
    }
}
