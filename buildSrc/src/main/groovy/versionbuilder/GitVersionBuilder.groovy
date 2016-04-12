package versionbuilder;

/*package*/ class GitVersionBuilder extends StaticVersionBuilder {
    static final int GIT_COMMIT_COUNT_NORMALIZE = 2320

    static boolean isGitEnvironment() {
        try {
            return getGitHistoryLength() > 0
        } catch (Exception e) {
            return false
        }
    }

    GitVersionBuilder(int major, int minor, int buildCountOffset) {
        super(major, minor, buildCountOffset, getGitHistoryLength())
    }

    private static int getGitHistoryLength() {
        int commits = Integer.parseInt('git rev-list --count HEAD --all'.execute().text.trim());
        int tags = 'git tag'.execute().text.readLines().size()
        return commits + tags - GIT_COMMIT_COUNT_NORMALIZE
    }
}
