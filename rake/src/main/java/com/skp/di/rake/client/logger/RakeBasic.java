package com.skp.di.rake.client.logger;

import com.skp.di.rake.client.config.RakeConfig;
import com.skp.di.rake.client.config.RakeConfig;
import com.skp.di.rake.client.persistent.RakeDao;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class RakeBasic implements Rake {

    private RakeDao dao;
    private String endPoint = RakeConfig.END_POINT;

    public RakeBasic(RakeDao dao) {
        this.dao = dao;
    }

    @Override
    public String getEndPoint() {
        return endPoint;
    }

    @Override
    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public void track(JSONObject log) {
        dao.add(log);
    }

    @Override
    public int getCount() {
        return dao.getCount();
    }

    @Override
    public String[] flush() {
        List<String> flushed = new ArrayList<>();
        List<JSONObject> tracked = dao.clear();

        Iterator<JSONObject> i = tracked.iterator();

        while(i.hasNext()) {
            JSONObject log = i.next();
            flushed.add(log.toString());
        }

        return flushed.toArray(new String[flushed.size()]);
    }

    @Override
    public void clear() {
        dao.clear();
    }
}
