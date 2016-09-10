package com.willkamp.myapplication.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.http.OfflineInterceptor;
import com.mapbox.mapboxsdk.http.OfflineInterceptorCallback;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.willkamp.myapplication.R;
import com.willkamp.myapplication.utility.AssetReader;
import com.willkamp.myapplication.vectortiles.VectorTileDao;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //region CONSTANTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final String TAG = MainActivity.class.getSimpleName();

    //endregion

    //region FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region INJECTED DEPENDENCIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region INJECTED VIEWS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private MapView mMapView;
    private MapboxMap mMapboxMap;

    //endregion

    //region CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region PUBLIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region ACCESSORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region ANDROID ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.mainMapView);
        mMapView.setStyleUrl("http://localhost/style.json", new OfflineInterceptor() {
            @Override
            public void cancel(Uri uri) {
                Log.d(TAG, "cancel() " + uri);
            }

            @Override
            public void handleRequest(Uri uri, OfflineInterceptorCallback callback) {
                Log.d(TAG, "handleRequest() " + uri);
                switch (uri.getPath()) {
                    case "/style.json": {
                        callback.onResult(true, AssetReader.readAssetByteArray(getResources(), "style.json"));
                        break;
                    }
                    case "/raster_data_source_v8.json": {
                        callback.onResult(true, AssetReader.readAssetByteArray(getResources(), "raster_data_source_v8.json"));
                        break;
                    }
                    case "/vector_data_source_v8.json": {
                        callback.onResult(true, AssetReader.readAssetByteArray(getResources(), "vector_data_source_v8.json"));
                        break;
                    }
                    default: {
                        List<String> segments = uri.getPathSegments();
                        if (segments.size() == 4) {
                            int z = Integer.parseInt(segments.get(1));
                            int x = Integer.parseInt(segments.get(2));
                            int y = Integer.parseInt(segments.get(3));
                            if ("raster".equals(segments.get(0))) {
                                callback.onResult(true, new byte[0]);
                            } else {
                                callback.onResult(true, VectorTileDao.INSTANCE.getVectorTile(z, x, y));
                            }
                        } else {
                            callback.onResult(false, new byte[0]);
                        }
                        break;
                    }
                }
            }

            @Override
            public String host() {
                return "localhost";
            }
        });
        mMapView.setAccessToken(getString(R.string.mapbox_access_token));
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mMapboxMap = mapboxMap;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void zoomIn(View view) {
        if (mMapboxMap != null) {
            mMapboxMap.moveCamera(CameraUpdateFactory.zoomIn());
        }
    }

    public void zoomOut(View view) {
        if (mMapboxMap != null) {
            mMapboxMap.moveCamera(CameraUpdateFactory.zoomOut());
        }
    }

    //endregion

    //region INNER CLASSES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

}
