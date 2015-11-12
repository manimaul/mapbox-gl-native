package com.mapbox.mapboxsdk.http;

import android.util.Log;

import com.mapbox.mapboxsdk.provider.OfflineProviderManager;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

@SuppressWarnings("unused") // Native invocation
public final class HTTPContext {

    public static final String TAG = HTTPContext.class.getSimpleName();

    private static HTTPContext mInstance = null;

    private final OkHttpClient mClient;
    private final OfflineProviderManager offlineProviderManager = OfflineProviderManager.getInstance();

    private HTTPContext() {
        super();
        mClient = new OkHttpClient();
        //mClient.interceptors().add(new LoggingInterceptor());
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

    /*
     * Application interceptor that logs the outgoing request and the incoming response.
     * Based on https://github.com/square/okhttp/wiki/Interceptors
     */
    class LoggingInterceptor implements Interceptor {

        private final static String LOG_TAG = "LoggingInterceptor";

        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Log.i(LOG_TAG, String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Log.i(LOG_TAG, String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            return response;
        }
    }
}
