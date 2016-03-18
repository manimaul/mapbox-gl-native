#ifndef STYLE_CASCADE_PARAMETERS
#define STYLE_CASCADE_PARAMETERS

#include <mbgl/map/mode.hpp>
#include <mbgl/util/chrono.hpp>
#include <mbgl/style/types.hpp>

#include <vector>

namespace mbgl {

class PropertyTransition;

class StyleCascadeParameters {
public:
    std::vector<ClassID> classes;
    TimePoint now;
    PropertyTransition transition;
};

} // namespace mbgl

#endif
