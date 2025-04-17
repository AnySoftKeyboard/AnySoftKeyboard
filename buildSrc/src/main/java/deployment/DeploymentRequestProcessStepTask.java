package deployment;

import static deployment.DeploymentStatusRequestTask.createEmptyOutputFile;

import github.DeploymentCreate;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class DeploymentRequestProcessStepTask extends DefaultTask {

  static class DeploymentCommandLineArgs extends RequestCommandLineArgs {
    static final String PROP_KEY_SHA = "Request.sha";
    final String sha;

    DeploymentCommandLineArgs(Map<String, ?> properties) {
      super(properties);
      this.sha = properties.get(PROP_KEY_SHA).toString();
    }
  }

  private final DeploymentProcessConfiguration mConfiguration;
  private final int mStepIndex;

  @Input
  public String getEnvironmentKey() {
    return getEnvironmentName(mConfiguration, mStepIndex);
  }

  @OutputFile
  public File getStatueFile() {
    return new File(
        getProject().getBuildDir(), String.format(Locale.ROOT, "%s_result.log", getName()));
  }

  @Inject
  public DeploymentRequestProcessStepTask(
      DeploymentProcessConfiguration configuration, int stepIndex) {
    mConfiguration = configuration;
    mStepIndex = stepIndex;
    setGroup("Publishing");
    setDescription("Request deployment of " + getEnvironmentName(configuration, stepIndex));
  }

  @TaskAction
  public void deploymentRequestAction() {
    try {
      createEmptyOutputFile(getStatueFile());
      final DeploymentCreate.Response response =
          deploymentRequest(
              new DeploymentCommandLineArgs(getProject().getProperties()),
              mConfiguration,
              mStepIndex);

      Files.write(
          getStatueFile().toPath(),
          Arrays.asList(
              response.id,
              response.environment,
              response.sha,
              response.ref,
              response.task,
              String.join(",", response.payload.environments_to_kill)),
          StandardCharsets.UTF_8,
          StandardOpenOption.TRUNCATE_EXISTING);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static DeploymentCreate.Response deploymentRequest(
      DeploymentCommandLineArgs data, DeploymentProcessConfiguration configuration, int stepIndex)
      throws Exception {

    DeploymentCreate deploymentCreate = new DeploymentCreate(data.apiUsername, data.apiUserToken);
    return requestDeploymentAction(deploymentCreate, data, configuration, stepIndex);
  }

  private static DeploymentCreate.Response requestDeploymentAction(
      DeploymentCreate deploymentCreate,
      DeploymentCommandLineArgs data,
      DeploymentProcessConfiguration environment,
      int stepIndex)
      throws Exception {
    final String environmentToDeploy = getEnvironmentName(environment, stepIndex);
    final String previousEnvironment = getPreviousEnvironmentName(environment, stepIndex);
    final List<String> environmentsToKill =
        environment.environmentSteps.stream()
            .map(name -> getEnvironmentName(environment.name, name))
            .filter(env -> !env.equals(environmentToDeploy))
            .collect(Collectors.toList());

    final DeploymentCreate.Response response =
        deploymentCreate.request(
            new DeploymentCreate.Request(
                data.sha,
                stepIndex == 0 ? "deploy" : "deploy:migration",
                false,
                environmentToDeploy,
                String.format(
                    Locale.ROOT,
                    "Deployment for '%s' from ('%s') request by '%s'.",
                    environmentToDeploy,
                    previousEnvironment,
                    data.apiUsername),
                Collections.singletonList("all-green-requirement"),
                new DeploymentCreate.RequestPayloadField(environmentsToKill, previousEnvironment)));

    System.out.println(
        String.format(
            Locale.ROOT,
            "Deploy request response: id %s, sha %s, environment %s, task %s.",
            response.id,
            response.sha,
            response.environment,
            response.task));

    return response;
  }

  private static String getEnvironmentName(String environmentName, String stepName) {
    return String.format(Locale.ROOT, "%s_%s", environmentName, stepName);
  }

  private static String getEnvironmentName(DeploymentProcessConfiguration environment, int index) {
    return getEnvironmentName(environment.name, environment.environmentSteps.get(index));
  }

  private static String getPreviousEnvironmentName(
      DeploymentProcessConfiguration environment, int index) {
    // searching for the first, non-empty, step.
    for (int previousStep = index - 1; previousStep >= 0; previousStep--) {
      final String previousEnvironmentStepName = environment.environmentSteps.get(previousStep);
      if (!previousEnvironmentStepName.isBlank())
        return getEnvironmentName(environment.name, previousEnvironmentStepName);
    }

    return "NONE";
  }
}
