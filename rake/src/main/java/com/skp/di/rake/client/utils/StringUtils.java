package com.skp.di.rake.client.utils;

public class StringUtils {

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
}
