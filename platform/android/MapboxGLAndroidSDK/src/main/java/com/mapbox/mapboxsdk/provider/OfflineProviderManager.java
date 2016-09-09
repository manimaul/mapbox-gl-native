package com.mapbox.mapboxsdk.provider;

import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.mapbox.mapboxsdk.http.HTTPRequest;

import java.util.List;

public class OfflineProviderManager {

    //region CONSTANTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final String LOG_TAG = OfflineProviderManager.class.getSimpleName();
    static final String LOCALHOST = "localhost";

    //endregion

    //region FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static OfflineProviderManager sInstance;
    @VisibleForTesting
    OfflineProvider mProvider = null;
    private final Resources mResources;
    private final VectorTileDAO mVectorTileDAO;

    //endregion

    //region INJECTED DEPENDENCIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region INJECTED VIEWS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Nullable
    public static OfflineProviderManager getInstance() {
        return sInstance;
    }

    public static OfflineProviderManager getInstance(Resources resources, SQLiteDatabase database) {
        if (sInstance == null) {
            sInstance = new OfflineProviderManager(resources, database);
        }
        return sInstance;
    }

    private OfflineProviderManager(Resources resources, SQLiteDatabase database) {
        mResources = resources;
        mVectorTileDAO = new VectorTileDAO(database);
    }

    //endregion

    //region PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region PUBLIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public String registerProvider(OfflineProvider provider) {
        mProvider = provider;
        return AssetReader.readAssetAsString(mResources, "style.json");
    }


    public void unRegisterProvider() {
    }

    public boolean willHandleUrl(Uri resourceUrl) {
        return mProvider != null && LOCALHOST.equals(resourceUrl.getHost());
    }

    public void handleRequest(final HTTPRequest httpRequest, Uri resourceUrl) {
        // http://localhost/raster/{z}/{x}/{y}              4
        // http://localhost/vector/{z}/{x}/{y}              4
        // http://localhost/fonts/{fontstack}/{range}.pbf   3
        // http://localhost/raster_data_source_v8.json      1
        // http://localhost/vector_data_source_v8.json      1
        List<String> segments = resourceUrl.getPathSegments();
        switch (segments.size()) {
            case 4: {
                int z = Integer.parseInt(segments.get(1));
                int x = Integer.parseInt(segments.get(2));
                int y = Integer.parseInt(segments.get(3));
                if ("raster".equals(segments.get(0))) {
                    mProvider.startFetchForRasterTile(z, x, y, new OfflineProviderCallback() {
                        @Override
                        public void onResult(boolean success, byte[] result) {
                            if (success) {
                                httpRequest.onOfflineResponse(result);
                            } else {
                                httpRequest.onOfflineFailure();
                            }
                        }
                    });
                } else {
                    httpRequest.onOfflineResponse(mVectorTileDAO.getVectorTile(z, x, y));
                }
                break;
            }
            case 3: {
                httpRequest.onOfflineResponse(AssetReader.readAssetByteArray(mResources, "font.pbf"));
                break;
            }
            case 1: {
                if ("raster_data_source_v8.json".equals(segments.get(0))) {
                    httpRequest.onOfflineResponse(AssetReader.readAssetByteArray(mResources, "raster_data_source_v8.json"));
                } else if ("vector_data_source_v8.json".equals(segments.get(0))) {
                    httpRequest.onOfflineResponse(AssetReader.readAssetByteArray(mResources, "vector_data_source_v8.json"));
                } else {
                    httpRequest.onOfflineFailure();
                }
                break;
            }
            default: {
                httpRequest.onOfflineFailure();
                break;
            }
        }
    }

    public void cancelRequest(HTTPRequest httpRequest) {
        Log.d(LOG_TAG, "Http request cancel no-op: " + httpRequest.getResourceUrl());
    }

    //endregion

    //region ACCESSORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public String getStyleDataUrl() {
        return "http://" + LOCALHOST + "/style.json";
    }

    //endregion

    //region INNER CLASSES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

}
