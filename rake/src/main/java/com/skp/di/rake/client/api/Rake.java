package com.skp.di.rake.client.api;

import org.json.JSONObject;

public interface Rake {
    void track(JSONObject json);
    String flush();
    int getCount();
    void clear();
}
