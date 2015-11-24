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
        super.onDraw(canvas);
        for (int i = 0; i < mOverlayList.size(); i++) {
            Overlay overlay = mOverlayList.get(i);
            if (overlay.isEnabled()) {
                overlay.drawLayer(canvas, mWgsBounds, mWgsCenter, mBearing, mZoom);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onAttachedToWindow();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onDetachedFromWindow();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        for (int i = 0; i < mOverlayList.size(); i++) {
            mOverlayList.get(i).onTouchEvent(event);
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
