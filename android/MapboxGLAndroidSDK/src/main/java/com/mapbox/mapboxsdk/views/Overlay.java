package com.mapbox.mapboxsdk.views;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

public abstract class Overlay {

    private boolean mEnabled = false;

    /**
     * Perform the overlay draw here.
     *
     * @param canvas a canvas for you to draw on.
     * @param wgsBounds bounds of the overlay in latitude, longitude
     * @param wgsCenter center of the overlay in latitude, longitude
     */
    public abstract void drawLayer(final Canvas canvas, final RectF wgsBounds, final PointF wgsCenter,
                                   float bearing, float zoom);

    public abstract void onTouchEvent(final MotionEvent event);

    public abstract void onAttachedToWindow();
    public abstract void onDetachedFromWindow();

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    boolean isEnabled() {
        return mEnabled;
    }
}
