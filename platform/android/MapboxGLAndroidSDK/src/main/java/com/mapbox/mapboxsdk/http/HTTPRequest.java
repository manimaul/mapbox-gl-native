package com.mapbox.mapboxsdk.http;

import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

public final class HTTPRequest extends DataRequest implements Callback {

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
    public void onFailure(Request request, IOException e) {
        int type = PERMANENT_ERROR;
        if ((e instanceof UnknownHostException) || (e instanceof SocketException) ||
                (e instanceof ProtocolException) || (e instanceof SSLException)) {
            type = CONNECTION_ERROR;
        } else if ((e instanceof InterruptedIOException)) {
            type = TEMPORARY_ERROR;
        } else if (mCall.isCanceled()) {
            type = CANCELED_ERROR;
        }

        nativeOnFailure(mNativePtr, type, e.getMessage());
    }

    @Override
    public void onResponse(Response response) throws IOException {
        byte[] body;
        try {
            body = response.body().bytes();
        } catch (IOException e) {
            onFailure(mRequest, e);
            return;
        } finally {
            response.body().close();
        }

        nativeOnResponse(mNativePtr, response.code(), response.message(), response.header("ETag"),
                response.header("Last-Modified"), response.header("Cache-Control"),
                response.header("Expires"), body);
    }
}