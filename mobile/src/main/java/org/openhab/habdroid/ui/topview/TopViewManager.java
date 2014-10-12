package org.openhab.habdroid.ui.topview;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.openhab.habdroid.R;
import org.openhab.habdroid.model.OpenHABItem;
import org.openhab.habdroid.model.OpenHABWidget;
import org.openhab.habdroid.model.topview.TopViewButtonDescriptor;
import org.openhab.habdroid.model.topview.TopViewButtonToItemAdapter;
import org.openhab.habdroid.model.topview.TopViewSVGToButtonParser;
import org.openhab.habdroid.model.topview.common.Communicator;
import org.openhab.habdroid.model.topview.common.StreamUtil;
import org.openhab.habdroid.model.topview.common.StringUtil;
import org.openhab.habdroid.ui.OpenHABMainActivity;
import org.openhab.habdroid.util.MyAsyncHttpClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by staufferr on 12.10.2014.
 */
public class TopViewManager {
    private final Activity activity;

    private final Communicator communicator;

    public TopViewManager(Activity activity) {
        if (activity == null) {
            throw new NullPointerException("activity must not be undefined!");
        }

        this.activity = activity;
        this.communicator = new Communicator(activity);
    }

    public void loadForSitemap(final String sitemapUrl, final RelativeLayout layout) {
        if (StringUtil.isStringUndefinedOrEmpty(sitemapUrl)) {
            throw new IllegalArgumentException("sitemapUrl must not be undefined or empty!");
        }
        if (layout == null) {
            throw new NullPointerException("layout must not be undefined!");
        }

//        long threadId = Thread.currentThread().getId();
//        String threadName = Thread.currentThread().getName();

        if (!sitemapUrl.equals("http://10.20.34.66:8080/rest/sitemaps/test/test")) {
            return;
        }

        final String modelSitemapUrl = "http://10.20.34.66:8080/rest/sitemaps/test_top_view/test_top_view";
        final String svgUrl = "http://10.20.34.66:8080/top_views/test_top_view.svg";

        // The "glue" (mediator) between the view and the model
        final Map<String, TopViewButtonToItemAdapter> buttonToItemAdapters = new HashMap<String, TopViewButtonToItemAdapter>();

        // View
        // Load view definition
//            InputStream svgStream = null;
//            final AtomicReference<ByteBuffer> svgBuffer = new AtomicReference<ByteBuffer>();
        MyAsyncHttpClient mAsyncHttpClient = OpenHABMainActivity.getAsyncHttpClient();
        mAsyncHttpClient.get(activity, svgUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseBody) {
//                    long threadId = Thread.currentThread().getId();
//                    String threadName = Thread.currentThread().getName();

//                    svgBuffer.set(ByteBuffer.wrap(responseBody.getBytes()));
//                    svgBuffer.notifyAll();

                InputStream svgStream = new ByteArrayInputStream(responseBody.getBytes());
                // Create view
                createView(svgStream, layout, buttonToItemAdapters);
            }

            @Override
            public void onFailure(Throwable throwable, String responseBody) {

            }
        }, this);
//            try {
//                svgBuffer.wait();
//                if (svgBuffer.get() != null) {
//                    svgStream = new ByteArrayInputStream(svgBuffer.get().array());
//                }
//            } catch (InterruptedException e) {
//                // Ignore exception
//            }

        // Load Model
        communicator.loadPage(modelSitemapUrl, new Communicator.StateUpdateHandler() {
            @Override
            public void stateUpdate(Iterable<OpenHABWidget> widgets) {
                // Potentially called from a background thread

                for (OpenHABWidget widget : widgets) {
                    OpenHABItem item = widget.getItem();
                    String itemName = item.getName();

                    if (buttonToItemAdapters.containsKey(itemName)) {
                        TopViewButtonToItemAdapter buttonToItemAdapter = buttonToItemAdapters.get(itemName);

                        buttonToItemAdapter.updateItem(item);
                    } // else: No button for item
                }
            }
        });
    }

    private void createView(/* Source: */ InputStream svgStream, /* Target */ RelativeLayout layout, Map<String, TopViewButtonToItemAdapter> buttonToItemAdapters) {
        assert svgStream != null;
        assert layout != null;
        assert buttonToItemAdapters != null;

        final ImageView topViewImageView = new SVGImageViewFactory(activity).create(svgStream);
//                final ImageView topViewImageView = new SVGImageViewFactory(this).createFromAsset(activity.getAssets(), "top_view.svg");
        //topViewImageView.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT); // Mandatory parameters
        layout.addView(topViewImageView, layoutParams);

        StreamUtil.resetStream(svgStream);

//            final Map<String, TopViewButtonDescriptor> buttonDescriptors = new TopViewSVGToButtonParser().parse(svgStream);
        final Map<String, TopViewButtonDescriptor> buttonDescriptors = new TopViewSVGToButtonParser().parseAsset(activity.getAssets(), "top_view.svg");
        TopViewButtonFactory buttonFactory = new TopViewButtonFactory(activity);
        for (TopViewButtonDescriptor buttonDescriptor : buttonDescriptors.values()) {
                /* Button button */
            TopViewButtonToItemAdapter buttonToItemAdapter = buttonFactory.create(buttonDescriptor, new TopViewButtonToItemAdapter.SendCommandHandler() {
                @Override
                public void sendCommand(OpenHABItem item, String command) {
                    // Called from the UI thread

                    communicator.sendCommand(item, command);
                }
            });
            buttonToItemAdapters.put(buttonToItemAdapter.getButtonDescriptor().getItem(), buttonToItemAdapter);
            layout.addView(/* button */ buttonToItemAdapter.getButton());
        }
    }
}