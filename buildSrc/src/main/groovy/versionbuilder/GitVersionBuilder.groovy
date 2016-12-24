package versionbuilder

import org.gradle.api.plugins.ExtensionContainer;

/*package*/ class GitVersionBuilder extends VersionBuilder {
    static boolean isGitEnvironment() {
        try {
            return getGitHistoryLength() > 0
        } catch (Exception e) {
            return false
        }
    }

    private static int getGitHistoryLength() {
        int commits = Integer.parseInt('git rev-list --count HEAD --all'.execute().text.trim())
        int tags = 'git tag'.execute().text.readLines().size()
        return commits + tags
    }

    private final int offset
    GitVersionBuilder(int major, int minor, ExtensionContainer exts) {
        super(major, minor, exts)
        offset = getValueFromExts(exts, "versionNumberBuilderGitOffset", 0)
    }

    @Override
    protected int getBuildCount() {
        return getGitHistoryLength() + offset
    }
}
