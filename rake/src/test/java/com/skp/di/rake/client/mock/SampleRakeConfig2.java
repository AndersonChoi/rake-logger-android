package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.api.RakeUserConfig;

public class SampleRakeConfig2 extends RakeUserConfig {

    @Override
    public Mode provideRunningMode() {
        return Mode.DEV;
    }

    @Override
    public String provideDevToken() { return "dev445l1"; }

    @Override
    public String provideLiveToken() { return "live9194"; }
}
