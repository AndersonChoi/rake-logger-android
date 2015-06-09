package com.skp.di.rake.client.network;

public abstract class RakeNetworkConfig {

    abstract protected String provideEndPoint();
    abstract protected int    provideSocketTimeout();
    abstract protected int    provideConnectionTimeout();
    abstract protected ContentType provideContentType();

    public int getSocketTimeout() { return provideSocketTimeout(); }
    public int getConnectionTimeout() { return provideConnectionTimeout(); }
    public String getEndPoint() { return provideEndPoint(); }
    public ContentType getContentType() { return provideContentType(); }

    public enum ContentType { JSON, URL_ENCODED_FORM }
}
