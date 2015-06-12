package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.api.RakeUserConfig;

public class SampleDevConfig2 extends RakeUserConfig {

    @Override
    public RUNNING_ENV provideRunningMode() { return RUNNING_ENV.DEV; }

    @Override
    public String provideDevToken() { return "dev1a021"; }

    @Override
    public String provideLiveToken() { return "live2k03"; }

    @Override
    public int provideFlushIntervalAsSecond() {
        return 60;
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
