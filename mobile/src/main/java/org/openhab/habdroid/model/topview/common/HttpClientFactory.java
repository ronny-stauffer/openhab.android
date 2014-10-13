package org.openhab.habdroid.model.topview.common;

import android.content.Context;

import org.openhab.habdroid.util.MyAsyncHttpClient;

/**
 * Created by staufferr on 13.10.2014.
 */
public class HttpClientFactory {
    private final Context context;

    private final Configurator configurator;

    public HttpClientFactory(Context context) {
        this(context, null);
    }

    public HttpClientFactory(Context context, Configurator configurator) {
        if (context == null) {
            throw new NullPointerException("context must not be undefined!");
        }

        this.context = context;
        this.configurator = configurator;
    }

    public MyAsyncHttpClient create() {
        MyAsyncHttpClient httpClient = new MyAsyncHttpClient(context);

        if (configurator != null) {
            httpClient.setBasicAuth(configurator.getUsername(), configurator.getPassword());
        }

//        httpClient.addHeader("Accept", "application/xml");
//        httpClient.setTimeout(10000); // Default

        return httpClient;
    }

    public static interface Configurator {
        String getUsername();
        String getPassword();
    }
}
