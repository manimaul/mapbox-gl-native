
package com.mapbox.mapboxsdk.provider;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//@Generated("org.jsonschema2pojo")
@SuppressWarnings("unused")
public class Layer {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("paint")
    @Expose
    private Paint paint;
    @SerializedName("source")
    @Expose
    private String source;
    @SerializedName("source-layer")
    @Expose
    private String sourceLayer;

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
     * @return The type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return The paint
     */
    public Paint getPaint() {
        return paint;
    }

    /**
     * @param paint The paint
     */
    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    /**
     * @return The source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source The source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return The sourceLayer
     */
    public String getSourceLayer() {
        return sourceLayer;
    }

    /**
     * @param sourceLayer The source-layer
     */
    public void setSourceLayer(String sourceLayer) {
        this.sourceLayer = sourceLayer;
    }

}
