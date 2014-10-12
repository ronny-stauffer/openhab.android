package org.openhab.habdroid.model.topview.common;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by staufferr on 12.10.2014.
 */
public final class StreamUtil {
    private StreamUtil() {
        // Prevent instantiation
    }

    public static void resetStream(InputStream stream) {
        try {
            stream.reset();
        } catch (IOException e) {
            throw new RuntimeException("I/O error while resetting stream!", e);
        }
    }

    public static void closeStream(InputStream stream) {
        assert stream != null;

        try {
            stream.close();
        } catch (IOException e) {
            // Ignore exception
        }
    }
}
