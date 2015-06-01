package com.skp.di.rake.client.api.impl;


import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.mock.MockRakeHttpClient;
import com.skp.di.rake.client.mock.SampleRakeConfig1;
import com.skp.di.rake.client.mock.SampleRakeConfig2;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
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

    RakeUserConfig config = new SampleRakeConfig1();
    RakeCore core = RakeCore.getInstance(
            new RakeDaoMemory(), new MockRakeHttpClient(), config);

    int count = config.getMaxLogTrackCount();
    JSONObject log;
    Observer<String> testObserver;

    @Before
    public void setUp() throws JSONException {
        ShadowLog.stream = System.out; /* Robolectric setting */

        log = new JSONObject();
        log.put("rake_lib", RakeMetaConfig.RAKE_CLIENT_VERSION);

        core.clearLog(); /* important, since core is singleton it can't be re-created */
        testObserver = mock(Observer.class);
        core.subscribeOnTest(testObserver);
    }

    @Test
    public void testRakeCoreSingleton() {
        RakeCore core1 = RakeCore.getInstance(null, null, config);
        RakeCore core2 = RakeCore.getInstance(null, null, new SampleRakeConfig2());

        assertTrue(core1 == core2);
    }

    @Test
    public void testTrackShouldIncreaseLogCount() throws InterruptedException {
        for (int i = 0; i < count - 1; i++) core.track(log);

        assertEquals(count - 1, core.getLogCount());
    }

    @Test
    public void testFlush() {
        core.track(log);
        assertEquals(1, core.getLogCount());

        core.flush();
        assertEquals(0, core.getLogCount());

        verify(testObserver, times(1)).onNext(any());
        verify(testObserver, never()).onError(any());
    }

    @Test
    public void testAutoFlush() {
        for (int i = 0; i < count; i++) core.track(log);

        assertEquals(0, core.getLogCount());
        verify(testObserver, times(1)).onNext(any());
        verify(testObserver, never()).onError(any());
    }

    @Test
    public void testShouldNotFlushWhenEmpty() {
        assertEquals(0, core.getLogCount());

        core.flush();

        verify(testObserver, never()).onNext(any());
        verify(testObserver, never()).onError(any());
    }
}
