package com.skp.di.rake.client.logger;

import com.skp.di.rake.client.config.RakeMetaConfig;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, emulateSdk = 17, reportSdk = 19)
public class RakeLoggerSpecWithNetwork {

    Rake logger;
    JSONObject json ;

    MockWebServer server;

    @Before
    public void setUp() throws IOException, JSONException {
        logger = RakeFactory.getLogger(RakeLoggerSpecWithNetwork.class);
        logger.clear();
        json = new JSONObject();

        json.put("version", "0.1");

        // mock server init

        JSONObject body = new JSONObject();
        body.put("errorCode", 20000);

        server = new MockWebServer();
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(body.toString()));

        server.start(9001);
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void testFlushShouldPost() throws InterruptedException, JSONException{
        logger.track(json);
        logger.track(json);
        String flushed = logger.flush();

        RecordedRequest req = server.takeRequest();

        // header assert
        assertEquals("POST /track HTTP/1.1", req.getRequestLine());
        assertEquals("application/json", req.getHeader("Content-Type"));
        assertEquals("application/json", req.getHeader("Accept"));

        // body assert
        JSONArray dataField = new JSONArray();
        dataField.put(json);
        dataField.put(json);

        JSONObject expected = new JSONObject();
        expected.put("data", dataField);

        assertNotNull(flushed);
    }

    @Test
    public void testFlushShouldDecreaseLogCount() {
        logger.track(json);
        logger.track(json);
        assertEquals(2, logger.getCount());

        // log count assert
        logger.flush();
        assertEquals(0, logger.getCount());
    }

    @Test
    public void testFlushShouldSendWhenLogIsEmpty() throws IOException, InterruptedException {
        String flushed = logger.flush();
        assertNull(flushed);
    }

    @Test
    public void testSingletonLogger() {
        Rake logger2 = RakeFactory.getLogger(RakeLoggerSpecWithNetwork.class);
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
    public void testTrackShouldNotAllowNullLog() throws JSONException {
        logger.track(null);
        assertEquals(0, logger.getCount());

        // the json having "" as keys are treated as null log
        JSONObject nullLog = new JSONObject();
        nullLog.put("", "");
        logger.track(nullLog);

        assertEquals(0, logger.getCount());
    }

    @Test
    public void testTrackedLogCannotExceedMaxCount() {
        int maxCount = RakeMetaConfig.MAX_TRACK_COUNT;

        for (int i = 0; i < maxCount; i++) {
            logger.track(json);
        }

        // previous N log should be sent
        logger.track(json);

        assertEquals(1, logger.getCount());
    }

    @Test
    public void testResponseShouldBe200() {
        logger.track(json);


    }
}
