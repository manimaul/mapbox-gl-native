package com.mapbox.mapboxsdk.http;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.mapboxsdk.constants.MapboxConstants;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;

public class HTTPRequest implements Callback {

    //region CONSTANTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final String LOG_TAG = HTTPRequest.class.getName();
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();
    private static final int CONNECTION_ERROR = 0;
    private static final int TEMPORARY_ERROR = 1;
    private static final int PERMANENT_ERROR = 2;

    public static final int OFFLINE_RESPONSE_CODE = 200;
    public static final int OFFLINE_FAILURE_CODE = 404;
    public static final String OFFLINE_FAILURE_MESSAGE = "";
    public static final String OFFLINE_RESPONSE_ETAG = "OK";
    public static final String OFFLINE_RESPONSE_MODIFIED = "";
    public static final String OFFLINE_RESPONSE_CACHE_CONTROL = "no-cache, no-store";
    public static final String OFFLINE_RESPONSE_CACHE_EXPIRES = "";

    //endregion

    //region FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // Reentrant is not needed, but "Lock" is an abstract class.
    private Lock mLock = new ReentrantLock();
    private long mNativePtr = 0;
    private final Uri mResourceUrl;
    private Call mCall;
    private static OfflineInterceptor sOfflineInterceptor;

    //endregion

    //region INJECTED DEPENDENCIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region INJECTED VIEWS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private HTTPRequest(long nativePtr, String resourceUrl, String userAgent, String etag, String modified) {
        mNativePtr = nativePtr;
        mResourceUrl = Uri.parse(resourceUrl);
        if (sOfflineInterceptor != null) {
            if (mResourceUrl.getHost().equals(sOfflineInterceptor.host())) {
                Log.d(LOG_TAG, "HTTPRequest thread " + Thread.currentThread().getId() + Thread.currentThread().getName());
                Observable<byte[]> responseObservable = sOfflineInterceptor.handleRequest(mResourceUrl);
                if (responseObservable != null) {
                    responseObservable.subscribe(new Subscriber<byte[]>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            // Log.e(LOG_TAG, "HTTPRequest onError() thread " + Thread.currentThread().getId() + Thread.currentThread().getName(), e);
                            onOfflineFailure();
                        }

                        @Override
                        public void onNext(byte[] bytes) {
                            // Log.d(LOG_TAG, "HTTPRequest onNext() thread " + Thread.currentThread().getId() + Thread.currentThread().getName());
                            onOfflineResponse(bytes);
                            unsubscribe(); // We're only expecting 1 response
                        }
                    });
                } else {
                    onOfflineFailure();
                }
                return;
            }
        }
        try {
            Request.Builder builder = new Request.Builder().url(resourceUrl).tag(resourceUrl.toLowerCase(MapboxConstants.MAPBOX_LOCALE)).addHeader("User-Agent", userAgent);
            if (etag.length() > 0) {
                builder = builder.addHeader("If-None-Match", etag);
            } else if (modified.length() > 0) {
                builder = builder.addHeader("If-Modified-Since", modified);
            }
            Request request = builder.build();
            mCall = OK_HTTP_CLIENT.newCall(request);
            mCall.enqueue(this);
        } catch (Exception e) {
            onFailure(e);
        }
    }

    //endregion

    //region PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @SuppressWarnings("JniMissingFunction")
    private native void nativeOnFailure(int type, String message);

    @SuppressWarnings("JniMissingFunction")
    private native void nativeOnResponse(int code, String etag, String modified, String cacheControl, String expires, byte[] body);

    private void onFailure(Exception e) {
        Log.w(LOG_TAG, String.format("[HTTP] Request could not be executed: %s", e.getMessage()));

        int type = PERMANENT_ERROR;
        if ((e instanceof UnknownHostException) || (e instanceof SocketException) || (e instanceof ProtocolException) || (e instanceof SSLException)) {
            type = CONNECTION_ERROR;
        } else if ((e instanceof InterruptedIOException)) {
            type = TEMPORARY_ERROR;
        }

        String errorMessage = e.getMessage() != null ? e.getMessage() : "Error processing the request";

        mLock.lock();
        if (mNativePtr != 0) {
            nativeOnFailure(type, errorMessage);
        }
        mLock.unlock();
    }

    public void onOfflineResponse(byte[] body) {
        mLock.lock();
        if (mNativePtr != 0) {
            nativeOnResponse(OFFLINE_RESPONSE_CODE,
                    OFFLINE_RESPONSE_ETAG,
                    OFFLINE_RESPONSE_MODIFIED,
                    OFFLINE_RESPONSE_CACHE_CONTROL,
                    OFFLINE_RESPONSE_CACHE_EXPIRES,
                    body);
        }
        mLock.unlock();
    }

    public void onOfflineFailure() {
        mLock.lock();
        if (mNativePtr != 0) {
            nativeOnFailure(OFFLINE_FAILURE_CODE, OFFLINE_FAILURE_MESSAGE);
        }
        mLock.unlock();
    }

    //endregion

    //region PUBLIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void cancel() {
        if (mCall == null) {
            if (sOfflineInterceptor != null) {
                sOfflineInterceptor.cancel(mResourceUrl);
            }
        } else {
            mCall.cancel();
        }

        // We need a lock here because we can try to cancel at the same time the request is getting
        // answered on the OkHTTP thread. We could get rid of this lock by using Runnable when we
        // move Android implementation of mbgl::RunLoop to Looper.
        mLock.lock();
        mNativePtr = 0;
        mLock.unlock();
    }

    //endregion

    //region ACCESSORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static void setOfflineInterceptor(@Nullable OfflineInterceptor offlineInterceptor) {
        sOfflineInterceptor = offlineInterceptor;
    }

    //endregion

    //region {Callback) ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            Log.v(LOG_TAG, String.format("[HTTP] Request was successful (code = %d).", response.code()));
        } else {
            // We don't want to call this unsuccessful because a 304 isn't really an error
            String message = !TextUtils.isEmpty(response.message()) ? response.message() : "No additional information";
            Log.d(LOG_TAG, String.format(
                    "[HTTP] Request with response code = %d: %s",
                    response.code(), message));
        }

        byte[] body;
        try {
            body = response.body().bytes();
        } catch (IOException e) {
            onFailure(e);
            return;
        } finally {
            response.body().close();
        }

        mLock.lock();
        if (mNativePtr != 0) {
            nativeOnResponse(response.code(), response.header("ETag"), response.header("Last-Modified"),
                    response.header("Cache-Control"), response.header("Expires"), body);
        }
        mLock.unlock();
    }

    @Override
    public void onFailure(Call call, IOException e) {
        onFailure(e);
    }

    //endregion

    //region INNER CLASSES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

}
