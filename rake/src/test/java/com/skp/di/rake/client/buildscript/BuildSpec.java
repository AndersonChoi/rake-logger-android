package com.skp.di.rake.client.buildscript;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class BuildSpec {

    @Test
    public void testLambda() {
        List<String> arr = new ArrayList<>();
        arr.add("abc");
        arr.add("def");
        arr.add("a");

        Collections.sort(arr, (str1, str2) -> str1.compareTo(str2));
        assertEquals("a", arr.get(0));
    }
}
