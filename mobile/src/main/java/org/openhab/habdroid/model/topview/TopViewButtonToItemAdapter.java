package org.openhab.habdroid.model.topview;

import android.graphics.Color;
import android.util.Log;
import android.widget.Button;

import org.openhab.habdroid.R;
import org.openhab.habdroid.model.OpenHABItem;
import org.openhab.habdroid.model.topview.common.types.SendCommandResult;

/**
 * Created by staufferr on 10.10.2014.
 */
public class TopViewButtonToItemAdapter {
    private final String TAG = this.getClass().getName();

    private final TopViewButtonDescriptor buttonDescriptor;
    private final Button button;

    // Potentially changed by an arbitrary thread, maybe a UI helper background thread
    private volatile boolean isOnline;
    // Potentially changed by a model (lower end) (from a background thread)
    private volatile OpenHABItem item;
    private volatile long lastItemUpdateTimestamp;
    private volatile boolean buttonState;
    private volatile long lastStateUpdateTimestamp;

    // Potentially changed by a model (lower end) (from a background thread)
    private volatile boolean isSendingCommand;
    private volatile boolean hasError;

    private final SendCommandHandler sendCommandHandler;

    private final Object monitor = new Object();

    public TopViewButtonDescriptor getButtonDescriptor() {
        return buttonDescriptor;
    }

    public Button getButton() {
        return button;
    }

    public boolean isOnline() {
        return isOnline;
    }

//    public OpenHABItem getItem() {
//        synchronized (monitor) {
//            return item;
//        }
//    }

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

        updateButton();
    }

    // Potentially called from an arbitrary thread, maybe a UI helper background thread
    public boolean checkOnlineStatus() {
        synchronized (monitor) {
            long timeElapsedSinceLastItemUpdate = System.nanoTime() - lastItemUpdateTimestamp;
            isOnline = item != null &&
                /* last bound timestamp is not older than 6 minutes (remember: the communicator maximum cycle time is 5 minutes) */ timeElapsedSinceLastItemUpdate <= 6l * 60 * 1000 * 1000 * 1000;
        }

        //button.invalidate();
        updateButton();

        return isOnline;
    }

    // Potentially called from a model (lower end) (from a background thread)
    public void updateItem(OpenHABItem item) {
        if (item == null) {
            throw new NullPointerException("item must not be null");
        }

        synchronized (monitor) {
            this.item = item;
            lastItemUpdateTimestamp = System.nanoTime();

            checkOnlineStatus();
        }

        boolean newButtonState = "ON".equals(item.getState());
        updateButtonState(newButtonState);
    }

    // Potentially called from a model (lower end) (from a background thread)
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
            //button.invalidate();
            updateButton();
        }
    }

    // Helper
    private void updateButton() {
        button.post(new Runnable() {
            public void run() {

                if (isOnline) {
                    if (isSendingCommand) {
                        button.setText(">>>");
                        button.setTextColor(Color.GREEN);
                    } else if (hasError) {
                        button.setText("!");
                        button.setTextColor(Color.RED);
                    } else {
                        button.setText("");
                    }
                    button.setBackgroundDrawable(button.getContext().getResources().getDrawable(R.drawable.top_view_button_background)); // Set background
                    if (buttonState) {
                        button.getBackground().setAlpha(255);
                    } else {
                        button.getBackground().setAlpha(0);
                    }
                } else {
                    //TODO Do something appropriate (maybe draw a big red cross over the button?)
                    Log.d(TAG, String.format("Button %s is offline.", buttonDescriptor.getItem()));

                    button.setText("Offline");
                    button.setTextColor(Color.RED);
                    button.setBackgroundDrawable(button.getContext().getResources().getDrawable(R.drawable.top_view_button_offline_background)); // Set background
                    button.getBackground().setAlpha(255);
                }

            }
        });
    }

    // Called from the UI (button) (upper end) (from the UI thread)
    public void sendCommand(Command command) {
        // Check precondition: ...
        if (isSendingCommand) {
            return;
        }

        synchronized (monitor) {
            // Check precondition: ...
            long timeElapsedSinceLastStateUpdate = System.nanoTime() - lastStateUpdateTimestamp;
            if (timeElapsedSinceLastStateUpdate < /* 2 seconds: */ 2l * 1000 * 1000 * 1000) {
                Log.d(TAG, String.format("Button press ignored because last external state update was only %d ns ago!", timeElapsedSinceLastStateUpdate));

                return;
            }
        }

        // Check precondition: Button is "online" (= there's an item bound to the button)
        if (!checkOnlineStatus()) {
            Log.d(TAG, "Button press ignored because the button is not online!");

            return;
        }

        boolean newButtonState = !buttonState;

        Log.d(TAG, String.format("Switch %s to %s...", buttonDescriptor.getItem(), newButtonState ? "ON" : "OFF"));

        isSendingCommand = true;

        // Immediately update the button state (and do not wait for the response from openHAB)
        updateButtonState(newButtonState, /* Internal call: */ true);

        synchronized (monitor) {
            sendCommandHandler.sendCommand(item, getCommandAsString(command, newButtonState), new SendCommandResult() {
                @Override
                protected void processResult(CommandResult result) {
                    // Potentially called from a model (lower end) (from a background thread)

                    isSendingCommand = false;
                    hasError = result != CommandResult.SUCCESS;

                    updateButton();
                }
            });
        }
    }

    // Helper
    private String getCommandAsString(Command command, boolean newButtonState) {
        String commandAsString = null;
        switch (command) {
            case TOGGLE:
                commandAsString = newButtonState ? "ON" : "OFF";
        }

        return commandAsString;
    }

    private static enum ItemType {
        BUTTON,
        SWITCH
    }

    public static enum Command {
        TOGGLE
    }

    public static interface SendCommandHandler {
        // Called from the UI (button) (upper end) (from the UI thread)
        void sendCommand(OpenHABItem item, String command, SendCommandResult result);
    }
}