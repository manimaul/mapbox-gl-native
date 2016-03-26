#include <mbgl/test/util.hpp>
#include <mbgl/test/stub_file_source.hpp>

#include <mbgl/map/map_data.hpp>
#include <mbgl/style/style.hpp>
#include <mbgl/util/io.hpp>

using namespace mbgl;

TEST(Style, UnusedSource) {
    util::RunLoop loop;
    util::ThreadContext context { "Map", util::ThreadType::Map, util::ThreadPriority::Regular };
    util::ThreadContext::Set(&context);

    MapData data { MapMode::Still, GLContextMode::Unique, 1.0 };
    StubFileSource fileSource;
    Style style { data, fileSource };

    auto now = Clock::now();

    style.setJSON(util::read_file("test/fixtures/resources/style-unused-sources.json"), "");
    style.cascade(now);
    style.recalculate(0, now);

    Source *usedSource = style.getSource("usedsource");
    EXPECT_TRUE(usedSource);
    EXPECT_TRUE(usedSource->isLoaded());

    Source *unusedSource = style.getSource("unusedsource");
    EXPECT_TRUE(unusedSource);
    EXPECT_FALSE(unusedSource->isLoaded());
}

TEST(Style, UnusedSourceActiveViaClassUpdate) {
    util::RunLoop loop;
    util::ThreadContext context { "Map", util::ThreadType::Map, util::ThreadPriority::Regular };
    util::ThreadContext::Set(&context);

    MapData data { MapMode::Still, GLContextMode::Unique, 1.0 };
    StubFileSource fileSource;
    Style style { data, fileSource };

    style.setJSON(util::read_file("test/fixtures/resources/style-unused-sources.json"), "");
    EXPECT_TRUE(style.addClass("visible"));
    EXPECT_TRUE(style.hasClass("visible"));

    auto now = Clock::now();

    style.cascade(now);
    style.recalculate(0, now);

    Source *unusedSource = style.getSource("unusedsource");
    EXPECT_TRUE(unusedSource);
    EXPECT_TRUE(unusedSource->isLoaded());

    // Style classes should be cleared upon new style load.
    style.setJSON(util::read_file("test/fixtures/resources/style-unused-sources.json"), "");
    EXPECT_FALSE(style.hasClass("visible"));

    now = Clock::now();

    style.cascade(now);
    style.recalculate(0, now);

    unusedSource = style.getSource("unusedsource");
    EXPECT_TRUE(unusedSource);
    EXPECT_FALSE(unusedSource->isLoaded());
}
