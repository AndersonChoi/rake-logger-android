package com.skp.di.rake.client.core;


import com.skp.di.rake.client.core.RakeCore;
import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.mock.MockRakeHttpClient;
import com.skp.di.rake.client.mock.SampleDevConfig;
import com.skp.di.rake.client.mock.SampleLiveConfig;
import com.skp.di.rake.client.persistent.RakeDaoMemory;
import com.skp.di.rake.client.persistent.RakeDaoSQLite;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import rx.Observer;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeCoreSpec {

    RakeCore liveCore;
    Observer<List<JSONObject>> liveObserver;
    RakeUserConfig liveConfig = new SampleLiveConfig();

    RakeCore devCore;
    Observer<List<JSONObject>> devObserver;
    RakeUserConfig devConfig  = new SampleDevConfig();

    int count = liveConfig.getMaxLogTrackCount();
    JSONObject log;

    @Before
    public void setUp() throws JSONException {
        ShadowLog.stream = System.out; /* Robolectric setting */

        log = new JSONObject();
        log.put("rake_lib", RakeMetaConfig.RAKE_CLIENT_VERSION);

        devCore  = new RakeCore(
                new RakeDaoMemory(),
                new MockRakeHttpClient(devConfig), devConfig);
        devObserver = mock(Observer.class);
        devCore.buildCore(
                devConfig,
                AndroidSchedulers.mainThread(),
                AndroidSchedulers.mainThread(),
                devObserver);

        liveCore = new RakeCore(
                new RakeDaoMemory(),
                new MockRakeHttpClient(liveConfig), liveConfig);
        liveObserver = mock(Observer.class);
        liveCore.buildCore(
                liveConfig,
                AndroidSchedulers.mainThread(),
                AndroidSchedulers.mainThread(),
                liveObserver);
    }

    @Test
    public void test_LiveCore_Should_Not_Flush() throws InterruptedException {
        for (int i = 0; i < count - 1; i++) liveCore.track(log);

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
        for (int i = 0; i < count; i++) liveCore.track(log);

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
    public void test_CoreWithDevConfig_Should_Flush_When_Track_Is_Called() {
        devCore.track(log);

        verify(devObserver, times(1)).onNext(any());
    }
}
