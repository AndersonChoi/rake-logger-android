package com.skp.di.rake.client.persistent;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class RakeDaoMemory implements RakeDao {

    private List<JSONObject> logQueue;

    public RakeDaoMemory() {
        logQueue = new ArrayList<>();
    }

    @Override
    public int add(JSONObject log) {
        if (null == log) return -1;
        return add(Arrays.asList(log));
    }

    @Override
    public int add(List<JSONObject> logs) {
        if (null == logs || 0 == logs.size()) return -1;
        logQueue.addAll(logs);
        return getCount();
    }

    public int getCount() {
        return logQueue.size();
    }

    @Override
    public List<JSONObject> getAndRemoveOldest(int N) {
        int count = (N < getCount()) ? getCount() : N;

        if (0 == count) return null;

        Iterator<JSONObject> i = logQueue.iterator();
        List<JSONObject> list = new ArrayList<JSONObject>();

        while(i.hasNext()) {
            list.add(i.next());
            i.remove();
        }

        // if empty, return null;
        return (null != list && 0 != list.size()) ? list : null;
    }

    @Override
    public void clear() {
        logQueue.clear();
    }
}
