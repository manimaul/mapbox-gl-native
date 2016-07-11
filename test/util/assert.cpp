#include <mbgl/test/util.hpp>

#include <mbgl/util/assert.hpp>

using namespace mbgl;

TEST(Assert, Always) {
    EXPECT_DEATH_IF_SUPPORTED(assert_always(true == false), "failed assertion `true == false'");
}
