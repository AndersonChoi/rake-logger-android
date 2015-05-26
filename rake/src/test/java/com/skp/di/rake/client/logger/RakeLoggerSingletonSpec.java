package com.skp.di.rake.client.logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeLoggerSingletonSpec {

    private Rake logger;
    private final String TOKEN = "token1";

    @Before
    public void setUp() {
        logger = RakeFactory.getLogger(TOKEN, null);
    }

    @Test
    public void testSingletonLoggerPerToken() {
        Rake logger2 = RakeFactory.getLogger(TOKEN, null);
        assertTrue(logger == logger2);

        String otherToken = "token2";
        Rake logger3 = RakeFactory.getLogger(otherToken, null);
        assertFalse(logger == logger3);
    }
}
