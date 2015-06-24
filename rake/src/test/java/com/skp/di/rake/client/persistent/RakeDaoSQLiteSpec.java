package com.skp.di.rake.client.persistent;

import com.skp.di.rake.client.utils.RakeTestUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeDaoSQLiteSpec {

    RakeDao rakeDao;

    @Before
    public void setUp() {

        rakeDao = new RakeDaoSQLite(
                RakeTestUtils.createDevConfig1(),
                RuntimeEnvironment.application);
    }

    @Test
    public void test_add() {
        assertEquals(0, rakeDao.getCount());
        rakeDao.add(new JSONObject());
        rakeDao.add(new JSONObject());
        assertEquals(2, rakeDao.getCount());
    }

    @Test
    public void test_getAndRemoveOldest() throws JSONException {
        for(int i = 0; i < 10; i++) {
            JSONObject log = new JSONObject();
            log.put("key", "value" + i);
            rakeDao.add(log);
        }

        List<JSONObject> list = rakeDao.getAndRemoveOldest(5);

        // tracked log should be in order.
        for(int i = 0; i < 5; i++) {
            JSONObject log = list.get(i);
            assertEquals("value" + i, log.get("key"));
        }
    }

    @Test
    public void test_getAndRemoveOldest_should_return_null_when_empty() {
        assertEquals(null, rakeDao.getAndRemoveOldest(5));
    }
}

