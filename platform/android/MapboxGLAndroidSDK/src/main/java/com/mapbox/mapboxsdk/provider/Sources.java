
package com.mapbox.mapboxsdk.provider;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//@Generated("org.jsonschema2pojo")
@SuppressWarnings("unused")
public class Sources {

    @SerializedName("mapbox")
    @Expose
    private Mapbox mapbox;

    /**
     * 
     * @return
     *     The mapbox
     */
    public Mapbox getMapbox() {
        return mapbox;
    }

    /**
     * 
     * @param mapbox
     *     The mapbox
     */
    public void setMapbox(Mapbox mapbox) {
        this.mapbox = mapbox;
    }

}
