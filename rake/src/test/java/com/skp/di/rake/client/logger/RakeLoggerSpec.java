package com.skp.di.rake.client.logger;


import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class RakeLoggerSpec {

    Rake logger;
    JSONObject json ;

    @Before
    public void setUp() throws IOException, JSONException {
        logger = RakeFactory.getLogger(RakeLoggerFlushSpec.class);
        logger.clear();
        json = new JSONObject();

        json.put("version", "0.1");
    }

    @Test
    public void testSingletonLogger() {
        Rake logger2 = RakeFactory.getLogger(RakeLoggerFlushSpec.class);
        assertTrue(logger == logger2);

        Rake logger3 = RakeFactory.getLogger(Integer.class);
        assertFalse(logger == logger3);
    }

    @Test
    public void testTrackShouldIncreaseLogCount() {
        logger.track(json);
        assertEquals(1, logger.getCount());
    }

    @Test
    public void testTrackShouldNotAllowTrackNullLog() throws JSONException {
        logger.track(null);
        assertEquals(0, logger.getCount());

        // the json having "" as keys are treated as null log
        JSONObject nullLog = new JSONObject();
        nullLog.put("", "");
        System.out.println(nullLog.toString());
        logger.track(nullLog);

        assertEquals(0, logger.getCount());
    }

    @Test
    public void testRakeDaoShouldTrackMaxNLog() {
        int maxCount = 5;

    }
}

