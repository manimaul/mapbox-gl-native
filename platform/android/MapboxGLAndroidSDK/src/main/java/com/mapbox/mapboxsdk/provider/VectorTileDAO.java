package com.mapbox.mapboxsdk.provider;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import no.ecc.vectortile.VectorTileEncoder;

public class VectorTileDAO {

    //region CONSTANTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static final String TAG = VectorTileDAO.class.getSimpleName();

    //endregion

    //region FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private final SQLiteDatabase mDatabase;
    private byte[] sFullTile;
    private byte[] sEmptyTile;

    //endregion

    //region CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public VectorTileDAO(SQLiteDatabase database) {
        mDatabase = database;
        VectorTileEncoder encoder = new VectorTileEncoder();
        sEmptyTile = encoder.encode();
        try {
            Polygon polygon = (Polygon) new WKTReader().read("POLYGON ((0 0, 4096 0, 4096 4096, 0 4096, 0 0))");
            encoder.addFeature("land", Collections.<String, Object>emptyMap(), polygon);
            sFullTile = encoder.encode();
        } catch (ParseException e) {
            Log.e(TAG, "", e);
        }
    }

    //endregion

    //region PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private GeometryRecord[] getGeometryRecords(String zxy) {
        final String table = "base_geometries";
        final String[] columns = { "source_file", "geometry", "layer_name", "label"};
        final String selection = "zxy=?";
        final String[] selectionArgs = { zxy };
        final String groupBy = null;
        final String having = null;
        final String orderBy = null;
        Cursor cursor = mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        final GeometryRecord[] records = new GeometryRecord[cursor.getCount()];
        cursor.moveToFirst();
        for (int i = 0; i < records.length; i++) {
            try {
                records[i] = new GeometryRecord(cursor.getString(0),
                        cursor.getBlob(1),
                        cursor.getString(2),
                        cursor.getString(3));
            } catch (ParseException e) {
                records[i] = null;
                Log.e(TAG, "", e);
            }
            cursor.moveToNext();
        }
        cursor.close();
        return records;
    }

    private boolean isFullTile(String zxy) {
        final String table = "base_geometries_full";
        final String[] columns = { "rowid" };
        final String selection = "zxy=?";
        final String[] selectionArgs = { zxy };
        final String groupBy = null;
        final String having = null;
        final String orderBy = null;
        Cursor cursor = mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        boolean retVal = cursor.moveToFirst();
        cursor.close();
        return retVal;
    }

    private byte[] getVectorTile(int tile_z, int tile_x, int tile_y, Target target) {
        String zxy = String.format(Locale.US, "%s/%s/%s", tile_z, tile_x, tile_y);
        if (isFullTile(zxy)) {
            return sFullTile;
        }
        if (target == null) {
            target = new Target(tile_z, tile_x, tile_y);
        }
        if (target.polygon == null) {
            return sEmptyTile;
        }

        Map<String, String> attributes = new HashMap<>();
        GeometryRecord[] records = getGeometryRecords(zxy);
        if (records.length > 0) {
            VectorTileEncoder encoder = new VectorTileEncoder();
            TileCoordinateTransformer transformer = new TileCoordinateTransformer(target.z, target.x, target.y);
            int count = 0;
            for (int i = 0; i < records.length; i++) {
                GeometryRecord record = records[i];
                Geometry intersecting = record.getGeometry().intersection(target.polygon);
                if (!intersecting.isEmpty()) {
                    Geometry tileGeometry = transformer.transform(intersecting);
                    attributes.clear();
                    attributes.put("name", record.getLabel());
                    encoder.addFeature(record.getLayerName(), attributes, tileGeometry);
                    count++;
                }
            }
            if (count > 0) {
                return encoder.encode();
            } else {
                return sEmptyTile;
            }
        }

        return getVectorTile(tile_z - 1, tile_x >> 1, tile_y >> 1, target);
    }

    //endregion

    //region PUBLIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public byte[] getVectorTile(int z, int x, int y) {
        return getVectorTile(z, x, y, null);
    }

    //endregion

    //region ACCESSORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region INNER CLASSES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final class Target {
        private final Polygon polygon;
        private final int z, x, y;

        private Target(int z, int x, int y) {
            this.z = z;
            this.x = x;
            this.y = y;
            polygon = TileSystem.tileClipPolygon(z, x, y);
        }
    }
    //endregion

}
