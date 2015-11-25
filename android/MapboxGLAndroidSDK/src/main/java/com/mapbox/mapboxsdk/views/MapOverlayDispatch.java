package com.mapbox.mapboxsdk.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

final class MapOverlayDispatch extends View {

    private List<Overlay> mOverlayList = new ArrayList<>();
    private PointF mWgsCenter = new PointF();
    private RectF mWgsBounds = new RectF();
    private float mBearing = 0;
    private float mZoom = 0;
    private MapView mMapView = null;

    public MapOverlayDispatch(Context context) {
        super(context);
    }

    public MapOverlayDispatch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapOverlayDispatch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setMapView(MapView mapView) {
        mMapView = mapView;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMapView != null) {
            for (int i = 0; i < mOverlayList.size(); i++) {
                Overlay overlay = mOverlayList.get(i);
                if (overlay.isOverlayEnabled()) {
                    overlay.onOverlayDraw(mMapView, canvas, mWgsBounds, mWgsCenter, mBearing, mZoom);
                }
            }
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onOverlayAttachedToWindow();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onOverlayDetachedFromWindow();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onOverlayTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    public void addOverlay(Overlay overlay) {
        mOverlayList.add(overlay);
    }

    public void clearOverlays() {
        mOverlayList.clear();
    }

    public void removeOverlay(Overlay overlay) {
        mOverlayList.remove(overlay);
    }

    public void invalidate(final RectF wgsBounds, final PointF wgsCenter, float bearing, float zoom) {
        mWgsCenter.set(wgsCenter);
        mWgsBounds.set(wgsBounds);
        mBearing = bearing;
        mZoom = zoom;
        super.invalidate();

    }
}
