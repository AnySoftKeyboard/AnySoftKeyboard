import { GitHubApi, DeploymentState } from './github_api.js';

export interface DeploymentStatusResponse {
  id: string;
  description: string;
  environment: string;
  state: string;
}

export class DeploymentStatusProcessor {
  private githubApi: GitHubApi;
  private owner: string;
  private repo: string;

  constructor(githubApi: GitHubApi, owner: string, repo: string) {
    this.githubApi = githubApi;
    this.owner = owner;
    this.repo = repo;
  }

  async updateDeploymentStatus(
    environmentName: string,
    deploymentId: string,
    deploymentState: DeploymentState,
  ): Promise<DeploymentStatusResponse> {
    const response = await this.githubApi.createDeploymentStatus({
      owner: this.owner,
      repo: this.repo,
      deployment_id: parseInt(deploymentId),
      state: deploymentState,
      environment: environmentName,
    });

    const statusResponse: DeploymentStatusResponse = {
      id: response.id.toString(),
      description: response.description || '',
      environment: environmentName,
      state: response.state,
    };

    console.log(
      `Deployment-status request response: id ${statusResponse.id}, state ${statusResponse.state}, environment ${statusResponse.environment}, description ${statusResponse.description}.`,
    );

    return statusResponse;
  }

  async updateDeploymentSuccess(environmentSha: string, environmentName: string): Promise<void> {
    const processName = environmentName.substring(0, environmentName.indexOf('_') + 1);

    // List deployments for the SHA
    const deployments = await this.githubApi.listDeployments({
      owner: this.owner,
      repo: this.repo,
      sha: environmentSha,
    });

    console.log(
      `Deployment-status request response: length ${deployments.length}, environments ${deployments.map((d) => d.id).join(',')}.`,
    );

    for (const deployment of deployments) {
      if (deployment.environment.startsWith(processName)) {
        const status = deployment.environment === environmentName ? 'success' : 'inactive';
        console.log(`Will change environment ${deployment.environment} with id ${deployment.id} to status ${status}`);

        await this.githubApi.createDeploymentStatus({
          owner: this.owner,
          repo: this.repo,
          deployment_id: deployment.id,
          state: status,
          environment: deployment.environment,
        });
      } else {
        console.log(`Skipping ${deployment.environment} since it's not in the same process.`);
      }
    }
  }
}
