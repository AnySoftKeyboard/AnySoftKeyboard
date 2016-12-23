package versionbuilder

import org.gradle.api.plugins.ExtensionContainer;

/*package*/ class ShippableVersionBuilder extends VersionBuilder {
    static boolean isShippableEnvironment() {
        return System.getenv().containsKey("BUILD_NUMBER") && System.getenv().get("BUILD_NUMBER") instanceof String && System.getenv().get("BUILD_NUMBER").toString().length() > 0
    }

    private final int offset;

    ShippableVersionBuilder(int major, int minor, ExtensionContainer exts) {
        super(major, minor, exts)
        offset = getValueFromExts(exts, "versionNumberBuilderShippableOffset", 0)
    }

    @Override
    protected int getBuildCount() {
        return Integer.parseInt(System.getenv().get("BUILD_NUMBER").toString()) + offset
    }
}
