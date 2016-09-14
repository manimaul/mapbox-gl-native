package com.willkamp.myapplication.tiles;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TileSystemTest {

    @Test
    public void test_tileClipPolygon() {
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(0, 0);
        coordinates[1] = new Coordinate(4096, 0);
        coordinates[2] = new Coordinate(4096, 4096);
        coordinates[3] = new Coordinate(0, 4096);
        coordinates[4] = new Coordinate(0, 0);
        Polygon polygon = new GeometryFactory().createPolygon(coordinates);

        Polygon clipPolygon = TileSystem.tileClipPolygon(9, 92, 175);
        TileCoordinateTransformer transformer = new TileCoordinateTransformer(9, 92, 175);
        Polygon tilePolygon = (Polygon) transformer.transform(clipPolygon);
        assertEquals(polygon, tilePolygon);
    }

    @Test
    public void test_latLngToPixel() {
        double lat = 47.93723829;
        double lng = -122.24981998237;
        Coordinate coordinate = new Coordinate(lng, lat);
        TileSystem.Pixel pixel = TileSystem.latLngToPixel(coordinate, 9);
        assertEquals(336419, pixel.x, 0);
        assertEquals(729546, pixel.y, 0);
    }

    @Test
    public void test_latLngToTileBoundedXy() {
        TileSystem.LatLngBounds bounds = TileSystem.zxyToLngLatBounds(9, 92, 175);

        Coordinate coordinate = new Coordinate(bounds.northWest.lngX, bounds.northWest.latY);
        TileSystem.latLngToTileBoundedXy(coordinate, 9, 92, 175);
        assertEquals(0, coordinate.x, 0);
        assertEquals(4096, coordinate.y, 0);

        coordinate = new Coordinate(bounds.northEast.lngX, bounds.northEast.latY);
        TileSystem.latLngToTileBoundedXy(coordinate, 9, 92, 175);
        assertEquals(4096, coordinate.x, 0);
        assertEquals(4096, coordinate.y, 0);

        coordinate = new Coordinate(bounds.southEast.lngX, bounds.southEast.latY);
        TileSystem.latLngToTileBoundedXy(coordinate, 9, 92, 175);
        assertEquals(4096, coordinate.x, 0);
        assertEquals(0, coordinate.y, 0);

        coordinate = new Coordinate(bounds.southWest.lngX, bounds.southWest.latY);
        TileSystem.latLngToTileBoundedXy(coordinate, 9, 92, 175);
        assertEquals(0, coordinate.x, 0);
        assertEquals(0, coordinate.y, 0);
    }

    @Test
    public void test_zxyToLngLatBounds_000() throws Exception {
        TileSystem.LatLngBounds bounds = TileSystem.zxyToLngLatBounds(0, 0, 0);
        assertNotNull(bounds);
        assertEquals(TileSystem.MIN_LNG_X, bounds.northWest.lngX, .1);
        assertEquals(TileSystem.MIN_LNG_X, bounds.southWest.lngX, .1);

        assertEquals(TileSystem.MIN_LAT_Y, bounds.southEast.latY, .1);
        assertEquals(TileSystem.MIN_LAT_Y, bounds.southWest.latY, .1);

        assertEquals(TileSystem.MAX_LNG_X, bounds.northEast.lngX, .1);
        assertEquals(TileSystem.MAX_LNG_X, bounds.southEast.lngX, .1);

        assertEquals(TileSystem.MAX_LAT_Y, bounds.northEast.latY, .1);
        assertEquals(TileSystem.MAX_LAT_Y, bounds.northWest.latY, .1);
    }

    @Test
    public void test_zxyToLngLatBounds_100() throws Exception {
        TileSystem.LatLngBounds bounds = TileSystem.zxyToLngLatBounds(1, 0, 0);
        assertNotNull(bounds);

        assertEquals(TileSystem.MIN_LNG_X, bounds.northWest.lngX, .1);
        assertEquals(TileSystem.MIN_LNG_X, bounds.southWest.lngX, .1);

        assertEquals(0.d, bounds.southEast.latY, .1);
        assertEquals(0.d, bounds.southWest.latY, .1);

        assertEquals(0.d, bounds.northEast.lngX, .1);
        assertEquals(0.d, bounds.southEast.lngX, .1);

        assertEquals(TileSystem.MAX_LAT_Y, bounds.northEast.latY, .1);
        assertEquals(TileSystem.MAX_LAT_Y, bounds.northWest.latY, .1);
    }

    @Test
    public void test_zxyToLngLatBounds_111() throws Exception {
        TileSystem.LatLngBounds bounds = TileSystem.zxyToLngLatBounds(1, 1, 1);
        assertNotNull(bounds);

        assertEquals(0.d, bounds.northWest.lngX, .1);
        assertEquals(0.d, bounds.southWest.lngX, .1);

        assertEquals(TileSystem.MIN_LAT_Y, bounds.southEast.latY, .1);
        assertEquals(TileSystem.MIN_LAT_Y, bounds.southWest.latY, .1);

        assertEquals(TileSystem.MAX_LNG_X, bounds.northEast.lngX, .1);
        assertEquals(TileSystem.MAX_LNG_X, bounds.southEast.lngX, .1);

        assertEquals(0.d, bounds.northEast.latY, .1);
        assertEquals(0.d, bounds.northWest.latY, .1);
    }

    @Test
    public void test_latLngToPixel_000() {
        Coordinate coordinate = new Coordinate();
        coordinate.x = -180;
        coordinate.y = 90;
        TileSystem.Pixel pixel = TileSystem.latLngToPixel(coordinate, 0);
        assertEquals(0, pixel.x, 0);
        assertEquals(0, pixel.y, 0);

        coordinate.x = 180;
        coordinate.y = -90;
        pixel = TileSystem.latLngToPixel(coordinate, 0);
        assertEquals(TileSystem.TILE_SIZE, pixel.x, 1);
        assertEquals(TileSystem.TILE_SIZE, pixel.y, 1);

        coordinate.x = 0;
        coordinate.y = 0;
        pixel = TileSystem.latLngToPixel(coordinate, 0);
        assertEquals(TileSystem.TILE_SIZE / 2, pixel.x, 1);
        assertEquals(TileSystem.TILE_SIZE / 2, pixel.y, 1);
    }

    @Test
    public void test_latLngToPixel_100() {
        Coordinate coordinate = new Coordinate();
        coordinate.x = -180;
        coordinate.y = 90;
        TileSystem.Pixel pixel = TileSystem.latLngToPixel(coordinate, 0);
        assertEquals(0, pixel.x, 0);
        assertEquals(0, pixel.y, 0);

        coordinate.x = 180;
        coordinate.y = -90;
        pixel = TileSystem.latLngToPixel(coordinate, 0);
        assertEquals(TileSystem.TILE_SIZE, pixel.x, 1);
        assertEquals(TileSystem.TILE_SIZE, pixel.y, 1);

        coordinate.x = 0;
        coordinate.y = 0;
        pixel = TileSystem.latLngToPixel(coordinate, 0);
        assertEquals(TileSystem.TILE_SIZE / 2, pixel.x, 1);
        assertEquals(TileSystem.TILE_SIZE / 2, pixel.y, 1);
    }

    @Test
    public void test_pixelToLngLat_z1() {
        TileSystem.Pixel p = new TileSystem.Pixel(8192, 4096);
        TileSystem.LatLng latLng = TileSystem.pixelToLngLat(p, 1);

        assertEquals(0, latLng.latY, .1);
        assertEquals(180, latLng.lngX, .1);
    }

}