package deployment;

import java.util.ArrayList;
import java.util.Locale;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DeploymentPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    final NamedDomainObjectContainer<DeploymentProcessConfiguration> configs =
        project.container(DeploymentProcessConfiguration.class);
    configs.all(
        config -> {
          config.environmentSteps = new ArrayList<>();
        });
    project.getExtensions().add("deployments", configs);

    project.afterEvaluate(this::createDeployTasks);
    createStatusTasks(project);
  }

  private String propertyOrDefault(Project project, String key, String defaultValue) {
    Object value = project.findProperty(key);
    if (value == null) return defaultValue;
    else return value.toString();
  }

  private void createStatusTasks(Project project) {
    project
        .getTasks()
        .register(
            "updateDeploymentState",
            DeploymentStatusRequestTask.class,
            task -> {
              task.setDescription("Ad-hoc update deployment state request.");

              task.setEnvironmentName(propertyOrDefault(project, "requestStatus.environment", ""));
              task.setDeploymentId(propertyOrDefault(project, "requestStatus.deployment_id", ""));
              task.setDeploymentState(
                  propertyOrDefault(project, "requestStatus.deployment_state", ""));
            });
    project
        .getTasks()
        .register(
            "updateDeploymentSuccess",
            DeploymentSuccessRequestTask.class,
            task -> {
              task.setEnvironmentName(propertyOrDefault(project, "requestStatus.environment", ""));
              task.setSha(propertyOrDefault(project, "requestStatus.sha", ""));
            });
  }

  private void createDeployTasks(Project project) {
    final NamedDomainObjectContainer<DeploymentProcessConfiguration> configs =
        (NamedDomainObjectContainer<DeploymentProcessConfiguration>)
            project.getExtensions().findByName("deployments");
    configs.all(
        config -> {
          for (int stepIndex = 0; stepIndex < config.environmentSteps.size(); stepIndex++) {
            final String stepName = config.environmentSteps.get(stepIndex);
            if (stepName.isEmpty()) continue;
            project
                .getTasks()
                .register(
                    String.format(Locale.ROOT, "deploymentRequest_%s_%s", config.name, stepName),
                    DeploymentRequestProcessStepTask.class,
                    config,
                    stepIndex);
          }
        });
  }
}
