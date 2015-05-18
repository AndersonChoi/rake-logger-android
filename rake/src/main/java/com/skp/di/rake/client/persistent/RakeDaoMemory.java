package com.skp.di.rake.client.persistent;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class RakeDaoMemory implements RakeDao {

    private List<JSONObject> logQueue;
    private int maxTrackCount;

    public RakeDaoMemory() {
        logQueue = new ArrayList<>();
    }

    @Override
    public void add(JSONObject log) {
        logQueue.add(log);
    }

    @Override
    public void add(List<JSONObject> logs) {
        logQueue.addAll(logs);
    }

    @Override
    public int getCount() {
        return logQueue.size();
    }

    @Override
    public JSONObject pop() {
        return (logQueue.size() == 0) ? null : logQueue.remove(0);
    }

    @Override
    public List<JSONObject> pop(int count) {
        // TODO
        return null;
    }

    @Override
    public List<JSONObject> clear() {
        List<JSONObject> removed = new ArrayList<>(logQueue);
        logQueue.clear();
        return removed;
    }

    @Override
    public int getMaxTrackCount() {
        return maxTrackCount;
    }

    @Override
    public void setMaxTrackCount(int n) {
        maxTrackCount = n;
    }
}
