package com.skp.di.rake.client.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class RakeCoreSpec {

    Observable<Long> timer;
    PublishSubject<Integer> flushable;
    PublishSubject<Long> trackable;
    Observable<Integer> empty = Observable.just(1);
    int value = 0;

    /* emit item per 200 milliseconds or when flushed */
    Observable<Integer> combined;

    Observer<Integer> observer1;
    Observer<Integer> observer2;

    @Before
    public void setUp() {
        timer = Observable.interval(200, TimeUnit.MILLISECONDS);
        trackable = PublishSubject.create();
        flushable = PublishSubject.create();

        observer1 = mock(Observer.class);
        observer2 = mock(Observer.class);

        /* flushable should be once started, since we are using combineLatest */
        combined = Observable.combineLatest(timer, flushable.startWith(value), (x, y) -> value);
    }

    @Test
    public void testZipEmptyAndFlush() throws InterruptedException {
        Observable<Integer> o = Observable.combineLatest(empty, flushable, (x, y) -> value);

        o.subscribe(observer1);

        flushable.onNext(null);

        o.subscribe(observer2);

        flushable.onNext(null);
        flushable.onCompleted();

        flushable.onNext(null);

        // verify `o` is hot observable
        verify(observer1, times(2)).onNext(value);
        verify(observer1, times(1)).onCompleted();

        verify(observer2, times(1)).onNext(value);
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
    public void testCombinedIsHot() throws InterruptedException {

        combined.subscribe(observer1);

        Thread.sleep(650); // emit 3 times (200 * 3 == 600)

        combined.subscribe(observer2);

        verify(observer1, times(3)).onNext(value);
        verify(observer2, never()).onNext(value);
    }

//    @Test
//    public void testCombinedWorks() throws InterruptedException {
//    }

}
