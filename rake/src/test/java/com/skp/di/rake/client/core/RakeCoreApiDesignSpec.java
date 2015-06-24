package com.skp.di.rake.client.core;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeCoreApiDesignSpec {

    private PublishSubject<Object> timer;
    private Subscription intervalSubscription;

    @BeforeClass
    static public void setUpBeforeClass() {
        ShadowLog.stream = System.out;
    }

    @Before
    public void setUp() {
        timer = PublishSubject.create();
    }

    @Test
    public void design_Timer_API() throws InterruptedException {
        Observer<Object> intervalObserver1 = mock(Observer.class);
        Observer<Object> intervalObserver2 = mock(Observer.class);
        Observer<Object> timerObserver = mock(Observer.class);
        timer.subscribe(timerObserver);

        int interval1 = 50;
        int expectedTimes1 = 4;
        setFlushInterval(interval1, intervalObserver1);

        Thread.sleep(interval1 * expectedTimes1 + interval1 / 2);
        // observer1: 4 onNext() calls
        // observer2: 0 onNext() calls

        int interval2 = 100;
        int expectedTimes2 = 2;
        setFlushInterval(interval2, intervalObserver2);

        Thread.sleep(interval2 * expectedTimes2 + interval2 / 2);
        // observer1: 0 onNext() calls
        // observer2: 2 onNext() calls

        // total:
        // observer1: 4 onNext() calls
        // observer2: 2 onNext() calls
        verify(intervalObserver1, times(expectedTimes1)).onNext(any());
        verify(intervalObserver2, times(expectedTimes2)).onNext(any());
        verify(timerObserver, times(expectedTimes1 + expectedTimes2)).onNext(any());
    }

    public void setFlushInterval(int milliseconds, Observer<Object> o) {
        if (null != intervalSubscription)
            intervalSubscription.unsubscribe();

        intervalSubscription = Observable
                .interval(milliseconds, TimeUnit.MILLISECONDS)
                .map(anyThing -> {
                    timer.onNext(null);
                    return anyThing;
                })
                // TODO onErrorReturn, Observer
                .subscribe(o);
    }
}
