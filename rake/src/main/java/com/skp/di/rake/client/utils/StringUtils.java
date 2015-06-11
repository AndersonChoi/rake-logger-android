package com.skp.di.rake.client.utils;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StringUtils {

    static public String encodeBase64(String str) {
        return Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
    }

    static public String decodeBase64(String encoded) {
        return new String(Base64.decode(encoded.getBytes(), Base64.DEFAULT));
    }

    public static String join(String[] arr, String sep) {
        if (null == arr) return null;
        if (arr.length < 1) return null;
        if (null == sep) sep = "";

        StringBuffer buffer = new StringBuffer();
        int lastIndex = arr.length - 1;
        boolean isLastNil = false;

        for (int i = 0; i <= lastIndex; i++) {
            if (null == arr[i] || arr[i].equals("")) {
                isLastNil = true;
                continue;
            }

            buffer.append(arr[i]);
            isLastNil = false;

            if (i != lastIndex) /* is not last index */
                buffer.append(sep);
        }

        String result = buffer.toString();

        /* remove sep which append to end of the string */
        if (isLastNil && !result.equals("") && !sep.equals("")) {
            result = result.substring(0, result.length() - 1);
        }

        return (result.equals("")) ? null : result;
    }

    public static String toString(final InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        br = new BufferedReader(new InputStreamReader(is));

        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            RakeLogger.e("Can't convert inputstream to string", e);
        } finally {
            closeQuietly(br);
        }

        return sb.toString();
    }

    public static void closeQuietly(final InputStream is) {
        closeQuietly((Closeable) is);
    }

    public static void closeQuietly(final BufferedReader br) {
        closeQuietly((Closeable) br);
    }

    public static void closeQuietly(final Closeable is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // TODO: print stacktrace using a custom logger, or just ignore
            }
        }
    }
}
