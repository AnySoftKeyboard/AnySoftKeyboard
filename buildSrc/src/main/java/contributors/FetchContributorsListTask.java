package contributors;

import github.ContributorsList;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class FetchContributorsListTask extends DefaultTask {
    /** This is used to connect the current state of the repo to the returned contributors. */
    private String mCurrentSha;

    private String mUsername;
    private String mPassword;

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
    public FetchContributorsListTask() {
        setGroup("AnySoftKeyboard");
    }

    @Input
    public String getRepositorySha() {
        return mCurrentSha;
    }

    public void setRepositorySha(String sha) {
        this.mCurrentSha = sha;
    }

    @Input
    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    @Input
    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        this.mPassword = password;
    }

    @OutputFile
    public File getContributorsListFile() {
        return new File(
                getProject().getBuildDir(),
                String.format(Locale.ROOT, "%s_contributors.lst", getName()));
    }

    @TaskAction
    public void fetchAction() {
        try {
            createEmptyOutputFile(getContributorsListFile());
            final List<ContributorsList.Response> response = statusRequest(mUsername, mPassword);
            Files.write(
                    getContributorsListFile().toPath(),
                    response.stream()
                            .map(c -> String.format(Locale.ROOT, "%s,%d", c.login, c.contributions))
                            .collect(Collectors.toList()),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static List<ContributorsList.Response> statusRequest(String username, String password)
            throws Exception {
        ContributorsList lister = new ContributorsList(username, password);
        ContributorsList.Response[] responses = lister.request(new ContributorsList.Request());

        return Arrays.asList(responses);
    }
}
