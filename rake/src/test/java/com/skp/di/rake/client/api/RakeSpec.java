package com.skp.di.rake.client.api;

import com.skp.di.rake.client.android.SystemInformation;
import com.skp.di.rake.client.api.impl.RakeCore;
import com.skp.di.rake.client.api.impl.RakeImpl;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.mock.MockSystemInformation;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeSpec {

    Rake rake;
    JSONObject json ;
    RakeUserConfig config;
    RakeCore mockCore;

    AppSampleSentinelShuttle shuttle;
    SystemInformation mockSysInfo;

    @Before
    public void setUp() throws IOException, JSONException {
        ShadowLog.stream = System.out; /* Robolectric setting */
        config = new SampleRakeConfig1();
        mockCore = mock(RakeCore.class);

        mockSysInfo = mock(SystemInformation.class);

        JSONObject mockDefaultProperties = MockSystemInformation.getDefaultProperties(config);
        when(mockSysInfo.getDefaultProperties(config)).thenReturn(mockDefaultProperties);

        rake = new RakeImpl(config, mockCore, mockSysInfo);

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
}
