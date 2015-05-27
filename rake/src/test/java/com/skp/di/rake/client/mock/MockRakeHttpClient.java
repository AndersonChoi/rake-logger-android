package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.network.RakeHttpClient;

import org.apache.http.HttpResponse;

import java.io.IOException;

public class MockRakeHttpClient extends RakeHttpClient {

    @Override
    protected HttpResponse executePost(String body) throws IOException {
        return MockServer.respond();
    }

    @Override
    protected void handleRakeException(int statusCode, String responseBody) {
        verifyResponse(statusCode, responseBody);
    }
}


