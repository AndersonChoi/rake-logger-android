package com.skp.di.rake.client.protocol;

import com.skp.di.rake.client.utils.Logger;
import com.skp.di.rake.client.utils.StringUtils;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeProtocolSpec {

    JSONObject rakeRequestBody;
    List<JSONObject> tracked;

    @Before
    public void setUp() throws JSONException {
        ShadowLog.stream = System.out;

        JSONObject log = new JSONObject();
        log.put("rake_lib", "Android");
        log.put("rake_lib_version", "0.4");

        tracked = Arrays.asList(log);
    }

    @Test
    public void testBuildUrlEncodedEntity() throws JSONException, IOException {
        UrlEncodedFormEntity entity = RakeProtocol.buildUrlEncodedEntity(tracked);

        JSONObject request = new JSONObject();
        List<NameValuePair> pairs = URLEncodedUtils.parse(entity);

        for (NameValuePair pair : pairs) request.put(pair.getName(), pair.getValue());

        assertRequestHasValidRakeFields(request);
    }

    @Test
    public void testBuildJsonEntity() throws JSONException, IOException {
        StringEntity entity = RakeProtocol.buildJsonEntity(tracked);
        JSONObject request = new JSONObject(EntityUtils.toString(entity));

        assertRequestHasValidRakeFields(request);
    }

    public void assertRequestHasValidRakeFields(JSONObject request) throws JSONException {
        assertTrue(request.has(RakeProtocol.FIELD_NAME_DATA));
        assertTrue(request.has(RakeProtocol.FIELD_NAME_COMPRESS));

        assertEquals(RakeProtocol.FIELD_VALUE_COMPRESS,
                request.getString(RakeProtocol.FIELD_NAME_COMPRESS));
    }


    @Test(expected = JSONException.class)
    public void testBase64Diff_ArrayList_vs_JsonArray() throws JSONException {

        String encoded1 = StringUtils.encodeBase64(tracked.toString());
        String encoded2 = StringUtils.encodeBase64(new JSONArray(tracked).toString());

        assertEquals(encoded1, encoded2);

        // looks same at glance, but different
        JSONObject json1 = new JSONObject(encoded1); /* throw exception */
        JSONObject json2 = new JSONObject(encoded2);

        assertNotEquals(json1, json2);
    }
}
