package com.skp.di.rake.client.api.impl;

import com.skp.di.rake.client.api.Rake;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class RakeImpl implements Rake {
    private RakeDao dao;
    private RakeHttpClient client;

    public RakeImpl(RakeDao dao, RakeHttpClient client) {
        this.dao = dao;
        this.client = client;
    }

    @Override
    public void track(JSONObject log) {
        if (null == log) return;
        if (log.toString().equals("{\"\":\"\"}")) return;

        if (RakeMetaConfig.MAX_TRACK_COUNT == dao.getCount())
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

        /* buildRequestBody returns json string */
        String body = buildRequestBody(tracked);
        String responseMessage = null;

        if (null != body) responseMessage = client.send(body);

        /* returning null means that sent nothing */
        return responseMessage;
    }

    private String buildRequestBody(List<JSONObject> tracked) {
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
        } catch (JSONException e) {
            Logger.e("Can't create request body", e);
        }

        String body = flushed.toString();

        return body;
    }

    @Override
    public void clear() {
        dao.clear();
    }
}
