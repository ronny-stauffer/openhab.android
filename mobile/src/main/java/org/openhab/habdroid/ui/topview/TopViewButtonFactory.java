package org.openhab.habdroid.ui.topview;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;

import org.openhab.habdroid.R;
import org.openhab.habdroid.model.topview.TopViewButtonDescriptor;
import org.openhab.habdroid.model.topview.TopViewButtonToItemAdapter;

/**
 * Created by staufferr on 10.10.2014.
 */
public class TopViewButtonFactory {
    private final String TAG = this.getClass().getName();

    private final Context context;

    public TopViewButtonFactory(Context context) {
        if (context == null) {
            throw new NullPointerException("context must not be undefined!");
        }

        this.context = context;
    }

    public TopViewButtonToItemAdapter create(final TopViewButtonDescriptor buttonDescriptor, TopViewButtonToItemAdapter.SendCommandHandler sendCommandHandler) {
        if (buttonDescriptor == null) {
            throw new NullPointerException("buttonDescriptor must not be null!");
        }
        if (sendCommandHandler == null) {
            throw new NullPointerException("sendCommandHandler must not be null!");
        }

        final Button button = new Button(context) {
//            @Override
////            public boolean onPreDraw() {
//            protected void onDraw(Canvas canvas) {
////                long threadId = Thread.currentThread().getId();
////                String threadName = Thread.currentThread().getName();
//
//                super.onDraw(canvas);
//
//                TopViewButtonToItemAdapter toItemAdapter = (TopViewButtonToItemAdapter)getTag();
//                if (toItemAdapter != null) { // Just to be on the safe side
//                    if (toItemAdapter.isOnline()) {
//                        getBackground().setAlpha(toItemAdapter.getButtonState() ? 255 : 0);
//                    } else {
//                        //TODO Do something appropriate (maybe draw a big red cross over the button?)
//                        Log.d(TAG, String.format("Button %s is offline.", toItemAdapter.getButtonDescriptor().getItem()));
//
//                        getBackground().setAlpha(0);
//                    }
//                }
//
////                return super.onPreDraw();
//            }
        };
//        button.setText(buttonDescriptor.getItem()); // Set text
//        button.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.top_view_button_background)); // Set background
//        button.getBackground().setAlpha(0); // Set background to transparent
//        button.getBackground().setAlpha(10); // Set background to nearly transparent
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TopViewButtonToItemAdapter toItemAdapter = (TopViewButtonToItemAdapter)view.getTag();
                if (toItemAdapter != null) {
                    toItemAdapter.sendCommand(TopViewButtonToItemAdapter.Command.TOGGLE);
                }

//                new AlertDialog.Builder(context)
//                .setTitle("Event")
//                .setMessage(String.format("%s was clicked!", buttonDescriptor.getItem()))
//                .create().show();
            }
        });
//        button.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                return true;
//            }
//        });
        TopViewButtonToItemAdapter toItemAdapter = new TopViewButtonToItemAdapter(buttonDescriptor, button, sendCommandHandler);
        button.setTag(toItemAdapter);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(buttonDescriptor.getWidth(), buttonDescriptor.getHeight()); // Mandatory parameters
        layoutParams.setMargins(buttonDescriptor.getLeft(), buttonDescriptor.getTop(), 0, 0); // Determines position
        button.setLayoutParams(layoutParams);

//        return button;
        return toItemAdapter;
    }
}