package library.test.json;


import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(JUnit4.class)
public class JSONObjectSpec {
   @Test
   public void testJSONObjectNullShouldRemoveKeyAlreadyExist() throws JSONException {
      JSONObject json = new JSONObject();
      json.put("key1", "value1");
      assertNotNull(json.get("key1"));

      json.put("key1", JSONObject.NULL);
      assertEquals(JSONObject.NULL, json.get("key1"));
      assertNotNull(json.get("key1"));
   }
}
