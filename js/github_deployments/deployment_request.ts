import { DeploymentConfiguration } from './deployment_config.js';
import { GitHubApi } from './github_api.js';

export interface DeploymentCreateResponse {
  id: string;
  environment: string;
  sha: string;
  ref: string;
  task: string;
  payload: {
    environments_to_kill: string[];
  };
}

export class DeploymentRequestProcessor {
  private githubApi: GitHubApi;
  private owner: string;
  private repo: string;

  constructor(githubApi: GitHubApi, owner: string, repo: string) {
    this.githubApi = githubApi;
    this.owner = owner;
    this.repo = repo;
  }

  async processDeploymentStep(
    sha: string,
    refname: string,
    configuration: DeploymentConfiguration,
    stepIndex: number,
  ): Promise<DeploymentCreateResponse> {
    if (!this.shouldDeploy(configuration, stepIndex)) {
      console.log(
        `Configuration ${configuration.name} is marked not to deploy for step ${stepIndex} on sha ${sha} in ref ${refname}.`,
      );
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
    const environmentToDeploy = this.getEnvironmentName(configuration, stepIndex);
    const previousEnvironment = this.getPreviousEnvironmentName(configuration, stepIndex);
    const environmentsToKill = configuration.environmentSteps
      .map((name) => this.getEnvironmentNameFromParts(configuration.name, name))
      .filter((env) => env !== environmentToDeploy);

    const request = {
      owner: this.owner,
      repo: this.repo,
      sha: sha,
      ref: refname,
      task: stepIndex === 0 ? 'deploy' : 'deploy:migration',
      auto_merge: false,
      environment: environmentToDeploy,
      description: `Deployment for '${environmentToDeploy}' from ('${previousEnvironment}').`,
      required_contexts: ['all-green-requirement'],
      payload: {
        environments_to_kill: environmentsToKill,
        previous_environment: previousEnvironment,
      },
    };
    console.log(`Will create a deployment in github with for step ${stepIndex}:\n${JSON.stringify(request)}`);
    const response = await this.githubApi.createDeployment(request);

    const deploymentResponse: DeploymentCreateResponse = {
      id: response.id.toString(),
      environment: response.environment,
      sha: response.sha,
      ref: response.ref,
      task: response.task,
      payload: {
        environments_to_kill: environmentsToKill,
      },
    };

    console.log(
      `Deploy request response: id ${deploymentResponse.id}, sha ${deploymentResponse.sha}, environment ${deploymentResponse.environment}, task ${deploymentResponse.task}.`,
    );

    return deploymentResponse;
  }

  private getEnvironmentNameFromParts(environmentName: string, stepName: string): string {
    return `${environmentName}_${stepName}`;
  }

  private getEnvironmentName(environment: DeploymentConfiguration, index: number): string {
    return this.getEnvironmentNameFromParts(environment.name, environment.environmentSteps[index]);
  }

  private shouldDeploy(environment: DeploymentConfiguration, index: number): boolean {
    return environment.environmentSteps[index] !== undefined && environment.environmentSteps[index].trim() !== '';
  }

  private getPreviousEnvironmentName(environment: DeploymentConfiguration, index: number): string {
    // searching for the first, non-empty, step.
    for (let previousStep = index - 1; previousStep >= 0; previousStep--) {
      const previousEnvironmentStepName = environment.environmentSteps[previousStep];
      if (previousEnvironmentStepName.trim() !== '') {
        return this.getEnvironmentNameFromParts(environment.name, previousEnvironmentStepName);
      }
    }

    return 'NONE';
  }
}
