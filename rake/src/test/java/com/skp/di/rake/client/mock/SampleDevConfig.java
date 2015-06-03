package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.api.RakeUserConfig;

public class SampleDevConfig extends RakeUserConfig {
    @Override
    public Mode provideRunningMode() {
        return Mode.DEV;
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
    public int provideFlushInterval() {
        return 10; /* seconds */
    }

    @Override
    public int provideMaxLogTrackCount() {
        return 5;
    }
}
