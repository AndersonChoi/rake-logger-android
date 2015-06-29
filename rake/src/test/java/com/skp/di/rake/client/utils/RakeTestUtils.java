package com.skp.di.rake.client.utils;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.core.RakeCore;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.protocol.RakeProtocolV2;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;

public class RakeTestUtils {
    static private RakeUserConfig config = createDevConfig1();
    static private String TEST_MODE_ENDPOINT = "http://localhost:9010/track";

    static public HttpClient createHttpClient() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, RakeHttpClient.DEFAULT_CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, RakeHttpClient.DEFAULT_SOCKET_TIMEOUT);
        HttpClient client = new DefaultHttpClient(params);

        return client;
    }

    static public HttpPost createHttpPost(StringEntity se) {
        HttpPost post = new HttpPost(TEST_MODE_ENDPOINT);
        post.setEntity(se);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Accept", "application/json");

        return post;
    }

    static public StringEntity createEntity(String body) throws UnsupportedEncodingException {
        return new StringEntity(body);
    }

    static public <E> List<E> toList(Iterable<E> iter) {
        ArrayList<E> list = new ArrayList<E>();

        for(E item : iter) { list.add(item); }

        return list;
    }

    static public HttpResponse sendHttpPost(String body) {
        HttpClient client = null;
        HttpPost     post = null;
        HttpResponse res  = null;

        try {
            client = createHttpClient();
            StringEntity se = createEntity("{}");
            post = createHttpPost(se);
            res = client.execute(post);
        } catch (Exception e) { throw OnErrorThrowable.from(e); }

        return res;
    }

    static public RakeCore createTestRakeCore(RakeDao dao,
                                              RakeHttpClient client,
                                              RakeUserConfig config) {
        RakeCore core = new RakeCore(
                dao, AndroidSchedulers.mainThread(),
                client, AndroidSchedulers.mainThread(), config);

        return core;
    }

    static public RakeUserConfig createDevConfig1() {
        return createRakeUserConfig(
                RakeUserConfig.RUNNING_ENV.DEV,
                "live1f00", "dev1a021",
                60000, 5
        );
    }

    static public RakeUserConfig createDevConfig2() {
        return RakeTestUtils.createRakeUserConfig(
                RakeUserConfig.RUNNING_ENV.DEV,
                "live2k03", "dev2zrfa",
                60000, 5
        );
    }

    static public RakeUserConfig createLiveConfig1() {
        return createRakeUserConfig(
                RakeUserConfig.RUNNING_ENV.LIVE,
                "example liveToken", "exampleDevToken",
                300, /* do not increase this interval value. it affects test running time */
                15);
    }

    static public RakeUserConfig createRakeUserConfig(RakeUserConfig.RUNNING_ENV env,
                                                      String liveToken,
                                                      String devToken,
                                               int intervalAsMilliseconds,
                                               int maxTrackCount) {

        return new RakeUserConfig() {
            @Override
            public RUNNING_ENV provideRunningMode() {
                return env;
            }

            @Override
            public String provideLiveToken() {
                return liveToken;
            }

            @Override
            public String provideDevToken() {
                return devToken;
            }

            @Override
            public long provideFlushIntervalAsMilliseconds() {
                return intervalAsMilliseconds;
            }

            @Override
            public int provideMaxLogTrackCount() {
                return maxTrackCount;
            }

            @Override
            public boolean printDebugInfo() {
                return true;
            }
        };
    }

    static public void setErrorCodeOnMockServer(int errorCode) {
        MockServer.setErrorCode(errorCode);
    }

    static public int getErorCodeWhenRakeProtocolBroken() {
        return MockServer.ERROR_CODE_RAKE_PROTOCOL_BROKEN;
    }

    static public RakeHttpClient createMockHttpClient(RakeUserConfig config) {
        return new MockRakeHttpClient(config);
    }

    static public JSONObject createSampleDefaultProperties(RakeUserConfig config) throws JSONException {
        return MockSystemInformation.getDefaultProperties(config);
    }
}

