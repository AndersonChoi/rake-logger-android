package com.skp.di.rake.client.api;

import com.skp.di.rake.client.config.RakeMetaConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.skp.di.rake.client.mock.SampleRakeConfig1;
import com.skp.di.rake.client.mock.SampleRakeConfig2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeUserConfigSpec {
    @Test
    public void testConfigShouldReturnProperTokenByRunningMode() {
        RakeUserConfig config = new SampleRakeConfig1();

        assertEquals(RakeUserConfig.Mode.DEV, config.getRunningMode());
        assertEquals(config.getDevToken(), config.getToken());
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


