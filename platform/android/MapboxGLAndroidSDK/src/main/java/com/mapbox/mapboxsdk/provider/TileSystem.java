package com.mapbox.mapboxsdk.provider;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class TileSystem {

    static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    static final int TILE_SIZE = 4096;
    static final double MIN_LAT_Y = -85.05112878;
    static final double MAX_LAT_Y = 85.05112878;
    static final double MIN_LNG_X = -180.;
    static final double MAX_LNG_X = 180.;

    public static void latLngToTileBoundedXy(Coordinate coordinate, int z, int x, int y) {
        Pixel pixel = latLngToPixel(coordinate, z);
        coordinate.x = pixel.x - x * TILE_SIZE;
        coordinate.y = (pixel.y - y * TILE_SIZE) - y;
    }

    static Pixel latLngToPixel(Coordinate lngLat, int z) {
        double lat_y = clip(lngLat.y, MIN_LAT_Y, MAX_LAT_Y);
        double lng_x = clip(lngLat.x, MIN_LNG_X, MAX_LNG_X);
        double x = (lng_x + 180.d) / 360.d;
        double sin_lat = Math.sin(lat_y * Math.PI / 180.d);
        double y = .5d - Math.log((1.d + sin_lat) / (1.d - sin_lat)) / (4.d * Math.PI);
        double m_size = mapSize(z);
        long px = (long) clip(x * m_size + .5d, 0.d, m_size - 1.d);
        long py = (long) clip(y * m_size + .5d, 0.d, m_size - 1.d);
        return new Pixel(px, py);
    }

    public static Polygon tileClipPolygon(int z, int x, int y) {
        LatLngBounds rect = zxyToLngLatBounds(z, x, y);
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(rect.westX, rect.northY);
        coordinates[1] = new Coordinate(rect.eastX, rect.northY);
        coordinates[2] = new Coordinate(rect.eastX, rect.southY);
        coordinates[3] = new Coordinate(rect.westX, rect.southY);
        coordinates[4] = new Coordinate(rect.westX, rect.northY);
        return GEOMETRY_FACTORY.createPolygon(coordinates);
    }

    static LatLngBounds zxyToLngLatBounds(int z, int x, int y) {
        final Pixel p = pixel(x, y);
        final LatLng tl = pixelToLngLat(p, z);
        p.x = p.x + TILE_SIZE;
        final LatLng tr = pixelToLngLat(p, z);
        p.y = p.y + TILE_SIZE;
        final LatLng br = pixelToLngLat(p, z);
        p.x = p.x - TILE_SIZE;
        final LatLng bl = pixelToLngLat(p, z);
        final double left = Math.min(tl.lngX, bl.lngX);
        final double top = Math.max(tl.latY, tr.latY);
        final double right = Math.max(tr.lngX, br.lngX);
        final double bottom = Math.min(br.latY, bl.latY);
        return new LatLngBounds(left, bottom, right, top);
    }

    public static double clip(double num, double min, double max) {
        return Math.min(Math.max(num, min), max);
    }

    static LatLng pixelToLngLat(Pixel pixel, int z) {
        double m_size = mapSize(z);
        double x = (clip(pixel.x, 0f, m_size - 1) / m_size) - .5f;
        double yf = .5f - (clip(pixel.y, 0f, m_size - 1) / m_size);
        double lat = (90.d - 360.d * Math.atan(Math.exp(-yf * 2d * Math.PI)) / Math.PI);
        double lng = (360.d * x);
        return new LatLng(lng, lat);
    }

    private static Pixel pixel(int x, int y) {
        return new Pixel(x * TILE_SIZE, y * TILE_SIZE);
    }

    private static int mapSize(int z) {
        return TILE_SIZE << z;
    }

    static class Pixel {
        long x;
        long y;

        public Pixel(long x, long y) {
            this.x = x;
            this.y = y;
        }
    }

    static class LatLng {
        double lngX;
        double latY;

        public LatLng(double lngX, double latY) {
            this.lngX = lngX;
            this.latY = latY;
        }
    }

    static class LatLngBounds {
        double westX;
        double southY;
        double eastX;
        double northY;

        public LatLngBounds(double westX, double southY, double eastX, double northY) {
            this.westX = westX;
            this.southY = southY;
            this.eastX = eastX;
            this.northY = northY;
        }
    }
}
