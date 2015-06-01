package com.skp.di.rake.client.api;

import org.json.JSONObject;

public interface Rake {
    void track(JSONObject json);
    void flush();
}
