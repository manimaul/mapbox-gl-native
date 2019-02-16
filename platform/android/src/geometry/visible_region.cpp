#include "visible_region.hpp"

namespace mbgl {
namespace android {

//jni::Local<jni::Object<LatLng>>

jni::Local<jni::Object<LatLng>> VisibleRegion::getFarLeft(jni::JNIEnv& env, jni::Object<VisibleRegion>& visibleRegion) {
    static auto& javaClass = jni::Class<VisibleRegion>::Singleton(env);
    static auto field = javaClass.GetField<jni::Object<LatLng>>(env, "farLeft");
    return visibleRegion.Get(env, field);
}

jni::Local<jni::Object<LatLng>> VisibleRegion::getFarRight(jni::JNIEnv& env, jni::Object<VisibleRegion>& visibleRegion) {
    static auto& javaClass = jni::Class<VisibleRegion>::Singleton(env);
    static auto field = javaClass.GetField<jni::Object<LatLng>>(env, "farRight");
    return visibleRegion.Get(env, field);
}

jni::Local<jni::Object<LatLng>> VisibleRegion::getNearLeft(jni::JNIEnv& env, jni::Object<VisibleRegion>& visibleRegion) {
    static auto& javaClass = jni::Class<VisibleRegion>::Singleton(env);
    static auto field = javaClass.GetField<jni::Object<LatLng>>(env, "nearLeft");
    return visibleRegion.Get(env, field);
}

jni::Local<jni::Object<LatLng>> VisibleRegion::getNearRight(jni::JNIEnv& env, jni::Object<VisibleRegion>& visibleRegion) {
    static auto& javaClass = jni::Class<VisibleRegion>::Singleton(env);
    static auto field = javaClass.GetField<jni::Object<LatLng>>(env, "nearRight");
    return visibleRegion.Get(env, field);
}

jni::Local<jni::Object<LatLngBounds>> VisibleRegion::getBounds(jni::JNIEnv& env, jni::Object<VisibleRegion>& visibleRegion) {
    static auto& javaClass = jni::Class<VisibleRegion>::Singleton(env);
    static auto field = javaClass.GetField<jni::Object<LatLngBounds>>(env, "latLngBounds");
    return visibleRegion.Get(env, field);
}

void VisibleRegion::registerNative(jni::JNIEnv& env) {
    jni::Class<VisibleRegion>::Singleton(env);
}

} // namespace android
} // namespace mbgl
