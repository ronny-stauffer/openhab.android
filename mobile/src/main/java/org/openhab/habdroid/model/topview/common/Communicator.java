package org.openhab.habdroid.model.topview.common;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.loopj.android.http.AsyncHttpAbortException;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.openhab.habdroid.core.DocumentHttpResponseHandler;
import org.openhab.habdroid.model.OpenHABItem;
import org.openhab.habdroid.model.OpenHABWidget;
import org.openhab.habdroid.model.OpenHABWidgetDataSource;
import org.openhab.habdroid.ui.OpenHABMainActivity;
import org.openhab.habdroid.util.MyAsyncHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by staufferr on 11.10.2014.
 */
public class Communicator {
    private static final String TAG = "Communicator";

    private final Context context;

    // Per thread state
    private String sitemapPageUrl;
    private String mAtmosphereTrackingId;

    private final OpenHABWidgetDataSource openHABWidgetDataSource = new OpenHABWidgetDataSource();

    private StateUpdateHandler stateUpdateHandler;

    private final Handler networkHandler = new Handler();
    private Runnable networkRunnable;
    // Per thread state END

    public Communicator(Context context) {
        if (context == null) {
            throw new NullPointerException("context must not be undefined!");
        }

        this.context = context;
    }

    // Single-shot
    public void loadPage(String sitemapPageUrl, /* TODO: UpdateHandler... updateHandlers */ StateUpdateHandler stateUpdateHandler) {
        //TODO Implement
    }

    // Continuous
    public void startLoadingPage(String sitemapPageUrl, StateUpdateHandler stateUpdateHandler) {
        if (StringUtil.isStringUndefinedOrEmpty(sitemapPageUrl)) {
            throw new IllegalArgumentException("sitemapPageUrl must not be undefined or empty!");
        }
        if (stateUpdateHandler == null) {
            throw new NullPointerException("stateUpdateHandler must not be undefined!");
        }

        Log.i(TAG, "Starting communicator for page " + sitemapPageUrl + "...");

        this.sitemapPageUrl = sitemapPageUrl;
        this.stateUpdateHandler = stateUpdateHandler;

        loadPage(false);
    }

    public void restart() {
        //TODO Implement
    }

