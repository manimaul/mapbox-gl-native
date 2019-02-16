package com.mapbox.mapboxsdk.module.http;

import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import com.mapbox.mapboxsdk.BuildConfig;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.http.HttpRequest;
import com.mapbox.mapboxsdk.http.HttpIdentifier;
import com.mapbox.mapboxsdk.http.HttpLogger;
import com.mapbox.mapboxsdk.http.HttpResponder;
import com.mapbox.mapboxsdk.http.HttpRequestUrl;
import com.mapbox.mapboxsdk.http.OfflineInterceptor;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import static com.mapbox.mapboxsdk.module.http.HttpRequestUtil.toHumanReadableAscii;

public class HttpRequestImpl implements HttpRequest {

  private static final String userAgentString = toHumanReadableAscii(
    String.format("%s %s (%s) Android/%s (%s)",
      HttpIdentifier.getIdentifier(),
      BuildConfig.MAPBOX_VERSION_STRING,
      BuildConfig.GIT_REVISION_SHORT,
      Build.VERSION.SDK_INT,
      Build.CPU_ABI)
  );

  private static final String TAG = HttpRequestImpl.class.getSimpleName();
  private static OkHttpClient client = new OkHttpClient.Builder().dispatcher(getDispatcher()).build();
  private static OfflineInterceptor interceptor;
  private static final Handler handler = new Handler();
  private static final Scheduler scheduler = AndroidSchedulers.from(handler.getLooper());

  private Call call;

  public static void setOfflineInterceptor(@Nullable OfflineInterceptor offlineInterceptor) {
    interceptor = offlineInterceptor;
  }

  /**
   * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control
   * The Cache-Control general-header field is used to specify directives for caching mechanisms in both,
   * requests and responses. Caching directives are unidirectional, meaning that a given directive
   * in a request is not implying that the same directive is to be given in the response.
   *
   * Cache-Control: max-age=<seconds>
   *     Specifies the maximum amount of time a resource will be considered fresh. Contrary to Expires,
   *     this directive is relative to the time of the request.
   * Cache-Control: s-maxage=<seconds>
   *     Overrides max-age or the Expires header, but it only applies to shared caches (e.g., proxies)
   *     and is ignored by a private cache.
   */
  private static final String OFFLINE_RESPONSE_CACHE_CONTROL = String.format(Locale.US, "max-age=%d", 60 * 60 * 24);

  /**
   * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Expires
   * The Expires header contains the date/time after which the response is considered stale.
   *
   * Invalid dates, like the value 0, represent a date in the past and mean that the resource is already expired.
   *
   * If there is a Cache-Control header with the "max-age" or "s-max-age" directive in the response,
   * the Expires header is ignored.
   */
  private static final String OFFLINE_RESPONSE_CACHE_EXPIRES = null;

  private void initializeOfflineRequest(final HttpResponder httpRequest, Uri uri) {
    //Log.d(LOG_TAG, "offline request thread: " + Thread.currentThread());
    Observable<byte[]> responseObservable = interceptor.handleRequest(uri);
      if (responseObservable != null) {
          responseObservable
                  .take(1)
                  .observeOn(scheduler)
                  .subscribe(new Observer<byte[]>() {
                      private Disposable disposable;

                      private void dispose() {
                          if (disposable != null && !disposable.isDisposed()) {
                              disposable.dispose();
                          }
                      }

                      @Override
                      public void onError(Throwable e) {
                          final String message = e.getMessage();
                          httpRequest.handleFailure(TEMPORARY_ERROR, message == null ? "unknown error" : message);
                          dispose();
                      }

                      @Override
                      public void onSubscribe(Disposable d) {
                          disposable = d;
                      }

                      @Override
                      public void onComplete() {
                          dispose();
                      }

                      @Override
                      public void onNext(byte[] bytes) {
                          //Log.d(LOG_TAG, "offline request thread: " + Thread.currentThread());
                          httpRequest.onResponse(200,
                                  makeEtag(bytes),
                                  DateFormat.format("EEE, dd MMM yyyy HH:mm:ss zzz", System.currentTimeMillis()).toString(),
                                  OFFLINE_RESPONSE_CACHE_CONTROL,
                                  OFFLINE_RESPONSE_CACHE_EXPIRES,
                                  null,
                                  null,
                                  bytes);
                          dispose();
                      }
                  });
    } else {
      httpRequest.handleFailure(TEMPORARY_ERROR, "offline interceptor produced a null observable");
    }
  }

  private static MessageDigest sMessageDigest;
  static {
    try {
      sMessageDigest = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      Log.e(TAG, "", e);
    }
  }


  private synchronized String makeEtag(byte[] body) {
    sMessageDigest.reset();
    return Base64.encodeToString(sMessageDigest.digest(body), Base64.DEFAULT);
  }

