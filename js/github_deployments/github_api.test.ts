import test from 'node:test';
import assert from 'node:assert';
import { OctokitGitHubApi } from './github_api.js';
import { getOctokit } from '@actions/github';

// Create a mock octokit instance
const mockOctokit = {
  rest: {
    repos: {
      getCommit: async ({ owner: _owner, repo: _repo, ref: _ref }: never) => ({
        data: {
          commit: {
            tree: { sha: 'tree-sha' },
            author: { email: 'author@email.com', date: '2024-06-12T12:00:00Z' },
          },
          author: { login: 'octocat' },
        },
      }),
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      createDeployment: async (params: any) => ({
        data: {
          id: 123,
          environment: params.environment,
          sha: 'commit-sha',
          ref: params.ref,
          task: params.task,
        },
      }),
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      createDeploymentStatus: async (params: any) => ({
        data: {
          id: 456,
          state: params.state,
          description: 'desc',
        },
      }),
      listDeployments: async (_params: never) => ({
        data: [
          { id: 1, environment: 'env1' },
          { id: 2, environment: 'env2' },
        ],
      }),
    },
  },
} as ReturnType<typeof getOctokit>;

test.describe('OctokitGitHubApi', () => {
  test.test('getCommit returns correct commit info', async () => {
    const api = new OctokitGitHubApi('token', (_) => mockOctokit);
    const result = await api.getCommit({ owner: 'o', repo: 'r', ref_or_sha: 'sha' });
    assert.equal(result.sha, 'tree-sha');
    assert.equal(result.login, 'octocat');
    assert.equal(result.email, 'author@email.com');
    assert.ok(result.date instanceof Date);
  });

  test.test('createDeployment returns correct deployment info', async () => {
    const api = new OctokitGitHubApi('token', (_) => mockOctokit);
    const result = await api.createDeployment({
      owner: 'o',
      repo: 'r',
      ref: 'sha',
      task: 'deploy',
      auto_merge: false,
      environment: 'env',
      description: 'desc',
      required_contexts: [],
      payload: { environments_to_kill: [], previous_environment: '' },
    });
    assert.equal(result.id, 123);
    assert.equal(result.environment, 'env');
    assert.equal(result.sha, 'commit-sha');
    assert.equal(result.ref, 'sha');
    assert.equal(result.task, 'deploy');
  });

  test.test('createDeploymentStatus returns correct status info', async () => {
    const api = new OctokitGitHubApi('token', (_) => mockOctokit);
    const result = await api.createDeploymentStatus({
      owner: 'o',
      repo: 'r',
      deployment_id: 123,
      state: 'success',
      environment: 'env',
    });
    assert.equal(result.id, 456);
    assert.equal(result.state, 'success');
    assert.equal(result.description, 'desc');
    assert.equal(result.environment, 'env');
  });

  test.test('listDeployments returns deployments', async () => {
    const api = new OctokitGitHubApi('token', (_) => mockOctokit);
    const result = await api.listDeployments({ owner: 'o', repo: 'r', sha: 'sha' });
    assert.deepEqual(result, [
      { id: 1, environment: 'env1' },
      { id: 2, environment: 'env2' },
    ]);
  });
});
