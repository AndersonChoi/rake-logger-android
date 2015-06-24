package com.skp.di.rake.client.network;


import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.mock.MockRakeHttpClient;
import com.skp.di.rake.client.protocol.RakeProtocol;
import com.skp.di.rake.client.protocol.exception.InsufficientJsonFieldException;
import com.skp.di.rake.client.protocol.exception.InternalServerErrorException;
import com.skp.di.rake.client.protocol.exception.InvalidEndPointException;
import com.skp.di.rake.client.protocol.exception.InvalidJsonSyntaxException;
import com.skp.di.rake.client.protocol.exception.NotRegisteredRakeTokenException;
import com.skp.di.rake.client.protocol.exception.RakeProtocolBrokenException;
import com.skp.di.rake.client.protocol.exception.WrongRakeTokenUsageException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import com.skp.di.rake.client.mock.MockServer;
import com.skp.di.rake.client.utils.RakeTestUtils;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeHttpClientSpec {

    RakeHttpClient mockClient;
    RakeHttpClient testHttpClient;
    String TEST_MODE_ENDPOINT = "http://localhost:9010/track";
    RakeUserConfig devConfig;

    @Before
    public void setUp() throws JSONException, IOException {
        ShadowLog.stream = System.out;

        devConfig = RakeTestUtils.createRakeUserConfig(
                RakeUserConfig.RUNNING_ENV.DEV,
                "liveff21", "devfzkx1",
                10000, 5
        );

        mockClient  = new MockRakeHttpClient(devConfig);

        testHttpClient = new RakeHttpClient(
                RakeTestUtils.createDevConfig1(),
                RakeHttpClient.ContentType.URL_ENCODED_FORM);

        testHttpClient.setEndPoint(TEST_MODE_ENDPOINT);
    }

    @After
    public void tearDown() throws IOException {
    }

    @Test
    /* support to RakeApi.setRakeServer */
    public void test_setEndPointLegacy() {
        testHttpClient.setEndPointLegacy("example");
        assertEquals("example/track", testHttpClient.getEndPoint());
    }

    @Test
    public void test_SocketTimeoutException_is_Handled() throws IOException {
        // there must be no ConnectTimeoutException
        // RakeClient should handle it
        MockWebServer server = new MockWebServer();
        server.start(9010);

        testHttpClient.setSocketTImeout(100);
        testHttpClient.send(Arrays.asList(new JSONObject()));

        try {
            server.shutdown();
        } catch (IOException e) {
            // server will throw IOException as planned
        }
    }

    @Test
    public void test_HttpHeader_ContentType_is_UrlEncodedForm() throws InterruptedException, IOException, JSONException {
        // prepare a mock response
        JSONObject body = new JSONObject();
        body.put("errorCode", 20000);
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(200).setBody(body.toString()));
        server.start(9010);

        RakeHttpClient httpClient  =
                new RakeHttpClient(devConfig, RakeHttpClient.ContentType.URL_ENCODED_FORM);

        httpClient.setEndPoint(TEST_MODE_ENDPOINT);
        httpClient.send(Arrays.asList(new JSONObject()));

        RecordedRequest requested = server.takeRequest();
        assertEquals("POST /track HTTP/1.1", requested.getRequestLine());
        assertEquals("application/x-www-form-urlencoded", requested.getHeader("Content-Type"));

        server.shutdown();
    }

    @Test
    public void test_HttpHeader_ContentType_is_JSON() throws InterruptedException, JSONException, IOException {
        // prepare a mock response
        JSONObject body = new JSONObject();
        body.put("errorCode", 20000);
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(200).setBody(body.toString()));
        server.start(9010);

        RakeHttpClient httpClient  =
                new RakeHttpClient(devConfig, RakeHttpClient.ContentType.JSON);

        httpClient.setEndPoint(TEST_MODE_ENDPOINT);
        httpClient.send(Arrays.asList(new JSONObject()));

        RecordedRequest requested = server.takeRequest();
        assertEquals("application/json", requested.getHeader("Content-Type"));
        assertEquals("application/json", requested.getHeader("Accept"));

        server.shutdown();
    }

    @Test(expected= InsufficientJsonFieldException.class)
    public void test_InsufficientJsonFieldException() {
        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_INSUFFICIENT_JSON_FIELD);
        mockClient.send(Arrays.asList(new JSONObject()));
    }

    @Test(expected= InvalidJsonSyntaxException.class)
    public void test_InvalidJsonSyntaxException() {
        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_INVALID_JSON_SYNTAX);
        mockClient.send(Arrays.asList(new JSONObject()));
    }

    @Test(expected= NotRegisteredRakeTokenException.class)
    public void test_NotRegisteredRakeTokenException() {
        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN);
        mockClient.send(Arrays.asList(new JSONObject()));
    }

    @Test(expected= WrongRakeTokenUsageException.class)
    public void test_WrongRakeTokenUsageException() {
        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_WRONG_RAKE_TOKEN_USAGE);
        mockClient.send(Arrays.asList(new JSONObject()));
    }

    @Test(expected= InvalidEndPointException.class)
    public void test_InvalidEndPointException() {
        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_INVALID_END_POINT);
        mockClient.send(Arrays.asList(new JSONObject()));
    }

    @Test(expected= InternalServerErrorException.class)
    public void test_InternalServerErrorException() {
        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_INTERNAL_SERVER_ERROR);
        mockClient.send(Arrays.asList(new JSONObject()));
    }

    @Test(expected= RakeProtocolBrokenException.class)
    public void test_RakeProtocolBrokenExceptionWhenServerReturnInvalidJsonFormat() {
        /* mock server will return invalid json format */
        MockServer.setErrorCode(MockServer.ERROR_CODE_RAKE_PROTOCOL_BROKEN);
        mockClient.send(Arrays.asList(new JSONObject()));
    }

    @Test(expected= RakeProtocolBrokenException.class)
    public void test_RakeProtocolBrokenExceptionWhenServerReturnInvalidErrorAndStatusCode() {
        /* mock server will return undefined error code and status code */
        MockServer.setErrorCode(909014);
        mockClient.send(Arrays.asList(new JSONObject()));
    }
}
