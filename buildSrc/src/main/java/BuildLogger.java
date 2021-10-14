import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import org.gradle.BuildAdapter;
import org.gradle.BuildResult;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.tasks.TaskState;

public class BuildLogger extends BuildAdapter {

    private final File mOutputFile;

    public BuildLogger(File outputFile) {
        mOutputFile = outputFile;
        if (mOutputFile.getParentFile().exists() || mOutputFile.getParentFile().mkdirs()) {
            try {
                try (OutputStreamWriter writer =
                        new OutputStreamWriter(
                                new FileOutputStream(mOutputFile, false), StandardCharsets.UTF_8)) {
                    writer.append("Build log created at ")
                            .append(Instant.now().toString())
                            .append(System.lineSeparator());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalStateException(
                    String.format(
                            Locale.ROOT,
                            "Can not create parent folder '%s' for logging!",
                            mOutputFile.getParentFile().getAbsolutePath()));
        }
    }

    private static String taskToString(TaskState state) {
        if (state.getSkipped()) return "SKIPPED";
        if (state.getNoSource()) return "NO-SOURCE";
        if (state.getUpToDate()) return "UP-TO-DATE";
        if (state.getFailure() != null) return "FAILED " + exceptionToString(state.getFailure());
        if (state.getExecuted()) return "EXECUTED";
        return "unknown";
    }

    private static String exceptionToString(Throwable throwable) {
        StringBuilder builder = new StringBuilder(throwable.getMessage());
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
            builder.append(" ]> ").append(throwable.getMessage());
        }

        return builder.toString();
    }

    @Override
    public void projectsEvaluated(Gradle gradle) {
        appendToFile(
                "Build started at "
                        + Instant.now().toString()
                        + System.lineSeparator()
                        + "Gradle "
                        + gradle.getGradleVersion()
                        + System.lineSeparator());
    }

    @Override
    public void buildFinished(BuildResult buildResult) {
        StringBuilder log = new StringBuilder();
        log.append("Build finished at ")
                .append(Instant.now().toString())
                .append(System.lineSeparator());
        log.append("build action: ").append(buildResult.getAction()).append(System.lineSeparator());
        log.append("build result ")
                .append(
                        buildResult.getFailure() != null
                                ? "FAILED with " + exceptionToString(buildResult.getFailure())
                                : "SUCCESS")
                .append(System.lineSeparator());
        log.append("build tasks:").append(System.lineSeparator());
        final Gradle gradle = buildResult.getGradle();
        if (gradle != null) {
            gradle.getTaskGraph()
                    .getAllTasks()
                    .forEach(
                            t ->
                                    log.append("* ")
                                            .append(t.getPath())
                                            .append(" state: ")
                                            .append(taskToString(t.getState()))
                                            .append(System.lineSeparator()));
        }
        appendToFile(log.toString());
    }

    private void appendToFile(String output) {
        try {
            try (OutputStreamWriter writer =
                    new OutputStreamWriter(
                            new FileOutputStream(mOutputFile, true), StandardCharsets.UTF_8)) {
                writer.append(output);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
