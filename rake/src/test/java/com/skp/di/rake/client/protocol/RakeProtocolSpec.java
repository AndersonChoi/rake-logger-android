package com.skp.di.rake.client.protocol;

import com.skp.di.rake.client.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.Shadow;
import org.robolectric.shadows.ShadowLog;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeProtocolSpec {

    JSONObject rakeRequestBody;

    @Before
    public void setUp() throws JSONException {
        ShadowLog.stream = System.out;

        JSONObject log = new JSONObject();
        log.put("rake_lib", "Android");
        log.put("rake_lib_version", "0.4");

        List<JSONObject> tracked = Arrays.asList(log);
        String returned = RakeProtocol.buildRakeRequestBody(tracked);
        rakeRequestBody = new JSONObject(returned);
    }

    @Test
    public void testBuildRakeProtocolBodyShouldInclude_data_Field() throws JSONException {
        assertTrue(rakeRequestBody.has(RakeProtocol.FIELD_NAME_DATA));
    }

    @Test
    public void testBuildRakeProtocolBodyShouldInclude_compress_field() throws JSONException {
        // compress always be `plain`
        assertTrue(rakeRequestBody.has(RakeProtocol.FIELD_NAME_COMPRESS));
        assertEquals(
                RakeProtocol.FIELD_VALUE_COMPRESS,
                rakeRequestBody.getString(RakeProtocol.FIELD_NAME_COMPRESS));
    }
}
