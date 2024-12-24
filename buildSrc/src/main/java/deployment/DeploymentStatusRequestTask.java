package deployment;

import github.DeploymentStatus;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Locale;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class DeploymentStatusRequestTask extends DefaultTask {
  private String mEnvironmentName;
  private String mDeploymentId;
  private String mDeploymentState;

  static void createEmptyOutputFile(File outputFile) throws IOException {
    File buildDir = outputFile.getParentFile();
    if (!buildDir.isDirectory() && !buildDir.mkdirs()) {
      throw new IOException("Failed to create build output folder: " + buildDir.getAbsolutePath());
    }

    if (outputFile.isFile() && !outputFile.delete()) {
      throw new IOException(
          "Failed to delete existing output file : " + outputFile.getAbsolutePath());
    }

    Files.createFile(outputFile.toPath());
  }

  @Inject
  public DeploymentStatusRequestTask() {
    setGroup("Publishing");
    setDescription("Request to change environment deployment status");
  }

  @Input
  public String getEnvironmentName() {
    return mEnvironmentName;
  }

  public void setEnvironmentName(String mEnvironmentName) {
    this.mEnvironmentName = mEnvironmentName;
  }

  @Input
  public String getDeploymentId() {
    return mDeploymentId;
  }

  public void setDeploymentId(String mDeploymentId) {
    this.mDeploymentId = mDeploymentId;
  }

  @Input
  public String getDeploymentState() {
    return mDeploymentState;
  }

  public void setDeploymentState(String mState) {
    this.mDeploymentState = mState;
  }

  @OutputFile
  public File getStatueFile() {
    return new File(
        getProject().getBuildDir(), String.format(Locale.ROOT, "%s_result.log", getName()));
  }

  @TaskAction
  public void statusAction() {
    try {
      createEmptyOutputFile(getStatueFile());
      final DeploymentStatus.Response response =
          statusRequest(
              new RequestCommandLineArgs(getProject().getProperties()),
              mEnvironmentName,
              mDeploymentId,
              mDeploymentState);
      Files.write(
          getStatueFile().toPath(),
          Arrays.asList(response.id, response.description, response.environment, response.state),
          StandardCharsets.UTF_8,
          StandardOpenOption.TRUNCATE_EXISTING);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static DeploymentStatus.Response statusRequest(
      RequestCommandLineArgs data, String environment, String deploymentId, String newStatus)
      throws Exception {

    DeploymentStatus status = new DeploymentStatus(data.apiUsername, data.apiUserToken);
    final DeploymentStatus.Response response =
        status.request(new DeploymentStatus.Request(deploymentId, environment, newStatus));

    System.out.println(
        String.format(
            Locale.ROOT,
            "Deployment-status request response: id %s, state %s, environment %s, description %s.",
            response.id,
            response.state,
            response.environment,
            response.description));

    return response;
  }
}
