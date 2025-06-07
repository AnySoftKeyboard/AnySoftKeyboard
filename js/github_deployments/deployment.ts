import { calculateDeploymentName, getDeploymentConfiguration } from './deployment_config.js';
import { DeploymentCreateResponse, DeploymentRequestProcessor } from './deployment_request.js';
import { GitHubApi } from './github_api.js';

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
    currentDate: number,
    sha: string,
    refname: string,
    shard: string,
  ): Promise<DeploymentCreateResponse> {
    const requester = new DeploymentRequestProcessor(this.githubApi, this.owner, this.repo);

    const config = getDeploymentConfiguration(calculateDeploymentName(refname, shard));

    const shaToDeploy = await this.githubApi.getCommit({
      owner: this.owner,
      repo: this.repo,
      ref_or_sha: sha === 'HEAD' ? refname : sha,
    });
    if (!shaToDeploy.date) {
      throw new Error(`Could not fetch date for ref ${refname} and sha ${sha}`);
    }

    let stepIndex = config.getStepIndex(currentDate, shaToDeploy.date.getUTCMilliseconds());
    if (stepIndex >= config.environmentSteps.length) stepIndex = config.environmentSteps.length - 1;

    return requester.processDeploymentStep(sha, config, stepIndex);
  }
}
