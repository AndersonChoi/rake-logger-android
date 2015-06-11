package library.test.json;


import com.skp.di.rake.client.mock.SampleDevConfig;
import com.skp.di.rake.client.utils.RakeLogger;
import com.skp.di.rake.client.utils.RakeLoggerFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class JSONObjectSpec {
   @Before
   public void setUp() {
      ShadowLog.stream = System.out;
   }

   @Test
   public void testJSONObjectNullShouldRemoveKeyAlreadyExist() throws JSONException {
      JSONObject json = new JSONObject();
      json.put("key1", "value1");
      assertNotNull(json.get("key1"));

      json.put("key1", JSONObject.NULL);
      assertEquals(JSONObject.NULL, json.get("key1"));
      assertNotNull(json.get("key1"));
   }

   @Test
   public void testPutJSONArray() throws JSONException {
      JSONObject json = new JSONObject();
      json.put("key1", 3);
      json.put("key2", "3");
   }
}
