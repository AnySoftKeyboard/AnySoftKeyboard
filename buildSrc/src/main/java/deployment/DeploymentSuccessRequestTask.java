package deployment;

import github.DeploymentsList;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
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

    @TaskAction
    public void statusAction() {
        final String processName = mEnvironmentName.substring(0, mEnvironmentName.indexOf('_') + 1);
        try {
            final RequestCommandLineArgs data =
                    new RequestCommandLineArgs(getProject().getProperties());
            final DeploymentsList.Response[] responses = listRequest(data, mEnvironmentSha);
            for (DeploymentsList.Response response : responses) {
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
                    DeploymentStatusRequestTask.statusRequest(
                            data, response.environment, response.id, status);
                } else {
                    System.out.println(
                            "Skipping "
                                    + response.environment
                                    + " since it's not in the same process.");
                }
            }
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
