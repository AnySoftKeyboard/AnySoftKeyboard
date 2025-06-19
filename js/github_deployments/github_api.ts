import { getOctokit } from '@actions/github';

export type DeploymentState = 'error' | 'failure' | 'inactive' | 'in_progress' | 'queued' | 'pending' | 'success';

export interface CreateDeploymentResponse {
  id: number;
  environment: string;
  sha: string;
  ref: string;
  task: string;
}

export interface GitHubApi {
  createDeployment(params: {
    owner: string;
    repo: string;
    ref: string;
    task: string;
    auto_merge: boolean;
    environment: string;
    description: string;
    required_contexts: string[];
    payload: {
      environments_to_kill: string[];
      previous_environment: string;
    };
  }): Promise<CreateDeploymentResponse>;

  createDeploymentStatus(params: {
    owner: string;
    repo: string;
    deployment_id: number;
    state: DeploymentState;
    environment?: string;
  }): Promise<{
    id: number;
    state: string;
    description?: string;
    environment?: string;
  }>;

  listDeployments(params: { owner: string; repo: string; sha: string }): Promise<
    Array<{
      id: number;
      environment: string;
    }>
  >;

  getCommit(params: { owner: string; repo: string; ref_or_sha: string }): Promise<{
    sha: string;
    login?: string;
    email?: string;
    date?: Date;
  }>;
}

export class OctokitGitHubApi implements GitHubApi {
  private octokit: ReturnType<typeof getOctokit>;

  constructor(token: string, octokitFactory?: (token: string) => ReturnType<typeof getOctokit>) {
    if (!octokitFactory) octokitFactory = getOctokit;
    this.octokit = octokitFactory(token);
  }

  async getCommit(params: { owner: string; repo: string; ref_or_sha: string }): Promise<{
    sha: string;
    login?: string;
    email?: string;
    date?: Date;
  }> {
    const response = await this.octokit.rest.repos.getCommit({
      owner: params.owner,
      repo: params.repo,
      ref: params.ref_or_sha,
    });
    const commit = response.data;
    return {
      sha: commit.commit.tree.sha,
      login: commit.author?.login,
      email: commit.commit.author?.email,
      date: commit.commit.author?.date ? new Date(commit.commit.author?.date) : undefined,
    };
  }

  async createDeployment(params: {
    owner: string;
    repo: string;
    ref: string;
    task: string;
    auto_merge: boolean;
    environment: string;
    description: string;
    required_contexts: string[];
    payload: {
      environments_to_kill: string[];
      previous_environment: string;
    };
  }): Promise<CreateDeploymentResponse> {
    const response = await this.octokit.rest.repos.createDeployment(params);
    if ('message' in response.data) {
      throw new Error(response.data.message);
    } else {
      const successData = response.data as CreateDeploymentResponse;
      return {
        id: successData.id,
        environment: successData.environment,
        sha: successData.sha,
        ref: successData.ref,
        task: successData.task,
      };
    }
  }

  async createDeploymentStatus(params: {
    owner: string;
    repo: string;
    deployment_id: number;
    state: DeploymentState;
    environment?: string;
  }): Promise<{
    id: number;
    state: string;
    description?: string;
    environment?: string;
  }> {
    const response = await this.octokit.rest.repos.createDeploymentStatus(params);
    return {
      id: response.data.id,
      state: response.data.state,
      description: response.data.description || undefined,
      environment: params.environment,
    };
  }

  async listDeployments(params: { owner: string; repo: string; sha: string }): Promise<
    Array<{
      id: number;
      environment: string;
    }>
  > {
    const response = await this.octokit.rest.repos.listDeployments(params);
    return response.data.map((deployment) => ({
      id: deployment.id,
      environment: deployment.environment,
    }));
  }
}
