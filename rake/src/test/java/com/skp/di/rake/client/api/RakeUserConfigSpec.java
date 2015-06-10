package com.skp.di.rake.client.api;

import android.content.Context;

import com.skp.di.rake.client.android.SystemInformation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.skp.di.rake.client.mock.SampleDevConfig;
import com.skp.di.rake.client.mock.SampleDevConfig2;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeUserConfigSpec {

    SystemInformation mockSysInfo;
    Context mockContext;

    @Before
    public void setUp() {
        mockSysInfo = mock(SystemInformation.class);
        mockContext = mock(Context.class);
    }

    @Test
    public void testConfigShouldReturnProperTokenByRunningMode() {
        RakeUserConfig config = new SampleDevConfig2();

        assertEquals(RakeUserConfig.RUNNING_ENV.DEV, config.getRunningMode());
        assertEquals(config.getDevToken(), config.getToken());
    }

    @Test
    public void testRakeUserConfigEquals() {
        RakeUserConfig config1 = new SampleDevConfig();
        RakeUserConfig config2 = new SampleDevConfig2();

        assertTrue(config1.equals(config1));
        assertFalse(config1.equals(config2));
    }

    class TestLogger {}

    @Test
    public void testRakeIsSingletonPerUserConfig() {
        RakeUserConfig config1 = new SampleDevConfig();
        RakeUserConfig config2 = new SampleDevConfig2();

        // Can't test using Rake instance due to Context
        HashMap<RakeUserConfig, TestLogger> map = new HashMap<>();
        map.put(config1, new TestLogger());
        map.put(config2, new TestLogger());

        TestLogger logger = map.get(config1);
        TestLogger loggerSame = map.get(config1);
        TestLogger loggerDiff = map.get(config2);

        // if rake has same config, it should always be same instance
        assertTrue(logger == loggerSame);
        assertFalse(loggerDiff == logger);
        assertFalse(loggerDiff == loggerSame);
    }
}


