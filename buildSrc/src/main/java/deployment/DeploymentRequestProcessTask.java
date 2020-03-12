package deployment;

import github.Deployment;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public class DeploymentRequestProcessTask extends DefaultTask {

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

    @Inject
    public DeploymentRequestProcessTask(
            DeploymentProcessConfiguration configuration, int stepIndex) {
        mConfiguration = configuration;
        mStepIndex = stepIndex;
        setGroup("Publishing");
        setDescription("Request deployment of " + getEnvironmentName(configuration, stepIndex));
    }

    @TaskAction
    public void deploymentRequestAction() {
        try {
            deploymentRequest(
                    new DeploymentCommandLineArgs(getProject().getProperties()),
                    mConfiguration,
                    mStepIndex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void deploymentRequest(
            DeploymentCommandLineArgs data,
            DeploymentProcessConfiguration configuration,
            int stepIndex)
            throws Exception {

        Deployment deployment = new Deployment(data.apiUsername, data.apiUserToken);
        if (stepIndex == 0) {
            requestNewDeploy(deployment, data, configuration);
        } else {
            throw new UnsupportedOperationException(
                    "step " + stepIndex + " for " + configuration.name + " is not implemented!");
        }
    }

    private static void requestNewDeploy(
            Deployment deployment,
            DeploymentCommandLineArgs data,
            DeploymentProcessConfiguration environment)
            throws Exception {
        final String environmentToDeploy = getEnvironmentName(environment, 0);
        final List<String> environmentsToKill =
                environment.environmentSteps.stream()
                        .map(name -> getEnvironmentName(environment.name, name))
                        .filter(env -> !env.equals(environmentToDeploy))
                        .collect(Collectors.toList());

        final Deployment.Response response =
                deployment.requestDeployment(
                        new Deployment.Request(
                                data.sha,
                                "deploy",
                                false,
                                environmentToDeploy,
                                String.format(
                                        Locale.ROOT,
                                        "Deployment for '%s' request by '%s'.",
                                        environmentToDeploy,
                                        data.apiUsername),
                                Collections.singletonList("master-green-requirement"),
                                new Deployment.RequestPayloadField(environmentsToKill)));

        System.out.println(
                String.format(
                        Locale.ROOT,
                        "Deploy request response: id %s, sha %s, environment %s, task %s.",
                        response.id,
                        response.sha,
                        response.environment,
                        response.task));
    }

    private static String getEnvironmentName(String environmentName, String stepName) {
        return String.format(Locale.ROOT, "%s_%s", environmentName, stepName);
    }

    private static String getEnvironmentName(
            DeploymentProcessConfiguration environment, int index) {
        return getEnvironmentName(environment.name, environment.environmentSteps.get(index));
    }
}
