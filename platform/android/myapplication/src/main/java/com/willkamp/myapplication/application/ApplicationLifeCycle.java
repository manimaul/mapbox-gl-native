package com.willkamp.myapplication.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public enum ApplicationLifeCycle implements Application.ActivityLifecycleCallbacks {

    //region CONSTANTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    INSTANCE;

    //endregion

    //region FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private int mCount = 0;
    private BehaviorSubject<Event> mEventSubject = BehaviorSubject.create();

    //endregion

    //region INJECTED DEPENDENCIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region INJECTED VIEWS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region CONSTRUCTOR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region PUBLIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //endregion

    //region ACCESSORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * @return an observable that emits application life cycle events.
     * Note: The last occurring event will be re-emitted upon subscription.
     */
    public static Observable<Event> getEventsObservable() {
        return INSTANCE.mEventSubject.asObservable();
    }

    //endregion

    //region {Application.ActivityLifecycleCallbacks} ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {}

    @Override
    public void onActivityStarted(Activity activity) {
        ++mCount;
        if (mCount == 1) {
            mEventSubject.onNext(Event.APPLICATION_DID_ENTER_FOREGROUND);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {
        --mCount;
        if (mCount == 0) {
            mEventSubject.onNext(Event.APPLICATION_DID_ENTER_BACKGROUND);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}

    //endregion

    //region INNER CLASSES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public enum Event {
        APPLICATION_DID_ENTER_FOREGROUND,
        APPLICATION_DID_ENTER_BACKGROUND
    }

    //endregion

}
