package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.api.RakeUserConfig;

public class SampleRakeConfig1 extends RakeUserConfig {

    @Override
    public Mode provideRunningMode() {
        return Mode.DEV;
    }

    @Override
    public String provideDevToken() { return "dev1a021"; }

    @Override
    public String provideLiveToken() { return "live2k03"; }
}
