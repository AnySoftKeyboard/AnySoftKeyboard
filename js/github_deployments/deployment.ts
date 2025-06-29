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
    shard: 'ime' | 'addons',
    requestProcessorFactory: (githubApi: GitHubApi, owner: string, repo: string) => DeploymentRequestProcessor,
  ): Promise<DeploymentCreateResponse> {
    const config = getDeploymentConfiguration(calculateDeploymentName(refname, shard));
    if (!config) {
      console.log(`refname and shard combination is not eligible for deployment: ${sha}, ${refname}, ${shard}.`);
      return {
        id: '',
        environment: '',
        sha: sha,
        ref: refname,
        task: '',
        payload: {
          environments_to_kill: [],
        },
      };
    }

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
        stepIndex = config.getStepIndex(currentDate, shaToDeploy.date.getTime());
        if (stepIndex === 0) stepIndex = 1;
        break;
      default:
        throw new Error(`Unknown value for deployMode: ${deployMode}`);
    }

    if (stepIndex >= config.environmentSteps.length) stepIndex = config.environmentSteps.length - 1;

    const requester = requestProcessorFactory(this.githubApi, this.owner, this.repo);
    return requester.processDeploymentStep(shaToDeploy.sha, refname, config, stepIndex);
  }
}
