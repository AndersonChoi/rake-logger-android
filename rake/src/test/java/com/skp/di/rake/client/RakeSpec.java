package com.skp.di.rake.client;

import com.skp.di.rake.client.logger.Rake;
import com.skp.di.rake.client.logger.RakeFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, emulateSdk = 19, reportSdk = 19)
public class RakeSpec {

    Rake logger;
    JSONObject json ;

    @Before
    public void setUp() {
        logger = RakeFactory.getLogger(RakeSpec.class);
        logger.clear();
        json = new JSONObject();

        try {
            json.put("version", "0.1");
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSingletonLogger() {
        Rake logger2 = RakeFactory.getLogger(RakeSpec.class);
        assertTrue(logger == logger2);

        Rake logger3 = RakeFactory.getLogger(Integer.class);
        assertFalse(logger == logger3);
    }

    @Test
    public void testTrack() {
        logger.track(json);
        assertEquals(1, logger.getCount());

        logger.flush();
        assertEquals(0, logger.getCount());
    }

    @Test
    public void testFlush() {
        logger.track(json);
    }
}
