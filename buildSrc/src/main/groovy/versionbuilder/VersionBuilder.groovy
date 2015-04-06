package versionbuilder

class VersionBuilder {
    static final int GIT_COMMIT_COUNT_NORMALIZE = 2320;
    static final int GIT_COMMIT_COUNT_MINOR_NORMALIZE = 140;

    static def buildGitVersionNumber() {
        return Integer.parseInt('git rev-list --count HEAD'.execute().text.trim()) - GIT_COMMIT_COUNT_NORMALIZE;
    }

    static def buildGitVersionName() {
        return String.format("%d.%d.%d", 1, 4, buildGitVersionNumber() - GIT_COMMIT_COUNT_MINOR_NORMALIZE);
    }

}