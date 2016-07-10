package com.willkamp.mbglcta;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.mapbox.mapboxsdk.provider.OfflineProvider;
import com.mapbox.mapboxsdk.provider.OfflineProviderCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

public class DrawTileOfflineProvider implements OfflineProvider {

    public static final String TAG = DrawTileOfflineProvider.class.getSimpleName();

    private final Paint blackPaint = new Paint();

    public DrawTileOfflineProvider() {
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStrokeWidth(3F);
    }

    @Override
    public void startFetchForTile(final int z, final int x, final int y, final OfflineProviderCallback callback) {
        final Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.YELLOW);
        canvas.drawLine(  0,   0, 256,   0, blackPaint);
        canvas.drawLine(256,   0, 256, 256, blackPaint);
        canvas.drawLine(256, 256,   0, 256, blackPaint);
        String tileStr = String.format(Locale.US, "%d, %d, %d", z, x, y);
        canvas.drawText(tileStr, 0, 128, blackPaint);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
        bitmap.recycle();
        byte[] result = os.toByteArray();
        try {
            os.close();
        } catch (IOException error) {
            Log.e(TAG, "", error);
        }

        if (result != null) {
            callback.onResult(true, result);
        } else {
            callback.onResult(false, null);
        }
    }

    @Override
    public int minZoom() {
        return 0;
    }

    @Override
    public int maxZoom() {
        return 21;
    }

    @Override
    public int pixelsPerSide() {
        return 256;
    }

    @Override
    public String name() {
        return "drawTileProvider";
    }

}
