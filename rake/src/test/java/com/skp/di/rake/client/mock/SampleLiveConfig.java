package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.api.RakeUserConfig;

public class SampleLiveConfig extends RakeUserConfig {
    @Override
    public Mode provideRunningMode() {
        return Mode.LIVE;
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
    public int provideFlushInterval() {
        return 10; /* seconds */
    }

    @Override
    public int provideMaxLogTrackCount() {
        return 5;
    }
}
