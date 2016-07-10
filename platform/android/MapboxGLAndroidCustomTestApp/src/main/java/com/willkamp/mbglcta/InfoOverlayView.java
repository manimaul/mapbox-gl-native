package com.willkamp.mbglcta;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Overlay;

public class InfoOverlayView implements Overlay {

    private final Context _context;
    private final Paint _paint = new Paint();
    private final Rect _rect = new Rect();

    public InfoOverlayView(Context context) {
        _context = context.getApplicationContext();
        _paint.setColor(Color.BLACK);
        _paint.setTextSize(25f);
        _paint.setStrokeWidth(3F);
    }

    @Override
    public void onOverlayDraw(MapboxMap mapboxMap, Canvas canvas, VisibleRegion visibleRegion, LatLng wgsCenter, float bearing, float zoom) {
        String message = "CENTER - lat:" + wgsCenter.getLatitude() +
                " lng:" + wgsCenter.getLongitude() +
                " bearing: " + bearing +
                " zoom: " + zoom;
        canvas.drawText(message, 0, _rect.height() / 2, _paint);
        canvas.drawText(visibleRegion.toString(), 0, _rect.height() / 4, _paint);
    }

    @Override
    public void onOverlayTouchEvent(MotionEvent event) {

    }

    @Override
    public void onOverlayAttached(MapboxMap mapboxMap) {

    }

    @Override
    public void onOverlayDetached() {

    }

    @Override
    public void onOverlaySingleTapConfirmed(LatLng pressPosition) {
        Toast.makeText(_context, "onOverlaySingleTapConfirmed() " + pressPosition.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOverlayLongPress(LatLng pressPosition) {
        Toast.makeText(_context, "onOverlayLongPress()" + pressPosition.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isOverlayDrawEnabled() {
        return true;
    }

    @Override
    public void onMapViewPixelBoundsChanged(Rect mapPixelBounds) {
        _rect.set(mapPixelBounds);
    }
}
