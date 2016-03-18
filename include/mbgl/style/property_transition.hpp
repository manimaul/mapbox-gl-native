#ifndef MBGL_STYLE_PROPERTY_TRANSITION
#define MBGL_STYLE_PROPERTY_TRANSITION

#include <mbgl/util/chrono.hpp>
#include <mbgl/util/optional.hpp>

namespace mbgl {

class PropertyTransition {
public:
    PropertyTransition(const optional<Duration>& duration_ = {}, const optional<Duration>& delay_ = {})
        : duration(duration_), delay(delay_) {}

    optional<Duration> duration;
    optional<Duration> delay;
};

} // namespace mbgl

#endif // MBGL_STYLE_PROPERTY_TRANSITION
