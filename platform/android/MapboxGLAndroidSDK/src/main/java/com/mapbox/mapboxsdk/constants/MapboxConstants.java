package com.mapbox.mapboxsdk.constants;

import android.content.Context;

import java.util.Locale;

/**
 * MapboxConstants exposes Mapbox related constants
 */
public class MapboxConstants {

    /**
     * Default Locale for data processing (ex: String.toLowerCase(MAPBOX_LOCALE, "foo"))
     */
    public static final Locale MAPBOX_LOCALE = Locale.US;

    /**
     * Key used to store access token in AndroidManifest.xml
     * @deprecated As of release 4.1.0, replaced by {@link com.mapbox.mapboxsdk.MapboxAccountManager#start(Context, String)}
     */
    @Deprecated
    public static final String KEY_META_DATA_MANIFEST = "com.mapbox.AccessToken";

    /**
     * Key used to store staging data server url in AndroidManifest.xml
     */
    public static final String KEY_META_DATA_STAGING_SERVER = "com.mapbox.TestEventsServer";

    /**
     * Key used to store staging data server access token in AndroidManifest.xml
     */
    public static final String KEY_META_DATA_STAGING_ACCESS_TOKEN = "com.mapbox.TestEventsAccessToken";

    /**
     * Key used to switch storage to external in AndroidManifest.xml
     */
    public final static String KEY_META_DATA_SET_STORAGE_EXTERNAL = "com.mapbox.SetStorageExternal";

    /**
     * Default animation time
     */
    public static final int ANIMATION_DURATION = 300;

    /**
     * Default short animation time
     */
    public static final int ANIMATION_DURATION_SHORT = 150;

    /**
     * The currently supported minimum zoom level.
     */
    public static final float MINIMUM_ZOOM = 0.0f;

    /**
     * The currently supported maximum zoom level.
     */
    public static final float MAXIMUM_ZOOM = 21.0f;

    /**
     * The currently supported maximum tilt value.
     */
    public static final double MAXIMUM_TILT = 60;

    /**
     * The currently supported minimum tilt value.
     */
    public static final double MINIMUM_TILT = 0;

    /**
     * The currently supported maximum direction
     */
    public static final double MAXIMUM_DIRECTION = 360;

    /**
     * The currently supported minimum direction
     */
    public static final double MINIMUM_DIRECTION = 0;

    /**
     * Fragment Argument Key for MapboxMapOptions
     */
    public static final String FRAG_ARG_MAPBOXMAPOPTIONS = "MapboxMapOptions";

    // Save instance state keys
    public static final String STATE_HAS_SAVED_STATE = "savedState";
    public static final String STATE_CAMERA_POSITION = "cameraPosition";
    public static final String STATE_ZOOM_ENABLED = "zoomEnabled";
    public static final String STATE_ZOOM_ENABLED_CHANGE = "zoomEnabledChange";
    public static final String STATE_SCROLL_ENABLED = "scrollEnabled";
    public static final String STATE_SCROLL_ENABLED_CHANGE = "scrollEnabledChange";
    public static final String STATE_ROTATE_ENABLED = "rotateEnabled";
    public static final String STATE_ROTATE_ENABLED_CHANGE = "rotateEnabledChange";
    public static final String STATE_TILT_ENABLED = "tiltEnabled";
    public static final String STATE_TILT_ENABLED_CHANGE = "tiltEnabledChange";
    public static final String STATE_ZOOM_CONTROLS_ENABLED = "zoomControlsEnabled";
    public static final String STATE_DEBUG_ACTIVE = "debugActive";
    public static final String STATE_STYLE_URL = "styleUrl";
    public static final String STATE_MY_LOCATION_ENABLED = "myLocationEnabled";
    public static final String STATE_MY_LOCATION_TRACKING_MODE = "myLocationTracking";
    public static final String STATE_MY_BEARING_TRACKING_MODE = "myBearingTracking";
    public static final String STATE_COMPASS_ENABLED = "compassEnabled";
    public static final String STATE_COMPASS_GRAVITY = "compassGravity";
    public static final String STATE_COMPASS_MARGIN_LEFT = "compassMarginLeft";
    public static final String STATE_COMPASS_MARGIN_TOP = "compassMarginTop";
    public static final String STATE_COMPASS_MARGIN_RIGHT = "compassMarginRight";
    public static final String STATE_COMPASS_MARGIN_BOTTOM = "compassMarginBottom";

    public static final String TAG = "MapboxMap";
}
