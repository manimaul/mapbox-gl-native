package com.mapbox.mapboxsdk.provider;

public interface OfflineProviderCallback {
    void onResult(boolean success, byte[] result);
}
