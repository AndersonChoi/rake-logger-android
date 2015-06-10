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

    static public JSONObject getTrackableShuttle(JSONObject shuttle, JSONObject defaultProperties)
            throws JSONException {
        JSONObject trackableShuttle = extractSentinelMeta(shuttle);
        JSONObject userProperties   = extractProperties(shuttle);
        JSONObject properties = mergeProperties(userProperties, defaultProperties);

        trackableShuttle.put(ShuttleProtocol.FIELD_NAME_PROPERTIES, properties);

        return trackableShuttle;
    }

    static private JSONObject extractSentinelMeta(JSONObject shuttle) throws JSONException {
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

    static private JSONObject extractProperties(JSONObject shuttle) throws JSONException {

        JSONObject properties = new JSONObject();

        Iterator<String> iter = shuttle.keys();

        while(iter.hasNext()) {
            String key = iter.next();
            if (key.equals(ShuttleProtocol.FIELD_NAME_SENTINEL_META)) continue;

            properties.put(key, shuttle.get(key));
        }

        return properties;
    }


    // write `first` first, then write `second`
    static private JSONObject mergeProperties(JSONObject first, JSONObject second) throws JSONException {
        JSONObject properties = new JSONObject();

        /* copy first */
        Iterator<String> iter = first.keys();
        while(iter.hasNext()) {
            String key = iter.next();
            properties.put(key, first.get(key));
        }

        /* copy second */
        iter = second.keys();

        while(iter.hasNext()) {
            String key = iter.next();
            properties.put(key, second.get(key));
        }

        return properties;
    }
}
