package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.network.RakeHttpClient;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class MockRakeHttpClient extends RakeHttpClient {

    public MockRakeHttpClient(RakeMetaConfig config) {
        super(config);
    }

    @Override
    protected HttpResponse executePost(List<JSONObject> tracked) throws IOException {
        return MockServer.respond();
    }

    @Override
    protected void handleRakeException(int statusCode, String responseBody) {
        verifyResponse(statusCode, responseBody);
    }
}


