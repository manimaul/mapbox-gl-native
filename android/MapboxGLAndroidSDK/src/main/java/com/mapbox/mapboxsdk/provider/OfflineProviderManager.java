package com.mapbox.mapboxsdk.provider;

import android.content.res.Resources;

import com.mapbox.mapboxsdk.http.DataRequest;
import com.mapbox.mapboxsdk.http.OfflineRequest;

public class OfflineProviderManager {

    public static final String LOCALHOST = "localhost";
    private static OfflineProviderManager sInstance = new OfflineProviderManager();
    private OfflineProvider mProvider = null;
    private byte[] mData;
    private boolean mAvailable = false;

    public static OfflineProviderManager getInstance() {
        return sInstance;
    }

    private OfflineProviderManager() {
    }

    public void setAvailable(boolean available) {
        this.mAvailable = available;
    }

    public String registerProvider(Resources resources, OfflineProvider provider) {
        if (!mAvailable) {
            throw new IllegalStateException("Offline providers are not available for this build. " +
                    "The Mapbox SDK must be compiled with an 'android' http context.");
        }

        String style = AssetReader.readAssetAsString(resources, "offline_style_v8.json");
        mData = AssetReader.readAssetByteArray(resources, "offline_data_v8.json");
        if (style == null && mData != null) {
            mProvider = null;
        } else {
            mProvider = provider;
        }
        return style;
    }

    public void unRegisterProvider() {
        mProvider = null;
    }

    public boolean willHandleUrl(String resourceUrl) {
        if (mProvider != null) {
            String host = getUrlHost(resourceUrl);
            if (LOCALHOST.equals(host)) {
                return true;
            }
        }

        return false;
    }

    public DataRequest createDataRequest(long nativePtr, String resourceUrl) {
        DataRequest request = null;
        int i = resourceUrl.indexOf(LOCALHOST);
        if (i > 0) {
            if (mProvider != null) {
                String[] zxy = resourceUrl.substring(i + LOCALHOST.length() + 1 /* / */, resourceUrl.length()).split("/");
                int z, x, y;
                if (zxy.length == 3) {
                    z = Integer.parseInt(zxy[0]);
                    x = Integer.parseInt(zxy[1]);
                    y = Integer.parseInt(zxy[2]);
                    request = OfflineRequest.create(nativePtr, mProvider, z, x, y);
                } else {
                    request = OfflineRequest.create(nativePtr, mData);
                }
            }
        }

        return request;
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
}