class MockServer {

    static public final int ERROR_CODE_RAKE_PROTOCOL_BROKEN = 90001;

    static private int errorCode = RakeProtocolV2.ERROR_CODE_OK;

    static public void setErrorCode(int errorCode) {
        MockServer.errorCode = errorCode;
    }

    /* mocking server action */
    static public HttpResponse respond() {
        return createResponse();
    }

    static private HttpResponse createResponse() {

        HttpResponse res = null;

        try {
            res = new BasicHttpResponse(HttpVersion.HTTP_1_1, 0, null);

            // fill statusCoed
            res.setStatusCode(getStatusCode());

            // fill body
            JSONObject body = createResponseBody();
            StringEntity entity = new StringEntity(body.toString());
            res.setEntity(entity);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return res;
    }

    static private JSONObject createResponseBody() throws JSONException {
        JSONObject body = new JSONObject();

        if (ERROR_CODE_RAKE_PROTOCOL_BROKEN != errorCode) {
            body.put("statusCode", getStatusCode());
            body.put("errorCode", errorCode);
            body.put("message", "");
            body.put("moreInfo", "http://www.google.com");
        }

        return body;
    }

    static private int getStatusCode() {
        switch (errorCode) {
            case RakeProtocolV2.ERROR_CODE_OK:
                return HttpStatus.SC_OK;
            case RakeProtocolV2.ERROR_CODE_INSUFFICIENT_JSON_FIELD:
            case RakeProtocolV2.ERROR_CODE_INVALID_JSON_SYNTAX:
                return HttpStatus.SC_BAD_REQUEST;
            case RakeProtocolV2.ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN:
                return HttpStatus.SC_UNAUTHORIZED;
            case RakeProtocolV2.ERROR_CODE_WRONG_RAKE_TOKEN_USAGE:
                return HttpStatus.SC_FORBIDDEN;
            case RakeProtocolV2.ERROR_CODE_INVALID_END_POINT:
                return HttpStatus.SC_NOT_FOUND;
            case RakeProtocolV2.ERROR_CODE_INTERNAL_SERVER_ERROR:
                return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }

        return 0;
    }
}

class MockSystemInformation {
    static public JSONObject getDefaultProperties(RakeUserConfig config) throws JSONException {
        DateFormat baseTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        DateFormat localTimeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        JSONObject defaultProperties = new JSONObject();

        Date now = new Date();
        defaultProperties.put("app_version", "1.0.0");
        defaultProperties.put("network_type", "WIFI");
        defaultProperties.put("language_code", "KR");

        defaultProperties.put("device_id", "example_device_id");
        defaultProperties.put("device_model", "example_device_model");
        defaultProperties.put("os_name", "Android");
        defaultProperties.put("os_version", "4.4.2");

        defaultProperties.put("resolution", "1080*1920");
        defaultProperties.put("screen_width", "1920");
        defaultProperties.put("screen_height", "1080");

        defaultProperties.put("carrier_name", "SK Telecom");
        defaultProperties.put("manufacturer", "samsung");

        // put properties irrelevant to android system information
        defaultProperties.put("token", config.getToken());
        defaultProperties.put("base_time", baseTimeFormat.format(now));
        defaultProperties.put("local_time", localTimeFormat.format(now));
        defaultProperties.put("rake_lib", "android");
        defaultProperties.put("rake_lib_version", RakeMetaConfig.RAKE_CLIENT_VERSION);

        return defaultProperties;
    }
}

class MockRakeHttpClient extends RakeHttpClient {

    public MockRakeHttpClient(RakeUserConfig config) {
        super(config, new RakeProtocolV2());
    }

    @Override
    protected HttpResponse executePost(List<JSONObject> tracked) throws IOException {
        return MockServer.respond();
    }
}
