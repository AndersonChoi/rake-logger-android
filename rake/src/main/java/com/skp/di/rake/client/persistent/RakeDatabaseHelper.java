package com.skp.di.rake.client.persistent;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.utils.RakeLogger;
import com.skp.di.rake.client.utils.RakeLoggerFactory;

import java.io.File;

public class RakeDatabaseHelper extends SQLiteOpenHelper {
    /* member variables */
    private File databaseFile;
    private RakeLogger debugLogger;

    /* constructor */
    RakeDatabaseHelper(RakeUserConfig config,
                       Context context,
                       String databaseName,
                       int databaseVersion) {

        super(context, databaseName, null, databaseVersion);

        databaseFile = context.getDatabasePath(databaseName);
        debugLogger = RakeLoggerFactory.getLogger(this.getClass(), config);
    }

    /**
     * Completely deletes the DB file from the file system.
     */
    public void dropDatabase() {
        close();
        databaseFile.delete();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        debugLogger.i("Creating a new Rake Database");

        db.execSQL(RakeDaoSQLite.QUERY_CREATE_EVENTS_TABLE);
        db.execSQL(RakeDaoSQLite.QUERY_EVENTS_TIME_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        debugLogger.i("Upgrading base, drop and creating new table [" +
                RakeDaoSQLite.Table.EVENTS.getName() + "]");

        db.execSQL("DROP TABLE IF EXISTS " +
                RakeDaoSQLite.Table.EVENTS.getName());

        db.execSQL(RakeDaoSQLite.QUERY_CREATE_EVENTS_TABLE);
        db.execSQL(RakeDaoSQLite.QUERY_EVENTS_TIME_INDEX);
    }
}
