package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.network.RakeNetworkConfig;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import library.test.json.JSONObjectSpec;

public class MockRakeHttpClient extends RakeHttpClient {

    public MockRakeHttpClient(RakeNetworkConfig config) {
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


