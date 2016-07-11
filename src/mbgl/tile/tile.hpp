#pragma once

#include <mbgl/tile/tile_id.hpp>
#include <mbgl/util/mat4.hpp>
#include <mbgl/util/ptr.hpp>
#include <mbgl/util/clip_id.hpp>

namespace mbgl {

class TileData;

class Tile {
public:
    Tile(const UnwrappedTileID& id_, TileData& data_) : id(id_), data(data_) {
    }

    Tile(const Tile&) = delete;
    Tile(Tile&&) = default;
    Tile& operator=(const Tile&) = delete;
    Tile& operator=(Tile&&) = default;

    const UnwrappedTileID id;
    TileData& data;
    ClipID clip;
    mat4 matrix;
};

} // namespace mbgl
