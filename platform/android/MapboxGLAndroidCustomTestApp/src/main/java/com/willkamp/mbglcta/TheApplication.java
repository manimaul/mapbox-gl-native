package com.willkamp.mbglcta;

import android.app.Application;

import com.mapbox.mapboxsdk.MapboxAccountManager;

public class TheApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MapboxAccountManager.start(getApplicationContext(), getString(R.string.mapbox_access_token));

    }
}
