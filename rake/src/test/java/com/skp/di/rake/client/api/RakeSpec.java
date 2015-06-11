package com.skp.di.rake.client.api;

import com.skp.di.rake.client.android.SystemInformation;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.core.RakeCore;
import com.skp.di.rake.client.mock.MockSystemInformation;
import com.skp.di.rake.client.mock.SampleDevConfig;
import com.skp.di.rake.client.mock.SampleDevConfig2;
import com.skp.di.rake.client.protocol.ShuttleProtocol;
import com.skp.di.rake.client.utils.Logger;
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
        config = new SampleDevConfig2();
        mockCore = mock(RakeCore.class);

        mockSysInfo = mock(SystemInformation.class);

        JSONObject mockDefaultProperties = MockSystemInformation.getDefaultProperties(config);
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
    public void test_DefaultProperty_ShouldNotOverwritten_By_Shuttle_And_SuperProperty() throws JSONException {
        rake.track(shuttle.toJSONObject());

        // if defaultProperties are overwritten by the shuttle,
        // local_time field will be "" (empty string)
        JSONObject properties = getPropertiesFromCaptor(mockCore, captor);
        assertNotEquals("", properties.getString("local_time"));

        // defaultProperties should not be overwritten by shuttle property, super property
        assertNotEquals("invalid local time", properties.getString("local_time"));
        assertNotEquals("", properties.getString("local_time"));
    }

    @Test
    public void test_SuperProperty_ShouldBeAdded_And_ShouldBeOverwritten_ByDefaultProperty()
            throws JSONException {
        JSONObject props = new JSONObject();
        props.put("session_id", "AED49-KA4");
        props.put("local_time", "invalid local time");

        rake.registerSuperProperties(props);
        rake.track(shuttle.toJSONObject());

        JSONObject properties = getPropertiesFromCaptor(mockCore, captor);

        // verify super property properly added {"session_id": "AED49-KA4"}
        assertEquals("AED49-KA4", properties.getString("session_id"));

        // verify super property is overwritten by default properties
        assertNotEquals("invalid local time", properties.getString("local_time"));
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

    private JSONObject getPropertiesFromCaptor(RakeCore mock, ArgumentCaptor<JSONObject> captor) throws JSONException {
        verify(mockCore, times(1)).track(captor.capture());
        JSONObject trackable = captor.getValue();
        JSONObject properties = trackable.getJSONObject(ShuttleProtocol.FIELD_NAME_PROPERTIES);
        return properties;
    }

    @Test
    public void test_ShuttleProperty_ShouldBeAdded() throws JSONException {
        JSONObject props = new JSONObject();
        props.put("session_id", "4919");
        rake.registerSuperProperties(props);

        shuttle.field1("field1 value");
        rake.track(shuttle.toJSONObject());

        JSONObject properties = getPropertiesFromCaptor(mockCore, captor);

        assertEquals("4919", properties.get("session_id"));

        JSONObject _$body = properties.getJSONObject(ShuttleProtocol.FIELD_NAME_BODY);
        assertEquals("field1 value", _$body.get("field1"));
    }

    @Test
    public void test_ShuttleProperty_ShouldNotBeOverwritten_BySuperProperty() throws JSONException {
        String log_version = shuttle.toJSONObject().getString("log_version");

        JSONObject props = new JSONObject();
        props.put("log_version", "invalid log version");
        rake.registerSuperProperties(props);

        rake.track(shuttle.toJSONObject());

        JSONObject properties = getPropertiesFromCaptor(mockCore, captor);

        assertEquals(log_version, properties.getString("log_version"));
    }
}