  @Override
  public void executeRequest(HttpResponder httpRequest, long nativePtr, @NonNull String resourceUrl,
                             @NonNull String etag, @NonNull String modified) {
    Uri uri = Uri.parse(resourceUrl);
    if (uri != null && interceptor != null && interceptor.host().equals(uri.getHost())) {
      initializeOfflineRequest(httpRequest, uri);
      return;
    }

    OkHttpCallback callback = new OkHttpCallback(httpRequest);
    try {
      HttpUrl httpUrl = HttpUrl.parse(resourceUrl);
      if (httpUrl == null) {
        HttpLogger.log(Log.ERROR, String.format("[HTTP] Unable to parse resourceUrl %s", resourceUrl));
        return;
      }

      final String host = httpUrl.host().toLowerCase(MapboxConstants.MAPBOX_LOCALE);
      resourceUrl = HttpRequestUrl.buildResourceUrl(host, resourceUrl, httpUrl.querySize());

      final Request.Builder builder = new Request.Builder()
        .url(resourceUrl)
        .tag(resourceUrl.toLowerCase(MapboxConstants.MAPBOX_LOCALE))
        .addHeader("User-Agent", userAgentString);
      if (etag.length() > 0) {
        builder.addHeader("If-None-Match", etag);
      } else if (modified.length() > 0) {
        builder.addHeader("If-Modified-Since", modified);
      }

      final Request request = builder.build();
      call = client.newCall(request);
      call.enqueue(callback);
    } catch (Exception exception) {
      callback.handleFailure(call, exception);
    }
  }

  @Override
  public void cancelRequest() {
    // call can be null if the constructor gets aborted (e.g, under a NoRouteToHostException).
    if (call != null) {
      call.cancel();
    }
  }

  public static void enablePrintRequestUrlOnFailure(boolean enabled) {
    HttpLogger.logRequestUrl = enabled;
  }

  public static void enableLog(boolean enabled) {
    HttpLogger.logEnabled = enabled;
  }

  public static void setOkHttpClient(OkHttpClient okHttpClient) {
    HttpRequestImpl.client = okHttpClient;
  }

  private static class OkHttpCallback implements Callback {

    private HttpResponder httpRequest;

    OkHttpCallback(HttpResponder httpRequest) {
      this.httpRequest = httpRequest;
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
      handleFailure(call, e);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) {
      if (response.isSuccessful()) {
        HttpLogger.log(Log.VERBOSE, String.format("[HTTP] Request was successful (code = %s).", response.code()));
      } else {
        // We don't want to call this unsuccessful because a 304 isn't really an error
        String message = !TextUtils.isEmpty(response.message()) ? response.message() : "No additional information";
        HttpLogger.log(Log.DEBUG, String.format("[HTTP] Request with response = %s: %s", response.code(), message));
      }

      ResponseBody responseBody = response.body();
      if (responseBody == null) {
        HttpLogger.log(Log.ERROR, "[HTTP] Received empty response body");
        return;
      }

      byte[] body;
      try {
        body = responseBody.bytes();
      } catch (IOException ioException) {
        onFailure(call, ioException);
        // throw ioException;
        return;
      } finally {
        response.close();
      }

      httpRequest.onResponse(response.code(),
        response.header("ETag"),
        response.header("Last-Modified"),
        response.header("Cache-Control"),
        response.header("Expires"),
        response.header("Retry-After"),
        response.header("x-rate-limit-reset"),
        body);
    }

    private void handleFailure(@Nullable Call call, Exception e) {
      String errorMessage = e.getMessage() != null ? e.getMessage() : "Error processing the request";
      int type = getFailureType(e);

      if (HttpLogger.logEnabled && call != null && call.request() != null) {
        String requestUrl = call.request().url().toString();
        HttpLogger.logFailure(type, errorMessage, requestUrl);
      }
      httpRequest.handleFailure(type, errorMessage);
    }

    private int getFailureType(Exception e) {
      if ((e instanceof NoRouteToHostException) || (e instanceof UnknownHostException) || (e instanceof SocketException)
        || (e instanceof ProtocolException) || (e instanceof SSLException)) {
        return CONNECTION_ERROR;
      } else if ((e instanceof InterruptedIOException)) {
        return TEMPORARY_ERROR;
      }
      return PERMANENT_ERROR;
    }
  }

  @NonNull
  private static Dispatcher getDispatcher() {
    Dispatcher dispatcher = new Dispatcher();
    // Matches core limit set on
    // https://github.com/mapbox/mapbox-gl-native/blob/master/platform/android/src/http_file_source.cpp#L192
    dispatcher.setMaxRequestsPerHost(20);
    return dispatcher;
  }
}