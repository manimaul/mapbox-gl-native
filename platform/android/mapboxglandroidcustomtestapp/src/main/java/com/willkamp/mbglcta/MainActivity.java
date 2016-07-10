package com.willkamp.mbglcta;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class MainActivity extends AppCompatActivity {

    private MapView _mapView;
    private MapboxMap _mapBoxMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _mapView = (MapView) findViewById(R.id.mapView);
        _mapView.setOfflineProvider(new DrawTileOfflineProvider());
        _mapView.onCreate(savedInstanceState);

        _mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                setMapBoxMap(mapboxMap);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        _mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        _mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        _mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        _mapView.onLowMemory();
    }

    public void setMapBoxMap(MapboxMap mapBoxMap) {
        _mapBoxMap = mapBoxMap;
        _mapBoxMap.addOverlay(new InfoOverlayView(this));
    }
}
