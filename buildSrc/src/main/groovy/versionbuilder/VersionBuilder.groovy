package versionbuilder

public abstract class VersionBuilder {
    private final int major
    private final int minor
    private final int buildCountOffset

    public static VersionBuilder getVersionBuilder(int major, int minor, int buildCountOffset) {
        if (ShippableVersionBuilder.isShippableEnvironment()) {
            println("Using ShippableVersionBuilder for versioning.")
            return new ShippableVersionBuilder(major, minor, buildCountOffset)
        } else if (GitVersionBuilder.isGitEnvironment()) {
            println("Using GitVersionBuilder for versioning.")
            return new GitVersionBuilder(major, minor, buildCountOffset)
        } else {
            println("Using fallback StaticVersionBuilder for versioning.")
            return new StaticVersionBuilder(major, minor, buildCountOffset, buildCountOffset+1)
        }
    }

    protected VersionBuilder(int major, int minor, int buildCountOffset) {
        this.buildCountOffset = buildCountOffset
        this.minor = minor
        this.major = major
    }
    public abstract int buildVersionNumber()

    public final String buildVersionName() {
        int versionCode = buildVersionNumber()
        if (versionCode + buildCountOffset > 0) versionCode += buildCountOffset

        return String.format("%d.%d.%d", major, minor, versionCode)
    }
}