    private void loadPage(/* String pageUrl, */ final boolean notInitialRequest) {
//        Log.i(TAG, " showPage for " + pageUrl + " notInitialRequest = " + notInitialRequest);

        MyAsyncHttpClient mAsyncHttpClient = OpenHABMainActivity.getAsyncHttpClient();

        // Cancel any existing http request to openHAB (typically ongoing long poll)
//        if (!notInitialRequest)
//            startProgressIndicator();
        List<BasicHeader> headers = new LinkedList<BasicHeader>();
        headers.add(new BasicHeader("Accept", "application/xml"));
        headers.add(new BasicHeader("X-Atmosphere-Framework", "1.0"));
        if (notInitialRequest) {
            // Initiate long-polling
            mAsyncHttpClient.setTimeout(300000);
            headers.add(new BasicHeader("X-Atmosphere-Transport", "long-polling"));
            if (mAtmosphereTrackingId == null) {
                headers.add(new BasicHeader("X-Atmosphere-tracking-id", "0"));
            } else {
                headers.add(new BasicHeader("X-Atmosphere-tracking-id", mAtmosphereTrackingId));
            }
        } else {
            mAsyncHttpClient.setTimeout(10000);
            headers.add(new BasicHeader("X-Atmosphere-tracking-id", "0"));
        }
        Log.i(TAG, "Issue page load request " + (notInitialRequest ? "(long-polling) " : "") + "to " + sitemapPageUrl + " with tracking id: " + mAtmosphereTrackingId);
        mAsyncHttpClient.get(context, sitemapPageUrl, headers.toArray(new BasicHeader[]{}), null, new DocumentHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, Document document) {
//                long threadId = Thread.currentThread().getId();
//                String threadName = Thread.currentThread().getName();

                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].getName().equalsIgnoreCase("X-Atmosphere-tracking-id")) {
                        Log.i(TAG, "Found Atmosphere tracking id: " + headers[i].getValue());
                        mAtmosphereTrackingId = headers[i].getValue();
                    }
                }
                if (document != null) {
                    Log.d(TAG, "Processing response...");
//                    Log.d(TAG, "Response: " + document.toString());
//                    if (!notInitialRequest)
//                        stopProgressIndicator();
                    processContent(document /*, notInitialRequest */);
                } else {
                    Log.e(TAG, "Got a empty (<null>) response.");
                    loadPage(/* displayPageUrl, */ true);
                }
            }

            @Override
            public void onFailure(Throwable error, String content) {
                mAtmosphereTrackingId = null;
//                if (!notInitialRequest)
//                    stopProgressIndicator();
                if (error instanceof AsyncHttpAbortException) {
                    Log.d(TAG, "Request for " + sitemapPageUrl + " was aborted! Cycle aborted!");
                    return;
                }
                if (error instanceof SocketTimeoutException) {
                    Log.d(TAG, "Connection timeout! Reconnecting...");
                    loadPage(/* displayPageUrl, */ false);
                    return;
                } else {
                    /*
                    * If we get a network error try connecting again, if the
                    * fragment is paused, the runnable will be removed
                    */
                    Log.e(TAG, "Error: " + error.getClass() + "! Cycle aborted! Restarting...");

                    networkHandler.removeCallbacks(networkRunnable);
                    networkRunnable = new Runnable() {
                        @Override
                        public void run() {
                            loadPage(/* displayPageUrl, */ false);
                        }
                    };
                    networkHandler.postDelayed(networkRunnable, 10 * 1000);
                }
            }
        }, /* tag: */ /* mTag */ this);
    }

    /**
     * Parse XML sitemap page and show it
     *
     * @param document XML Document
     * @return void
     */
    private void processContent(Document document /* , boolean notInitialRequest */) {
        // As we change the page we need to stop all videos on current page
        // before going to the new page. This is quite dirty, but is the only
        // way to do that...
//        openHABWidgetAdapter.stopVideoWidgets();
//        openHABWidgetAdapter.stopImageRefresh();
        Node rootNode = document.getFirstChild();
        openHABWidgetDataSource.setSourceNode(rootNode);

//        widgetList.clear();
        List<OpenHABWidget> widgets = new ArrayList<OpenHABWidget>();
        List<OpenHABItem> items = new ArrayList<OpenHABItem>();
        for (OpenHABWidget widget : openHABWidgetDataSource.getWidgets()) {
            // Remove frame widgets with no label text
            if (widget.getType().equals("Frame") && TextUtils.isEmpty(widget.getLabel()))
                continue;

            OpenHABItem item = widget.getItem();

            Log.d(TAG, "Widget: " + widget.getType() + ", Item: " + item.getName() + ", State: " + item.getState());

//            widgetList.add(widget);
            widgets.add(widget);
            items.add(item);
        }

        stateUpdateHandler.stateUpdate(items);

//        openHABWidgetAdapter.notifyDataSetChanged();
//        if (!longPolling && isAdded()) {
//            getListView().clearChoices();
//            Log.d(TAG, String.format("processContent selectedItem = %d", mCurrentSelectedItem));
//            if (mCurrentSelectedItem >= 0)
//                getListView().setItemChecked(mCurrentSelectedItem, true);
//        }

//        if (getActivity() != null && mIsVisible)
//            getActivity().setTitle(openHABWidgetDataSource.getTitle());

//        // Set widget list index to saved or zero position
//        // This would mean we got widget and command from nfc tag, so we need to do some automatic actions!
//        if (this.nfcWidgetId != null && this.nfcCommand != null) {
//            Log.d(TAG, "Have widget and command, NFC action!");
//            OpenHABWidget nfcWidget = this.openHABWidgetDataSource.getWidgetById(this.nfcWidgetId);
//            OpenHABItem nfcItem = nfcWidget.getItem();
//            // Found widget with id from nfc tag and it has an item
//            if (nfcWidget != null && nfcItem != null) {
//                // TODO: Perform nfc widget action here
//                if (this.nfcCommand.equals("TOGGLE")) {
//                    if (nfcItem.getType().equals("RollershutterItem")) {
//                        if (nfcItem.getStateAsBoolean())
//                            this.openHABWidgetAdapter.sendItemCommand(nfcItem, "UP");
//                        else
//                            this.openHABWidgetAdapter.sendItemCommand(nfcItem, "DOWN");
//                    } else {
//                        if (nfcItem.getStateAsBoolean())
//                            this.openHABWidgetAdapter.sendItemCommand(nfcItem, "OFF");
//                        else
//                            this.openHABWidgetAdapter.sendItemCommand(nfcItem, "ON");
//                    }
//                } else {
//                    this.openHABWidgetAdapter.sendItemCommand(nfcItem, this.nfcCommand);
//                }
//            }
//            this.nfcWidgetId = null;
//            this.nfcCommand = null;
//            if (this.nfcAutoClose) {
//                getActivity().finish();
//            }
//        }
        loadPage(/* displayPageUrl, */ true);
    }

    public void sendCommand(OpenHABItem item, String command) {
        if (item == null) {
            throw new NullPointerException("item must not be undefined!");
        }
        if (StringUtil.isStringUndefinedOrEmpty(command)) {
            throw new IllegalArgumentException("command must not be undefined or empty!");
        }

        Log.d(TAG, String.format("Send command '%s' to item %s...", command, item.getName()));

        StringEntity commandEntity;
        try {
            commandEntity = new StringEntity(command);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot convert command string to entity!", e);
        }

        MyAsyncHttpClient mAsyncHttpClient = OpenHABMainActivity.getAsyncHttpClient();
        mAsyncHttpClient.post(context, item.getLink(), commandEntity, "text/plain", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Command sent successfully.");
            }
            @Override
            public void onFailure(Throwable error, String content) {
                Log.e(TAG, "Error: " + error.getClass() + "/ " + content + "!");
            }
        });
    }

    private MyAsyncHttpClient createHttpClient() {
        MyAsyncHttpClient httpClient = new MyAsyncHttpClient(context);

//        mAsyncHttpClient.setBasicAuth(openHABUsername, openHABPassword);
//        httpClient.addHeader("Accept", "application/xml");
//        httpClient.setTimeout(10000);

        return httpClient;
    }

    public static interface UpdateHandler {

    }

    public static interface PageUpdateHandler extends UpdateHandler {
        void pageUpdate(Iterable<OpenHABWidget> widgets);
    }

    public static interface StateUpdateHandler extends UpdateHandler {
        void stateUpdate(Iterable<OpenHABItem> items);
    }
}