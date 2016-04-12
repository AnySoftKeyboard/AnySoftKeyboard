package versionbuilder;

/*package*/ class ShippableVersionBuilder extends StaticVersionBuilder {
    static final int BUILDS_COUNT_NORMALIZE = 852
    static boolean isShippableEnvironment() {
        return System.getenv().containsKey("BUILD_NUMBER") && System.getenv().get("BUILD_NUMBER") instanceof String && System.getenv().get("BUILD_NUMBER").toString().length() > 0
    }

    ShippableVersionBuilder(int major, int minor, int buildCountOffset) {
        super(major, minor, buildCountOffset, Integer.parseInt(System.getenv().get("BUILD_NUMBER"))+BUILDS_COUNT_NORMALIZE)
    }
}
