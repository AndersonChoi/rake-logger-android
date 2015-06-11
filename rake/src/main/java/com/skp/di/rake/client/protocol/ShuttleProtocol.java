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


    static public JSONObject getTrackable(JSONObject shuttle,
                                          JSONObject superProps,
                                          JSONObject defaultProps) throws JSONException {

        JSONObject trackable = extractSentinelMeta(shuttle);
        JSONObject shuttleProps   = extractProperties(shuttle);

        // must be in order 'shuttleProps, superProps, defaultProps'
        // precedence: superProps < shuttleProps < defaultProps
        JSONObject properties = mergeProperties(shuttleProps, superProps, defaultProps);

        trackable.put(ShuttleProtocol.FIELD_NAME_PROPERTIES, properties);

        return trackable;
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


    /* shuttle property means that the property a user inserted using a shuttle */
    static private JSONObject mergeProperties(JSONObject shuttleProps,
                                              JSONObject superProps,
                                              JSONObject defaultProps)
            throws JSONException {

        JSONObject properties = new JSONObject();

        // remove _$body from super property to prevent from sending invalid _$body
        superProps.remove(ShuttleProtocol.FIELD_NAME_BODY);

        // !important: must be ordered 'shuttleProps', 'superProps', 'defaultProps'
        copyProperties(shuttleProps, properties, true);
        copyProperties(superProps, properties, false);
        copyProperties(defaultProps, properties, true);

        return properties;
    }

    static private void copyProperties(JSONObject from, JSONObject to, boolean overwrite)
            throws JSONException {
        Iterator<String> iter = from.keys();
        while(iter.hasNext()) {
            String key = iter.next();

            // JSONObject.NULL 일 경우 테스트
            if (!overwrite && to.has(key)) continue;

            to.put(key, from.get(key));
        }
    }
}
