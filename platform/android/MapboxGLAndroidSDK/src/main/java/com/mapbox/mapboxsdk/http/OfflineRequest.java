package com.mapbox.mapboxsdk.http;

import com.mapbox.mapboxsdk.provider.OfflineProvider;
import com.mapbox.mapboxsdk.provider.OfflineProviderCallback;

public class OfflineRequest extends DataRequest implements OfflineProviderCallback {

    private int z, x, y;
    private OfflineProvider provider;
    private boolean mCanceled = false;
    private byte[] response;

    private OfflineRequest(long mNativePtr) {
        super(mNativePtr);
    }

    public static DataRequest create(long nativePtr, OfflineProvider provider, int z, int x, int y) {
        OfflineRequest offlineRequest = new OfflineRequest(nativePtr);
        offlineRequest.provider = provider;
        offlineRequest.z = z;
        offlineRequest.x = x;
        offlineRequest.y = y;
        return offlineRequest;
    }

    public static DataRequest create(long nativePtr, byte[] response) {
        OfflineRequest offlineRequest = new OfflineRequest(nativePtr);
        offlineRequest.response = response;
        return offlineRequest;
    }

    @Override
    void start() {
        if (provider != null) {
            provider.startFetchForTile(z, x, y, this);
        } else if (response != null) {
            nativeOnResponse(mNativePtr,
                    200,
                    "OK",
                    ""/*some id tag*/,
                    ""/*Sat, 03 Oct 2015 20:59:43 GMT*/,
                    "no-cache, no-store",
                    null,
                    response);
        } else {
            nativeOnFailure(mNativePtr,
                    404,
                    "");
        }
    }

    @Override
    void cancel() {
        mCanceled = true;
    }

    @Override
    public void onResult(boolean success, byte[] result) {
        if (!mCanceled) {
            if (success) {
                nativeOnResponse(mNativePtr,
                        200,
                        "OK",
                        ""/*some id tag*/,
                        ""/*Sat, 03 Oct 2015 20:59:43 GMT*/,
                        "no-cache, no-store",
                        null,
                        result);
            } else {
                nativeOnFailure(mNativePtr,
                        404,
                        "");
            }
        }
    }
}
