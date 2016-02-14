package versionbuilder

public class VersionBuilder {
    static final int GIT_COMMIT_COUNT_NORMALIZE = 2320
    static final int GIT_COMMIT_COUNT_MINOR_NORMALIZE = 140+50+38+167

    public static def buildGitVersionNumber() {
        try {
            return Integer.parseInt('git rev-list --count HEAD'.execute().text.trim()) - GIT_COMMIT_COUNT_NORMALIZE
        } catch (Exception e) {
            println("Failed to get version from git data. Error: "+e.message);
            return 1
        }
    }

    public static def buildGitVersionName() {
        int gitVersion = buildGitVersionNumber()
        if (gitVersion < GIT_COMMIT_COUNT_MINOR_NORMALIZE) gitVersion = GIT_COMMIT_COUNT_MINOR_NORMALIZE + 1
        return String.format("%d.%d.%d", 1, 7, gitVersion - GIT_COMMIT_COUNT_MINOR_NORMALIZE)
    }

}