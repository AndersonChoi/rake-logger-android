package com.skp.di.rake.client.logger;

import com.skp.di.rake.client.config.RakeUserConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import mock.AppRakeConfig;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class RakeUserConfigSpec {


    @Test
    public void testConfigShouldImplRakeUserConfig() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        RakeUserConfig config = new AppRakeConfig();
        Rake logger = RakeFactory.getLogger("token", config);

        assertEquals(RakeUserConfig.Mode.DEV, config.getRunningMode());
    }
}


