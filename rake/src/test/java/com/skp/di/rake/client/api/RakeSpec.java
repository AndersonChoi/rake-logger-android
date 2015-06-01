package com.skp.di.rake.client.api;

import com.skp.di.rake.client.api.impl.RakeCore;
import com.skp.di.rake.client.api.impl.RakeImpl;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.mock.SampleRakeConfig1;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeSpec {

    Rake rake;
    JSONObject json ;
    RakeUserConfig config;
    RakeCore mockCore;

    @Before
    public void setUp() throws IOException, JSONException {
        config = new SampleRakeConfig1();
        mockCore = mock(RakeCore.class);
        rake = new RakeImpl(config, mockCore);

        json = new JSONObject();
        json.put("rake_lib", RakeMetaConfig.RAKE_CLIENT_VERSION);
    }

    @Test
    public void testShouldNotTrackNullAndEmptyLog() throws JSONException {
        rake.track(null);

        // the json having "" as keys are treated as null log
        JSONObject empty = new JSONObject();
        empty.put("", "");
        rake.track(empty);

        verify(mockCore, never()).track(any());
    }
}
