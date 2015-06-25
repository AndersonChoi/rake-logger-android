package com.skp.di.rake.client.core;


import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.persistent.RakeDaoMemory;
import com.skp.di.rake.client.utils.RakeTestUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeCoreSpec {

    RakeCore devCore;
    Observer<List<JSONObject>> devObserver;
    RakeUserConfig devConfig = RakeTestUtils.createDevConfig1();

    RakeCore liveCore;
    Observer<List<JSONObject>> liveObserver;
    RakeUserConfig liveConfig = RakeTestUtils.createLiveConfig1();

    JSONObject log;
    int failedCount;
    Random r;

    @Before
    public void setUp() throws JSONException {
        ShadowLog.stream = System.out; /* Robolectric setting */

        log = new JSONObject();
        log.put("rake_lib", RakeMetaConfig.RAKE_CLIENT_VERSION);

        devObserver = mock(Observer.class);
        devCore  = RakeTestUtils.createTestRakeCore(
                new RakeDaoMemory(),
                RakeTestUtils.createMockHttpClient(devConfig),
                devConfig,
                devObserver
        );

        devCore.setTestObserverAndScheduler(AndroidSchedulers.mainThread(), devObserver);

        liveCore = new RakeCore(
                new RakeDaoMemory(),
                RakeTestUtils.createMockHttpClient(liveConfig), liveConfig);
        liveObserver = mock(Observer.class);
        liveCore.setTestObserverAndScheduler(AndroidSchedulers.mainThread(), liveObserver);

        /* withRetry */
        failedCount = 0;
        r = new Random(new Date().getTime());
    }

    @Test
    public void test_LiveCore_Should_Not_Flush() throws InterruptedException {
        for (int i = 0; i < liveConfig.getMaxLogTrackCount() - 1; i++) liveCore.track(log);
        verify(liveObserver, never()).onNext(any());
    }

    @Test
    public void test_Not_To_fLush_Immediately_When_Live_Env() {
        liveCore.track(log);
        verify(liveObserver, never()).onNext(any());

        liveCore.flush();
        verify(liveObserver, times(1)).onNext(any());
        verify(liveObserver, never()).onError(any());
    }

    @Test
    public void test_Auto_Flush_When_Persistence_Is_Full() {
        for (int i = 0; i < liveConfig.getMaxLogTrackCount(); i++)
            liveCore.track(log);

        verify(liveObserver, times(1)).onNext(any());
        verify(liveObserver, never()).onError(any());
    }

    @Test
    public void test_Not_To_Flush_When_Empty() {
        liveCore.flush();

        verify(liveObserver, never()).onNext(any());
        verify(liveObserver, never()).onError(any());
    }

    @Test
    public void test_RakeCore_With_DevConfig_Should_Flush_When_Track_Is_Called() {
        devCore.track(log);

        verify(devObserver, times(1)).onNext(any());
    }

    @Test
    public void test_RakeCore_withRetry_when_RUNNING_MODE_is_DEV() throws InterruptedException {
        RakeDao dao = new RakeDaoMemory();
        RakeHttpClient unstableClient = mock(RakeHttpClient.class);

        when(unstableClient.send(any())).then(x -> {
            /* randomly generate failure */
            if (r.nextBoolean()) { /* failed */
                failedCount += 1;
                return x.getArgumentAt(0, List.class);
            }

            /* success */
            return null;
        });

        Observer<List<JSONObject>> observer = mock(Observer.class);

        RakeCore testCore = RakeTestUtils.createTestRakeCore(
                dao,
                unstableClient,
                RakeTestUtils.createDevConfig1(),
                observer);

        testCore.setTestObserverAndScheduler(AndroidSchedulers.mainThread(), observer);

        int count = 10;
        for (int i = 0; i < count; i++) testCore.track(new JSONObject());

        // make sure all log is sent
        assertEquals(0, dao.getCount());

        // verify whether all failed log were retried or not
        verify(observer, times(count + failedCount)).onNext(any());
    }

    @Test
    public void test_setFlushInterval() throws InterruptedException {
        int interval1 = 100;
        int expectedOnNextCallNumberOnTimer1 = 2;

        int interval2 = 50; /* milliseconds */
        int expectedOnNextCallNumberOnTimer2 = 5;

        RakeUserConfig config =
                RakeTestUtils.createRakeUserConfig(
                        RakeUserConfig.RUNNING_ENV.LIVE,
                        "example liveToken", "exampleDevToken",
                        interval1, 10);

        /* since we can't test full data flow chain due to filtering null dao
           we will test timer (PublishSubject) only.
          */
        Observable<List<JSONObject>> timer = liveCore.getTimer();
        Observer<List<JSONObject>> timerObserver = mock(Observer.class);

        timer.subscribe(timerObserver);

        Thread.sleep(interval1 * expectedOnNextCallNumberOnTimer1 + interval1 / 2);

        liveCore.setFlushInterval(interval2);

        Thread.sleep(interval2 * expectedOnNextCallNumberOnTimer2 + interval2 / 2);

        // verify total call number of onNext()
        int total = expectedOnNextCallNumberOnTimer1 + expectedOnNextCallNumberOnTimer2;
        // because the interval observable startsWith(-1L),
        // we need to add +1 to total number
        total += 1;
        verify(timerObserver, times(total)).onNext(any());
    }
}
