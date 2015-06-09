package com.skp.di.rake.client.network;

public class RakeDefaultNetworkConfig extends RakeNetworkConfig {
    @Override
    protected String provideEndPoint() {
        return "https://localhost:9010/track";
    }

    @Override
    protected int provideSocketTimeout() {
        return 120000;
    }

    @Override
    protected int provideConnectionTimeout() {
        return 3000;
    }

    @Override
    protected ContentType provideContentType() {
        return ContentType.URL_ENCODED_FORM;
    }
}
