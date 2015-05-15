package com.skp.di.rake.client.logger;

import org.json.JSONObject;

public interface Rake {
    void track(JSONObject json);
    String[] flush();
    int getCount();
    void clear();

    String getEndPoint();
    void setEndPoint(String url);
}
