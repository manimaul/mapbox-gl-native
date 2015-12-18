package com.mapbox.mapboxsdk.testapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.mapbox.mapboxsdk.provider.OfflineProvider;
import com.mapbox.mapboxsdk.provider.OfflineProviderCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DemoOfflineTileProvider implements OfflineProvider {

    public static final int TILE_PIXELS = 256;

    @Override
    public void startFetchForTile(int z, int x, int y, OfflineProviderCallback callback) {
        byte[] png;
        try {
            final Bitmap newbitmap = Bitmap.createBitmap(TILE_PIXELS, TILE_PIXELS,
                    Bitmap.Config.ARGB_8888);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            final Canvas canvas = new Canvas(newbitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setTextSize(22f);
            canvas.drawColor(Color.WHITE);
            String message = "z: " + z + " x: " + x + " y: " + y;
            final float w = (TILE_PIXELS - paint.measureText(message)) / 2;
            canvas.drawText(message, w, TILE_PIXELS / 2, paint);
            newbitmap.compress(Bitmap.CompressFormat.PNG, 0, os);

            png = os.toByteArray();
            os.close();
        } catch (IOException ignored) {
            png = new byte[0];
        }

        callback.onResult(png.length > 0, png);
    }

    @Override
    public int minZoom() {
        return 0;
    }

    @Override
    public int maxZoom() {
        return 22;
    }

    @Override
    public int pixelsPerSide() {
        return TILE_PIXELS;
    }

    @Override
    public String name() {
        return DemoOfflineTileProvider.class.getSimpleName();
    }
}
