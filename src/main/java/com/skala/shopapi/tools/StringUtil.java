package com.skala.shopapi.tools;

public class StringUtil {

    private StringUtil() {
    }

    public static boolean isAnyEmpty(String... values) {
        if (values == null || values.length == 0) {
            return true;
        }
        for (String value : values) {
            if (value == null || value.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
