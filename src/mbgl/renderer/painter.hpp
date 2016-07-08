#pragma once

#include <mbgl/map/transform_state.hpp>

#include <mbgl/tile/tile_id.hpp>

#include <mbgl/renderer/frame_history.hpp>
#include <mbgl/renderer/render_item.hpp>
#include <mbgl/renderer/bucket.hpp>

#include <mbgl/geometry/vao.hpp>
#include <mbgl/geometry/static_vertex_buffer.hpp>

#include <mbgl/gl/gl_config.hpp>
#include <mbgl/gl/gl.hpp>

#include <mbgl/style/style.hpp>

#include <mbgl/util/noncopyable.hpp>
#include <mbgl/util/chrono.hpp>
#include <mbgl/util/constants.hpp>

#include <array>
#include <vector>
#include <set>
#include <map>

namespace mbgl {

class RenderTile;
class SpriteAtlas;
class GlyphAtlas;
class LineAtlas;
struct FrameData;
class Tile;

class DebugBucket;
class FillBucket;
class LineBucket;
class CircleBucket;
class SymbolBucket;
class RasterBucket;

class SDFShader;
class PlainShader;
class OutlineShader;
class OutlinePatternShader;
class LineShader;
class LinejoinShader;
class LineSDFShader;
class LinepatternShader;
class CircleShader;
class PatternShader;
class IconShader;
class RasterShader;
class CollisionBoxShader;

struct ClipID;

namespace util {
class ObjectStore;
} // namespace util

namespace style {
class Style;
class Source;
class FillLayer;
class LineLayer;
class CircleLayer;
class SymbolLayer;
class RasterLayer;
class BackgroundLayer;
} // namespace style

struct FrameData {
    std::array<uint16_t, 2> framebufferSize;
    TimePoint timePoint;
    float pixelRatio;
    MapMode mapMode;
    GLContextMode contextMode;
    MapDebugOptions debugOptions;
};

class Painter : private util::noncopyable {
public:
    Painter(const TransformState&, gl::ObjectStore&);
    ~Painter();

    void render(const style::Style&,
                const FrameData&,
                SpriteAtlas& annotationSpriteAtlas);

    // Renders debug information for a tile.
    void renderTileDebug(const RenderTile&);

    // Renders the red debug frame around a tile, visualizing its perimeter.
    void renderDebugFrame(const mat4 &matrix);

    void renderClipMasks();

    void renderDebugText(Tile&, const mat4&);
    void renderFill(FillBucket&, const style::FillLayer&, const UnwrappedTileID&, const mat4&);
    void renderLine(LineBucket&, const style::LineLayer&, const UnwrappedTileID&, const mat4&);
    void renderCircle(CircleBucket&, const style::CircleLayer&, const UnwrappedTileID&, const mat4&);
    void renderSymbol(SymbolBucket&, const style::SymbolLayer&, const UnwrappedTileID&, const mat4&);
    void renderRaster(RasterBucket&, const style::RasterLayer&, const UnwrappedTileID&, const mat4&);
    void renderBackground(const style::BackgroundLayer&);

    float saturationFactor(float saturation);
    float contrastFactor(float contrast);
    std::array<float, 3> spinWeights(float spin_value);

    void drawClippingMasks(const std::map<UnwrappedTileID, ClipID>&);

    bool needsAnimation() const;

private:
    mat4 translatedMatrix(const mat4& matrix,
                          const std::array<float, 2>& translation,
                          const UnwrappedTileID& id,
                          style::TranslateAnchorType anchor);

    std::vector<RenderItem> determineRenderOrder(const style::Style&);

    template <class Iterator>
    void renderPass(RenderPass,
                    Iterator it, Iterator end,
                    GLsizei i, int8_t increment);

    void setClipping(const ClipID&);

    void renderSDF(SymbolBucket &bucket,
                   const UnwrappedTileID &tileID,
                   const mat4 &matrixSymbol,
                   float scaleDivisor,
                   std::array<float, 2> texsize,
                   SDFShader& sdfShader,
                   void (SymbolBucket::*drawSDF)(SDFShader&, gl::ObjectStore&, bool),

                   // Layout
                   style::AlignmentType rotationAlignment,
                   style::AlignmentType pitchAlignment,
                   float layoutSize,

                   // Paint
                   float opacity,
                   Color color,
                   Color haloColor,
                   float haloWidth,
                   float haloBlur,
                   std::array<float, 2> translate,
                   style::TranslateAnchorType translateAnchor,
                   float paintSize);

