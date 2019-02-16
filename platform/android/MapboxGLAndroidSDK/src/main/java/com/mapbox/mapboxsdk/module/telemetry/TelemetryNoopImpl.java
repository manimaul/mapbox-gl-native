package com.mapbox.mapboxsdk.module.telemetry;

import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.maps.TelemetryDefinition;
import com.mapbox.mapboxsdk.offline.OfflineRegionDefinition;

public class TelemetryNoopImpl implements TelemetryDefinition {

  /**
   * Register the app user turnstile event
   */
  @Override
  public void onAppUserTurnstileEvent() {
  }

  /**
   * Register an end-user gesture interaction event.
   *
   * @param eventType type of gesture event occurred
   * @param latitude  the latitude value of the gesture focal point
   * @param longitude the longitude value of the gesture focal point
   * @param zoom      current zoom of the map
   */
  @Override
  public void onGestureInteraction(String eventType, double latitude, double longitude, double zoom) { }

  /**
   * Set the end-user selected state to participate or opt-out in telemetry collection.
   */
  @Override
  public void setUserTelemetryRequestState(boolean enabledTelemetry) { }

  /**
   * Set the debug logging state of telemetry.
   *
   * @param debugLoggingEnabled true to enable logging
   */
  @Override
  public void setDebugLoggingEnabled(boolean debugLoggingEnabled) { }

  /**
   * Set the telemetry rotation session id interval
   *
   * @param interval the selected session interval
   * @return true if rotation session id was updated
   */
  @Override
  public boolean setSessionIdRotationInterval(int interval) {
    return false;
  }

  @Override
  public void onCreateOfflineRegion(@NonNull OfflineRegionDefinition offlineDefinition) { }
}