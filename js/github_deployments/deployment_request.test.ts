import test from 'node:test';
import assert from 'node:assert';
import { DeploymentRequestProcessor } from './deployment_request.js';
import { GitHubApi } from './github_api.js';
import { DeploymentConfiguration } from './deployment_config.js';

test.describe('DeploymentRequestProcessor', () => {
  test.describe('processDeploymentStep', () => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let createDeploymentParams: any = null;
    const mockApi: GitHubApi = {
      createDeployment: async (params) => {
        createDeploymentParams = params;
        return {
          id: 123,
          environment: params.environment,
          sha: params.ref,
          ref: params.ref,
          task: params.task,
        };
      },
      createDeploymentStatus: async () => ({
        id: 0,
        state: '',
        description: '',
      }),
      listDeployments: async () => [],
      getCommit: async () => ({ sha: '' }),
    };

    test.test('environments_to_kill includes all environments except current', async () => {
      const config = new DeploymentConfiguration(
        'test',
        ['env1', 'env2', 'env3'],
        () => 1, // step index 1 means env2 is current
      );
      const processor = new DeploymentRequestProcessor(mockApi, 'owner', 'repo');
      await processor.processDeploymentStep('sha', 'ref', config, 1);

      assert.deepEqual(createDeploymentParams.payload.environments_to_kill.sort(), ['test_env1', 'test_env3'].sort());
    });

    test.test('previous_environment is set to previous step environment', async () => {
      const config = new DeploymentConfiguration(
        'test',
        ['env1', 'env2', 'env3'],
        () => 1, // step index 1 means env2 is current
      );
      const processor = new DeploymentRequestProcessor(mockApi, 'owner', 'repo');
      await processor.processDeploymentStep('sha', 'ref', config, 1);

      assert.equal(createDeploymentParams.payload.previous_environment, 'test_env1');
    });

    test.test('previous_environment is "NONE" for first step', async () => {
      const config = new DeploymentConfiguration(
        'test',
        ['env1', 'env2', 'env3'],
        () => 0, // step index 0 means env1 is current
      );
      const processor = new DeploymentRequestProcessor(mockApi, 'owner', 'repo');
      await processor.processDeploymentStep('sha', 'ref', config, 0);

      assert.equal(createDeploymentParams.payload.previous_environment, 'NONE');
    });

    test.test('task is "deploy" for step 0, "deploy:migration" otherwise', async () => {
      const config = new DeploymentConfiguration('test', ['env1', 'env2'], () => 0);
      const processor = new DeploymentRequestProcessor(mockApi, 'owner', 'repo');

      await processor.processDeploymentStep('sha', 'ref', config, 0);
      assert.equal(createDeploymentParams.task, 'deploy');

      await processor.processDeploymentStep('sha', 'ref', config, 1);
      assert.equal(createDeploymentParams.task, 'deploy:migration');
    });

    test.test('environment is set to correct step value', async () => {
      const config = new DeploymentConfiguration('test', ['env1', 'env2', 'env3'], () => 1);
      const processor = new DeploymentRequestProcessor(mockApi, 'owner', 'repo');
      await processor.processDeploymentStep('sha', 'ref', config, 1);

      assert.equal(createDeploymentParams.environment, 'test_env2');
    });

    test.test('required_contexts is exactly ["all-green-requirement"]', async () => {
      const config = new DeploymentConfiguration('test', ['env1'], () => 0);
      const processor = new DeploymentRequestProcessor(mockApi, 'owner', 'repo');
      await processor.processDeploymentStep('sha', 'ref', config, 0);

      assert.deepEqual(createDeploymentParams.required_contexts, ['all-green-requirement']);
    });

    test.test('ref name is passed', async () => {
      const config = new DeploymentConfiguration('test', ['env1'], () => 0);
      const processor = new DeploymentRequestProcessor(mockApi, 'owner', 'repo');
      await processor.processDeploymentStep('sha', 'ref', config, 0);

      assert.deepEqual(createDeploymentParams.ref, 'ref');
    });

    test.test('throws error when step value is empty', async () => {
      createDeploymentParams = null;
      const config = new DeploymentConfiguration('test', ['env1', '', 'env3'], () => 1);
      const processor = new DeploymentRequestProcessor(mockApi, 'owner', 'repo');

      const response = await processor.processDeploymentStep('sha', 'ref', config, 1);
      // API was not called
      assert.equal(createDeploymentParams, null);
      assert.equal(response.id, '');
    });
  });
});
