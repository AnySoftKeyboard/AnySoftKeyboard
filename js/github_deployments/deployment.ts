import { calculateDeploymentName, getDeploymentConfiguration } from './deployment_config.js';
import { DeploymentCreateResponse, DeploymentRequestProcessor } from './deployment_request.js';
import { GitHubApi } from './github_api.js';

export type DeployMode = 'force_new' | 'force_promote';

export class DeploymentProcessor {
  private githubApi: GitHubApi;
  private owner: string;
  private repo: string;

  constructor(githubApi: GitHubApi, owner: string, repo: string) {
    this.githubApi = githubApi;
    this.owner = owner;
    this.repo = repo;
  }

  public async requestDeployment(
    deployMode: DeployMode,
    currentDate: number,
    sha: string,
    refname: string,
    shard: string,
    requestProcessorFactory: (githubApi: GitHubApi, owner: string, repo: string) => DeploymentRequestProcessor,
  ): Promise<DeploymentCreateResponse> {
    const requester = requestProcessorFactory(this.githubApi, this.owner, this.repo);

    const config = getDeploymentConfiguration(calculateDeploymentName(refname, shard));

    const shaToDeploy = await this.githubApi.getCommit({
      owner: this.owner,
      repo: this.repo,
      ref_or_sha: sha === 'HEAD' ? refname : sha,
    });
    if (!shaToDeploy.date) {
      throw new Error(`Could not fetch date for ref ${refname} and sha ${sha}`);
    }

    let stepIndex: number;
    switch (deployMode) {
      case 'force_new':
        stepIndex = 0;
        break;
      case 'force_promote':
        stepIndex = config.getStepIndex(currentDate, shaToDeploy.date.getUTCMilliseconds());
        if (stepIndex === 0) stepIndex = 1;
        break;
    }

    if (stepIndex >= config.environmentSteps.length) stepIndex = config.environmentSteps.length - 1;

    return requester.processDeploymentStep(shaToDeploy.sha, config, stepIndex);
  }
}
