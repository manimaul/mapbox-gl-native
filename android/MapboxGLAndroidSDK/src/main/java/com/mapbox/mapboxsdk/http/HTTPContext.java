package com.mapbox.mapboxsdk.http;

import android.util.Log;

import com.mapbox.mapboxsdk.provider.OfflineProviderManager;
import com.squareup.okhttp.OkHttpClient;

@SuppressWarnings("unused") // Native invocation
public final class HTTPContext {

    public static final String TAG = HTTPContext.class.getSimpleName();

    private static HTTPContext mInstance = null;

    private final OkHttpClient mClient;
    private final OfflineProviderManager offlineProviderManager = OfflineProviderManager.getInstance();

    private HTTPContext() {
        super();
        mClient = new OkHttpClient();
    }

    // Native invocation
    public static HTTPContext getInstance() {
        if (mInstance == null) {
            mInstance = new HTTPContext();
        }

        OfflineProviderManager.getInstance().setAvailable(true);

        return mInstance;
    }

    // Native invocation
    public DataRequest createRequest(long nativePtr, String resourceUrl, String userAgent,
                                     String etag, String modified) {

        Log.d(TAG, String.format("HTTPRequest$create(resourceUrl %s : userAgent %s : etag: %s : modified %s) ",
                resourceUrl, userAgent, etag, modified));

        if (offlineProviderManager.willHandleUrl(resourceUrl)) {
            return offlineProviderManager.createDataRequest(nativePtr, resourceUrl);
        } else {
            return HTTPRequest.create(nativePtr, resourceUrl, userAgent, etag, modified);
        }
    }

    public OkHttpClient getClient() {
        return mClient;
    }
}
