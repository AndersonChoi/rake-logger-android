package com.skp.di.rake.client.config;

public interface RakeUserConfig {
    Mode getRunningMode();
    Integer getFlushInterval();

    enum Mode {DEV, LIVE}
}
