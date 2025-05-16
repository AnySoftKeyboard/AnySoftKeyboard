import test from 'node:test';
import assert from 'node:assert';
import { checkJobStatuses } from './green_checker.js';

test.describe('Check Greens', () => {
  test.describe('syntactic', () => {
    test.test('All success', () => {
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

    test.test('Some skipped', () => {
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

    test.test('Some failed', () => {
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

    test.test('Some unknown', () => {
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

    test.test('no needs', () => {
      const jsonInput = `{}`;

      assert.equal(checkJobStatuses(jsonInput), true);
    });

    test.test('empty result', () => {
      const jsonInput = `{"job1": {}}`;

      assert.equal(checkJobStatuses(jsonInput), false);
    });

    test.test('some empty result', () => {
      const jsonInput = `{
          "job1": {
            "result": "success"
          },
          "job2": {},
        }`;

      assert.equal(checkJobStatuses(jsonInput), false);
    });
  });

  test.describe('from github', () => {
    test.test('Some failures', () => {
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
        bazel: {
          result: 'failure',
          outputs: {},
        },
      });

      assert.equal(checkJobStatuses(jsonInput), false);
    });
  });
});
