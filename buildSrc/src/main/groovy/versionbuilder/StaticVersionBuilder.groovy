package versionbuilder

import org.gradle.api.plugins.ExtensionContainer;

/*package*/ class StaticVersionBuilder extends VersionBuilder {

    StaticVersionBuilder(int major, int minor, ExtensionContainer exts) {
        super(major, minor, exts)
    }

    @Override
    protected int getBuildCount() {
        return minorBuildOffset+1;
    }
}
