package deployment;

class DeploymentCommandLineArgs {
    final String sha;
    final String apiUsername;
    final String apiUserToken;

    DeploymentCommandLineArgs(String sha, String apiUsername, String apiUserToken) {
        this.sha = sha;
        this.apiUsername = apiUsername;
        this.apiUserToken = apiUserToken;
    }
}
