package com.skp.di.rake.client.logger;

import com.skp.di.rake.client.config.RakeLoggerConfig;
import com.skp.di.rake.client.persistent.RakeDao;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

public final class RakeLogger implements Rake {

    private RakeDao dao;
    private String endPoint = RakeLoggerConfig.END_POINT;

    public RakeLogger(RakeDao dao) {
        this.dao = dao;
    }

    @Override
    public void track(JSONObject log) {
        if (null == log) return;
        if (log.toString().equals("{\"\":\"\"}")) return;

        if (RakeLoggerConfig.MAX_TRACK_COUNT == dao.getCount())
            flush();

        dao.add(log);
    }

    @Override
    public int getCount() {
        return dao.getCount();
    }

    @Override
    public String flush() {
        List<JSONObject> tracked = dao.clear();

        String body = createBody(tracked);

        if (null != body) send(body);

        /* returning null means that nothing was sent */
        return body;
    }

    private String createBody(List<JSONObject> tracked) {
        if (0 == tracked.size()) return null;

        Iterator<JSONObject> i = tracked.iterator();
        JSONArray dataField = new JSONArray();

        while(i.hasNext()) {
            JSONObject log = i.next();
            dataField.put(log);
        }

        JSONObject flushed = new JSONObject();

        try {
            flushed.put("data", dataField);
        } catch(JSONException e) {
            e.printStackTrace();
            return null;
        }

        String body = flushed.toString();

        return body;
    }

    private void send(String body) {
        HttpClient client = createHttpClient();
        HttpPost post     = createHttpPost(body);

        try {
            client.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HttpClient createHttpClient() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, RakeLoggerConfig.CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, RakeLoggerConfig.SOCKET_TIMEOUT);
        HttpClient client = new DefaultHttpClient(params);

        return client;
    }

    private HttpPost createHttpPost(String body) {
        HttpPost post = new HttpPost(endPoint);
        StringEntity se = null;

        try {
            se = new StringEntity(body);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        post.setEntity(se);
        post.setHeader("Content-Type", "application/json; charset=utf-8");

        return post;
    }

    @Override
    public void clear() {
        dao.clear();
    }
}
