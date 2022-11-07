package contributors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class GenerateContributorsFileTask extends DefaultTask {
    private File mInputFile;
    private int mMaxContributors;

    static void createEmptyOutputFile(File outputFile) throws IOException {
        File buildDir = outputFile.getParentFile();
        if (!buildDir.isDirectory() && !buildDir.mkdirs()) {
            throw new IOException(
                    "Failed to create build output folder: " + buildDir.getAbsolutePath());
        }

        if (outputFile.isFile() && !outputFile.delete()) {
            throw new IOException(
                    "Failed to delete existing output file : " + outputFile.getAbsolutePath());
        }

        Files.createFile(outputFile.toPath());
    }

    @Inject
    public GenerateContributorsFileTask() {
        setGroup("AnySoftKeyboard");
    }

    @InputFile
    public File getRawContributorsFile() {
        return mInputFile;
    }

    public void setRawContributorsFile(File file) {
        this.mInputFile = file;
    }

    @Input
    public int getMaxContributorsCount() {
        return mMaxContributors;
    }

    public void setMaxContributors(int maxCount) {
        this.mMaxContributors = maxCount;
    }

    @OutputFile
    public File getContributorsMarkDownFile() {
        return new File(
                getProject().getBuildDir(),
                String.format(Locale.ROOT, "%s_contributors.md", getName()));
    }

    @TaskAction
    public void generateAction() {
        try {
            createEmptyOutputFile(getContributorsMarkDownFile());
            Files.write(
                    getContributorsMarkDownFile().toPath(),
                    Files.readAllLines(mInputFile.getAbsoluteFile().toPath()).stream()
                            .map(l -> l.split(","))
                            .map(p -> new RawData(p[0], Integer.parseInt(p[1])))
                            .sorted(
                                    (r1, r2) -> {
                                        if (r1.contributions == r2.contributions)
                                            return r1.login.compareTo(r2.login);
                                        else return r2.contributions - r1.contributions;
                                    })
                            .limit(mMaxContributors)
                            .map(
                                    r ->
                                            String.format(
                                                    Locale.ROOT,
                                                    "1. [%s](%s) (%s)",
                                                    r.getLogin(),
                                                    r.getProfileUrl(),
                                                    r.getContributions()))
                            .collect(Collectors.toList()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class RawData {
        private final String login;
        private final int contributions;

        private RawData(String login, int contributions) {
            this.login = login;
            this.contributions = contributions;
        }

        private boolean isBot() {
            return login.equalsIgnoreCase("anysoftkeyboard-bot");
        }

        public String getLogin() {
            if (isBot()) {
                return String.format(Locale.ROOT, "\uD83E\uDD16 %s", login);
            } else {
                return login;
            }
        }

        public String getProfileUrl() {
            return String.format(Locale.ROOT, "https://github.com/%s", login);
        }

        public String getContributions() {
            if (isBot()) {
                return String.format(Locale.ROOT, "%.1fk", (contributions / 1000f));
            }

            if (contributions > 999) {
                return String.format(Locale.ROOT, "%.1fk", (contributions / 1000f));
            } else if (contributions > 499) {
                return String.format(Locale.ROOT, "%.2fk", (contributions / 1000f));
            } else {
                return Integer.toString(contributions);
            }
        }
    }
}
