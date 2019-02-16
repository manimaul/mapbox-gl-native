package com.mapbox.mapboxsdk.http;

import android.net.Uri;
import android.support.annotation.NonNull;

import io.reactivex.Observable;

public interface OfflineInterceptor {

    void cancel(@NonNull Uri uri);

    /**
     * An observable that emits a single response for a given {@link Uri}.
     *
     * @param uri the uri to handle the request.
     * @return an observable of a request response.
     */
    Observable<byte[]> handleRequest(Uri uri);

    /**
     * The name of the host to delegate request handling to. E.g. "localhost" as in http://localhost
     *
     * @return the name of the host.
     */
    @NonNull
    String host();
}
