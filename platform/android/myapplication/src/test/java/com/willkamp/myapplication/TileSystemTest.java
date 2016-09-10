package com.willkamp.myapplication;

import com.vividsolutions.jts.geom.Coordinate;
import com.willkamp.myapplication.vectortiles.TileSystem;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TileSystemTest {

    @Test
    public void test_zxyToLngLatBounds_000() throws Exception {
        TileSystem.LatLngBounds bounds = TileSystem.zxyToLngLatBounds(0, 0, 0);
        assertNotNull(bounds);
        assertEquals(TileSystem.MIN_LNG_X, bounds.westX, .1);
        assertEquals(TileSystem.MIN_LAT_Y, bounds.southY, .1);
        assertEquals(TileSystem.MAX_LNG_X, bounds.eastX, .1);
        assertEquals(TileSystem.MAX_LAT_Y, bounds.northY, .1);
    }

    @Test
    public void test_zxyToLngLatBounds_100() throws Exception {
        TileSystem.LatLngBounds bounds = TileSystem.zxyToLngLatBounds(1, 0, 0);
        assertNotNull(bounds);
        assertEquals(TileSystem.MIN_LNG_X, bounds.westX, .1);
        assertEquals(0.d, bounds.southY, .1);
        assertEquals(0.d, bounds.eastX, .1);
        assertEquals(TileSystem.MAX_LAT_Y, bounds.northY, .1);
    }

    @Test
    public void test_zxyToLngLatBounds_111() throws Exception {
        TileSystem.LatLngBounds bounds = TileSystem.zxyToLngLatBounds(1, 1, 1);
        assertNotNull(bounds);
        assertEquals(0.d, bounds.westX, .1);
        assertEquals(TileSystem.MIN_LAT_Y, bounds.southY, .1);
        assertEquals(TileSystem.MAX_LNG_X, bounds.eastX, .1);
        assertEquals(0.d, bounds.northY, .1);
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
    public void test_latLngToPixel_314() {
        Coordinate coordinate = new Coordinate();
        coordinate.x = -90;
        coordinate.y = 0;
        TileSystem.Pixel pixel = TileSystem.latLngToPixel(coordinate, 3);
        assertEquals(TileSystem.TILE_SIZE * 2, pixel.x, 0);
        assertEquals(TileSystem.TILE_SIZE * 4, pixel.y, 0);

        coordinate = new Coordinate();
        coordinate.x = -90;
        coordinate.y = -22.5;
        pixel = TileSystem.latLngToPixel(coordinate, 3);
        assertEquals(TileSystem.TILE_SIZE * 2, pixel.x, 0);
        assertEquals(TileSystem.TILE_SIZE * 5, pixel.y, 0);
    }

    @Test
    public void test_pixelToLngLat_z1() {
        TileSystem.Pixel p = new TileSystem.Pixel(8192, 4096);
        TileSystem.LatLng latLng = TileSystem.pixelToLngLat(p, 1);

        assertEquals(0, latLng.latY, .1);
        assertEquals(180, latLng.lngX, .1);
    }

}