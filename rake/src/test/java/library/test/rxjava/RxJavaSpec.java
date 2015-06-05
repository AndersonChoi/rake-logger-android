package library.test.rxjava;

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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
                .interval(100, TimeUnit.MILLISECONDS)
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

    @Test
    public void testPublishSubject() {
        PublishSubject<String> subject = PublishSubject.create();
        Observer observer = mock(Observer.class);
        subject.subscribe(observer);

        subject.onNext("1ambda");
        subject.onNext("2ambda");
        subject.onCompleted();
        subject.onNext("3ambda");

        verify(observer, times(1)).onNext("1ambda");
        verify(observer, times(1)).onNext("2ambda");
        verify(observer, never()).onNext("3ambda");
        verify(observer, times(1)).onCompleted();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void testColdObservable() throws InterruptedException {
        Observable<String> cold = Observable.just("1ambda", "2ambda", "3ambda");

        Observer observer1 = mock(Observer.class);
        cold.subscribe(observer1);

        Thread.sleep(100);

        Observer observer2 = mock(Observer.class);
        cold.subscribe(observer2);

        // verify
        verify(observer1, times(1)).onNext("1ambda");
        verify(observer1, times(1)).onNext("2ambda");
        verify(observer1, times(1)).onNext("3ambda");
        verify(observer1, times(1)).onCompleted();

        verify(observer2, times(1)).onNext("1ambda");
        verify(observer2, times(1)).onNext("2ambda");
        verify(observer2, times(1)).onNext("3ambda");
        verify(observer2, times(1)).onCompleted();
    }

    @Test
    public void testColdObservableInterval() throws InterruptedException {
        Observable<Long> cold = Observable
                .interval(50, TimeUnit.MILLISECONDS)
                .take(2);

        Observer observer1 = mock(Observer.class);
        cold.subscribe(observer1);

        Thread.sleep(120);

        Observer observer2 = mock(Observer.class);
        cold.subscribe(observer2);

        Thread.sleep(120);

        // verify
        verify(observer1, times(1)).onNext(0L);
        verify(observer1, times(1)).onNext(1L);
        verify(observer1, times(1)).onCompleted();

        verify(observer2, times(1)).onNext(0L);
        verify(observer2, times(1)).onNext(1L);
        verify(observer2, times(1)).onCompleted();
    }

    @Test
    public void testSubjectIsHot() {
        PublishSubject<String> s = PublishSubject.create();
        Observer<String> o1 = mock(Observer.class);
        Observer<String> o2 = mock(Observer.class);

        s.subscribe(o1);
        s.onNext("first");

        s.subscribe(o2);
        s.onNext("second");

        s.onCompleted();

        verify(o1, times(1)).onNext("first");
        verify(o1, times(1)).onNext("second");
        verify(o1, times(1)).onCompleted();

        verify(o2, never()).onNext("first");
        verify(o2, times(1)).onNext("second");
        verify(o2, times(1)).onCompleted();
    }

    @Test
    public void testConvertSubjectToObservable() {
        ReplaySubject<String> subject = ReplaySubject.create();
        Observable<Long> interval = Observable.interval(100, TimeUnit.MILLISECONDS);

        Observable<String> o = interval.zipWith(subject, (x, y) -> {
            return y;
        });

        subject.onNext("1ambda");
        subject.onNext("2ambda");
        subject.onCompleted();

        Iterable<String> iter = o.toBlocking().toIterable();
        List<String> result   = TestUtils.toList(iter);
        List<String> expected = Arrays.asList("1ambda", "2ambda");

        assertEquals(expected, result);
    }


    private Observable<HttpResponse> getErroneousObservable(int number) {
        return  Observable
                .interval(100, TimeUnit.MILLISECONDS)
                .take(number)
                .map(x -> {
                    Boolean serverError = true;
                    if (serverError) throw new RuntimeException("e");

                    HttpResponse res = TestUtils.sendHttpPost("{}");
                    return res;
                });
    }

}
