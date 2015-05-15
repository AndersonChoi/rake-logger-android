package com.skp.di.rake.client.persistent;

        import org.json.JSONObject;

        import java.util.List;

public interface RakeDao {

    void add(JSONObject log);
    void add(List<JSONObject> logList);

    int getCount();

    JSONObject pop();
    List<JSONObject> pop(int count);
    List<JSONObject> clear();
}
