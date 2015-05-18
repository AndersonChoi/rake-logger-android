package com.skp.di.rake.client.persistent;

import org.json.JSONObject;

import java.util.List;

public class RakeDaoSQLite implements RakeDao {
    @Override
    public void add(JSONObject log) {

    }

    @Override
    public void add(List<JSONObject> logList) {

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public JSONObject pop() {
        return null;
    }

    @Override
    public List<JSONObject> pop(int count) {
        return null;
    }

    @Override
    public List<JSONObject> clear() {
        return null;
    }

    @Override
    public int getMaxTrackCount() {
        return 0;
    }

    @Override
    public void setMaxTrackCount(int n) {

    }
}
