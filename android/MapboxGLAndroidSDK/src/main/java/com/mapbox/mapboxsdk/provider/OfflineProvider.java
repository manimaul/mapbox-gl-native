package com.mapbox.mapboxsdk.provider;

public interface OfflineProvider {
     void startFetchForTile(int z, int x, int y, OfflineProviderCallback callback);
     int minZoom();
     int maxZoom();
     int pixelsPerSide();
     String name();
}
