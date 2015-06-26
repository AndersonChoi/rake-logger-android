package library.test.rxjava;

import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.persistent.RakeDaoMemory;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class RxJavaSpec2 {

    Observable<Integer> timer;
    PublishSubject<Integer> flushable;
    PublishSubject<JSONObject> trackable1;
    PublishSubject<JSONObject> trackable2;
    Observable<JSONObject> trackable;
    Observable<Integer> flushCommanded;
    Observer<Integer> observer1;
    Observer<Integer> observer2;

    int interval = 100; /* milliseconds */
    final int MAX_SAVE_COUNT = 3;
    RakeDao dao;

    @Before
    public void setUp() {
        dao = new RakeDaoMemory();

        timer = Observable
                .interval(interval, TimeUnit.MILLISECONDS).map(x -> 0)
                .startWith(-1);
        trackable1 = PublishSubject.create(); /* created per RakeUserConfig */
        trackable2 = PublishSubject.create();
        flushable = PublishSubject.create();  /* a sort of flush button */
        int interval = 100; /* milliseconds */

        trackable = trackable1.mergeWith(trackable2);

        observer1 = mock(Observer.class);
        observer2 = mock(Observer.class);

        /* flushable should be once started, since we are using combineLatest */
        flushCommanded = timer.mergeWith(flushable);
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
        timer.subscribe(observer1);

        Thread.sleep(3 * interval - 50); // emit 1 + 2 times because of `startWith`

        verify(observer1, times(1 + 2)).onNext(any());
    }

    @Test
    public void testZipTrackable1() {
        Observer<JSONObject> observer = mock(Observer.class);

        trackable2.onNext(null);

        trackable.subscribe(observer);

        trackable1.onNext(null);
        trackable2.onNext(null);

        verify(observer, times(2)).onNext(any());
    }



    @Test
    public void testWhenTrackableAdded() {

        Observer<String> o1 = mock(Observer.class);
        Observer<String> o2 = mock(Observer.class);

        Subscription s = trackable
                .map(x -> "first")
                .subscribe(o1);

        trackable1.onNext(null);
        s.unsubscribe();

        PublishSubject<JSONObject> trackable3 = PublishSubject.create();
        Observable<JSONObject> newTrackable = trackable.mergeWith(trackable3);

        newTrackable
                .map(x -> "second")
                .subscribe(o2);

        trackable1.onNext(null);
        trackable3.onNext(null);

        // verify
        verify(o1, times(1)).onNext("first");
        verify(o2, times(2)).onNext("second");
    }

    @Test
    public void testErrorHandling() {
        // TODO
    }

    @Test
    public void testSubjectMerged() {
        Observer<JSONObject> o1 = mock(Observer.class);
        Observer<JSONObject> o2 = mock(Observer.class);

        trackable.subscribe(o1);

        trackable1.onNext(null);
        trackable2.onNext(null);

        verify(o1, times(2)).onNext(any());
        verify(o2, never()).onNext(any());

        Observer<JSONObject> o3 = mock(Observer.class);
    }

    @Test
    public void test_ColdObservable() {

        PublishSubject<Integer> a = PublishSubject.create();
        PublishSubject<Integer> b = PublishSubject.create();

        Observable<Integer> c = a
                .mergeWith(b)
                .map(x -> {
                    // x will be printed twice
                    System.out.println("x: " + x);
                    return x;
                });

        c.subscribe(x -> {
            System.out.println("first");
        });

        c.subscribe(x -> {
            System.out.println("second");
        });

        a.onNext(3);
    }

}
