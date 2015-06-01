package com.skp.di.rake.client.network;


import com.skp.di.rake.client.mock.MockRakeHttpClient;
import com.skp.di.rake.client.protocol.RakeProtocol;
import com.skp.di.rake.client.protocol.exception.InsufficientJsonFieldException;
import com.skp.di.rake.client.protocol.exception.InternalServerErrorException;
import com.skp.di.rake.client.protocol.exception.InvalidEndPointException;
import com.skp.di.rake.client.protocol.exception.InvalidJsonSyntaxException;
import com.skp.di.rake.client.protocol.exception.NotRegisteredRakeTokenException;
import com.skp.di.rake.client.protocol.exception.RakeProtocolBrokenException;
import com.skp.di.rake.client.protocol.exception.WrongRakeTokenUsageException;

import org.apache.tools.ant.taskdefs.condition.Http;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.skp.di.rake.client.mock.MockServer;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeHttpClientSpec {

    RakeHttpClient mockClient;
    RakeHttpClient realClient;
    MockWebServer  server;

    @Before
    public void setUp() throws JSONException, IOException {
        mockClient = new MockRakeHttpClient();
        realClient = new RakeHttpClient();

        JSONObject body = new JSONObject();
        body.put("errorCode", 20000);

        server = new MockWebServer();
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body.toString()));

        server.start(9001);
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void testHttpHeader() throws InterruptedException {
        realClient.send("");

        RecordedRequest requested = server.takeRequest();

        // header assert
        assertEquals("POST /track HTTP/1.1", requested.getRequestLine());
        assertEquals("application/json", requested.getHeader("Content-Type"));
        assertEquals("application/json", requested.getHeader("Accept"));

        // body assert should be done in RakeSpec not here.
    }

    @Test(expected= InsufficientJsonFieldException.class)
    public void testInsufficientJsonFieldException() {
        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_INSUFFICIENT_JSON_FIELD);
        mockClient.send("");
    }

    @Test(expected= InvalidJsonSyntaxException.class)
    public void testInvalidJsonSyntaxException() {
        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_INVALID_JSON_SYNTAX);
        mockClient.send("");
    }

    @Test(expected= NotRegisteredRakeTokenException.class)
    public void testNotRegisteredRakeTokenException() {
        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN);
        mockClient.send("");
    }

    @Test(expected= WrongRakeTokenUsageException.class)
    public void testWrongRakeTokenUsageException() {
        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_WRONG_RAKE_TOKEN_USAGE);
        mockClient.send("");
    }

    @Test(expected= InvalidEndPointException.class)
    public void testInvalidEndPointException() {
        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_INVALID_END_POINT);
        mockClient.send("");
    }

    @Test(expected= InternalServerErrorException.class)
    public void testInternalServerErrorException() {
        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_INTERNAL_SERVER_ERROR);
        mockClient.send("");
    }

    @Test(expected= RakeProtocolBrokenException.class)
    public void testRakeProtocolBrokenExceptionWhenServerReturnInvalidJsonFormat() {
        /* mock server will return invalid json format */
        MockServer.setErrorCode(MockServer.ERROR_CODE_RAKE_PROTOCOL_BROKEN);
        mockClient.send("");
    }

    @Test(expected= RakeProtocolBrokenException.class)
    public void testRakeProtocolBrokenExceptionWhenServerReturnInvalidErrorAndStatusCode() {
        /* mock server will return undefined error code and status code */
        MockServer.setErrorCode(909014);
        mockClient.send("");
    }
}
