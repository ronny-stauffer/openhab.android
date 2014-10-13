package org.openhab.habdroid.model.topview.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.openhab.habdroid.util.Constants;

/**
 * Created by staufferr on 13.10.2014.
 */
public class Preferences implements HttpClientFactory.Configurator {
    private final Context context;

    private final SharedPreferences preferences;

    public Preferences(Context context) {
        if (context == null) {
            throw new NullPointerException("context must not be undefined!");
        }

        this.context = context;
        preferences =  PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public String getUsername() {
        String username = preferences.getString(Constants.PREFERENCE_USERNAME, null);

        return username;
    }

    @Override
    public String getPassword() {
        String password = preferences.getString(Constants.PREFERENCE_PASSWORD, null);

        return password;
    }
}