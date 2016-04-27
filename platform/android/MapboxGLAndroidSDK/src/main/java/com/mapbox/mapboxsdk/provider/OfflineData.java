
package com.mapbox.mapboxsdk.provider;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//@Generated("org.jsonschema2pojo")
@SuppressWarnings("unused")
public class OfflineData {

    @SerializedName("attribution")
    @Expose
    private String attribution;
    @SerializedName("autoscale")
    @Expose
    private Boolean autoscale;
    @SerializedName("bounds")
    @Expose
    private List<Integer> bounds = new ArrayList<>();
    @SerializedName("center")
    @Expose
    private List<Integer> center = new ArrayList<>();
    @SerializedName("created")
    @Expose
    private Integer created;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("mapbox_logo")
    @Expose
    private Boolean mapboxLogo;
    @SerializedName("maxzoom")
    @Expose
    private Integer maxzoom;
    @SerializedName("minzoom")
    @Expose
    private Integer minzoom;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("private")
    @Expose
    private Boolean _private;
    @SerializedName("scheme")
    @Expose
    private String scheme;
    @SerializedName("tilejson")
    @Expose
    private String tilejson;
    @SerializedName("tiles")
    @Expose
    private List<String> tiles = new ArrayList<>();
    @SerializedName("webpage")
    @Expose
    private String webpage;

    /**
     * @return The attribution
     */
    public String getAttribution() {
        return attribution;
    }

    /**
     * @param attribution The attribution
     */
    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    /**
     * @return The autoscale
     */
    public Boolean getAutoscale() {
        return autoscale;
    }

    /**
     * @param autoscale The autoscale
     */
    public void setAutoscale(Boolean autoscale) {
        this.autoscale = autoscale;
    }

    /**
     * @return The bounds
     */
    public List<Integer> getBounds() {
        return bounds;
    }

    /**
     * @param bounds The bounds
     */
    public void setBounds(List<Integer> bounds) {
        this.bounds = bounds;
    }

    /**
     * @return The center
     */
    public List<Integer> getCenter() {
        return center;
    }

    /**
     * @param center The center
     */
    public void setCenter(List<Integer> center) {
        this.center = center;
    }

    /**
     * @return The created
     */
    public Integer getCreated() {
        return created;
    }

    /**
     * @param created The created
     */
    public void setCreated(Integer created) {
        this.created = created;
    }

    /**
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The mapboxLogo
     */
    public Boolean getMapboxLogo() {
        return mapboxLogo;
    }

    /**
     * @param mapboxLogo The mapbox_logo
     */
    public void setMapboxLogo(Boolean mapboxLogo) {
        this.mapboxLogo = mapboxLogo;
    }

    /**
     * @return The maxzoom
     */
    public Integer getMaxzoom() {
        return maxzoom;
    }

    /**
     * @param maxzoom The maxzoom
     */
    public void setMaxzoom(Integer maxzoom) {
        this.maxzoom = maxzoom;
    }

    /**
     * @return The minzoom
     */
    public Integer getMinzoom() {
        return minzoom;
    }

    /**
     * @param minzoom The minzoom
     */
    public void setMinzoom(Integer minzoom) {
        this.minzoom = minzoom;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The _private
     */
    public Boolean getPrivate() {
        return _private;
    }

    /**
     * @param _private The private
     */
    public void setPrivate(Boolean _private) {
        this._private = _private;
    }

    /**
     * @return The scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * @param scheme The scheme
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * @return The tilejson
     */
    public String getTilejson() {
        return tilejson;
    }

    /**
     * @param tilejson The tilejson
     */
    public void setTilejson(String tilejson) {
        this.tilejson = tilejson;
    }

    /**
     * @return The tiles
     */
    public List<String> getTiles() {
        return tiles;
    }

    /**
     * @param tiles The tiles
     */
    public void setTiles(List<String> tiles) {
        this.tiles = tiles;
    }

    /**
     * @return The webpage
     */
    public String getWebpage() {
        return webpage;
    }

    /**
     * @param webpage The webpage
     */
    public void setWebpage(String webpage) {
        this.webpage = webpage;
    }

}