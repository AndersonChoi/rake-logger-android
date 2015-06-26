package com.skp.di.rake.client.core;


import android.util.Log;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.Date;
import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeCoreSpec {

    RakeCore devCore;
    RakeUserConfig devConfig = RakeTestUtils.createDevConfig1();

    RakeCore liveCore;
    RakeUserConfig liveConfig = RakeTestUtils.createLiveConfig1();

    JSONObject log;
    int failedCount;
    Random r;

    @Before
    public void setUp() throws JSONException {
        ShadowLog.stream = System.out; /* Robolectric setting */

        log = new JSONObject();
        log.put("rake_lib", RakeMetaConfig.RAKE_CLIENT_VERSION);

        devCore  = RakeTestUtils.createTestRakeCore(
                new RakeDaoMemory(),
                RakeTestUtils.createMockHttpClient(devConfig),
                devConfig);

        liveCore = RakeTestUtils.createTestRakeCore(
                new RakeDaoMemory(),
                RakeTestUtils.createMockHttpClient(liveConfig),
                liveConfig);

        /* withRetry */
        failedCount = 0;
        r = new Random(new Date().getTime());
    }

    @Test
    public void test_LiveCore_Should_Not_Flush() throws InterruptedException {
        Observable<List<JSONObject>> logStream = liveCore.getLogStream();
        Observer<List<JSONObject>> o = mock(Observer.class);
        logStream.subscribe(o);

        for (int i = 0; i < liveConfig.getMaxLogTrackCount() - 1; i++)
            liveCore.track(log);

        verify(o, never()).onNext(any());
    }

    @Test
    public void test_LiveCore_Should_Not_To_Flush_When_Empty() {
        Observable<List<JSONObject>> logStream = liveCore.getLogStream();
        Observer<List<JSONObject>> o = mock(Observer.class);
        logStream.subscribe(o);

        liveCore.flush();

        verify(o, never()).onNext(any());
        verify(o, never()).onError(any());
    }

    @Test
    public void test_LiveCore_Auto_Flush_When_Persistence_Is_Full() {
        Observable<List<JSONObject>> logStream = liveCore.getLogStream();
        Observer<List<JSONObject>> o = mock(Observer.class);
        logStream.subscribe(o);

        for (int i = 0; i < liveConfig.getMaxLogTrackCount(); i++)
            liveCore.track(log);

        verify(o, times(1)).onNext(any());
        verify(o, never()).onError(any());
    }

    @Test
    public void test_LiveCore_Should_Not_To_Flush_Immediately() {
        Observable<List<JSONObject>> logStream = liveCore.getLogStream();
        Observer<List<JSONObject>> o = mock(Observer.class);
        logStream.subscribe(o);

        liveCore.track(log);
        verify(o, never()).onNext(any());

        liveCore.flush();
        verify(o, times(1)).onNext(any());
        verify(o, never()).onError(any());
    }

    @Test
    public void test_DevCore_Should_Send_When_Track_Is_Called() {
        Observable<List<JSONObject>> logStream = devCore.getLogStream();
        Observer<List<JSONObject>> o = mock(Observer.class);
        logStream.subscribe(o);

        devCore.track(log);

        verify(o, times(1)).onNext(any());
    }

    @Test
    public void test_DevCore_Should_Disable_Flush_Command() {
        Observable<RakeCore.Command> flushStream = devCore.getFlushStream();
        Observer<RakeCore.Command> o = mock(Observer.class);

        flushStream.subscribe(o);
        devCore.flush();
        devCore.flush();

        verify(o, never()).onNext(any());
    }

    @Test
    public void test_Dev_TrackStream_Should_Return_TRACK_FULL() {
        Observable<RakeCore.Command> trackStream = devCore.getTrackStream();
        Observer<RakeCore.Command> o = mock(Observer.class);
        trackStream.subscribe(o);

        devCore.track(log);

        verify(o, times(1)).onNext(RakeCore.Command.TRACK_FULL);
    }

    @Test
    public void test_Live_TrackStream_Should_Return_TRACK_NOT_FULL() {
        // iff getMaxTrackCount > 1;

        Observable<RakeCore.Command> trackStream = liveCore.getTrackStream();
        Observer<RakeCore.Command> o = mock(Observer.class);
        trackStream.subscribe(o);

        liveCore.track(log);

        verify(o, times(1)).onNext(RakeCore.Command.TRACK_NOT_FULL);
    }



    @Ignore
    public void test_DevCore_withRetry() throws InterruptedException {
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
                RakeTestUtils.createDevConfig1());

        int count = 10;
        for (int i = 0; i < count; i++) testCore.track(new JSONObject());

        // make sure all log is sent
        assertEquals(0, dao.getCount());

        // verify whether all failed log were retried or not
        verify(observer, times(count + failedCount)).onNext(any());
    }
//
//    @Ignore
//    public void test_setFlushInterval() throws InterruptedException {
//        int interval1 = 100;
//        int expectedOnNextCallNumberOnTimer1 = 2;
//
//        int interval2 = 50; /* milliseconds */
//        int expectedOnNextCallNumberOnTimer2 = 5;
//
//        RakeUserConfig config =
//                RakeTestUtils.createRakeUserConfig(
//                        RakeUserConfig.RUNNING_ENV.LIVE,
//                        "example liveToken", "exampleDevToken",
//                        interval1, 10);
//
//        /* since we can't test full data flow chain due to filtering null dao
//           we will test timer (PublishSubject) only.
//          */
//        Observable<RakeCore.Command> timer = liveCore.getTimer();
//        Observer<RakeCore.Command> timerObserver = mock(Observer.class);
//
//        timer.subscribe(timerObserver);
//
//        Thread.sleep(interval1 * expectedOnNextCallNumberOnTimer1 + interval1 / 2);
//
//        liveCore.setFlushInterval(interval2);
//
//        Thread.sleep(interval2 * expectedOnNextCallNumberOnTimer2 + interval2 / 2);
//
//        // verify total call number of onNext()
//        int total = expectedOnNextCallNumberOnTimer1 + expectedOnNextCallNumberOnTimer2;
//        // because the interval observable startsWith(-1L),
//        // we need to add +1 to total number
//        total += 1;
//        verify(timerObserver, times(total)).onNext(any());
//    }
}