    void setDepthSublayer(int n);

    bool isOverdraw() const { return frame.debugOptions & MapDebugOptions::Overdraw; }

    mat4 projMatrix;
    mat4 nativeMatrix;

    std::array<float, 2> pixelsToGLUnits;

    // used to composite images and flips the geometry upside down
    const mat4 flipMatrix = []{
        mat4 flip;
        matrix::ortho(flip, 0, util::EXTENT, -util::EXTENT, 0, 0, 1);
        matrix::translate(flip, flip, 0, -util::EXTENT, 0);
        return flip;
    }();

    const mat4 identityMatrix = []{
        mat4 identity;
        matrix::identity(identity);
        return identity;
    }();

    const TransformState& state;
    gl::ObjectStore& store;

    FrameData frame;

    int indent = 0;

    gl::Config config;

    RenderPass pass = RenderPass::Opaque;

    int numSublayers = 3;
    GLsizei currentLayer;
    float depthRangeSize;
    const float depthEpsilon = 1.0f / (1 << 16);

    SpriteAtlas* spriteAtlas = nullptr;
    GlyphAtlas* glyphAtlas = nullptr;
    LineAtlas* lineAtlas = nullptr;

    FrameHistory frameHistory;

    struct {
        std::unique_ptr<PlainShader> plain;
        std::unique_ptr<OutlineShader> outline;
        std::unique_ptr<OutlinePatternShader> outlinePattern;
        std::unique_ptr<LineShader> line;
        std::unique_ptr<LineSDFShader> linesdf;
        std::unique_ptr<LinepatternShader> linepattern;
        std::unique_ptr<PatternShader> pattern;
        std::unique_ptr<IconShader> icon;
        std::unique_ptr<RasterShader> raster;
        std::unique_ptr<SDFShader> sdfGlyph;
        std::unique_ptr<SDFShader> sdfIcon;
        std::unique_ptr<CollisionBoxShader> collisionBox;
        std::unique_ptr<CircleShader> circle;
    } shader;

    struct {
        std::unique_ptr<PlainShader> plain;
        std::unique_ptr<OutlineShader> outline;
        std::unique_ptr<OutlinePatternShader> outlinePattern;
        std::unique_ptr<LineShader> line;
        std::unique_ptr<LineSDFShader> linesdf;
        std::unique_ptr<LinepatternShader> linepattern;
        std::unique_ptr<PatternShader> pattern;
        std::unique_ptr<IconShader> icon;
        std::unique_ptr<RasterShader> raster;
        std::unique_ptr<SDFShader> sdfGlyph;
        std::unique_ptr<SDFShader> sdfIcon;
        std::unique_ptr<CircleShader> circle;
    } overdrawShader;

    // Set up the stencil quad we're using to generate the stencil mask.
    StaticVertexBuffer tileStencilBuffer {
        // top left triangle
        {{ 0, 0 }},
        {{ util::EXTENT, 0 }},
        {{ 0, util::EXTENT }},

        // bottom right triangle
        {{ util::EXTENT, 0 }},
        {{ 0, util::EXTENT }},
        {{ util::EXTENT, util::EXTENT }},
    };

    StaticRasterVertexBuffer rasterBoundsBuffer {
        {{ 0, 0, 0, 0 }},
        {{ util::EXTENT, 0, 32767, 0 }},
        {{ 0, util::EXTENT, 0, 32767 }},
        {{ util::EXTENT, util::EXTENT, 32767, 32767 }},
    };

    VertexArrayObject coveringPlainArray;
    VertexArrayObject coveringRasterArray;
    VertexArrayObject backgroundPatternArray;
    VertexArrayObject backgroundArray;

    VertexArrayObject coveringPlainOverdrawArray;
    VertexArrayObject coveringRasterOverdrawArray;
    VertexArrayObject backgroundPatternOverdrawArray;
    VertexArrayObject backgroundOverdrawArray;

    // Set up the tile boundary lines we're using to draw the tile outlines.
    StaticVertexBuffer tileBorderBuffer {
        {{ 0, 0 }},
        {{ util::EXTENT, 0 }},
        {{ util::EXTENT, util::EXTENT }},
        {{ 0, util::EXTENT }},
        {{ 0, 0 }},
    };

    VertexArrayObject tileBorderArray;
};

} // namespace mbgl
