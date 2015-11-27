package com.mapbox.mapboxsdk.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

final class MapOverlayDispatch extends View {

    //region FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private List<Overlay> mOverlayList = new ArrayList<>();
    private PointF mWgsCenter = new PointF();
    private RectF mWgsBounds = new RectF();
    private float mBearing = 0;
    private float mZoom = 0;
    private MapView mMapView = null;
    private boolean mAttached = false;

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
        if (mMapView != null) {
            Overlay overlay;
            for (int i = 0; i < mOverlayList.size(); i++) {
                overlay = mOverlayList.get(i);
                if (overlay.isOverlayDrawEnabled()) {
                    overlay.onOverlayDraw(mMapView, canvas, mWgsBounds, mWgsCenter, mBearing, mZoom);
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

    //endregion

    //region PRIVATE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private void onOverlayAttached() {
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onOverlayAttached(mMapView);
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

    //endregion

    //region PACKAGE LOCAL ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    void setMapView(MapView mapView) {
        mMapView = mapView;
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
            overlay.onOverlayAttached(mMapView);
        }

        mOverlayList.add(overlay);
    }

    void clearOverlays() {
        mOverlayList.clear();
    }

    void removeOverlay(Overlay overlay) {
        mOverlayList.remove(overlay);
    }

    void invalidate(final RectF wgsBounds, final PointF wgsCenter, float bearing, float zoom) {
        mWgsCenter.set(wgsCenter);
        mWgsBounds.set(wgsBounds);
        mBearing = bearing;
        mZoom = zoom;
        super.invalidate();
    }

    //endregion
}
