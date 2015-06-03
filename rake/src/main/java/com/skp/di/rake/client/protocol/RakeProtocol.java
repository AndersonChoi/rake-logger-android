package com.skp.di.rake.client.protocol;

import com.skp.di.rake.client.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import rx.exceptions.OnErrorThrowable;

public class RakeProtocol {
    /* Ref: http://wiki.skplanet.com/display/DIT/Rake+API+Spec */
    static public final int ERROR_CODE_OK                        = 20000;
    static public final int ERROR_CODE_INSUFFICIENT_JSON_FIELD   = 40001;
    static public final int ERROR_CODE_INVALID_JSON_SYNTAX       = 40002;
    static public final int ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN = 40101;
    static public final int ERROR_CODE_WRONG_RAKE_TOKEN_USAGE    = 40301;
    static public final int ERROR_CODE_INVALID_END_POINT         = 40401;
    static public final int ERROR_CODE_INTERNAL_SERVER_ERROR     = 50001;

    static public String buildRakeRequestBody(List<JSONObject> tracked) {
        JSONObject body = null;

        try {
            body = buildProtocolBody(tracked);
        } catch (JSONException e) {
            Logger.e("Can't build RakeRequestBody", e);
            throw OnErrorThrowable.from(e);
        }

        return body.toString();
    }

    static public String buildRakeRequestBody(JSONObject log) {
        List<JSONObject> tracked = Arrays.asList(log);
        return buildRakeRequestBody(tracked);
    }

    static private JSONObject buildProtocolBody(List<JSONObject> tracked) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("data", tracked);

        return body;
    }
}
