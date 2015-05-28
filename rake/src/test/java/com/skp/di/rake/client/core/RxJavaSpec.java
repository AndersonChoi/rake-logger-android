package com.skp.di.rake.client.core;

import com.skp.di.rake.client.api.Rake;
import com.skp.di.rake.client.api.RakeFactory;
import com.skp.di.rake.client.mock.SampleRakeConfig1;
import com.skp.di.rake.client.utils.TestUtils;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class RxJavaSpec {

    String[] names = new String[] {"1ambda", "2ambda", "3ambda"};

    MockWebServer server;

    @Before
    public void setUp() throws IOException, JSONException {
        JSONObject body = new JSONObject();

        body.put("key", "value");

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
    public void testRxJava() {
        Iterable<String> iterable = Observable.from(names).toBlocking().toIterable();

        List<String> result   = TestUtils.toList(iterable);
        List<String> expected = Arrays.asList(names);

        assertEquals(expected, result);
    }

    @Test
    public void testRxJavaWithLambda() {
        Iterable<String> iterable = Observable.from(names).map(s -> {
            return s + "!";
        }).toBlocking().toIterable();

        List<String> result = TestUtils.toList(iterable);
        List<String> expected = Arrays.asList("1ambda!", "2ambda!", "3ambda!");

        assertEquals(expected, result);
    }

    @Test
    public void testRxJavaIntervalWithZip() {
        Iterable<String> iter = Observable
                .interval(500, TimeUnit.MILLISECONDS)
                .zipWith(Observable.just("1ambda", "2ambda"), (x, y) -> y)
                .toBlocking()
                .toIterable();

        List<String> result = TestUtils.toList(iter);
        List<String> expected = Arrays.asList("1ambda", "2ambda");

        assertEquals(expected, result);
    }

    @Test
    public void testOnErrorReturn() {

        Observable<HttpResponse> erroneous = getErroneousObservable(1 /* how many*/);
        Observable<HttpResponse> handled = erroneous
                .onErrorReturn(t -> {
                    HttpResponse res = new BasicHttpResponse(HttpVersion.HTTP_1_1, 0, null);
                    res.setStatusCode(499);
                    return res;
                });

        int result = handled.toBlocking().first().getStatusLine().getStatusCode();
        int expected = 499;

        assertEquals(expected, result);
    }

    private Observable<HttpResponse> getErroneousObservable(int number) {
        return  Observable
                .interval(1, TimeUnit.SECONDS)
                .take(number)
                .map(x -> {
                    Boolean serverError = true;
                    if (serverError) throw new RuntimeException("e");

                    HttpResponse res = TestUtils.sendHttpPost("{}");
                    return res;
                });
    }

    @Ignore
    public void defineApiSpec() {
        Rake rake = RakeFactory.getLogger(new SampleRakeConfig1());

        /**
        // RakeUserConfig 별로 Observable 하나씩 생성
        Observable<String> o = Observable
                .interval(60, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.computation())
                .map(x -> { return new RakeHttpClient().send(requestBody); })
                .filter(responseBody -> null != responseBody);

        Subscription s = o.subscribe(responseBody -> Logger.e(responseBody));
        **/
    }
}
