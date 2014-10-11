package org.openhab.habdroid.model.topview;

/**
 * Created by staufferr on 10.10.2014.
 */
public class TopViewParsingException extends RuntimeException {
    public TopViewParsingException(String message) {
        super(message);
    }

    public TopViewParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
