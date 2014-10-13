package org.openhab.habdroid.model.topview.common.types;

/**
 * Created by staufferr on 13.10.2014.
 */
public abstract class SendCommandResult {
    public static enum CommandResult {
        SUCCESS,
        FAILURE
    }

    // Potentially called from a model (lower end) (from a background thread)
    public final void success() {
        processResult(CommandResult.SUCCESS);
    }

    // Potentially called from a model (lower end) (from a background thread)
    public final void failure() {
        processResult(CommandResult.FAILURE);
    }

    protected abstract void processResult(CommandResult result);
}