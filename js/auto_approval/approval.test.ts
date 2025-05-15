import test from 'node:test';
import assert from 'node:assert';
import { ActionInputs, getActionInputs, shouldApprove, CommandLineInputs } from './approval.js';

test.describe('Approval', () => {
  const actionInput: CommandLineInputs = {
    token: 'GH_TOKEN',
    review_as: 'a_reviewer',
    allowed_review_for: 'allowed_1,allowed_2',
  };

  const expectedInputs: ActionInputs = {
    token: 'GH_TOKEN',
    allowed_review_for: ['allowed_1', 'allowed_2'],
    requested_reviewers: ['this', 'that', 'a_reviewer', 'another'],
    review_as: 'a_reviewer',
    sender_login: 'allowed_1',
    source_git: 'https://github.com/repo/valid',
    target_git: 'https://github.com/repo/valid',
  };

  test.describe('Action Inputs', () => {
    test.test('should return a full class', () => {
      const result = getActionInputs(actionInput, {
        pull_request: {
          number: 123,
          body: 'blah',
          user: { login: 'allowed_1' },
          requested_reviewers: [{ login: 'this' }, { login: 'that' }, { login: 'a_reviewer' }, { login: 'another' }],
          base: { git_url: 'https://github.com/repo/valid' },
          head: { git_url: 'https://github.com/repo/valid' },
        },
      });

      assert.deepEqual(result, expectedInputs);
    });
  });

  test.describe('Should Approve', () => {
    test.test('happy path', () => {
      assert.equal(shouldApprove(expectedInputs), true);
    });

    test.test('should not approve because repos are not the same', () => {
      const inputs: ActionInputs = {
        ...expectedInputs,
        target_git: 'https://github.com/repo2/valid',
      };
      assert.equal(shouldApprove(inputs), false);
    });

    test.test('should not approve because sender_login not in list', () => {
      const inputs: ActionInputs = {
        ...expectedInputs,
        sender_login: 'unknown',
      };
      assert.equal(shouldApprove(inputs), false);
    });

    test.test('should not approve because review_as not in requested_reviewers', () => {
      const inputs: ActionInputs = {
        ...expectedInputs,
        requested_reviewers: ['this', 'that', 'another'],
      };
      assert.equal(shouldApprove(inputs), false);
    });
  });

  test.describe('Approving', () => {
    test.test('happy path', () => {
      const result = getActionInputs(actionInput, {
        pull_request: {
          number: 123,
          body: 'blah',
          user: { login: 'allowed_1' },
          requested_reviewers: [{ login: 'this' }, { login: 'that' }, { login: 'a_reviewer' }, { login: 'another' }],
          base: { git_url: 'https://github.com/repo/valid' },
          head: { git_url: 'https://github.com/repo/valid' },
        },
      });

      assert.deepEqual(result, expectedInputs);
    });
  });
});
