package com.skp.di.rake.client.persistent;

import org.json.JSONObject;

import java.util.List;

public interface RakeDao {

    void add(JSONObject log);
    List<JSONObject> clear();
    int getCount();

    // TODO
    void add(List<JSONObject> logList);
    JSONObject pop();
    List<JSONObject> pop(int count);
}
