package com.skp.di.rake.client.mock;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.protocol.RakeProtocol;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class MockRakeHttpClient extends RakeHttpClient {

    public MockRakeHttpClient(RakeUserConfig config) {
        super(config, ContentType.URL_ENCODED_FORM);
    }

    @Override
    protected HttpResponse executePost(List<JSONObject> tracked) throws IOException {
        return MockServer.respond();
    }

    @Override
    protected void verifyResponse(int statusCode, String responseBody) {
        RakeProtocol.verifyStatusCode(statusCode);
        RakeProtocol.verifyErrorCode(responseBody);
    }
}


