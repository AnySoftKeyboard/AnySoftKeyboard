package versionbuilder

import org.gradle.api.plugins.ExtensionContainer

public abstract class VersionBuilder {

    public static VersionBuilder getVersionBuilder(int major, int minor, ExtensionContainer exts) {
        if (ShippableVersionBuilder.isShippableEnvironment()) {
            println("Using ShippableVersionBuilder for versioning.")
            return new ShippableVersionBuilder(major, minor, exts)
        } else if (CircleCIVersionBuilder.isCircleCiEnvironment()) {
            println("Using CircleCIVersionBuilder for versioning.")
            return new CircleCIVersionBuilder(major, minor, exts)
        } else if (GitVersionBuilder.isGitEnvironment()) {
            println("Using GitVersionBuilder for versioning.")
            return new GitVersionBuilder(major, minor, exts)
        } else {
            println("Using fallback StaticVersionBuilder for versioning.")
            return new StaticVersionBuilder(major, minor, exts)
        }
    }

    protected final int major
    protected final int minor
    protected final int minorBuildOffset

    protected VersionBuilder(int major, int minor, ExtensionContainer exts) {
        this.minor = minor
        this.major = major
        this.minorBuildOffset = getValueFromExts(exts, "versionBuildMinorOffset", 0)
    }

    protected static int getValueFromExts(ExtensionContainer ext, String key, int defaultValue) {
        Object value = ext.findByName(key)
        return value == null? defaultValue : Integer.parseInt(value.toString())
    }

    public final int getVersionCode() {
        return getBuildCount();
    }

    protected abstract int getBuildCount()

    private int getBuildVersionNumber() {
        return getBuildCount() - minorBuildOffset
    }

    public final String getVersionName() {
        return String.format("%d.%d.%d", major, minor, getBuildVersionNumber())
    }
}