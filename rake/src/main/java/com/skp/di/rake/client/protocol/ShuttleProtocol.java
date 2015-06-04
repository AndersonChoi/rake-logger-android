package com.skp.di.rake.client.protocol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ShuttleProtocol {
    /* refer - http://wiki.skplanet.com/pages/viewpage.action?pageId=73266369 */

    static public final String FIELD_NAME_BODY              = "_$body";
    static public final String FIELD_NAME_SCHEMA_ID         = "_$schemaId";
    static public final String FIELD_NAME_PROPERTIES        = "properties";
    static public final String FIELD_NAME_PROJECT_ID        = "_$projectId";
    static public final String FIELD_NAME_FIELD_ORDER       = "_$fieldOrder";
    static public final String FIELD_NAME_SENTINEL_META     = "sentinel_meta";
    static public final String FIELD_NAME_ENCRYPTION_FIELDS = "_$encryptionFields";

    static public JSONObject extractSentinelMeta(JSONObject shuttle) throws JSONException {
        JSONObject willBeTracked = new JSONObject();

        // extract `sentinel_meta`
        JSONObject sentinel_meta = shuttle.getJSONObject(ShuttleProtocol.FIELD_NAME_SENTINEL_META);
        JSONArray _$encryptionFields = (JSONArray) sentinel_meta.get(ShuttleProtocol.FIELD_NAME_ENCRYPTION_FIELDS);
        String _$projectId = sentinel_meta.getString(ShuttleProtocol.FIELD_NAME_PROJECT_ID);
        JSONObject _$fieldOrder = sentinel_meta.getJSONObject(ShuttleProtocol.FIELD_NAME_FIELD_ORDER);
        String _$schemaId = sentinel_meta.getString(ShuttleProtocol.FIELD_NAME_SCHEMA_ID);

        // fill
        willBeTracked.put(ShuttleProtocol.FIELD_NAME_ENCRYPTION_FIELDS, _$encryptionFields);
        willBeTracked.put(ShuttleProtocol.FIELD_NAME_PROJECT_ID, _$projectId);
        willBeTracked.put(ShuttleProtocol.FIELD_NAME_FIELD_ORDER, _$fieldOrder);
        willBeTracked.put(ShuttleProtocol.FIELD_NAME_SCHEMA_ID, _$schemaId);

        return willBeTracked;
    }

    static public JSONObject extractProperties(JSONObject shuttle) throws JSONException {

        JSONObject properties = new JSONObject();

        Iterator<String> iter = shuttle.keys();

        while(iter.hasNext()) {
            String key = iter.next();
            if (key.equals(ShuttleProtocol.FIELD_NAME_SENTINEL_META)) continue;

            properties.put(key, shuttle.get(key));
        }

        return properties;
    }
}
