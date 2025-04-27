import { assert } from 'chai';
import { checkJobStatuses } from './green_checker.js';

describe('Check Greens', () => {
  describe('syntactic', () => {
    it('All success', () => {
      const jsonInput = JSON.stringify({
        job1: {
          result: 'success',
        },
        job2: {
          result: 'success',
        },
        job3: {
          result: 'success',
        },
      });

      assert.equal(checkJobStatuses(jsonInput), true);
    });

    it('Some skipped', () => {
      const jsonInput = JSON.stringify({
        job1: {
          result: 'success',
        },
        job2: {
          result: 'skipped',
        },
        job3: {
          result: 'success',
        },
      });

      assert.equal(checkJobStatuses(jsonInput), false);
    });

    it('Some failed', () => {
      const jsonInput = JSON.stringify({
        job1: {
          result: 'success',
        },
        job2: {
          result: 'failure',
        },
        job3: {
          result: 'success',
        },
      });

      assert.equal(checkJobStatuses(jsonInput), false);
    });

    it('Some unknown', () => {
      const jsonInput = JSON.stringify({
        job1: {
          result: 'success',
        },
        job2: {
          result: 'unknown',
        },
        job3: {
          result: 'success',
        },
      });

      assert.equal(checkJobStatuses(jsonInput), false);
    });

    it('no needs', () => {
      const jsonInput = `{}`;

      assert.equal(checkJobStatuses(jsonInput), true);
    });

    it('empty result', () => {
      const jsonInput = `{"job1": {}}`;

      assert.equal(checkJobStatuses(jsonInput), false);
    });

    it('some empty result', () => {
      const jsonInput = `{
          "job1": {
            "result": "success"
          },
          "job2": {},
        }`;

      assert.equal(checkJobStatuses(jsonInput), false);
    });
  });

  describe('from github', () => {
    it('Some failures', () => {
      const jsonInput = JSON.stringify({
        'static-checks': {
          result: 'failure',
          outputs: {},
        },
        linters: {
          result: 'success',
          outputs: {},
        },
        tests: {
          result: 'failure',
          outputs: {},
        },
        'deploy-dry-run': {
          result: 'success',
          outputs: {},
        },
        'js-checks': {
          result: 'success',
          outputs: {},
        },
        bazel: {
          result: 'failure',
          outputs: {},
        },
      });

      assert.equal(checkJobStatuses(jsonInput), false);
    });
  });
});
