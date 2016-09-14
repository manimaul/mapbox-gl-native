package com.willkamp.myapplication.tiles;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.willkamp.myapplication.application.ApplicationLifeCycle;
import com.willkamp.myapplication.application.MyApplication;
import com.willkamp.myapplication.utility.HandlerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import no.ecc.vectortile.VectorTileEncoder;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public enum VectorTileDao {

    //region CONSTANTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    INSTANCE;

    private static final String DATABASE_NAME = "base_map_tile_geometries.s3db";
    private final String TAG = VectorTileDao.class.getSimpleName();

    //endregion

    //region FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private Context mContext;
    private final Handler mHandler = HandlerFactory.provideHandlerOwnThread();
    private final Scheduler mScheduler = AndroidSchedulers.from(mHandler.getLooper());
    private SQLiteDatabase mDatabase;

    private byte[] sFullTile;
    private byte[] sEmptyTile;
    private Observable<SQLiteDatabase> mDatabaseObservable = null;

    //endregion

    //region INJECTED DEPENDENCIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region INJECTED VIEWS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    VectorTileDao() {
        mContext = MyApplication.getAppContext();
        VectorTileEncoder encoder = new VectorTileEncoder();
        sEmptyTile = encoder.encode();
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(0, 0);
        coordinates[1] = new Coordinate(4096, 0);
        coordinates[2] = new Coordinate(4096, 4096);
        coordinates[3] = new Coordinate(0, 4096);
        coordinates[4] = new Coordinate(0, 0);
        Polygon polygon = new GeometryFactory().createPolygon(coordinates);
        encoder.addFeature("land", Collections.<String, Object>emptyMap(), polygon);
        sFullTile = encoder.encode();

        ApplicationLifeCycle.getEventsObservable().filter(new Func1<ApplicationLifeCycle.Event, Boolean>() {
            @Override
            public Boolean call(ApplicationLifeCycle.Event event) {
                return event == ApplicationLifeCycle.Event.APPLICATION_DID_ENTER_FOREGROUND && mDatabase == null;
            }
        }).flatMap(new Func1<ApplicationLifeCycle.Event, Observable<SQLiteDatabase>>() {
            @Override
            public Observable<SQLiteDatabase> call(ApplicationLifeCycle.Event event) {
                return getDatabase();
            }
        }).observeOn(mScheduler).subscribe(new Action1<SQLiteDatabase>() {
            @Override
            public void call(SQLiteDatabase database) {
                mDatabase = database;
            }
        });

        ApplicationLifeCycle.getEventsObservable().filter(new Func1<ApplicationLifeCycle.Event, Boolean>() {
            @Override
            public Boolean call(ApplicationLifeCycle.Event event) {
                return event == ApplicationLifeCycle.Event.APPLICATION_DID_ENTER_BACKGROUND && mDatabase != null;
            }
        }).observeOn(mScheduler).subscribe(new Action1<ApplicationLifeCycle.Event>() {
            @Override
            public void call(ApplicationLifeCycle.Event event) {
                mDatabase.close();
                mDatabase = null;
            }
        });
    }

    //endregion

    //region PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static File dbFile(Context context) {
        return new File(context.getFilesDir(), DATABASE_NAME);
    }

    private Observable<SQLiteDatabase> getDatabase() {
        if (mDatabase != null) {
            Log.d(TAG, "getDatabase() thread " + Thread.currentThread().getId() + Thread.currentThread().getName());
            return Observable.just(mDatabase).subscribeOn(mScheduler);
        } else if (mDatabaseObservable != null) {
            return mDatabaseObservable;
        } else {
            mDatabaseObservable = Observable.create(new Observable.OnSubscribe<SQLiteDatabase>() {
                @Override
                public void call(Subscriber<? super SQLiteDatabase> subscriber) {
                    File dbFile = new File(mContext.getFilesDir(), DATABASE_NAME);
                    if (!dbFile.exists()) {
                        try (InputStream is = mContext.getAssets().open(DATABASE_NAME);
                             OutputStream os = new FileOutputStream(dbFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = is.read(buffer)) > 0) {
                                os.write(buffer, 0, length);
                            }
                            os.flush();
                        } catch (Exception e) {
                            subscriber.onError(e);
                            return;
                        }
                    }
                    Log.d(TAG, "getDatabase() opening DB thread " + Thread.currentThread().getId() + Thread.currentThread().getName());
                    SQLiteDatabase database = SQLiteDatabase.openDatabase(dbFile(mContext).getAbsolutePath(), null,
                            SQLiteDatabase.OPEN_READWRITE);
                    subscriber.onNext(database);
                    subscriber.onCompleted();
                }
            }).doOnCompleted(new Action0() {
                @Override
                public void call() {
                    mDatabaseObservable = null;
                }
            })
                    .subscribeOn(mScheduler)
                    .share(); // multi-cast source observable so we only have one api request in-flight
        }

        return mDatabaseObservable;
    }

    private GeometryRecord[] getGeometryRecords(SQLiteDatabase database, String zxy) {
        Log.d(TAG, "getGeometryRecords() thread " + Thread.currentThread().getId() + Thread.currentThread().getName());
        final String table = "base_geometries";
        final String[] columns = {"source_file", "geometry", "layer_name", "label"};
        final String selection = "zxy=?";
        final String[] selectionArgs = {zxy};
        final String groupBy = null;
        final String having = null;
        final String orderBy = null;
        Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
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
        Log.d(TAG, "isFullTile() thread " + Thread.currentThread().getId() + Thread.currentThread().getName());
        final String table = "base_geometries_full";
        final String[] columns = {"rowid"};
        final String selection = "zxy=?";
        final String[] selectionArgs = {zxy};
        final String groupBy = null;
        final String having = null;
        final String orderBy = null;
        Cursor cursor = mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        boolean retVal = cursor.moveToFirst();
        cursor.close();
        return retVal;
    }

    private byte[] getVectorTile(int tile_z, int tile_x, int tile_y, Target target) {
        Log.d(TAG, "getVectorTile() thread " + Thread.currentThread().getId() + Thread.currentThread().getName());
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
        GeometryRecord[] records = getGeometryRecords(mDatabase, zxy);
        if (records.length > 0) {
            VectorTileEncoder encoder = new VectorTileEncoder(TileSystem.TILE_SIZE, 8, false);
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

    public Observable<Void> getIsDatabaseReadyObservable() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                while (mDatabase == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        subscriber.onError(e);
                        return;
                    }
                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread()).asObservable();
    }

    public Observable<byte[]> getVectorTileObservable(final int z, final int x, final int y) {
        final long t = System.currentTimeMillis();
        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(Subscriber<? super byte[]> subscriber) {
                if (mDatabase != null) {
                    subscriber.onNext(getVectorTile(z, x, y, null));
                    long dt = System.currentTimeMillis() - t;
                    String message = String.format(Locale.US, "Tile access time for %d/%d/%d = %dms", z, x, y, dt);
                    Log.d(TAG, message);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new Throwable("database is closed"));
                }
            }
        }).subscribeOn(mScheduler).asObservable();
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
