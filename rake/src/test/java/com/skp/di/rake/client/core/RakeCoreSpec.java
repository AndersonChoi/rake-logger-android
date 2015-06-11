package com.skp.di.rake.client.core;


import com.skp.di.rake.client.core.RakeCore;
import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.mock.MockRakeHttpClient;
import com.skp.di.rake.client.mock.SampleDevConfig;
import com.skp.di.rake.client.mock.SampleLiveConfig;
import com.skp.di.rake.client.persistent.RakeDaoMemory;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

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

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeCoreSpec {

    RakeCore liveCore;
    Observer<String> liveObserver;
    RakeUserConfig liveConfig = new SampleLiveConfig();

    RakeCore devCore;
    Observer<String> devObserver;
    RakeUserConfig devConfig  = new SampleDevConfig();

    int count = liveConfig.getMaxLogTrackCount();
    JSONObject log;

    @Before
    public void setUp() throws JSONException {
        ShadowLog.stream = System.out; /* Robolectric setting */

        log = new JSONObject();
        log.put("rake_lib", RakeMetaConfig.RAKE_CLIENT_VERSION);

        devCore  = new RakeCore(new RakeDaoMemory(), new MockRakeHttpClient(null), devConfig);
        devObserver = mock(Observer.class);
        devCore.subscribe(AndroidSchedulers.mainThread(), devObserver);

        liveCore = new RakeCore(new RakeDaoMemory(), new MockRakeHttpClient(null), liveConfig);
        liveObserver = mock(Observer.class);
        liveCore.subscribe(AndroidSchedulers.mainThread(), liveObserver);
    }

    @Test
    public void testTrackShouldIncreaseLogCount() throws InterruptedException {
        for (int i = 0; i < count - 1; i++) liveCore.track(log);

        assertEquals(count - 1, liveCore.getLogCount());
    }

    @Test
    public void testFlush() {
        liveCore.track(log);
        assertEquals(1, liveCore.getLogCount());

        liveCore.flush();
        assertEquals(0, liveCore.getLogCount());

        verify(liveObserver, times(1)).onNext(any());
        verify(liveObserver, never()).onError(any());
    }

    @Test
    public void testAutoFlush() {
        for (int i = 0; i < count; i++) liveCore.track(log);

        assertEquals(0, liveCore.getLogCount());
        verify(liveObserver, times(1)).onNext(any());
        verify(liveObserver, never()).onError(any());
    }

    @Test
    public void testShouldNotFlushWhenEmpty() {
        assertEquals(0, liveCore.getLogCount());

        liveCore.flush();

        verify(liveObserver, never()).onNext(any());
        verify(liveObserver, never()).onError(any());
    }

    @Test
    public void testCoreWithDevConfigShouldFlushWhenTrackCalled() {
        assertEquals(0, devCore.getLogCount());

        devCore.track(log);

        verify(devObserver, times(1)).onNext(any());
    }
}
