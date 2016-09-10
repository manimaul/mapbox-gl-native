package com.mapbox.mapboxsdk.http;

public interface OfflineInterceptorCallback {
    void onResult(boolean success, byte[] result);
}
