package com.skp.di.rake.client.utils;


import com.skp.di.rake.client.utils.StringUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class StringUtilsSpec {

    @Test
    public void testJoin() {
        String log = "{ \"version\": \"0.0.1\"}";
        String[] logs = new String[] { log, log };

        String expected1 = String.format("%s\n%s", log, log);
        assertEquals(expected1, StringUtils.join(logs, "\n"));

        String expected2 = String.format("%s\t%s", log, log);
        assertEquals(expected2, StringUtils.join(logs, "\t"));
    }

    @Test
    public void testJoinNullAndEmptyArray() {
        assertEquals(null, StringUtils.join(null, "\t"));

        assertEquals(null, StringUtils.join(new String[]{}, "\t"));

        assertEquals(null, StringUtils.join(new String[]{""}, "\t"));

        assertEquals(null, StringUtils.join(new String[]{null}, "\t"));

        assertEquals("1@2@3", StringUtils.join(new String[]{null, "1", null, "2", "3", null, null}, "@"));

        assertEquals("1@2@3", StringUtils.join(new String[]{"", "1", "", "2", "3", null, ""}, "@"));

        assertEquals("123", StringUtils.join(new String[]{"", "1", "", "2", "3", null, ""}, null));
    }

    // TODO: research potential performance problems
    @Test
    public void testInputStreamToString() {
        InputStream is = new ByteArrayInputStream("Hello World".getBytes());

        String converted = StringUtils.toString(is);
        assertEquals("Hello World", converted);
    }

    @Test
    public void testEncodeBase64() {
        String origin = "[Hello World! & Java World! ++ Scala World!]";

        String encoded = StringUtils.encodeBase64(origin);
        String decoded = StringUtils.decodeBase64(encoded);

        assertEquals(origin, decoded);
    }
}
