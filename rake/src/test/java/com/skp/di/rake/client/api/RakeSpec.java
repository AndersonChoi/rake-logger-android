package com.skp.di.rake.client.api;

import com.skp.di.rake.client.api.impl.RakeCore;
import com.skp.di.rake.client.api.impl.RakeImpl;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.mock.SampleRakeConfig1;
import com.skp.di.rake.client.protocol.ShuttleProtocol;
import com.skp.di.rake.client.utils.Logger;
import com.skplanet.pdp.sentinel.shuttle.AppSampleSentinelShuttle;
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
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeSpec {

    Rake rake;
    JSONObject json ;
    RakeUserConfig config;
    RakeCore mockCore;

    AppSampleSentinelShuttle shuttle;

    @Before
    public void setUp() throws IOException, JSONException {
        ShadowLog.stream = System.out; /* Robolectric setting */
        config = new SampleRakeConfig1();
        mockCore = mock(RakeCore.class);
        rake = new RakeImpl(config, mockCore);

        json = new JSONObject();
        json.put("rake_lib", RakeMetaConfig.RAKE_CLIENT_VERSION);

        shuttle = new AppSampleSentinelShuttle();
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

    @Test
    public void testTrackedJsonHasPropertiesAndBodyField() throws JSONException {
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);

        rake.track(shuttle.toJSONObject());
        verify(mockCore, times(1)).track(captor.capture());

        // verify tracked has `properties` field
        JSONObject tracked = captor.getValue();
        assertTrue(tracked.has(ShuttleProtocol.FIELD_NAME_PROPERTIES));

        // verify tracked has `properties._$body` field
        JSONObject properties = tracked.getJSONObject(ShuttleProtocol.FIELD_NAME_PROPERTIES);
        assertTrue(properties.has(ShuttleProtocol.FIELD_NAME_BODY));

    }

    /* below tests dependent on Shuttle Type */
    @Test
    public void testTrackedJsonHasBodyField() throws JSONException {
        ArgumentCaptor<JSONObject> captor = ArgumentCaptor.forClass(JSONObject.class);

        String field3Value = "field3 value";
        String field4Value = "field4 value";

        shuttle.field3(field3Value);
        shuttle.field4(field4Value);

        rake.track(shuttle.toJSONObject());
        verify(mockCore, times(1)).track(captor.capture());

        JSONObject tracked = captor.getValue();
        JSONObject properties = tracked.getJSONObject(ShuttleProtocol.FIELD_NAME_PROPERTIES);
        JSONObject _$body = properties.getJSONObject(ShuttleProtocol.FIELD_NAME_BODY);

        assertEquals(field3Value, _$body.getString("field3"));
        assertEquals(field4Value, _$body.getString("field4"));
    }
}
