package com.skp.di.rake.client.utils;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.network.RakeHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
                100, /* do not increase this interval value. it affects test running time */
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
            public int provideFlushIntervalAsMilliseconds() {
                return intervalAsMilliseconds;
            }

            @Override
            public int provideMaxLogTrackCount() {
                return maxTrackCount;
            }

            @Override
            public boolean printDebugInfo() {
                return false;
            }
        };
    }
}
