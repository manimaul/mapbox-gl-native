package com.mapbox.mapboxsdk.http;

public abstract class DataRequest {

    long mNativePtr;

    public DataRequest(long mNativePtr) {
        this.mNativePtr = mNativePtr;
    }

    native void nativeOnFailure(int type, String message);
    native void nativeOnResponse(int code, String etag, String modified, String cacheControl, String expires, byte[] body);

    @SuppressWarnings("unused") // Native invocation
    abstract void cancel();
}
