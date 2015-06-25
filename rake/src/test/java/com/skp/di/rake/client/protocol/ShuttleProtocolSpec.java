package com.skp.di.rake.client.protocol;

import com.skp.di.rake.client.utils.RakeTestUtils;
import com.skplanet.pdp.sentinel.shuttle.AppSampleSentinelShuttle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class ShuttleProtocolSpec {

    /* AppSampleSentinelShuttle 67

     action: action1, action2, action3, action4
     encryption: field1, field3

     */

    AppSampleSentinelShuttle shuttle;
    JSONObject emptySuperProps;

    @Before
    public void setUp() {
        ShadowLog.stream = System.out; /* Robolectric setting */
        shuttle = new AppSampleSentinelShuttle();
        emptySuperProps = new JSONObject();
    }

    @Test
    public void test_ShuttleProtocolCanReturn_extractedMetaAndProperties() throws JSONException {
        JSONObject defaultProperties =
                RakeTestUtils.createSampleDefaultProperties(RakeTestUtils.createDevConfig1());
        JSONObject trackable =
                ShuttleProtocol.getTrackable(shuttle.toJSONObject(), emptySuperProps, defaultProperties);

        assertTrue(trackable.has(ShuttleProtocol.FIELD_NAME_SCHEMA_ID));
        assertTrue(trackable.has(ShuttleProtocol.FIELD_NAME_PROJECT_ID));
        assertTrue(trackable.has(ShuttleProtocol.FIELD_NAME_ENCRYPTION_FIELDS));
        assertTrue(trackable.has(ShuttleProtocol.FIELD_NAME_FIELD_ORDER));
        assertTrue(trackable.has(ShuttleProtocol.FIELD_NAME_PROPERTIES));
        assertTrue(trackable.getJSONObject(ShuttleProtocol.FIELD_NAME_PROPERTIES)
                .has(ShuttleProtocol.FIELD_NAME_BODY));

        assertFalse(trackable.has(ShuttleProtocol.FIELD_NAME_SENTINEL_META));
    }

    @Test
    public void test_TrackableHasValidDefaultProperties() throws JSONException {
        JSONObject defaultProperties =
                RakeTestUtils.createSampleDefaultProperties(RakeTestUtils.createDevConfig1());
        JSONObject trackable = ShuttleProtocol.getTrackable(shuttle.toJSONObject(), emptySuperProps, defaultProperties);

        JSONObject properties = trackable.getJSONObject(ShuttleProtocol.FIELD_NAME_PROPERTIES);

        List<String> defaultPropertyNames = Arrays.asList(
                "base_time", "local_time", "recv_time", ShuttleProtocol.FIELD_NAME_BODY,
                "device_id", "device_model", "os_name", "os_version",
                "resolution", "screen_width", "screen_height",
                "app_version", "carrier_name", "network_type", "language_code",
                "rake_lib", "rake_lib_version", "manufacturer", "ip",
                "recv_host", "token"
        );

        for(String key : defaultPropertyNames) {
            assertTrue(properties.has(key));
        }
    }

    @Test
    public void test_ShuttleHasSentinelMeta() throws JSONException {
        JSONObject sentinel_meta = shuttle.toJSONObject().getJSONObject(ShuttleProtocol.FIELD_NAME_SENTINEL_META);
        JSONArray _$encryptionFields = (JSONArray) sentinel_meta.get(ShuttleProtocol.FIELD_NAME_ENCRYPTION_FIELDS);
        String _$projectId = sentinel_meta.getString(ShuttleProtocol.FIELD_NAME_PROJECT_ID);
        JSONObject _$fieldOrder = sentinel_meta.getJSONObject(ShuttleProtocol.FIELD_NAME_FIELD_ORDER);
        String _$schemaId = sentinel_meta.getString(ShuttleProtocol.FIELD_NAME_SCHEMA_ID);
    }
}

