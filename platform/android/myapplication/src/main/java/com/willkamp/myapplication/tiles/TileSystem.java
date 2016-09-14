package com.willkamp.myapplication.tiles;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class TileSystem {

    //region CONSTANTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    static final int TILE_SIZE = 4096;
    static final double MIN_LAT_Y = -85.05112878;
    static final double MAX_LAT_Y = 85.05112878;
    static final double MIN_LNG_X = -180.;
    static final double MAX_LNG_X = 180.;

    //endregion

    //region FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region INJECTED DEPENDENCIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region INJECTED VIEWS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private TileSystem() {
    }

    //endregion

    //region PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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

    static LatLngBounds zxyToLngLatBounds(int z, int x, int y) {
        final Pixel p = pixel(x, y);
        final LatLng northWest = pixelToLngLat(p, z);
        p.x = p.x + TILE_SIZE;
        final LatLng northEast = pixelToLngLat(p, z);
        p.y = p.y + TILE_SIZE;
        final LatLng southEast = pixelToLngLat(p, z);
        p.x = p.x - TILE_SIZE;
        final LatLng southWest = pixelToLngLat(p, z);
        return new LatLngBounds(northWest, northEast, southEast, southWest);
    }

    static LatLng pixelToLngLat(Pixel pixel, int z) {
        double m_size = mapSize(z);
        double x = (clip(pixel.x, 0f, m_size - 1) / m_size) - .5f;
        double yf = .5f - (clip(pixel.y, 0f, m_size - 1) / m_size);
        double lat = (90.d - 360.d * Math.atan(Math.exp(-yf * 2d * Math.PI)) / Math.PI);
        double lng = (360.d * x);
        return new LatLng(lng, lat);
    }

    static Pixel pixel(int x, int y) {
        return new Pixel(x * TILE_SIZE, y * TILE_SIZE);
    }

    static int mapSize(int z) {
        return TILE_SIZE << z;
    }

    //endregion

    //region PUBLIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static void latLngToTileBoundedXy(Coordinate coordinate, int z, int x, int y) {
        Pixel pixel = latLngToPixel(coordinate, z);
        coordinate.x = pixel.x - x * TILE_SIZE;
        coordinate.y = TILE_SIZE - (pixel.y - y * TILE_SIZE);
    }

    public static double clip(double num, double min, double max) {
        return Math.min(Math.max(num, min), max);
    }

    public static Polygon tileClipPolygon(int z, int x, int y) {
        LatLngBounds bounds = zxyToLngLatBounds(z, x, y);
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(bounds.northWest.lngX, bounds.northWest.latY);
        coordinates[1] = new Coordinate(bounds.northEast.lngX, bounds.northEast.latY);
        coordinates[2] = new Coordinate(bounds.southEast.lngX, bounds.southEast.latY);
        coordinates[3] = new Coordinate(bounds.southWest.lngX, bounds.southWest.latY);
        coordinates[4] = new Coordinate(bounds.northWest.lngX, bounds.northWest.latY);
        return GEOMETRY_FACTORY.createPolygon(coordinates);
    }

    //endregion

    //region ACCESSORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region INNER CLASSES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
        LatLng northWest;
        LatLng northEast;
        LatLng southEast;
        LatLng southWest;

        public LatLngBounds(LatLng northWest, LatLng northEast, LatLng southEast, LatLng southWest) {
            this.northWest = northWest;
            this.northEast = northEast;
            this.southEast = southEast;
            this.southWest = southWest;
        }
    }

    //endregion

}
