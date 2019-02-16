#include "lat_lng.hpp"

namespace mbgl {
namespace android {

jni::Local<jni::Object<LatLng>> LatLng::New(jni::JNIEnv& env, const mbgl::LatLng& latLng) {
    static auto& javaClass = jni::Class<LatLng>::Singleton(env);
    static auto constructor = javaClass.GetConstructor<double, double>(env);
    return javaClass.New(env, constructor, latLng.latitude(), latLng.longitude());
}

mbgl::Point<double> LatLng::getGeometry(jni::JNIEnv& env, const jni::Object<LatLng>& latLng) {
    static auto& javaClass = jni::Class<LatLng>::Singleton(env);
    static auto latitudeField = javaClass.GetField<jni::jdouble>(env, "latitude");
    static auto longitudeField = javaClass.GetField<jni::jdouble>(env, "longitude");
    return mbgl::Point<double>(latLng.Get(env, longitudeField), latLng.Get(env, latitudeField));
}

mbgl::LatLng LatLng::getLatLng(jni::JNIEnv& env, const jni::Object<LatLng>& latLng) {
    auto point = LatLng::getGeometry(env, latLng);
    return mbgl::LatLng(point.y, point.x);
}

void LatLng::registerNative(jni::JNIEnv& env) {
    jni::Class<LatLng>::Singleton(env);
}

void LatLng::setLatitude(jni::JNIEnv& env, jni::Object<LatLng>& latLng, double latitude) {
    static auto& javaClass = jni::Class<LatLng>::Singleton(env);
    static auto field = javaClass.GetField<double>(env, "latitude");
    latLng.Set(env, field, latitude);
}

void LatLng::setLongitude(jni::JNIEnv& env, jni::Object<LatLng>& latLng, double longitude) {
    static auto& javaClass = jni::Class<LatLng>::Singleton(env);
    static auto field = javaClass.GetField<double>(env, "longitude");
    latLng.Set(env, field, longitude);
}

void LatLng::setAltitude(jni::JNIEnv &env, jni::Object<LatLng> latLng, double altitude) {
    static auto& javaClass = jni::Class<LatLng>::Singleton(env);
    static auto field = javaClass.GetField<double>(env, "latitude");
    latLng.Set(env, field, altitude);
}

} // namespace android
} // namespace mbgl