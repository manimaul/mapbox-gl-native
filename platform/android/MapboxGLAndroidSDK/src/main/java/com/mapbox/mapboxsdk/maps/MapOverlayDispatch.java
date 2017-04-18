package com.mapbox.mapboxsdk.maps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;

import java.util.ArrayList;
import java.util.List;

/**
 * View placed over the GL MapView that can draw {@link Overlay}s.
 */
final class MapOverlayDispatch extends View {

    //region FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private List<Overlay> overlays = new ArrayList<>();
    private LatLng wgsCenter = new LatLng();
    private VisibleRegion visibleRegion = VisibleRegion.create();
    private final Rect mapPixelBounds = new Rect();
    private float bearing = 0;
    private float zoom = 0;
    private MapboxMap mapboxMap = null;
    private boolean isAttached = false;

    //endregion

    //region CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public MapOverlayDispatch(Context context) {
        super(context);
    }

    public MapOverlayDispatch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapOverlayDispatch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //endregion

    //region LIFE CYCLE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    protected void onDraw(Canvas canvas) {
        if (mapboxMap != null && visibleRegion != null && wgsCenter != null) {
            Overlay overlay;
            for (int i = 0; i < overlays.size(); i++) {
                overlay = overlays.get(i);
                if (overlay.isOverlayDrawEnabled()) {
                    overlay.onOverlayDraw(mapboxMap, canvas, visibleRegion, wgsCenter, bearing, zoom);
                }
            }
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
        onOverlayAttached();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttached = false;
        onOverlayDettached();
    }

    //endregion

    //region PRIVATE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private void onOverlayAttached() {
        for (int i = 0; i < overlays.size(); i++) {
            overlays.get(i).onOverlayAttached(mapboxMap);
        }
    }

    private void onOverlayDettached() {
        for (int i = 0; i < overlays.size(); i++) {
            overlays.get(i).onOverlayDetached();
        }
    }

    //endregion

    //region PACKAGE LOCAL ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    void onOverlayTouchEvent(MotionEvent event) {
        for (int i = 0; i < overlays.size(); i++) {
            overlays.get(i).onOverlayTouchEvent(event);
        }
    }

    void setMapBoxMap(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
    }

    void onOverlaySingleTapConfirmed(LatLng pressPosition) {
        for (int i = 0; i < overlays.size(); i++) {
            overlays.get(i).onOverlaySingleTapConfirmed(pressPosition);
        }
    }

    void onOverlayLongPress(LatLng pressPosition) {
        for (int i = 0; i < overlays.size(); i++) {
            overlays.get(i).onOverlayLongPress(pressPosition);
        }
    }

    void addOverlay(Overlay overlay) {
        if (isAttached) {
            overlay.onOverlayAttached(mapboxMap);
        }

        overlays.add(overlay);
        overlay.onMapViewPixelBoundsChanged(mapPixelBounds);
    }

    void removeOverlay(Overlay overlay) {
        overlays.remove(overlay);
    }

    void update(final VisibleRegion visibleRegion, final LatLng wgsCenter, float bearing, float zoom) {
        this.wgsCenter.set(wgsCenter);
        this.visibleRegion = visibleRegion;
        this.bearing = bearing;
        this.zoom = zoom;
        super.invalidate();
    }

    void onSizeChanged(int width, int height) {
        //left top right bottom
        mapPixelBounds.set(0, 0, width, height);
        for (int i = 0; i < overlays.size(); i++) {
            overlays.get(i).onMapViewPixelBoundsChanged(mapPixelBounds);
        }
    }

    //endregion
}