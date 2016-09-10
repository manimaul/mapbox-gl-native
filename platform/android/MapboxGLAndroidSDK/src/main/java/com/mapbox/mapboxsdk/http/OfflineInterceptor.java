package com.mapbox.mapboxsdk.http;

import android.net.Uri;
import android.support.annotation.Nullable;

import rx.Observable;

public interface OfflineInterceptor {
    void cancel(Uri uri);
    @Nullable Observable<byte[]> handleRequest(Uri uri);
    String host();
}
