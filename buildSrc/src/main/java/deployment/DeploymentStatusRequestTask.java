package deployment;

import github.DeploymentStatus;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public abstract class DeploymentStatusRequestTask extends DefaultTask {
    static class StatusCommandLineArgs extends RequestCommandLineArgs {

        StatusCommandLineArgs(Map<String, ?> properties) {
            super(properties);
        }
    }

    private String mEnvironmentName;
    private String mDeploymentId;
    private String mDeploymentState;

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

    @TaskAction
    public void statusAction() {
        try {
            statusRequest(
                    new StatusCommandLineArgs(getProject().getProperties()),
                    mEnvironmentName,
                    mDeploymentId,
                    mDeploymentState);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void statusRequest(
            StatusCommandLineArgs data, String environment, String deploymentId, String newStatus)
            throws Exception {

        DeploymentStatus status = new DeploymentStatus(data.apiUsername, data.apiUserToken);
        final DeploymentStatus.Response response =
                status.requestDeploymentStatus(
                        deploymentId, new DeploymentStatus.Request(environment, newStatus));

        System.out.println(
                String.format(
                        Locale.ROOT,
                        "Deployment-status request response: id %s, state %s, environment %s, description %s.",
                        response.id,
                        response.state,
                        response.environment,
                        response.description));
    }
}
