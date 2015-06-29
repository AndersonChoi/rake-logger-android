package com.skp.di.rake.client.protocol;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface RakeProtocol {
    HttpPost buildRequest(List<JSONObject> tracked, String endPoint) throws UnsupportedEncodingException, JSONException;
    void verifyResponse(int statusCode, String responseBody);
}
