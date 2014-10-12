package org.openhab.habdroid.model.topview.common;

/**
 * Created by staufferr on 10.10.2014.
 */
public final class StringUtil {
    private StringUtil() {
        // Prevent instantiation
    }

    public static boolean isStringUndefinedOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
