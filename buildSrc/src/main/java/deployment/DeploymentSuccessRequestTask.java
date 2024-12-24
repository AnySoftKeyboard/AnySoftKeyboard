package deployment;

import static deployment.DeploymentStatusRequestTask.createEmptyOutputFile;

import github.DeploymentStatus;
import github.DeploymentsList;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class DeploymentSuccessRequestTask extends DefaultTask {

  private String mEnvironmentSha;
  private String mEnvironmentName;

  @Inject
  public DeploymentSuccessRequestTask() {
    setGroup("Publishing");
    setDescription(
        "Request to set status to success for "
            + mEnvironmentName
            + " with sha "
            + mEnvironmentSha);
  }

  @Input
  public String getEnvironmentName() {
    return mEnvironmentName;
  }

  public void setEnvironmentName(String environmentName) {
    this.mEnvironmentName = environmentName;
  }

  @Input
  public String getSha() {
    return mEnvironmentSha;
  }

  public void setSha(String sha) {
    this.mEnvironmentSha = sha;
  }

  @OutputFile
  public File getStatueFile() {
    return new File(
        getProject().getBuildDir(), String.format(Locale.ROOT, "%s_result.log", getName()));
  }

  @TaskAction
  public void statusAction() {
    final String processName = mEnvironmentName.substring(0, mEnvironmentName.indexOf('_') + 1);
    try {
      createEmptyOutputFile(getStatueFile());
      final RequestCommandLineArgs data = new RequestCommandLineArgs(getProject().getProperties());
      final DeploymentsList.Response[] responses = listRequest(data, mEnvironmentSha);
      ArrayList<String> responseContent = new ArrayList<>(responses.length * 3);
      responseContent.add(processName);
      responseContent.add(Integer.toString(responses.length));
      for (DeploymentsList.Response response : responses) {
        responseContent.add(response.environment);
        if (response.environment.startsWith(processName)) {
          final String status =
              response.environment.equals(mEnvironmentName) ? "success" : "inactive";
          System.out.println(
              "Will change environment "
                  + response.environment
                  + " with id "
                  + response.id
                  + " to status "
                  + status);
          final DeploymentStatus.Response statusUpdateResponse =
              DeploymentStatusRequestTask.statusRequest(
                  data, response.environment, response.id, status);
          responseContent.add(statusUpdateResponse.id);
          responseContent.add(statusUpdateResponse.environment);
          responseContent.add(statusUpdateResponse.description);
          responseContent.add(statusUpdateResponse.state);
        } else {
          responseContent.add("skipped");
          System.out.println(
              "Skipping " + response.environment + " since it's not in the same process.");
        }
      }

      Files.write(
          getStatueFile().toPath(),
          responseContent,
          StandardCharsets.UTF_8,
          StandardOpenOption.TRUNCATE_EXISTING);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static DeploymentsList.Response[] listRequest(RequestCommandLineArgs data, String sha)
      throws Exception {

    DeploymentsList list = new DeploymentsList(data.apiUsername, data.apiUserToken);
    final DeploymentsList.Response[] response = list.request(new DeploymentsList.Request(sha));

    System.out.println(
        String.format(
            Locale.ROOT,
            "Deployment-status request response: length %d, environments %s.",
            response.length,
            Arrays.stream(response).map(r -> r.id).collect(Collectors.joining(","))));

    return response;
  }
}
