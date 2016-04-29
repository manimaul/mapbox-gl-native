package com.mapbox.mapboxsdk.provider;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.gson.Gson;
import com.mapbox.mapboxsdk.http.HTTPRequest;

import java.util.UUID;

public class OfflineProviderManager {

    private static final String LOG_TAG = OfflineProviderManager.class.getSimpleName();

    public static final String LOCALHOST = "localhost";
    @VisibleForTesting String mHost = LOCALHOST;
    private static final Gson sGson = new Gson();
    private static OfflineProviderManager sInstance = new OfflineProviderManager();
    private byte[] mData;
    @VisibleForTesting OfflineProvider mProvider = null;
    private String mStyleDataUrl;

    public static OfflineProviderManager getInstance() {
        return sInstance;
    }

    private OfflineProviderManager() {
    }

    public String registerProvider(OfflineProvider provider) {
        String id = "offline_" + provider.name();
        String uid = UUID.randomUUID().toString();
        mHost = LOCALHOST + "_" + uid;
        Log.d(LOG_TAG, "Configuring host: " + mHost);
        mProvider = provider;
        OfflineStyle offlineStyle = createOfflineStyle(id);
        OfflineData offlineData = createOfflineData(id);
        mData = sGson.toJson(offlineData).getBytes();
        return sGson.toJson(offlineStyle);
    }

    private OfflineData createOfflineData(String id) {
        /*
        {
          "attribution": "",
          "autoscale": true,
          "bounds": [
            -180,
            -85,
            180,
            85
          ],
          "center": [
            0,
            0,
            3
          ],
          "created": 1358310600000,
          "description": "",
          "id": "offline",
          "mapbox_logo": false,
          "maxzoom": 19,
          "minzoom": 0,
          "name": "Offline",
          "private": false,
          "scheme": "xyz",
          "tilejson": "2.0.0",
          "tiles": [
            "http://localhost/{z}/{x}/{y}"
          ],
          "webpage": ""
        }
         */

        OfflineData offlineData = new OfflineData();
        offlineData.setAttribution("http://mxmariner.com/");
        offlineData.setAutoscale(true);
        offlineData.getBounds().add(-180);
        offlineData.getBounds().add(-85);
        offlineData.getBounds().add(180);
        offlineData.getBounds().add(85);
        offlineData.getCenter().add(0);
        offlineData.getCenter().add(0);
        offlineData.getCenter().add(3);
        offlineData.setCreated(0);
        offlineData.setDescription("");
        offlineData.setId(id);
        offlineData.setMapboxLogo(false);
        offlineData.setMaxzoom(21);
        offlineData.setMinzoom(0);
        offlineData.setName(mProvider.name());
        offlineData.setPrivate(true);
        offlineData.setScheme("xyz");
        offlineData.setTilejson("2.0.0");
        String tileUrl = String.format("http://%s/{z}/{x}/{y}", mHost);
        offlineData.getTiles().add(tileUrl);
        offlineData.setWebpage("");
        return offlineData;
    }

    private OfflineStyle createOfflineStyle(String id) {
        /*
        {
          "version": 8,
          "name": "Offline",
          "sources": {
            "mapbox": {
              "type": "raster",
              "url": "http://localhost/offline_data_v8.json",
              "tileSize": 256
            }
          },
          "layers": [
            {
              "id": "background",
              "type": "background",
              "paint": {
                "background-color": "rgb(255,255,255)"
              }
            },
            {
              "id": "offline",
              "type": "raster",
              "source": "mapbox",
              "source-layer": "mapbox_offline_full"
            }
          ]
        }
         */
        OfflineStyle offlineStyle = new OfflineStyle();
        offlineStyle.setVersion(8);
        offlineStyle.setName(mProvider.name());

        Sources sources = new Sources();
        Mapbox mapbox = new Mapbox();
        mapbox.setType("raster");
        mStyleDataUrl = String.format("http://%s/offline_data_v8.json", mHost);
        mapbox.setUrl(mStyleDataUrl);
        mapbox.setTileSize(256);
        sources.setMapbox(mapbox);
        offlineStyle.setSources(sources);

        Layer bgLayer = new Layer();
        bgLayer.setId("background");
        bgLayer.setType("background");
        Paint paint = new Paint();
        paint.setBackgroundColor("rgb(255,255,255)");
        bgLayer.setPaint(paint);
        offlineStyle.getLayers().add(bgLayer);


        Layer fgLayer = new Layer();
        fgLayer.setId(id);
        fgLayer.setType("raster");
        fgLayer.setSource("mapbox");
        fgLayer.setSourceLayer("mapbox_offline_full");
        offlineStyle.getLayers().add(fgLayer);
        return offlineStyle;
    }

    String getUrlHost(String url) {
        if (url != null) {
            int li = url.indexOf("://");
            if (li > -1) {
                li += 3; // length of ://
                int ri = url.indexOf("/", li);
                if (ri > -1) {
                    return url.substring(li, ri);
                }
            }
        }
        return null;
    }

    public String getStyleDataUrl() {
        if (mStyleDataUrl == null) {
            return "http://" + LOCALHOST + "/";
        }
        return mStyleDataUrl;
    }

    public void unRegisterProvider() {
        mProvider = null;
        mData = null;
        mHost = LOCALHOST;
    }

    public boolean willHandleUrl(String resourceUrl) {
        if (mProvider != null) {
            String host = getUrlHost(resourceUrl);
            if (mHost.equals(host)) {
                return true;
            }
        }

        return false;
    }

    public void handleRequest(final HTTPRequest httpRequest, String resourceUrl) {

        int i = resourceUrl.indexOf(mHost);
        if (i > 0) {
            if (mProvider != null) {
                String[] zxy = resourceUrl.substring(i + mHost.length() + 1, resourceUrl.length()).split("/");
                int z, x, y;
                if (zxy.length == 3) {
                    // Tile request
                    z = Integer.parseInt(zxy[0]);
                    x = Integer.parseInt(zxy[1]);
                    y = Integer.parseInt(zxy[2]);

                    mProvider.startFetchForTile(z, x, y, new OfflineProviderCallback() {
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
                    // Style data json
                    httpRequest.onOfflineResponse(mData);
                }
            }
        }
    }

    public void cancelRequest(HTTPRequest httpRequest) {
        Log.d(LOG_TAG, "Http request cancel no-op: " + httpRequest.getResourceUrl());
    }
}
