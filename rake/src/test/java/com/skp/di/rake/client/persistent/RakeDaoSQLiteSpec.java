package com.skp.di.rake.client.persistent;

import com.skp.di.rake.client.mock.SampleDevConfig;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)

public class RakeDaoSQLiteSpec {

    RakeDao rakeDao = new RakeDaoSQLite(new SampleDevConfig(), )

    @Before
    public void setUp() {
    }
}
