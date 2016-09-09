package com.mapbox.mapboxsdk.provider;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.util.Locale;

public class TileSystem {

    public static final String TAG = TileSystem.class.getSimpleName();
    private static final WKTReader sWktReader = new WKTReader();
    private static final int TILE_SIZE = 4096;
    private static final double EARTH_RADIUS = 6378137.;
    private static final double EARTH_CIRCUMFERENCE = 2. * Math.PI * EARTH_RADIUS;  // at equator;
    private static final double ORIGIN_SHIFT = EARTH_CIRCUMFERENCE / 2.d;  // 20037508.342789244;
    private static final double MIN_LAT_Y = -85.05112878;
    private static final double MAX_LAT_Y = 85.05112878;
    private static final double MIN_LNG_X = -180.;
    private static final double MAX_LNG_X = 180.;
    private static final double INCHES_PER_METER = 39.3701;
    private static final double MAX_ZOOM_LEVEL = 23;

    public static void latLngToTileBoundedXy(Coordinate lngLat, int z, int x, int y) {
        Coordinate p = latLngToPixel(lngLat, z);
        p.x = p.x - x * TILE_SIZE;
        p.y = p.y - y * TILE_SIZE;
    }

    private static Coordinate latLngToPixel(Coordinate lngLat, int z) {
        double lat_y = clip(lngLat.y, MIN_LAT_Y, MAX_LAT_Y);
        double lng_x = clip(lngLat.x, MIN_LNG_X, MAX_LNG_X);
        double x = (lng_x + 180.) / 360.;
        double sin_lat = Math.sin(lat_y * Math.PI / 180.d);
        double y = .5d - Math.log((1.d + sin_lat) / (1.d - sin_lat)) / (4.d * Math.PI);
        double m_size = mapSize(z);
        int px = (int) clip(x * m_size + .5d, 0.d, m_size - 1.d);
        int py = (int) clip(y * m_size + .5d, 0.d, m_size - 1.d);
        return new Coordinate(px, py);
    }

    public static Polygon tileClipPolygon(int z, int x, int y) {
        RectF rect = zxyToLngLatBounds(z, x, y);
        String wkt = String.format(Locale.US, "POLYGON ((%f %f, %f %f, %f %f, %f %f, %f %f))",
                rect.left, rect.top,
                rect.right, rect.top,
                rect.right, rect.bottom,
                rect.left, rect.bottom,
                rect.left, rect.top);
        try {
            return (Polygon) sWktReader.read(wkt);
        } catch (ParseException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    private static RectF zxyToLngLatBounds(int z, int x, int y) {
        final Point p = pixel(x, y);
        final PointF tl = pixelToLngLat(p, z);
        p.x = p.x + TILE_SIZE;
        final PointF tr = pixelToLngLat(p, z);
        p.y = p.y + TILE_SIZE;
        final PointF br = pixelToLngLat(p, z);
        p.x = p.x - TILE_SIZE;
        final PointF bl = pixelToLngLat(p, z);
        final float left = Math.min(tl.x, bl.x);
        final float top = Math.max(tl.y, tr.y);
        final float right = Math.max(tr.x, br.x);
        final float bottom = Math.min(br.y, bl.y);
        return new RectF(left, top, right, bottom);
    }

    public static float clip(float num, float min, float max) {
        return Math.min(Math.max(num, min), max);
    }

    public static double clip(double num, double min, double max) {
        return Math.min(Math.max(num, min), max);
    }

    private static PointF pixelToLngLat(Point pixel, int z) {
        float m_size = mapSize(z);
        float xf = (clip(pixel.x, 0f, TILE_SIZE - 1) / m_size) - .5f;
        float yf = .5f - (clip(pixel.y, 0f, m_size - 1) / m_size);
        float lat = (float) (90.d - 360.d * Math.atan(-yf * 2d * Math.PI) / Math.PI);
        float lng = (float) 360.d * xf;
        return new PointF(lng, lat);
    }

    private static Point pixel(int x, int y) {
        return new Point(x * TILE_SIZE, y * TILE_SIZE);
    }

    private static float mapSize(int z) {
        return TILE_SIZE << z;
    }
}
