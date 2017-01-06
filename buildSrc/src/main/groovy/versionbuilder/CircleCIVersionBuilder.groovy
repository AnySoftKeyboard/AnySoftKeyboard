package versionbuilder

import org.gradle.api.plugins.ExtensionContainer;

/*package*/

class CircleCIVersionBuilder extends VersionBuilder {
    static boolean isCircleCiEnvironment() {
        return System.getenv().containsKey("CIRCLECI") && System.getenv().containsKey("CIRCLE_BUILD_NUM")
    }

    private final int offset;

    CircleCIVersionBuilder(int major, int minor, ExtensionContainer exts) {
        super(major, minor, exts)
        offset = getValueFromExts(exts, "versionNumberBuilderCircleCiOffset", 0)
    }

    @Override
    protected int getBuildCount() {
        return Integer.parseInt(System.getenv().get("CIRCLE_BUILD_NUM").toString()) + offset
    }
}
