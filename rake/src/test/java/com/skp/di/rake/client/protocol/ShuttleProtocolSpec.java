package com.skp.di.rake.client.protocol;

import com.skp.di.rake.client.utils.Logger;
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

import java.util.Iterator;

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

    @Before
    public void setUp() {
        ShadowLog.stream = System.out; /* Robolectric setting */
        shuttle = new AppSampleSentinelShuttle();
    }

    @Test
    public void testShuttleHasSentinelMeta() throws JSONException {
        JSONObject sentinel_meta = shuttle.toJSONObject().getJSONObject(ShuttleProtocol.FIELD_NAME_SENTINEL_META);
        JSONArray _$encryptionFields = (JSONArray) sentinel_meta.get(ShuttleProtocol.FIELD_NAME_ENCRYPTION_FIELDS);
        String _$projectId = sentinel_meta.getString(ShuttleProtocol.FIELD_NAME_PROJECT_ID);
        JSONObject _$fieldOrder = sentinel_meta.getJSONObject(ShuttleProtocol.FIELD_NAME_FIELD_ORDER);
        String _$schemaId = sentinel_meta.getString(ShuttleProtocol.FIELD_NAME_SCHEMA_ID);
    }

    @Test
    public void testExtractSentinelMeta() throws JSONException {

        JSONObject willBeTracked = ShuttleProtocol.extractSentinelMeta(shuttle.toJSONObject());
        JSONArray _$encryptionFields = (JSONArray) willBeTracked.get(ShuttleProtocol.FIELD_NAME_ENCRYPTION_FIELDS);
        String _$projectId = willBeTracked.getString(ShuttleProtocol.FIELD_NAME_PROJECT_ID);
        JSONObject _$fieldOrder = willBeTracked.getJSONObject(ShuttleProtocol.FIELD_NAME_FIELD_ORDER);
        String _$schemaId = willBeTracked.getString(ShuttleProtocol.FIELD_NAME_SCHEMA_ID);

        JSONObject sentinel_meta = shuttle.toJSONObject().getJSONObject(ShuttleProtocol.FIELD_NAME_SENTINEL_META);

        assertEquals(
                sentinel_meta.getJSONArray(ShuttleProtocol.FIELD_NAME_ENCRYPTION_FIELDS),
                _$encryptionFields);

        assertEquals(
                sentinel_meta.getString(ShuttleProtocol.FIELD_NAME_PROJECT_ID),
                _$projectId);

        assertEquals(
                sentinel_meta.getString(ShuttleProtocol.FIELD_NAME_SCHEMA_ID),
                _$schemaId);

        assertEquals(
                sentinel_meta.getJSONObject(ShuttleProtocol.FIELD_NAME_FIELD_ORDER).toString(),
                _$fieldOrder.toString());
    }

    @Test
    public void testExtractProperties() throws JSONException {
        JSONObject shuttleJSON = shuttle.toJSONObject();
        JSONObject properties = ShuttleProtocol.extractProperties(shuttleJSON);

        // properties should contain the same key-values
        // except`ShuttleProtocol.FIELD_NAME_PROPERTIES`

        Iterator<String> iter = shuttleJSON.keys();

        while(iter.hasNext()) {
            String key = iter.next();

            if (key.equals(ShuttleProtocol.FIELD_NAME_SENTINEL_META)) {
                assertFalse(properties.has(key));
                continue;
            }

            assertTrue(properties.has(key));
            assertEquals(shuttleJSON.get(key), properties.get(key));
        }

    }

}

