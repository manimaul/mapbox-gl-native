package com.mapbox.mapboxsdk.http;

import android.net.Uri;

public interface OfflineInterceptor {
    void cancel(Uri uri);
    void handleRequest(Uri uri, OfflineInterceptorCallback callback);
    String host();
}
