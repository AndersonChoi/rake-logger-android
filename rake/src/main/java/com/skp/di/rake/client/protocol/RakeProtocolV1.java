package com.skp.di.rake.client.protocol;

import com.skp.di.rake.client.protocol.exception.InternalServerErrorException;
import com.skp.di.rake.client.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class RakeProtocolV1 implements RakeProtocol {

    public HttpEntity buildUrlEncodedFormEntity(List<JSONObject> tracked) throws JSONException, UnsupportedEncodingException {
        List<NameValuePair> pairs = new ArrayList<>(2);

        String FIELD_VALUE_DATA = StringUtils.encodeBase64(new JSONArray(tracked).toString());

        pairs.add(new BasicNameValuePair(
                RakeProtocolV2.FIELD_NAME_COMPRESS, RakeProtocolV2.FIELD_VALUE_COMPRESS));
        pairs.add(new BasicNameValuePair(
                RakeProtocolV2.FIELD_NAME_DATA, FIELD_VALUE_DATA));

        return new UrlEncodedFormEntity(pairs);
    }

    @Override
    public HttpPost buildRequest(List<JSONObject> tracked, String endPoint)
            throws UnsupportedEncodingException, JSONException {

        HttpPost post = new HttpPost(endPoint);

        HttpEntity entity = buildUrlEncodedFormEntity(tracked);

        post.setEntity(entity);

        return post;
    }

    @Override
    public void verifyResponse(int statusCode, String responseBody) {
            // handle 500 headers
        verifyStatusCode(statusCode);
        verifyResponseBody(responseBody);
    }

    private void verifyStatusCode(int statusCode) {
        /* handle 500 only */
        if (HttpStatus.SC_INTERNAL_SERVER_ERROR == statusCode) {
            throw new InternalServerErrorException("statusCode: " + statusCode);
        }
    }

    private void verifyResponseBody(String responseBody) {
        if (responseBody.equals("-1")) {
            throw new RuntimeException("Server rejected the request. It might be due to wrong token or invalid shuttle format.");
        }
    }
}
