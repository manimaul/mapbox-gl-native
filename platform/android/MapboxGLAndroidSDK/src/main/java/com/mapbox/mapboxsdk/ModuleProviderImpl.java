package com.mapbox.mapboxsdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mapbox.mapboxsdk.http.HttpRequest;
import com.mapbox.mapboxsdk.maps.TelemetryDefinition;
import com.mapbox.mapboxsdk.module.http.HttpRequestImpl;
import com.mapbox.mapboxsdk.module.telemetry.TelemetryImpl;
import com.mapbox.mapboxsdk.module.telemetry.TelemetryNoopImpl;

public class ModuleProviderImpl implements ModuleProvider {

  @Override
  @NonNull
  public HttpRequest createHttpRequest() {
    return new HttpRequestImpl();
  }

  @Override
  @Nullable
  public TelemetryDefinition obtainTelemetry() {
    return new TelemetryNoopImpl();
  }
}
