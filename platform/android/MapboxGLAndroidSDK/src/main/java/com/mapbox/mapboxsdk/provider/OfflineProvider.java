package com.mapbox.mapboxsdk.provider;

public interface OfflineProvider {
     void startFetchForRasterTile(int z, int x, int y, OfflineProviderCallback callback);
}
