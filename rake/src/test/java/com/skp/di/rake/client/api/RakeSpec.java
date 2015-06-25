package com.skp.di.rake.client.api;

import android.util.Log;

import com.skp.di.rake.client.android.SystemInformation;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.core.RakeCore;
import com.skp.di.rake.client.protocol.ShuttleProtocol;
import com.skp.di.rake.client.utils.RakeTestUtils;
import com.skplanet.pdp.sentinel.shuttle.AppSampleSentinelShuttle;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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
    ArgumentCaptor<JSONObject> captor;
    @Before
    public void setUp() throws IOException, JSONException {
        ShadowLog.stream = System.out; /* Robolectric setting */
        config = RakeTestUtils.createDevConfig1();

        mockCore = mock(RakeCore.class);

        mockSysInfo = mock(SystemInformation.class);

        JSONObject mockDefaultProperties =
                RakeTestUtils.createSampleDefaultProperties(config);
        when(mockSysInfo.getDefaultProperties(config)).thenReturn(mockDefaultProperties);

        rake = new Rake(config, mockCore, ShadowApplication.getInstance().getApplicationContext(), mockSysInfo);

        json = new JSONObject();
        json.put("rake_lib", RakeMetaConfig.RAKE_CLIENT_VERSION);

        shuttle = new AppSampleSentinelShuttle();

        captor = ArgumentCaptor.forClass(JSONObject.class);
    }

    @Test
    public void test_ShouldNotTrackNullAndEmptyLog() throws JSONException {
        rake.track(null);

        // the json having "" as keys are treated as null log
        JSONObject empty = new JSONObject();
        empty.put("", "");
        rake.track(empty);

        verify(mockCore, never()).track(any());
    }

    @Test
    public void test_hasSuperProperty() throws JSONException {
        JSONObject props = new JSONObject();
        props.put("superProperty1", "value");

        rake.registerSuperProperties(props);
        assertFalse(rake.hasSuperProperty("sup"));
        assertTrue(rake.hasSuperProperty("superProperty1"));
    }

    @Test
    public void test_unRegisterSuperProperties_AND_clearSuperProperties() throws JSONException {
        JSONObject props = new JSONObject();
        props.put("superProperty1", "value1");
        props.put("superProperty2", "value2");

        rake.registerSuperProperties(props);
        assertTrue(rake.hasSuperProperty("superProperty1"));
        assertTrue(rake.hasSuperProperty("superProperty2"));

        rake.unregisterSuperProperties("superProperty1");
        rake.unregisterSuperProperties("superProperty1"); // remove again
        assertFalse(rake.hasSuperProperty("superProperty1"));
        assertTrue(rake.hasSuperProperty("superProperty2"));

        rake.clearSuperProperties();
        assertFalse(rake.hasSuperProperty("superProperty1"));
        assertFalse(rake.hasSuperProperty("superProperty2"));
    }

    @Test
    public void test_RegisterSuperPropertiesOnce() throws JSONException {
        JSONObject props1 = new JSONObject();
        props1.put("key1", "0");
        props1.put("key2", "0");

        rake.registerSuperPropertiesOnce(props1);
        assertTrue(rake.hasSuperProperty("key1"));
        assertTrue(rake.hasSuperProperty("key2"));
        assertEquals("0", rake.getSuperPropertyValue("key1"));
        assertEquals("0", rake.getSuperPropertyValue("key2"));

        // register another props

        JSONObject props2 = new JSONObject();
        props2.put("key1", "1");
        props2.put("key3", "0");

        rake.registerSuperPropertiesOnce(props2);
        assertTrue(rake.hasSuperProperty("key1"));
        assertTrue(rake.hasSuperProperty("key2"));
        assertTrue(rake.hasSuperProperty("key3"));

        assertEquals("0", rake.getSuperPropertyValue("key1"));
        assertEquals("0", rake.getSuperPropertyValue("key2"));
        assertEquals("0", rake.getSuperPropertyValue("key3"));
    }

    @Test
    public void test_$body_ShouldRemovedFrom_SuperProperty() throws JSONException {
        // _$body field included in super property by used should be removed before merged
        JSONObject props = new JSONObject();
        props.put(ShuttleProtocol.FIELD_NAME_BODY, "invalid body");
        rake.registerSuperProperties(props);

        shuttle.field1("field1");
        rake.track(shuttle.toJSONObject());

        JSONObject properties = getPropertiesFromCaptor(mockCore, captor);

        assertNotEquals("invalid body", properties.get(ShuttleProtocol.FIELD_NAME_BODY));
    }

    @Test
    public void test_SuperProperty_ShouldOverwrite_ShuttleBody_WhichIsNotSet() throws JSONException {
        // field2 is not set
        shuttle.field4("");

        JSONObject props = new JSONObject();
        props.put("field2", "super prop field2");

        registerSuperPropsAndTrack(props, rake, shuttle.toJSONObject());

        JSONObject properties = getPropertiesFromCaptor(mockCore, captor);

        Log.i("TEST", properties.toString());

        JSONObject _$body = properties.getJSONObject(ShuttleProtocol.FIELD_NAME_BODY);

        assertEquals("super prop field2", _$body.getString("field2"));
    }

    @Test
    public void test_SuperProperty_ShouldOverwrite_ShuttleBody_WhichIsEmptyString() throws JSONException {
        shuttle.setBodyOfaction2("", "field4 body value");

        JSONObject props = new JSONObject();
        props.put("field2", "super prop field2");

        registerSuperPropsAndTrack(props, rake, shuttle.toJSONObject());

        JSONObject properties = getPropertiesFromCaptor(mockCore, captor);
        JSONObject _$body = properties.getJSONObject(ShuttleProtocol.FIELD_NAME_BODY);

        assertEquals("super prop field2", _$body.getString("field2"));
    }

    @Test
    public void test_SuperProperty_ShouldNotOverwrite_ShuttleBody_WhichIsNotEmpty() throws JSONException {
        shuttle.setBodyOfaction2("", "field4 body value");

        JSONObject props = new JSONObject();
        props.put("field4", "should not be sent");

        registerSuperPropsAndTrack(props, rake, shuttle.toJSONObject());

        JSONObject properties = getPropertiesFromCaptor(mockCore, captor);
        JSONObject _$body = properties.getJSONObject(ShuttleProtocol.FIELD_NAME_BODY);

        assertEquals("field4 body value", _$body.getString("field4"));
    }

    @Test
    public void test_SuperProperty_ShouldOverwrite_ShuttleHeader_WhichIsEmpty() throws JSONException {
        String header1 = "header1 super prop value";

        registerSuperPropsAndTrack("header1", header1, rake, shuttle.toJSONObject());
        JSONObject properties = getPropertiesFromCaptor(mockCore, captor);

        assertEquals(header1, properties.getString("header1"));
    }

    @Test
    public void test_SuperProperty_ShouldNotOverwrite_ShuttleHeader_WhichIsNotEmpty() throws JSONException {
        JSONObject props = new JSONObject();
        props.put("log_version", "invalid log version"); // default header property
        props.put("header1", "invalid header1 value");   // custom  header property

        shuttle.header1("valid header1 value");
        registerSuperPropsAndTrack(props, rake, shuttle.toJSONObject());

        JSONObject properties = getPropertiesFromCaptor(mockCore, captor);

        String log_version = shuttle.toJSONObject().getString("log_version");
        assertEquals(log_version, properties.getString("log_version")); // when header is not empty
        assertEquals("valid header1 value", properties.getString("header1"));
    }

    @Test
    public void test_DefaultProperty_ShouldOverwrite_Always() throws JSONException {
        registerSuperPropsAndTrack("token", "invalid token", rake, shuttle.toJSONObject());

        JSONObject properties = getPropertiesFromCaptor(mockCore, captor);
        assertEquals(config.getToken(), properties.getString("token")); // when header is empty
    }

    private JSONObject getPropertiesFromCaptor(RakeCore mock, ArgumentCaptor<JSONObject> captor) throws JSONException {
        verify(mockCore, times(1)).track(captor.capture());
        JSONObject trackable = captor.getValue();
        JSONObject properties = trackable.getJSONObject(ShuttleProtocol.FIELD_NAME_PROPERTIES);
        return properties;
    }

    private void registerSuperPropsAndTrack(String key, String value, Rake rake, JSONObject shuttle) throws JSONException {
        rake.clearSuperProperties();
        JSONObject props = new JSONObject();
        props.put(key, value);

        registerSuperPropsAndTrack(props, rake, shuttle);
    }

    private void registerSuperPropsAndTrack(JSONObject props, Rake rake, JSONObject shuttle) throws JSONException {
        rake.registerSuperProperties(props);
        rake.track(shuttle);
    }

}
