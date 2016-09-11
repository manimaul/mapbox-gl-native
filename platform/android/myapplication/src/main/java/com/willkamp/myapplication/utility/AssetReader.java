package com.willkamp.myapplication.utility;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssetReader {

    /**
     * Read asset contents as a String.
     *
     * @param resources application resources.
     * @param asset name of the asset to open.
     * @return the contents of the asset.
     */
    @Nullable
    public static String readAssetAsString(Resources resources, String asset) {
        byte[] data = readAssetByteArray(resources, asset);
        if (data != null) {
            return new String(data);
        }
        return "";
    }

    @Nullable
    public static byte[] readAssetByteArray(Resources resources, String asset) {
        byte[] data;
        AssetManager assetManager = resources.getAssets();
        try (InputStream is = assetManager.open(asset);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read = is.read(buffer);
            while (read != -1) {
                os.write(buffer, 0, read);
                read = is.read(buffer);
            }
            data = os.toByteArray();
        } catch (IOException ignored) {
            data = new byte[0];
        }
        return data;
    }

}
