package com.skp.di.rake.client.protocol;

import com.skp.di.rake.client.utils.RakeLogger;

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

        JSONObject trackable    = extractSentinelMeta(shuttle);
        JSONObject shuttleProps = extractProperties(shuttle);

        // must be in order 'shuttleProps, superProps, defaultProps'
        // precedence: superProps < shuttleProps < defaultProps
        JSONObject properties = mergeProperties(shuttleProps, superProps, defaultProps, trackable);

        trackable.put(ShuttleProtocol.FIELD_NAME_PROPERTIES, properties);

        return trackable;
    }

    static private JSONObject extractSentinelMeta(JSONObject shuttle) throws JSONException {
        JSONObject trackable = new JSONObject();

        // extract `sentinel_meta`
        JSONObject sentinel_meta = shuttle.getJSONObject(ShuttleProtocol.FIELD_NAME_SENTINEL_META);
        JSONArray _$encryptionFields = (JSONArray) sentinel_meta.get(ShuttleProtocol.FIELD_NAME_ENCRYPTION_FIELDS);
        String _$projectId = sentinel_meta.getString(ShuttleProtocol.FIELD_NAME_PROJECT_ID);
        JSONObject _$fieldOrder = sentinel_meta.getJSONObject(ShuttleProtocol.FIELD_NAME_FIELD_ORDER);
        String _$schemaId = sentinel_meta.getString(ShuttleProtocol.FIELD_NAME_SCHEMA_ID);

        // fill
        trackable.put(ShuttleProtocol.FIELD_NAME_ENCRYPTION_FIELDS, _$encryptionFields);
        trackable.put(ShuttleProtocol.FIELD_NAME_PROJECT_ID, _$projectId);
        trackable.put(ShuttleProtocol.FIELD_NAME_FIELD_ORDER, _$fieldOrder);
        trackable.put(ShuttleProtocol.FIELD_NAME_SCHEMA_ID, _$schemaId);

        return trackable;
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
                                              JSONObject defaultProps,
                                              JSONObject trackable)
            throws JSONException {

        JSONObject properties = new JSONObject();

        // remove _$body from super property to prevent from sending invalid _$body
        superProps.remove(ShuttleProtocol.FIELD_NAME_BODY);

        // !important: must be ordered 'shuttleProps', 'superProps', 'defaultProps'
        copyProperties(shuttleProps, properties, true);
//        copyProperties(superProps, properties, false);
        copySuperProperties(superProps, properties, trackable);
        copyProperties(defaultProps, properties, true);

        return properties;
    }

    static private void copyProperties(JSONObject from,
                                       JSONObject to,
                                       boolean overwrite) throws JSONException {

        Iterator<String> iter = from.keys();
        while(iter.hasNext()) {
            String key = iter.next();

            /* not overwrite when `to.get(key)` is not empty */
            if (!overwrite && to.has(key) && !to.getString(key).isEmpty())
                continue;

            to.put(key, from.get(key));
        }
    }

    static private void copySuperProperties(JSONObject superProps, JSONObject props, JSONObject trackable) throws JSONException {
         // _$fieldOrder could be null if a user does not use shuttle (legacy)
        JSONObject _$fieldOrder = null;

        try {
            _$fieldOrder = trackable.getJSONObject(ShuttleProtocol.FIELD_NAME_FIELD_ORDER);
        } catch (JSONException e) { /* do nothing */ }

        Iterator<String> iter = superProps.keys();
        while(iter.hasNext()) {
            String key = iter.next();

            if (null != _$fieldOrder && !_$fieldOrder.has(key)) { /* body */
                JSONObject _$body = props.getJSONObject(ShuttleProtocol.FIELD_NAME_BODY);

                /* if the body field is not empty, do not overwrite */
                if (_$body.has(key) && (!_$body.getString(key).isEmpty())) continue;

                /* since _$body is mutable object, we don't need to put it back into props */
                _$body.put(key, superProps.get(key));

            } else /* header or might be not shuttle */ {

                /* if the header field is not empty, do not overwrite */
                if (props.has(key) && (!props.getString(key).isEmpty())) continue;

                props.put(key, superProps.get(key));
            }
        }
    }
}
