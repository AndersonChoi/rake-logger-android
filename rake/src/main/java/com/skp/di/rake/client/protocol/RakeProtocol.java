package com.skp.di.rake.client.protocol;

import android.util.Base64;

import com.skp.di.rake.client.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RakeProtocol {
    /* Ref: http://wiki.skplanet.com/display/DIT/Rake+API+Spec */
    static public final int ERROR_CODE_OK                        = 20000;
    static public final int ERROR_CODE_INSUFFICIENT_JSON_FIELD   = 40001;
    static public final int ERROR_CODE_INVALID_JSON_SYNTAX       = 40002;
    static public final int ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN = 40101;
    static public final int ERROR_CODE_WRONG_RAKE_TOKEN_USAGE    = 40301;
    static public final int ERROR_CODE_INVALID_END_POINT         = 40401;
    static public final int ERROR_CODE_INTERNAL_SERVER_ERROR     = 50001;

    static public final String FIELD_NAME_DATA = "data";
    static public final String FIELD_NAME_COMPRESS = "compress";
    static public final String FIELD_VALUE_COMPRESS = "plain";

    static public UrlEncodedFormEntity buildRequestEntity(List<JSONObject> tracked) throws JSONException, UnsupportedEncodingException {
        if (null == tracked || 0 == tracked.size())
            throw new JSONException("`tracked` is null.");

        // TODO: return buildJsonEntity(tracked);
        return buildUrlEncodedEntity(tracked);
    }

    static public HttpEntity buildRequestEntity(JSONObject log) throws JSONException, UnsupportedEncodingException {
        List<JSONObject> tracked = Arrays.asList(log);
        return buildRequestEntity(tracked);
    }

    static public UrlEncodedFormEntity buildUrlEncodedEntity(List<JSONObject> tracked) throws JSONException, UnsupportedEncodingException {
        List<NameValuePair> pairs = new ArrayList<>(2);

        String FIELD_VALUE_DATA = StringUtils.encodeBase64(new JSONArray(tracked).toString());

        pairs.add(new BasicNameValuePair(
                RakeProtocol.FIELD_NAME_COMPRESS, RakeProtocol.FIELD_VALUE_COMPRESS));
        pairs.add(new BasicNameValuePair(
                RakeProtocol.FIELD_NAME_DATA, FIELD_VALUE_DATA));

        return new UrlEncodedFormEntity(pairs);
    }

    static public StringEntity buildJsonEntity(List<JSONObject> tracked) throws JSONException, UnsupportedEncodingException {
        JSONObject body = new JSONObject();
        body.put(FIELD_NAME_DATA, new JSONArray(tracked));
        body.put(FIELD_NAME_COMPRESS, FIELD_VALUE_COMPRESS);

        return new StringEntity(body.toString());
    }



}
