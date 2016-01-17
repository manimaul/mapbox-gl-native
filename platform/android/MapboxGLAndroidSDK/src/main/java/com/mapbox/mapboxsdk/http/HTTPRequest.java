package com.mapbox.mapboxsdk.http;

import android.text.TextUtils;
import android.util.Log;

import com.mapbox.mapboxsdk.constants.MapboxConstants;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public final class HTTPRequest extends DataRequest implements Callback {

    public static final String LOG_TAG = HTTPRequest.class.getSimpleName();

    private static final int CONNECTION_ERROR = 0;
    private static final int TEMPORARY_ERROR = 1;
    private static final int PERMANENT_ERROR = 2;
    private static final int CANCELED_ERROR = 3;

    private Call mCall;
    private Request mRequest;

    private HTTPRequest(long nativePtr) {
        super(nativePtr);
    }

    static HTTPRequest create(long nativePtr, String resourceUrl, String userAgent,
                                      String etag, String modified) {

        HTTPRequest request = new HTTPRequest(nativePtr);

        Request.Builder builder = new Request.Builder()
                .url(resourceUrl)
                .tag(resourceUrl.toLowerCase(MapboxConstants.MAPBOX_LOCALE))
                .addHeader("User-Agent", userAgent);

        if (etag.length() > 0) {
            builder = builder.addHeader("If-None-Match", etag);
        } else if (modified.length() > 0) {
            builder = builder.addHeader("If-Modified-Since", modified);
        }
        request.mRequest = builder.build();
        return request;
    }

    // Native invocation
    @Override
    public void start() {
        mCall = HTTPContext.getInstance()
                .getClient()
                .newCall(mRequest);

        mCall.enqueue(this);
    }


    // Native invocation
    @Override
    public void cancel() {
        mCall.cancel();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            Log.d(LOG_TAG, String.format("[HTTP] Request was successful (code = %d).", response.code()));
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
            onFailure(null, e);
            //throw e;
            return;
        } finally {
            response.body().close();
        }

        nativeOnResponse(mNativePtr, response.code(), response.message(), response.header("ETag"),
                response.header("Last-Modified"), response.header("Cache-Control"),
                response.header("Expires"), body);
    }

    @Override
    public void onFailure(Call call, IOException e) {
        Log.w(LOG_TAG, String.format("[HTTP] Request could not be executed: %s", e.getMessage()));

        int type = PERMANENT_ERROR;
        if ((e instanceof UnknownHostException)  ||
                (e instanceof SocketException)   ||
                (e instanceof ProtocolException) ||
                (e instanceof SSLException)) {
            type = CONNECTION_ERROR;
        } else if ((e instanceof InterruptedIOException)) {
            type = TEMPORARY_ERROR;
        } else if (mCall.isCanceled()) {
            type = CANCELED_ERROR;
        }

        nativeOnFailure(mNativePtr, type, e.getMessage());
    }
}