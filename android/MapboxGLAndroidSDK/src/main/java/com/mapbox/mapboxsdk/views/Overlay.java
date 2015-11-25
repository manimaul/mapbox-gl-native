package com.mapbox.mapboxsdk.views;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

public interface Overlay {

    /**
     * Perform the overlay draw here.
     *
     * @param mapView   the map view.
     * @param canvas    a canvas for you to draw on.
     * @param wgsBounds bounds of the overlay in latitude, longitude
     * @param wgsCenter center of the overlay in latitude, longitude
     * @param bearing   the map bearing / rotation degrees
     * @param zoom      the map zoom level
     */
    void onOverlayDraw(final MapView mapView, final Canvas canvas,
                       final RectF wgsBounds, final PointF wgsCenter,
                       float bearing, float zoom);

    void onOverlayTouchEvent(final MotionEvent event);

    void onOverlayAttachedToWindow();

    void onOverlayDetachedFromWindow();

    boolean isOverlayEnabled();
}
