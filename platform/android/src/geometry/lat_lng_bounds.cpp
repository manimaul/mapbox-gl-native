#include "lat_lng_bounds.hpp"

namespace mbgl {
namespace android {

jni::Local<jni::Object<LatLngBounds>> LatLngBounds::New(jni::JNIEnv& env, mbgl::LatLngBounds bounds) {
    static auto& javaClass = jni::Class<LatLngBounds>::Singleton(env);
    static auto constructor = javaClass.GetConstructor<double, double, double, double>(env);
    return javaClass.New(env, constructor, bounds.north(), bounds.east(), bounds.south(), bounds.west());
}

mbgl::LatLngBounds LatLngBounds::getLatLngBounds(jni::JNIEnv& env, const jni::Object<LatLngBounds>& bounds) {
    static auto& javaClass = jni::Class<LatLngBounds>::Singleton(env);
    static auto swLatField = javaClass.GetField<jni::jdouble>(env, "latitudeSouth");
    static auto swLonField = javaClass.GetField<jni::jdouble>(env, "longitudeWest");
    static auto neLatField = javaClass.GetField<jni::jdouble>(env, "latitudeNorth");
    static auto neLonField = javaClass.GetField<jni::jdouble>(env, "longitudeEast");

    mbgl::LatLng sw = { bounds.Get(env, swLatField), bounds.Get(env, swLonField) };
    mbgl::LatLng ne = { bounds.Get(env, neLatField), bounds.Get(env, neLonField) };

    return mbgl::LatLngBounds::hull(sw, ne);
}

void LatLngBounds::setLatitudeNorth(jni::JNIEnv& env, jni::Object<LatLngBounds>& bounds, double value) {
    static auto& javaClass = jni::Class<LatLngBounds>::Singleton(env);
    static auto field = javaClass.GetField<double>(env, "latitudeNorth");
    bounds.Set(env, field, value);
}

void LatLngBounds::setLatitudeSouth(jni::JNIEnv& env, jni::Object<LatLngBounds>& bounds, double value) {
    static auto& javaClass = jni::Class<LatLngBounds>::Singleton(env);
    static auto field = javaClass.GetField<double>(env, "latitudeSouth");
    bounds.Set(env, field, value);
}

void LatLngBounds::setLongitudeEast(jni::JNIEnv& env, jni::Object<LatLngBounds>& bounds, double value) {
    static auto& javaClass = jni::Class<LatLngBounds>::Singleton(env);
    static auto field = javaClass.GetField<double>(env, "longitudeEast");
    bounds.Set(env, field, value);
}

void LatLngBounds::setLongitudeWest(jni::JNIEnv& env, jni::Object<LatLngBounds>& bounds, double value) {
    static auto& javaClass = jni::Class<LatLngBounds>::Singleton(env);
    static auto field = javaClass.GetField<double>(env, "longitudeWest");
    bounds.Set(env, field, value);
}

void LatLngBounds::registerNative(jni::JNIEnv& env) {
    jni::Class<LatLngBounds>::Singleton(env);
}

} // namespace android
} // namespace mbgl