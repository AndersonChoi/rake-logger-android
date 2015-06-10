package com.skp.di.rake.client.config;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.protocol.exception.RakeProtocolBrokenException;

public class RakeMetaConfig {
    /* fields and enums */
    public enum ContentType { JSON, URL_ENCODED_FORM }
    private RakeUserConfig.RUNNING_ENV runningMode;
    static public final String TAG = "RAKE";
    private String endPoint;

    /* settings which Rake-Client developer (not user) should provide */
    static public final String RAKE_CLIENT_VERSION = "r0.5.0_c0.3.15";
    static private ContentType CONTENT_TYPE = ContentType.URL_ENCODED_FORM;
    static private String TEST_MODE_ENDPOINT = "http://localhost:9010/track";
    static private String LIVE_MODE_ENDPOINT = "https://rake.skplanet.com:8443/log/track";
    static private String DEV_MODE_ENDPOINT  = "https://pg.rake.skplanet.com:8443/log/track";
    static private int HTTP_SOCKET_TIMEOUT = 120000;
    static private int HTTP_CONNECTION_TIMEOUT = 3000;
    /* end provided settings */

    public RakeMetaConfig(RakeUserConfig config) {
        this.runningMode = config.getRunningMode();

        if (RakeUserConfig.RUNNING_ENV.DEV == runningMode) endPoint = DEV_MODE_ENDPOINT;
        else if (RakeUserConfig.RUNNING_ENV.LIVE == runningMode) endPoint = LIVE_MODE_ENDPOINT;
        else throw new RakeProtocolBrokenException("Can't set endpoint due to invalid runningMode from RakeUserConfig");
    }

    public String getEndpoint() { return endPoint; }
    public ContentType getContentType() { return CONTENT_TYPE; }
    public int getHttpSocketTimeout() { return HTTP_SOCKET_TIMEOUT; }
    public int getHttpConnectionTimeout() { return HTTP_CONNECTION_TIMEOUT; }

    /* for test */
    static public void setContentType(ContentType type) { CONTENT_TYPE = type; }
    public void setTestEndPoint() { this.endPoint = TEST_MODE_ENDPOINT; }
}
