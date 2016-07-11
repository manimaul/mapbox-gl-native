#pragma once

#include <string>

namespace mbgl {
namespace util {

enum class ThreadPriority : bool {
    Regular,
    Low,
};

struct ThreadContext {
public:
    ThreadContext(const std::string& name, ThreadPriority priority = ThreadPriority::Regular);

    std::string name;
    ThreadPriority priority;
};

} // namespace util
} // namespace mbgl
