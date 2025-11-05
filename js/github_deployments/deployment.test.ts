import test from 'node:test';
import assert from 'node:assert';
import { DeploymentProcessor } from './deployment.js';
import { GitHubApi } from './github_api.js';
import { DeploymentRequestProcessor } from './deployment_request.js';

const mockGitHubApi: GitHubApi = {
  getCommit: async (_) => ({
    sha: 'sha',
    login: 'octocat',
    email: 'octocat@email.com',
    date: new Date('2024-06-12T12:00:00Z'),
  }),
  createDeployment: async () => {
    throw new Error('not implemented');
  },
  createDeploymentStatus: async () => {
    throw new Error('not implemented');
  },
  listDeployments: async () => {
    throw new Error('not implemented');
  },
};

test.describe('DeploymentProcessor', () => {
  test.test('requestDeployment calls processDeploymentStep with correct stepIndex for force_new', async () => {
    let calledWithStepIndex: number | undefined;
    const mockProcessDeploymentStep = async (_sha: string, _ref: string, _config: never, stepIndex: number) => {
      calledWithStepIndex = stepIndex;
      return {
        id: '1',
        environment: 'env',
        sha: 'sha',
        ref: 'ref',
        task: 'deploy',
        payload: { environments_to_kill: [] },
      };
    };
    const mockRequestProcessorFactory = (_githubApi: GitHubApi, _owner: string, _repo: string) => {
      return {
        processDeploymentStep: mockProcessDeploymentStep,
      } as unknown as DeploymentRequestProcessor;
    };
    const processor = new DeploymentProcessor(mockGitHubApi, 'owner', 'repo');
    const result = await processor.requestDeployment(
      'force_new',
      Date.now(),
      'sha',
      'main',
      'ime',
      mockRequestProcessorFactory,
    );
    assert.equal(calledWithStepIndex, 0);
    assert.equal(result.id, '1');
    assert.equal(result.environment, 'env');
  });

  test.test('requestDeployment calls processDeploymentStep with correct stepIndex for force_promote', async () => {
    let calledWithStepIndex: number | undefined;
    const mockProcessDeploymentStep = async (_sha: string, _ref: string, _config: never, stepIndex: number) => {
      calledWithStepIndex = stepIndex;
      return {
        id: '2',
        environment: 'env',
        sha: 'sha',
        ref: 'ref',
        task: 'deploy',
        payload: { environments_to_kill: [] },
      };
    };
    const mockRequestProcessorFactory = (_githubApi: GitHubApi, _owner: string, _repo: string) => {
      return {
        processDeploymentStep: mockProcessDeploymentStep,
      } as unknown as DeploymentRequestProcessor;
    };
    const processor = new DeploymentProcessor(mockGitHubApi, 'owner', 'repo');
    // Use Wednesday so getStepIndex returns 1
    const wednesday = new Date('2024-06-12T12:00:00Z').getTime();
    const result = await processor.requestDeployment(
      'force_promote',
      wednesday,
      'sha',
      'main',
      'ime',
      mockRequestProcessorFactory,
    );
    assert.equal(calledWithStepIndex, 1);
    assert.equal(result.id, '2');
    assert.equal(result.environment, 'env');
  });

  test.test('requestDeployment throws if getCommit returns no date', async () => {
    const badGitHubApi: GitHubApi = {
      ...mockGitHubApi,
      getCommit: async () => ({ sha: 'sha' }),
    };
    const mockRequestProcessorFactory = (_githubApi: GitHubApi, _owner: string, _repo: string) => {
      return {
        processDeploymentStep: async () => ({}),
      } as unknown as DeploymentRequestProcessor;
    };
    const processor = new DeploymentProcessor(badGitHubApi, 'owner', 'repo');
    await assert.rejects(
      () => processor.requestDeployment('force_new', Date.now(), 'sha', 'main', 'ime', mockRequestProcessorFactory),
      /Could not fetch date/,
    );
  });

  test.test('requestDeployment uses branch head sha when sha is HEAD', async () => {
    let usedSha: string | undefined;
    const mockGitHubApiHead: GitHubApi = {
      ...mockGitHubApi,
      getCommit: async ({ ref_or_sha: _ref_or_sha }) => ({
        sha: 'branch-head-sha',
        login: 'octocat',
        email: 'octocat@email.com',
        date: new Date('2024-06-12T12:00:00Z'),
      }),
    };
    const mockProcessDeploymentStep = async (sha: string, _ref: string, _config: never, _stepIndex: number) => {
      usedSha = sha;
      return {
        id: '3',
        environment: 'env',
        sha,
        ref: 'ref',
        task: 'deploy',
        payload: { environments_to_kill: [] },
      };
    };
    const mockRequestProcessorFactory = (_githubApi: GitHubApi, _owner: string, _repo: string) => {
      return {
        processDeploymentStep: mockProcessDeploymentStep,
      } as unknown as DeploymentRequestProcessor;
    };
    const processor = new DeploymentProcessor(mockGitHubApiHead, 'owner', 'repo');
    await processor.requestDeployment('force_new', Date.now(), 'HEAD', 'main', 'ime', mockRequestProcessorFactory);
    assert.equal(usedSha, 'branch-head-sha');
  });

  test.test('requestDeployment uses specific commit sha when provided', async () => {
    let usedSha: string | undefined;
    const mockGitHubApiCommit: GitHubApi = {
      ...mockGitHubApi,
      getCommit: async ({ ref_or_sha: _ref_or_sha }) => ({
        sha: _ref_or_sha,
        login: 'octocat',
        email: 'octocat@email.com',
        date: new Date('2024-06-12T12:00:00Z'),
      }),
    };
    const mockProcessDeploymentStep = async (sha: string, _ref: string, _config: never, _stepIndex: number) => {
      usedSha = sha;
      return {
        id: '4',
        environment: 'env',
        sha,
        ref: 'ref',
        task: 'deploy',
        payload: { environments_to_kill: [] },
      };
    };
    const mockRequestProcessorFactory = (_githubApi: GitHubApi, _owner: string, _repo: string) => {
      return {
        processDeploymentStep: mockProcessDeploymentStep,
      } as unknown as DeploymentRequestProcessor;
    };
    const processor = new DeploymentProcessor(mockGitHubApiCommit, 'owner', 'repo');
    await processor.requestDeployment(
      'force_new',
      Date.now(),
      'my-commit-sha',
      'main',
      'ime',
      mockRequestProcessorFactory,
    );
    assert.equal(usedSha, 'my-commit-sha');
  });

  test.test('requestDeployment does not call deploy if not the right branch for addons', async () => {
    let called: boolean = false;
    const mockProcessDeploymentStep = async (sha: string, _ref: string, _config: never, _stepIndex: number) => {
      called = true;
      return {
        id: '4',
        environment: 'env',
        sha,
        ref: 'ref',
        task: 'deploy',
        payload: { environments_to_kill: [] },
      };
    };
    const mockRequestProcessorFactory = (_githubApi: GitHubApi, _owner: string, _repo: string) => {
      return {
        processDeploymentStep: mockProcessDeploymentStep,
      } as unknown as DeploymentRequestProcessor;
    };
    const processor = new DeploymentProcessor(mockGitHubApi, 'owner', 'repo');
    const emptyResult = await processor.requestDeployment(
      'force_new',
      Date.now(),
      'my-commit-sha',
      '',
      'addons',
      mockRequestProcessorFactory,
    );
    assert.equal(called, false);
    assert.equal(emptyResult.id, '');
    assert.equal(emptyResult.task, '');
    assert.equal(emptyResult.environment, '');
  });

  test.test('requestDeployment does not call deploy if not the right branch for ime', async () => {
    let called: boolean = false;
    const mockProcessDeploymentStep = async (sha: string, _ref: string, _config: never, _stepIndex: number) => {
      called = true;
      return {
        id: '4',
        environment: 'env',
        sha,
        ref: 'ref',
        task: 'deploy',
        payload: { environments_to_kill: [] },
      };
    };
    const mockRequestProcessorFactory = (_githubApi: GitHubApi, _owner: string, _repo: string) => {
      return {
        processDeploymentStep: mockProcessDeploymentStep,
      } as unknown as DeploymentRequestProcessor;
    };
    const processor = new DeploymentProcessor(mockGitHubApi, 'owner', 'repo');
    const emptyResult = await processor.requestDeployment(
      'force_new',
      Date.now(),
      'my-commit-sha',
      '',
      'ime',
      mockRequestProcessorFactory,
    );
    assert.equal(called, false);
    assert.equal(emptyResult.id, '');
    assert.equal(emptyResult.task, '');
    assert.equal(emptyResult.environment, '');
  });
});
