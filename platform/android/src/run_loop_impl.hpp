#pragma once

#include "jni.hpp"

#include <mbgl/util/atomic.hpp>
#include <mbgl/util/chrono.hpp>
#include <mbgl/util/run_loop.hpp>

#include <list>
#include <memory>
#include <mutex>

struct ALooper;

namespace mbgl {
namespace util {

class RunLoop::Impl {
public:
    class Runnable {
    public:
        virtual ~Runnable() = default;

        virtual void runTask() = 0;
        virtual TimePoint dueTime() const = 0;

        std::list<Runnable*>::iterator iter;
    };

    Impl(RunLoop*, RunLoop::Type);
    ~Impl();

    void wake();

    void addRunnable(Runnable*);
    void removeRunnable(Runnable*);
    void initRunnable(Runnable*);

    Milliseconds processRunnables();

private:
    friend RunLoop;

    int fds[2];

    JNIEnv *env = nullptr;
    bool detach = false;

    ALooper* loop = nullptr;
    util::Atomic<bool> running;

    std::recursive_mutex mtx;
    std::list<Runnable*> runnables;
};

} // namespace util
} // namespace mbgl
