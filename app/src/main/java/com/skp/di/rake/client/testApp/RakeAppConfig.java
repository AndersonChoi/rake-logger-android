package com.skp.di.rake.client.testApp;

import com.skp.di.rake.client.api.RakeUserConfig;

public class RakeAppConfig extends RakeUserConfig {
    @Override
    public RUNNING_ENV provideRunningMode() { return RUNNING_ENV.DEV; }

    @Override
    public String provideDevToken() { return "34e03e87dfd7152596e15bdefa4626de9bdf8c32"; }

    @Override
    public String provideLiveToken() { return "617b4dfd0ffd25fbb6740e4b5c6e2561c241e44"; }

    @Override
    public int provideFlushIntervalAsSecond() { return 30; }

    @Override
    public int provideMaxLogTrackCount() { return 5; }

    @Override
    public boolean printDebugInfo() {
        return true;
    }
}
