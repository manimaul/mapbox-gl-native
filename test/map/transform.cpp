#include "../fixtures/util.hpp"
#include "../fixtures/mock_view.hpp"

#include <mbgl/map/transform.hpp>
#include <mbgl/util/geo.hpp>

using namespace mbgl;

TEST(Transform, InvalidScale) {
    MockView view;
    Transform transform(view, ConstrainMode::HeightOnly);

    ASSERT_DOUBLE_EQ(0, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(0, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(1, transform.getScale());

    transform.setScale(2 << 0);

    ASSERT_DOUBLE_EQ(0, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(0, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(2, transform.getScale());

    const double invalid = std::nan("");
    transform.setScale(invalid);

    ASSERT_DOUBLE_EQ(0, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(0, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(2, transform.getScale());

    transform.scaleBy(invalid);

    ASSERT_DOUBLE_EQ(0, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(0, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(2, transform.getScale());

    transform.setZoom(invalid);

    ASSERT_DOUBLE_EQ(0, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(0, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(2, transform.getScale());

    transform.setLatLngZoom({ 0, 0 }, invalid);

    ASSERT_DOUBLE_EQ(0, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(0, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(2, transform.getScale());
}

TEST(Transform, InvalidLatLng) {
    MockView view;
    Transform transform(view, ConstrainMode::HeightOnly);

    ASSERT_DOUBLE_EQ(0, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(0, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(1, transform.getScale());

    transform.setScale(2 << 0);
    transform.setLatLng({ 8, 10 });

    ASSERT_DOUBLE_EQ(8, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(10, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(2, transform.getScale());

    transform.setLatLngZoom({ 10, 8 }, 2);

    ASSERT_DOUBLE_EQ(10, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(8, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(4, transform.getScale());

    const double invalid = std::nan("");
    transform.setLatLngZoom({ invalid, 8 }, 2);

    ASSERT_DOUBLE_EQ(10, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(8, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(4, transform.getScale());

    transform.setLatLngZoom({ 10, invalid }, 2);

    ASSERT_DOUBLE_EQ(10, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(8, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(4, transform.getScale());
}


TEST(Transform, InvalidBearing) {
    MockView view;
    Transform transform(view, ConstrainMode::HeightOnly);

    ASSERT_DOUBLE_EQ(0, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(0, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(1, transform.getScale());

    transform.setScale(2 << 0);
    transform.setAngle(2);

    ASSERT_DOUBLE_EQ(0, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(0, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(2, transform.getScale());
    ASSERT_DOUBLE_EQ(2, transform.getAngle());

    const double invalid = std::nan("");
    transform.setAngle(invalid);

    ASSERT_DOUBLE_EQ(0, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(0, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(2, transform.getScale());
    ASSERT_DOUBLE_EQ(2, transform.getAngle());
}

TEST(Transform, PerspectiveProjection) {
    MockView view;
    LatLng loc;

    Transform transform(view, ConstrainMode::HeightOnly);
    transform.resize({{ 1000, 1000 }});
    transform.setScale(2 << 9);
    transform.setPitch(0.9);
    transform.setLatLng(LatLng(38, -77));

    // expected values are from mapbox-gl-js

    loc = transform.getLatLng();
    ASSERT_NEAR(-77, loc.longitude, 0.0001);
    ASSERT_NEAR(38, loc.latitude, 0.0001);

    loc = transform.getState().screenCoordinateToLatLng({ 0, 1000 });
    ASSERT_NEAR(-77.59198961199148, loc.longitude, 0.0002);
    ASSERT_NEAR(38.74661326302018, loc.latitude, 0.0001);

    loc = transform.getState().screenCoordinateToLatLng({ 1000, 0 });
    ASSERT_NEAR(-76.75823239205641, loc.longitude, 0.0001);
    ASSERT_NEAR(37.692872969426375, loc.latitude, 0.0001);

    ScreenCoordinate point = transform.getState().latLngToScreenCoordinate({38.74661326302018, -77.59198961199148});
    ASSERT_NEAR(point.x, 0, 0.01);
    ASSERT_NEAR(point.y, 1000, 0.01);

    point = transform.getState().latLngToScreenCoordinate({37.692872969426375, -76.75823239205641});
    ASSERT_NEAR(point.x, 1000, 0.02);
    ASSERT_NEAR(point.y, 0, 0.02);
}

TEST(Transform, UnwrappedLatLng) {
    MockView view;
    Transform transform(view, ConstrainMode::HeightOnly);
    transform.resize({{ 1000, 1000 }});
    transform.setScale(2 << 9);
    transform.setPitch(0.9);
    transform.setLatLng(LatLng(38, -77));

    const TransformState& state = transform.getState();

    LatLng fromGetLatLng = state.getLatLng();
    ASSERT_DOUBLE_EQ(fromGetLatLng.latitude, 38);
    ASSERT_DOUBLE_EQ(fromGetLatLng.longitude, -77);

    LatLng fromScreenCoordinate = state.screenCoordinateToLatLng({ 500, 500 });
    ASSERT_NEAR(fromScreenCoordinate.latitude,   37.999999999999829, 0.0001); // 1.71E-13
    ASSERT_NEAR(fromScreenCoordinate.longitude, -76.999999999999773, 0.0001); // 2.27E-13

    LatLng wrappedForwards = state.screenCoordinateToLatLng(state.latLngToScreenCoordinate({ 38, 283 }));
    ASSERT_NEAR(wrappedForwards.latitude, 37.999999999999716, 0.0001); // 2.84E-13
    ASSERT_NEAR(wrappedForwards.longitude, 282.99999999988751, 0.0001); // 1.1249E-11
    wrappedForwards.wrap();
    ASSERT_NEAR(wrappedForwards.longitude, -77.000000000112493, 0.001); // 1.1249E-11

    LatLng wrappedBackwards = state.screenCoordinateToLatLng(state.latLngToScreenCoordinate({ 38, -437 }));
    ASSERT_NEAR(wrappedBackwards.latitude, wrappedForwards.latitude, 0.001);
    ASSERT_NEAR(wrappedBackwards.longitude, -436.99999999988728, 0.001); // 1.1272E-11
    wrappedBackwards.wrap();
    ASSERT_NEAR(wrappedBackwards.longitude, -76.99999999988728, 0.001); // 1.1272E-11
}

TEST(Transform, ConstrainHeightOnly) {
    MockView view;
    LatLng loc;

    Transform transform(view, ConstrainMode::HeightOnly);
    transform.resize({{ 1000, 1000 }});
    transform.setScale(std::pow(2, util::MAX_ZOOM));

    transform.setLatLng(LatLngBounds::world().southwest());
    loc = transform.getLatLng();
    ASSERT_NEAR(-util::LATITUDE_MAX, loc.latitude, 0.001);
    ASSERT_NEAR(util::LONGITUDE_MAX, std::abs(loc.longitude), 0.001);

    transform.setLatLng(LatLngBounds::world().northeast());
    loc = transform.getLatLng();
    ASSERT_NEAR(util::LATITUDE_MAX, loc.latitude, 0.001);
    ASSERT_NEAR(util::LONGITUDE_MAX, std::abs(loc.longitude), 0.001);
}

TEST(Transform, ConstrainWidthAndHeight) {
    MockView view;
    LatLng loc;

    Transform transform(view, ConstrainMode::WidthAndHeight);
    transform.resize({{ 1000, 1000 }});
    transform.setScale(std::pow(2, util::MAX_ZOOM));

    transform.setLatLng(LatLngBounds::world().southwest());
    loc = transform.getLatLng();
    ASSERT_NEAR(-util::LATITUDE_MAX, loc.latitude, 0.001);
    ASSERT_NEAR(-util::LONGITUDE_MAX, loc.longitude, 0.001);

    transform.setLatLng(LatLngBounds::world().northeast());
    loc = transform.getLatLng();
    ASSERT_NEAR(util::LATITUDE_MAX, loc.latitude, 0.001);
    ASSERT_NEAR(util::LONGITUDE_MAX, std::abs(loc.longitude), 0.001);
}

TEST(Transform, Anchor) {
    MockView view;
    Transform transform(view, ConstrainMode::HeightOnly);
    transform.resize({{ 1000, 1000 }});

    const LatLng latLng { 10, -100 };
    transform.setLatLngZoom(latLng, 10);

    ASSERT_DOUBLE_EQ(latLng.latitude, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(latLng.longitude, transform.getLatLng().longitude);
    ASSERT_DOUBLE_EQ(10, transform.getZoom());
    ASSERT_DOUBLE_EQ(0, transform.getAngle());

    transform.setAngle(M_PI_4);
    ASSERT_NEAR(M_PI_4, transform.getAngle(), 0.000001);
    ASSERT_DOUBLE_EQ(latLng.latitude, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(latLng.longitude, transform.getLatLng().longitude);

    const ScreenCoordinate anchorPoint = { 150, 150 };
    const LatLng anchorLatLng = transform.getState().screenCoordinateToLatLng(anchorPoint);
    transform.setAngle(-45 * util::DEG2RAD, anchorPoint);
    ASSERT_NEAR(-45 / util::RAD2DEG, transform.getAngle(), 0.000001);
    ASSERT_NE(latLng.latitude, transform.getLatLng().latitude);
    ASSERT_NE(latLng.longitude, transform.getLatLng().longitude);
    ASSERT_NEAR(anchorLatLng.latitude, transform.getLatLng().latitude, 1);
    ASSERT_NEAR(anchorLatLng.longitude, transform.getLatLng().longitude, 1);
}

TEST(Transform, Padding) {
    MockView view;
    Transform transform(view, ConstrainMode::HeightOnly);
    transform.resize({{ 1000, 1000 }});

    ASSERT_DOUBLE_EQ(0, transform.getLatLng().latitude);
    ASSERT_DOUBLE_EQ(0, transform.getLatLng().longitude);

    transform.setLatLngZoom({ 10, -100 }, 10);

    const LatLng trueCenter = transform.getLatLng();
    ASSERT_DOUBLE_EQ(10, trueCenter.latitude);
    ASSERT_DOUBLE_EQ(-100, trueCenter.longitude);
    ASSERT_DOUBLE_EQ(10, transform.getZoom());
    
    const LatLng manualShiftedCenter = transform.getState().screenCoordinateToLatLng({
        1000.0 / 2.0,
        1000.0 / 4.0,
    });
    
    EdgeInsets padding;

    padding.top = 0;
    ASSERT_FALSE(bool(padding));

    padding.top = NAN;
    ASSERT_FALSE(bool(padding));

    padding.top = 1000.0 / 2.0;
    ASSERT_TRUE(bool(padding));
    
    const LatLng shiftedCenter = transform.getLatLng(padding);
    ASSERT_NE(trueCenter.latitude, shiftedCenter.latitude);
    ASSERT_DOUBLE_EQ(trueCenter.longitude, shiftedCenter.longitude);
    ASSERT_DOUBLE_EQ(manualShiftedCenter.latitude, shiftedCenter.latitude);
    ASSERT_DOUBLE_EQ(manualShiftedCenter.longitude, shiftedCenter.longitude);
}

TEST(Transform, MoveBy) {
    MockView view;
    Transform transform(view, ConstrainMode::HeightOnly);
    transform.resize({{ 1000, 1000 }});
    transform.setLatLngZoom({ 0, 0 }, 10);

    LatLng trueCenter = transform.getLatLng();
    ASSERT_DOUBLE_EQ(0, trueCenter.latitude);
    ASSERT_DOUBLE_EQ(0, trueCenter.longitude);
    ASSERT_DOUBLE_EQ(10, transform.getZoom());

    for (uint8_t x = 0; x < 20; ++x) {
        bool odd = x % 2;
        bool forward = x % 10;

        LatLng coordinate = transform.screenCoordinateToLatLng({ odd ? 400. : 600., forward ? 400. : 600 });
        transform.moveBy({ odd ? 100. : -100., forward ? 100. : -100 });

        trueCenter = transform.getLatLng();
        ASSERT_NEAR(coordinate.latitude, trueCenter.latitude, 0.0001);
        ASSERT_NEAR(coordinate.longitude, trueCenter.longitude, 0.0001);
    }

    // We have ~1.1 precision loss for each coordinate for 20 rounds of moveBy.
    ASSERT_NEAR(0, trueCenter.latitude, 1.1);
    ASSERT_NEAR(0, trueCenter.longitude, 1.1);
}

TEST(Transform, Antimeridian) {
    MockView view;
    Transform transform(view, ConstrainMode::HeightOnly);
    transform.resize({{ 1000, 1000 }});
    transform.setLatLngZoom({ 0, 0 }, 1);

    const LatLng coordinateSanFrancisco { 37.7833, -122.4167 };
    ScreenCoordinate pixelSF = transform.latLngToScreenCoordinate(coordinateSanFrancisco);
    ASSERT_DOUBLE_EQ(151.79409149185352, pixelSF.x);
    ASSERT_DOUBLE_EQ(383.76774094913071, pixelSF.y);

    transform.setLatLng({ 0, -181 });
    ScreenCoordinate pixelSFBackwards = transform.latLngToScreenCoordinate(coordinateSanFrancisco);
    ASSERT_DOUBLE_EQ(666.63617954008976, pixelSFBackwards.x);
    ASSERT_DOUBLE_EQ(pixelSF.y, pixelSFBackwards.y);

    transform.setLatLng({ 0, 179 });
    ScreenCoordinate pixelSFForwards = transform.latLngToScreenCoordinate(coordinateSanFrancisco);
    ASSERT_DOUBLE_EQ(pixelSFBackwards.x, pixelSFForwards.x);
    ASSERT_DOUBLE_EQ(pixelSFBackwards.y, pixelSFForwards.y);

    const LatLng coordinateWaikiri{ -16.9310, 179.9787 };
    transform.setLatLngZoom(coordinateWaikiri, 10);
    ScreenCoordinate pixelWaikiri = transform.latLngToScreenCoordinate(coordinateWaikiri);
    ASSERT_DOUBLE_EQ(500.00000000007759, pixelWaikiri.x);
    ASSERT_DOUBLE_EQ(500, pixelWaikiri.y);

    transform.setLatLng({ coordinateWaikiri.latitude, 180.0213 });
    ScreenCoordinate pixelWaikiriForwards = transform.latLngToScreenCoordinate(coordinateWaikiri);
    ASSERT_DOUBLE_EQ(437.95953728819512, pixelWaikiriForwards.x);
    ASSERT_DOUBLE_EQ(pixelWaikiri.y, pixelWaikiriForwards.y);

    transform.setLatLng({ coordinateWaikiri.latitude, -179.9787 });
    ScreenCoordinate pixelWaikiriBackwards = transform.latLngToScreenCoordinate(coordinateWaikiri);
    ASSERT_DOUBLE_EQ(pixelWaikiriForwards.x, pixelWaikiriBackwards.x);
    ASSERT_DOUBLE_EQ(pixelWaikiriForwards.y, pixelWaikiriBackwards.y);
}
