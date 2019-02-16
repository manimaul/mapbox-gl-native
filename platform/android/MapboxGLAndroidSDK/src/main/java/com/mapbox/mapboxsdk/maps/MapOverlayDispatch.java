package com.mapbox.mapboxsdk.maps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;

import java.util.ArrayList;
import java.util.List;

/**
 * View placed over the GL MapView that can draw {@link Overlay}s.
 */
public final class MapOverlayDispatch extends View {

    private List<Overlay> mOverlayList = new ArrayList<>();
    private LatLng mWgsCenter = new LatLng();
    private VisibleRegion mVisibleRegion = new VisibleRegion(new LatLng(), new LatLng(), new LatLng(), new LatLng(), new LatLngBounds(0,0,0,0));
    private final Rect mMapPixelBounds = new Rect();
    private float mBearing = 0;
    private float mZoom = 0;
    private MapboxMap mMapboxMap = null;
    private boolean mAttached = false;

    public MapOverlayDispatch(Context context) {
        super(context);
    }

    public MapOverlayDispatch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapOverlayDispatch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMapboxMap != null && mVisibleRegion != null && mWgsCenter != null) {
            Overlay overlay;
            for (int i = 0; i < mOverlayList.size(); i++) {
                overlay = mOverlayList.get(i);
                if (overlay.isOverlayDrawEnabled()) {
                    overlay.onOverlayDraw(mMapboxMap, canvas, mVisibleRegion, mWgsCenter, mBearing, mZoom);
                }
            }
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttached = true;
        onOverlayAttached();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttached = false;
        onOverlayDettached();
    }

    private void onOverlayAttached() {
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onOverlayAttached(mMapboxMap);
        }
    }

    private void onOverlayDettached() {
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onOverlayDetached();
        }
    }

    void onOverlayTouchEvent(MotionEvent event) {
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onOverlayTouchEvent(event);
        }
    }

    void setMapBoxMap(MapboxMap mapboxMap) {
        mMapboxMap = mapboxMap;
    }

    void onOverlaySingleTapConfirmed(LatLng pressPosition) {
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onOverlaySingleTapConfirmed(pressPosition);
        }
    }

    void onOverlayLongPress(LatLng pressPosition) {
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onOverlayLongPress(pressPosition);
        }
    }

    void addOverlay(Overlay overlay) {
        if (mAttached) {
            overlay.onOverlayAttached(mMapboxMap);
        }

        mOverlayList.add(overlay);
        overlay.onMapViewPixelBoundsChanged(mMapPixelBounds);
    }

    void removeOverlay(Overlay overlay) {
        mOverlayList.remove(overlay);
    }

    void update(final VisibleRegion visibleRegion, final LatLng wgsCenter, float bearing, float zoom) {
        mWgsCenter.set(wgsCenter);
        mVisibleRegion = visibleRegion;
        mBearing = bearing;
        mZoom = zoom;
        super.invalidate();
    }

    void onSizeChanged(int width, int height) {
        //left top right bottom
        mMapPixelBounds.set(0, 0, width, height);
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onMapViewPixelBoundsChanged(mMapPixelBounds);
        }
    }
}
