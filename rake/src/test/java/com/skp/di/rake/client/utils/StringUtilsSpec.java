package com.skp.di.rake.client.utils;


import com.skp.di.rake.client.utils.StringUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
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
}
