package com.mapbox.mapboxsdk.offline;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class BaseMapVectorDataDb {

    //region CONSTANTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public static final String DATABASE_NAME = "base_map_tile_geometries.s3db";

    //endregion

    //region FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private Context mContext;
    private final Handler mHandler = provideHandlerOwnThread();
    private final Scheduler mScheduler = AndroidSchedulers.from(mHandler.getLooper());
    private SQLiteDatabase mDatabase;

    //endregion

    //region INJECTED DEPENDENCIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region INJECTED VIEWS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public BaseMapVectorDataDb(Context context) {
        mContext = context;
    }

    //endregion

    //region PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static File dbFile(Context context) {
        return new File(context.getFilesDir(), DATABASE_NAME);
    }

    private static Handler provideHandlerOwnThread() {
        HandlerThread handlerThread = new HandlerThread(UUID.randomUUID().toString());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        return new Handler(looper);
    }

    //endregion

    //region PUBLIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public Observable<SQLiteDatabase> getDatabase() {
        return Observable.create(new Observable.OnSubscribe<SQLiteDatabase>() {
            @Override
            public void call(Subscriber<? super SQLiteDatabase> subscriber) {
                if (mDatabase != null) {
                    subscriber.onNext(mDatabase);
                    subscriber.onCompleted();
                } else {
                    File dbFile = new File(mContext.getFilesDir(), DATABASE_NAME);
                    if (!dbFile.exists()) {
                        try {
                            InputStream is = mContext.getAssets().open(DATABASE_NAME);
                            OutputStream os = new FileOutputStream(dbFile);
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = is.read(buffer)) > 0) {
                                os.write(buffer, 0, length);
                            }
                            os.flush();
                            os.close();
                            is.close();
                        } catch (Exception e) {
                            subscriber.onError(e);
                            return;
                        }
                    }
                    mDatabase =SQLiteDatabase.openDatabase(dbFile(mContext).getAbsolutePath(), null,
                            SQLiteDatabase.OPEN_READWRITE);
                    subscriber.onNext(mDatabase);
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(mScheduler).asObservable();
    }

    //endregion

    //region ACCESSORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region {Closeable} ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region INNER CLASSES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

}
