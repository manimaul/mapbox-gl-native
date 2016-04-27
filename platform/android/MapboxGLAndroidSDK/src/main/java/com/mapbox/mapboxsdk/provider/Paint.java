
package com.mapbox.mapboxsdk.provider;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//@Generated("org.jsonschema2pojo")
@SuppressWarnings("unused")
public class Paint {

    @SerializedName("background-color")
    @Expose
    private String backgroundColor;

    /**
     * 
     * @return
     *     The backgroundColor
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * 
     * @param backgroundColor
     *     The background-color
     */
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

}
