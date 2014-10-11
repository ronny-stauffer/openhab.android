package org.openhab.habdroid.model.topview;

import android.util.Log;
import android.widget.Button;

import org.openhab.habdroid.model.OpenHABItem;

/**
 * Created by staufferr on 10.10.2014.
 */
public class TopViewButtonToItemAdapter {
    private final String TAG = this.getClass().getName();

    private final TopViewButtonDescriptor buttonDescriptor;
    private final Button button;

    private OpenHABItem item;

    private volatile boolean buttonState;
    private volatile long lastStateUpdateTimestamp;

    private final SendCommandHandler sendCommandHandler;

    private final Object monitor = new Object();

    public TopViewButtonDescriptor getButtonDescriptor() {
        return buttonDescriptor;
    }

    public Button getButton() {
        return button;
    }

    public OpenHABItem getItem() {
        return item;
    }

    public boolean getButtonState() {
        return buttonState;
    }

    public TopViewButtonToItemAdapter(TopViewButtonDescriptor buttonDescriptor, Button button, SendCommandHandler sendCommandHandler) {
        if (buttonDescriptor == null) {
            throw new NullPointerException("buttonDescriptor must not be undefined!");
        }
        if (button == null) {
            throw new NullPointerException("button must not be undefined!");
        }
        if (sendCommandHandler == null) {
            throw new NullPointerException("sendCommandHandler must not be undefined!");
        }

        this.buttonDescriptor = buttonDescriptor;
        this.button = button;
        this.sendCommandHandler = sendCommandHandler;
    }

    // Called from the model (lower end) (from a background thread)
    public void updateItem(OpenHABItem item) {
        if (item == null) {
            throw new NullPointerException("item must not be null");
        }

        synchronized (monitor) {
            this.item = item;
        }

        boolean newButtonState = "ON".equals(item.getState());
        updateButtonState(newButtonState);
    }

    // Called from the model (lower end) (from a background thread)
    private void updateButtonState(boolean buttonState) {
        updateButtonState(buttonState, false);
    }

    private void updateButtonState(boolean buttonState, boolean isInternalCall) {
        boolean oldButtonState = this.buttonState;

        this.buttonState = buttonState;

        if (buttonState != oldButtonState) {
            if (!isInternalCall) {
                synchronized (monitor) {
                    lastStateUpdateTimestamp = System.nanoTime();
                }
            }

            //button.refreshDrawableState();
            button.invalidate();
        }
    }

    // Called from the UI (button) (upper end) (from the UI thread)
    public boolean toggleCommand() {
        synchronized (monitor) {
            // Check precondition: ...
            long timeElapsedSinceLastStateUpdate = System.nanoTime() - lastStateUpdateTimestamp;
            if (timeElapsedSinceLastStateUpdate < /* 2 seconds: */ 2l * 1000 * 1000 * 1000) {
                Log.d(TAG, String.format("Button press ignored (last external state update was %d ns ago)!", timeElapsedSinceLastStateUpdate));

                return buttonState;
            }
        }

        boolean newButtonState = !buttonState;

        Log.d(TAG, String.format("Switch button %s to %s...", buttonDescriptor.getItem(), newButtonState ? "ON" : "OFF"));

        // Immediately update the button state (and do not wait for the response from openHAB)
        updateButtonState(newButtonState, /* Internal call: */ true);

        synchronized (monitor) {
            sendCommandHandler.sendCommand(item, getCommandAsString(Command.TOGGLE, newButtonState));
        }

        return newButtonState;
    }

    private String getCommandAsString(Command command, boolean newButtonState) {
        String commandAsString = null;
        switch (command) {
            case TOGGLE:
                commandAsString = newButtonState ? "ON" : "OFF";
        }

        return commandAsString;
    }

    private static enum Command {
        TOGGLE
    }

    public static interface SendCommandHandler {
        // Called from the UI (button) (upper end) (from the UI thread)
        void sendCommand(OpenHABItem item, String command);
    }
}