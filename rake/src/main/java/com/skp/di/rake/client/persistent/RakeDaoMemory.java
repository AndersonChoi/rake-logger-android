package com.skp.di.rake.client.persistent;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RakeDaoMemory implements RakeDao {

    private List<JSONObject> logQueue;

    public RakeDaoMemory() {
        logQueue = new ArrayList<>();
    }

    @Override
    public int add(JSONObject log) {
        logQueue.add(log);
        return getCount();
    }

    @Override
    public int add(List<JSONObject> logs) {
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

        return list;
    }

    @Override
    public void clear() {
        logQueue.clear();
    }
}
