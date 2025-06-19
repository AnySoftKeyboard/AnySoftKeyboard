import test from 'node:test';
import assert from 'node:assert';
import { DeploymentStatusProcessor } from './deployment_status.js';
import { GitHubApi } from './github_api.js';

test.describe('DeploymentStatusProcessor', () => {
  test.test('updateDeploymentSuccess only changes environments with correct processName prefix', async () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const calls: any[] = [];
    const mockApi: GitHubApi = {
      createDeploymentStatus: async (params) => {
        calls.push({ method: 'createDeploymentStatus', params });
        return { id: params.deployment_id, state: params.state, description: '', environment: params.environment };
      },
      listDeployments: async () => [
        { id: 1, environment: 'prod_010' },
        { id: 2, environment: 'prod_020' },
        { id: 3, environment: 'other_010' },
        { id: 4, environment: 'prod_030' },
      ],
      getCommit: async () => ({ sha: '' }),
      createDeployment: async () => ({ id: 0, environment: '', sha: '', ref: '', task: '' }),
    };
    const processor = new DeploymentStatusProcessor(mockApi, 'owner', 'repo');
    await processor.updateDeploymentSuccess('sha', 'prod_020');
    // Should only call for prod_010, prod_020, prod_030
    const changed = calls.filter((c) => c.method === 'createDeploymentStatus');
    assert.equal(changed.length, 3);
    assert.deepEqual(
      changed.map((c) => [c.params.deployment_id, c.params.environment, c.params.state]),
      [
        [1, 'prod_010', 'inactive'],
        [2, 'prod_020', 'success'],
        [4, 'prod_030', 'inactive'],
      ],
    );
  });

  test.test('updateDeploymentSuccess skips environments with different processName', async () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const calls: any[] = [];
    const mockApi: GitHubApi = {
      createDeploymentStatus: async (params) => {
        calls.push({ method: 'createDeploymentStatus', params });
        return { id: params.deployment_id, state: params.state, description: '', environment: params.environment };
      },
      listDeployments: async () => [
        { id: 1, environment: 'foo_010' },
        { id: 2, environment: 'bar_020' },
      ],
      getCommit: async () => ({ sha: '' }),
      createDeployment: async () => ({ id: 0, environment: '', sha: '', ref: '', task: '' }),
    };
    const processor = new DeploymentStatusProcessor(mockApi, 'owner', 'repo');
    await processor.updateDeploymentSuccess('sha', 'foo_010');
    // Only foo_010 should be changed
    const changed = calls.filter((c) => c.method === 'createDeploymentStatus');
    assert.equal(changed.length, 1);
    assert.deepEqual(changed[0].params, {
      owner: 'owner',
      repo: 'repo',
      deployment_id: 1,
      state: 'success',
      environment: 'foo_010',
    });
  });

  test.test('updateDeploymentStatus calls github-api with correct values', async () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let calledParams: any = null;
    const mockApi: GitHubApi = {
      createDeploymentStatus: async (params) => {
        calledParams = params;
        return { id: params.deployment_id, state: params.state, description: '', environment: params.environment };
      },
      listDeployments: async () => [],
      getCommit: async () => ({ sha: '' }),
      createDeployment: async () => ({ id: 0, environment: '', sha: '', ref: '', task: '' }),
    };
    const processor = new DeploymentStatusProcessor(mockApi, 'owner', 'repo');
    await processor.updateDeploymentStatus('env', '42', 'success');
    assert.deepEqual(calledParams, {
      owner: 'owner',
      repo: 'repo',
      deployment_id: 42,
      state: 'success',
      environment: 'env',
    });
  });
});
