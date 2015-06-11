package com.skp.di.rake.client.android;

import android.app.Application;
import android.content.Context;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class SystemInformationSpec {

    public static class TestApplication extends Application {}

    static private SystemInformation sysInfo = null;
    static private Context appContext;

    @BeforeClass
    static public void setUpBeforeClass() {
        ShadowLog.stream = System.out;
        // appContext = new TestApplication().getApplicationContext();
        appContext = RuntimeEnvironment.application;
        // sysInfo = SystemInformation.getInstance(appContext);
    }

    @Test
    public void testIsSystemInformationSingleton() {
//        SystemInformation info1 = SystemInformation.getInstance(appContext);
//        SystemInformation info2 = SystemInformation.getInstance(appContext);
//
//        assertTrue(info1 == info2);
    }
}
