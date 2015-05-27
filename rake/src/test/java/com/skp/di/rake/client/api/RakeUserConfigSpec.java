package com.skp.di.rake.client.api;

import com.skp.di.rake.api.Rake;
import com.skp.di.rake.api.RakeFactory;
import com.skp.di.rake.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.skp.di.rake.client.mock.SampleRakeConfig1;
import com.skp.di.rake.client.mock.SampleRakeConfig2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class RakeUserConfigSpec {


    @Test
    public void testConfigShouldReturnProperTokenByRunningMode() {
        RakeUserConfig config = new SampleRakeConfig1();

        assertEquals(RakeUserConfig.Mode.DEV, config.getRunningMode());
        assertEquals(config.getDevToken(), config.getToken());
    }

    @Test
    public void testFlushIntervalCanBeConfiguredByUser() {
        RakeUserConfig config = new SampleRakeConfig1();

        assertEquals(
                RakeMetaConfig.DEFAULT_FLUSH_INTERVAL,
                RakeUserConfig.getFlushInterval());

        RakeUserConfig.setFlushInterval(20);
        assertEquals(20, RakeUserConfig.getFlushInterval());

        /* sub classes are also to modify flush interval */
        SampleRakeConfig1.setFlushInterval(10);
        assertEquals(10, config.getFlushInterval());
        assertEquals(10, RakeUserConfig.getFlushInterval());
    }

    @Test
    public void testRakeUserConfigEquals() {
        RakeUserConfig config1 = new SampleRakeConfig1();
        RakeUserConfig config2 = new SampleRakeConfig2();

        assertTrue(config1.equals(config1));
        assertFalse(config1.equals(config2));
    }

    @Test
    public void testRakeIsSingletonPerUserConfig() {
        // TODO
        RakeUserConfig config1 = new SampleRakeConfig1();
        RakeUserConfig config2 = new SampleRakeConfig2();

        Rake loggerSame1 = RakeFactory.getLogger(config1);
        Rake loggerSame2 = RakeFactory.getLogger(config1);
        Rake loggerDifferent = RakeFactory.getLogger(config2);

        // if rake has same config, it should always be same instance
        assertTrue(loggerSame1 == loggerSame2);
        assertFalse(loggerSame1 == loggerDifferent);
        assertFalse(loggerSame2 == loggerDifferent);
    }
}


