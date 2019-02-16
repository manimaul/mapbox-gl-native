package com.mapbox.mapboxsdk.maps;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;

public interface Overlay {

    /**
     * Perform the overlay draw here.
     *
     * @param mapboxMap     the map.
     * @param canvas        a canvas for you to draw on.
     * @param visibleRegion bounds of the overlay in latitude, longitude
     * @param wgsCenter     center of the overlay in latitude, longitude
     * @param bearing       the map bearing / rotation degrees
     * @param zoom          the map zoom level
     */
    void onOverlayDraw(final MapboxMap mapboxMap, final Canvas canvas,
                       final VisibleRegion visibleRegion, final LatLng wgsCenter,
                       float bearing, float zoom);

    void onOverlayTouchEvent(final MotionEvent event);

    /**
     * Called when the overlay is attached to the MapboxMap view hierarchy.
     */
    void onOverlayAttached(final MapboxMap mapboxMap);

    /**
     * Called when the overlay is detached to the MapView view hierarchy.
     */
    void onOverlayDetached();

    /**
     * A single tap event occurred.
     *
     * @param pressPosition the position of the press.
     */
    void onOverlaySingleTapConfirmed(LatLng pressPosition);

    /**
     * A long press event occurred.
     *
     * @param pressPosition the position of the press.
     */
    void onOverlayLongPress(LatLng pressPosition);

    /**
     * Let us know if you'd like to receive the onOverlayDraw() callback.
     *
     * @return true to get the
     * {@link Overlay#onOverlayDraw(MapboxMap, Canvas, VisibleRegion, LatLng, float, float)} callback.
     */
    boolean isOverlayDrawEnabled();

    /**
     * Called when the {@link MapView} size changes
     *
     * @param mapPixelBounds the pixel bounds of the view
     */
    void onMapViewPixelBoundsChanged(Rect mapPixelBounds);
}
