package com.skp.di.rake.client.persistent;

import org.json.JSONObject;

import java.util.List;

public interface RakeDao {

    int add(JSONObject log);
    List<JSONObject> getAndRemoveOldest(int N);
    int add(List<JSONObject> logList);
    int getCount();
    void clear();
}
