package com.mapbox.mapboxsdk.http;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;

import com.mapbox.mapboxsdk.BuildConfig;
import com.mapbox.mapboxsdk.constants.MapboxConstants;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import rx.Observable;
import rx.Observer;

@SuppressWarnings({"JniMissingFunction", "unused"})
public class HTTPRequest implements Callback {

  //region CONSTANTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  private static final String LOG_TAG = HTTPRequest.class.getName();
  private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();
  private String USER_AGENT_STRING = null;
  private static final int CONNECTION_ERROR = 0;
  private static final int TEMPORARY_ERROR = 1;
  private static final int PERMANENT_ERROR = 2;

  public static final int OFFLINE_RESPONSE_CODE = 200;
  public static final int OFFLINE_FAILURE_CODE = 404;
  public static final String UNKNOWN_ERROR = "unknown error";
  public static final String OFFLINE_INTERCEPTOR_NULL_OBSERVABLE = "offline interceptor producted a null observable";
  public static final String OFFLINE_RESPONSE_CACHE_CONTROL = "max-age=43200,s-maxage=300";
  public static final String OFFLINE_RESPONSE_CACHE_EXPIRES = null;
  private static MessageDigest sMessageDigest;
  static {
    try {
      sMessageDigest = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      Log.e(LOG_TAG, "", e);
    }
  }

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

  public HTTPRequest(long nativePtr, String resourceUrl, String etag, String modified) {
    mNativePtr = nativePtr;
    mResourceUrl = Uri.parse(resourceUrl);
    if (sOfflineInterceptor != null) {
      if (mResourceUrl.getHost().equals(sOfflineInterceptor.host())) {
        Observable<byte[]> responseObservable = sOfflineInterceptor.handleRequest(mResourceUrl);
        if (responseObservable != null) {
          responseObservable.take(1).subscribe(new Observer<byte[]>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
              final String message = e.getMessage();
              onOfflineFailure(message == null ? UNKNOWN_ERROR : message);
            }

            @Override
            public void onNext(byte[] bytes) {
              onOfflineResponse(bytes);
            }
          });
        } else {
          onOfflineFailure(OFFLINE_INTERCEPTOR_NULL_OBSERVABLE);
        }
        return;
      }
    }
    try {
      Request.Builder builder = new Request.Builder()
              .url(resourceUrl)
              .tag(resourceUrl.toLowerCase(MapboxConstants.MAPBOX_LOCALE))
              .addHeader("User-Agent", getUserAgent());
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


  private native void nativeOnFailure(int type,
                                      String message);
  private native void nativeOnResponse(int code,
                                       String etag,
                                       String modified,
                                       String cacheControl,
                                       String expires,
                                       String retryAfter,
                                       String xRateLimitReset,
                                       byte[] body);

  private void onFailure(Exception e) {
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
      String etag = makeEtag(body);
      String mod = makeModified();
      nativeOnResponse(OFFLINE_RESPONSE_CODE,
              etag,
              mod,
              OFFLINE_RESPONSE_CACHE_CONTROL,
              OFFLINE_RESPONSE_CACHE_EXPIRES,
              null,
              null,
              body);
    }
    mLock.unlock();
  }

  private String getUserAgent() {
    if (USER_AGENT_STRING == null) {
      return USER_AGENT_STRING = Util.toHumanReadableAscii(
              String.format("%s (%s) Android/%s (%s)",
                      BuildConfig.MAPBOX_VERSION_STRING,
                      BuildConfig.GIT_REVISION_SHORT,
                      Build.VERSION.SDK_INT,
                      Build.CPU_ABI)
      );
    } else {
      return USER_AGENT_STRING;
    }
  }

  public String makeModified() {
    return DateFormat.format("EEE, dd MMM yyyy HH:mm:ss zzz", System.currentTimeMillis()).toString();
  }

  public synchronized String makeEtag(byte[] body) {
    assert sMessageDigest != null;
    sMessageDigest.reset();
    return Base64.encodeToString(sMessageDigest.digest(body), Base64.DEFAULT);
  }

  public void onOfflineFailure(final String message) {
    mLock.lock();
    if (mNativePtr != 0) {
      nativeOnFailure(OFFLINE_FAILURE_CODE, message);
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
  public void onResponse(Call call,
                         Response response) throws IOException {
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
              response.header("Cache-Control"), response.header("Expires"), null, null, body);
    }
    mLock.unlock();
  }

  @Override
  public void onFailure(Call call,
                        IOException e) {
    onFailure(e);
  }

  //endregion

  //region INNER CLASSES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  //endregion

}
