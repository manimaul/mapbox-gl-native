
package com.mapbox.mapboxsdk.provider;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

//@Generated("org.jsonschema2pojo")
@SuppressWarnings("unused")
public class OfflineStyle {

    @SerializedName("version")
    @Expose
    private Integer version;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("sources")
    @Expose
    private Sources sources;
    @SerializedName("layers")
    @Expose
    private List<Layer> layers = new ArrayList<>();

    /**
     * 
     * @return
     *     The version
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * 
     * @param version
     *     The version
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * 
     * @return
     *     The name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The sources
     */
    public Sources getSources() {
        return sources;
    }

    /**
     * 
     * @param sources
     *     The sources
     */
    public void setSources(Sources sources) {
        this.sources = sources;
    }

    /**
     * 
     * @return
     *     The layers
     */
    public List<Layer> getLayers() {
        return layers;
    }

    /**
     * 
     * @param layers
     *     The layers
     */
    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

}
