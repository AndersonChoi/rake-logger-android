package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.api.RakeUserConfig;

public class SampleDevConfig extends RakeUserConfig {
    @Override
    public RUNNING_ENV provideRunningMode() {
        return RUNNING_ENV.DEV;
    }

    @Override
    public String provideDevToken() {
        return "dev_token_SampleDevConfig";
    }

    @Override
    public String provideLiveToken() {
        return "dev_token_SampleLiveConfig";
    }

    @Override
    public int provideFlushIntervalAsSecond() {
        return 10; /* seconds */
    }

    @Override
    public int provideMaxLogTrackCount() {
        return 5;
    }

    @Override
    public boolean printDebugInfo() { return false; }
}
