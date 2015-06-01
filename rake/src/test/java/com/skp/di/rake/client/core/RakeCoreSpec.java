package com.skp.di.rake.client.core;

import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.persistent.RakeDaoMemory;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;

import java_cup.emit;
import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class RakeCoreSpec {

    Observable<Long> timer;
    PublishSubject<Long> flushable;
    PublishSubject<JSONObject> trackable1;
    PublishSubject<JSONObject> trackable2;
    Observable<Integer> empty = Observable.just(1);
    int interval = 100; /* milliseconds */

    RakeDao dao;

    /* emit item per `interval` milliseconds or when flushed */
    Observable<Long> combined;

    Observer<Long> observer1;
    Observer<Long> observer2;

    @Before
    public void setUp() {
        dao = new RakeDaoMemory();

        timer = Observable.interval(interval, TimeUnit.MILLISECONDS);
        trackable1 = PublishSubject.create();
        trackable2 = PublishSubject.create();
        flushable = PublishSubject.create();

        observer1 = mock(Observer.class);
        observer2 = mock(Observer.class);

        /* flushable should be once started, since we are using combineLatest */
        combined = timer.mergeWith(flushable);
    }

    @Test
    public void testCombineEmptyAndFlush() throws InterruptedException {
        Observable<Long> o = Observable.combineLatest(empty, flushable, (x, y) -> 0L);

        o.subscribe(observer1);

        flushable.onNext(null);

        o.subscribe(observer2);

        flushable.onNext(null);
        flushable.onCompleted();

        flushable.onNext(null);

        // verify `o` is hot observable
        verify(observer1, times(2)).onNext(any());
        verify(observer1, times(1)).onCompleted();

        verify(observer2, times(1)).onNext(any());
        verify(observer2, times(1)).onCompleted();
    }

    /* we can wrap onNext like */
    private void flush() {
        flushable.onNext(null);
    }

    private void done() {
        flushable.onCompleted();
    }

    @Test
    public void testTimer() throws InterruptedException {
        timer.startWith(-1L).subscribe(observer1);

        Thread.sleep(3 * interval - 50); // emit 1 + 2 times because of `startWith`

        verify(observer1, times(1 + 2)).onNext(any());
    }

    @Test
    public void testCombinedWorks1() throws InterruptedException {
        combined.subscribe(observer1);

        flushable.onNext(null); /* emit 1 item */

        Thread.sleep(3 * interval - 50); /* emit 2 items */

        flushable.onNext(null); /* emit 1 item */

        verify(observer1, times(2 + 2)).onNext(any());
    }

    @Test
    public void testCombinedWorks2() throws InterruptedException {
        combined.startWith(-1L).subscribe(observer1); /* emit 1 item */

        flushable.onNext(null); /* emit 1 item */

        Thread.sleep(3 * interval - 50); /* emit 2 items */

        flushable.onNext(null); /* emit 1 item */

        verify(observer1, times(3 + 2)).onNext(any());
    }

    @Test
    public void testZipTrackable1() {
        Observable<JSONObject> trackable = trackable1.mergeWith(trackable2);
        Observer<JSONObject> observer = mock(Observer.class);

        trackable2.onNext(null);

        trackable.subscribe(observer);

        trackable1.onNext(null);
        trackable2.onNext(null);

        verify(observer, times(2)).onNext(any());
    }

    @Test
    public void testApiDesign1() {
        /* trackable is a hot observable */
        Observable<JSONObject> trackable = trackable1.mergeWith(trackable2);

        Observable<Long> shouldBeFlushed = trackable
                .map(log -> {
                    dao.add(log);
                    return dao.getCount();
                })
                .filter(count -> count >= 5).map(log -> 0L) /* map is used to convert type */
                .mergeWith(combined);

        Observable<String> flushed = shouldBeFlushed.map(x -> {
            List<JSONObject> logList = dao.clear();
            return logList.toString();
        });
    }

    @Test
    public void testDesiredApiDesign() {
        /*
            trackable
            .map(log -> {dao.add(log); return.dao.getCount(); })
            .filter(count -> count >= 5)
            .mergeWith(flushCommanded)
            .map(x -> {
              List<JSONObject> logs = dao.clean();
              rakeHttpClient.send(logs);
            })
            .onErrorReturn(TODO);
         */
    }
}
