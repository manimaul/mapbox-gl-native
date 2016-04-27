
package com.mapbox.mapboxsdk.provider;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//@Generated("org.jsonschema2pojo")
@SuppressWarnings("unused")
public class Mapbox {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("tileSize")
    @Expose
    private Integer tileSize;

    /**
     * 
     * @return
     *     The type
     */
    public String getType() {
        return type;
    }

    /**
     * 
     * @param type
     *     The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 
     * @return
     *     The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * 
     * @param url
     *     The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 
     * @return
     *     The tileSize
     */
    public Integer getTileSize() {
        return tileSize;
    }

    /**
     * 
     * @param tileSize
     *     The tileSize
     */
    public void setTileSize(Integer tileSize) {
        this.tileSize = tileSize;
    }

}
