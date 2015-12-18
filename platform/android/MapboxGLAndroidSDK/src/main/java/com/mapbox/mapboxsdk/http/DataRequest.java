package com.mapbox.mapboxsdk.http;

public abstract class DataRequest {

    final long mNativePtr;

    public DataRequest(long mNativePtr) {
        this.mNativePtr = mNativePtr;
    }

    native void nativeOnFailure(long nativePtr, int type, String message);
    native void nativeOnResponse(long nativePtr, int code, String message, String etag,
                                 String modified, String cacheControl, String expires, byte[] body);

    @SuppressWarnings("unused") // Native invocation
    abstract void start();

    @SuppressWarnings("unused") // Native invocation
    abstract void cancel();
}
