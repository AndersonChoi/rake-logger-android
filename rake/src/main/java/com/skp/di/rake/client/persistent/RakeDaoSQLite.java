package com.skp.di.rake.client.persistent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.utils.RakeLogger;
import com.skp.di.rake.client.utils.RakeLoggerFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Not thread-safe.
 * Instances of this class should only be used by a single thread.
 */

public class RakeDaoSQLite implements RakeDao {

    /* constants, enums */
    private static final String DATABASE_NAME = "rake";
    private static final int DATABASE_VERSION = 4;

    public static final String KEY_DATA = "data";
    public static final String KEY_CREATED_AT = "created_at";

    public static final String QUERY_CREATE_EVENTS_TABLE =
            "CREATE TABLE " + Table.EVENTS.getName() +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_DATA + " STRING NOT NULL, " +
                    KEY_CREATED_AT + " INTEGER NOT NULL);";

    public static final String QUERY_EVENTS_TIME_INDEX =
            "CREATE INDEX IF NOT EXISTS time_idx ON " +
                    Table.EVENTS.getName() +
                    " (" + KEY_CREATED_AT + ");";

    public enum Table {
        EVENTS("events");
        Table(String name) { tableName = name; }
        public String getName() { return tableName; }
        private final String tableName;
    }

    public static final Table RAKE_LOG_TABLE = Table.EVENTS;

    /* member variables */
    private final RakeDatabaseHelper dbHelper;
    private RakeLogger debugLogger;

    /* constructor */
    public RakeDaoSQLite(RakeUserConfig config, Context context) {
        this.debugLogger = RakeLoggerFactory.getLogger(this.getClass(), config);
        debugLogger.i("Rake Database '" + DATABASE_NAME + "' constructed.");
        dbHelper = new RakeDatabaseHelper(config, context, DATABASE_NAME, DATABASE_VERSION);
    }

    /* methods */

    public int add(JSONObject json) {
        if (null == json) return -1;

        return add(Arrays.asList(json));
    }

    public int add(List<JSONObject> list) {
        if (null == list || 0 == list.size())
            return -1;

        debugLogger.i("Add " + list.size() + " log to database");

        int count = -1;
        String tableName = RAKE_LOG_TABLE.getName();

        Cursor c = null;

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            for(JSONObject json : list) {
                ContentValues cv = new ContentValues();
                cv.put(KEY_DATA, json.toString());
                cv.put(KEY_CREATED_AT, System.currentTimeMillis());
                db.insert(tableName, null, cv);
            }

            c = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
            c.moveToFirst();
            count = c.getInt(0);
        } catch (SQLiteException e) {
            debugLogger.e("Add failed", e);

            // We assume that in general, the results of a SQL exception are
            // unrecoverable, and could be associated with an oversized or
            // otherwise unusable DB. Better to bomb it and get back on track
            // than to leave it junked up (and maybe filling up the disk.)
            dbHelper.dropDatabase();
        } finally {
            dbHelper.close();
            if (c != null) c.close();
        }

        return count;
    }

    /* do not use this function out of transaction scope */
    private void _removeRowOldest (long time, SQLiteDatabase db) {
        String tableName = RAKE_LOG_TABLE.getName();

        debugLogger.i("Clean up all events until [time: " + time + "] from table " + tableName);

        try {
            db.delete(tableName, KEY_CREATED_AT + " <= " + time, null);
        } catch (SQLiteException e) {
            debugLogger.e("Clean up event time failed. Delete DB.", e);

            // We assume that in general, the results of a SQL exception are
            // unrecoverable, and could be associated with an oversized or
            // otherwise unusable DB. Better to bomb it and get back on track
            // than to leave it junked up (and maybe filling up the disk.)
            dbHelper.dropDatabase();
        }
    }

    public List<JSONObject> getAndRemoveOldest(int N) {
        Cursor c = null;
        String tableName = RAKE_LOG_TABLE.getName();
        List<JSONObject> result = new ArrayList<>();
        Long latest = null;

        String QUERY_GET_OLDEST_ROW_N =
                "SELECT * FROM " + tableName + " ORDER BY " + KEY_CREATED_AT + " ASC LIMIT " + N;

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            c = db.rawQuery(QUERY_GET_OLDEST_ROW_N, null);
            String storedJsonString = null;

            while (c.moveToNext()) {
                if (c.isLast()) latest = c.getLong(c.getColumnIndex(KEY_CREATED_AT));

                try {
                    storedJsonString = c.getString(c.getColumnIndex(KEY_DATA));
                    JSONObject json = new JSONObject(storedJsonString);
                    result.add(json);
                } catch (JSONException e) {
                    // TODO metric, failed count
                    debugLogger.e("Failed to convert stored String into JSONObject: " + storedJsonString, e);
                }
            }

            if (null != latest) _removeRowOldest(latest, db);

        } catch (SQLiteException e) {
            // TODO metric, failed count
            debugLogger.e("Clear Oldest N log failed.", e);

            // We'll dump the DB on write failures, but with reads we can
            // let things ride in hopes the issue clears up.
            // (A bit more likely, since we're opening the DB for read and not write.)
            // A corrupted or disk-full DB will be cleaned up on the next write or clear call.
        } finally {
            dbHelper.close();
            if (c != null) { c.close(); }
        }

        // if empty, return null
        return (null != result && 0 != result.size()) ? result : null;
    }

    @Override
    public int getCount() {
        String QUERY_GET_COUNT = "SELECT * FROM " + RAKE_LOG_TABLE.getName();
        Cursor cursor = null;
        int count = -1;

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery(QUERY_GET_COUNT, null);
            count = cursor.getCount();
        } catch(SQLiteException e) {
            debugLogger.e("Can't get count due to", e);
            dbHelper.close();

            if (null != cursor) cursor.close();
        }

        return count;
    }

    @Override
    public void clear() {
        dbHelper.dropDatabase();
    }
}
