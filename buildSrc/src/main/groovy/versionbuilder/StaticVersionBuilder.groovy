package versionbuilder;

/*package*/ class StaticVersionBuilder extends VersionBuilder {
    private final int mVersionNumber;

    StaticVersionBuilder(int major, int minor, int buildCountOffset, int versionNumber) {
        super(major, minor, buildCountOffset)
        mVersionNumber = versionNumber;
    }

    public final int buildVersionNumber() { return mVersionNumber; }
}
