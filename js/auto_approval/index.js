'use strict';

import * as github from '@actions/github';
import * as core from '@actions/core';

const main = async () => {
  const token = core.getInput('token');
  const users = core
    .getInput('allowed_users')
    .split(',')
    .map((u) => u.trim())
    .filter((u) => u.length > 0);
  const reviewer_login = core.getInput('review_for');

  const sender_login = github.context.payload.pull_request.user.login;
  const reviewers = github.context.payload.pull_request.requested_reviewers
    .map((u) => u.login)
    .filter((u) => u.length > 0);
  const source_git = github.context.payload.pull_request.base.git_url;
  const target_git = github.context.payload.pull_request.head.git_url;

  if (source_git == target_git) {
    // required, since we can only get the secret from when running in our repo context.
    core.info(`PR originated from the target git repo, we can review this.`);
    if (reviewers.includes(reviewer_login)) {
      core.info(`'${reviewer_login}' has been requested to review.`);
      if (users.includes(sender_login)) {
        core.info(`User '${sender_login}' PR will be approved.`);
        const octokit = github.getOctokit(token);

        await octokit.rest.pulls.createReview({
          ...github.context.repo,
          pull_number: github.context.payload.number,
          event: 'APPROVE',
        });
      } else {
        core.info(`User '${sender_login}' is not in allowed list: ${users.join(', ')}. PR will not be auto-approved.`);
      }
    } else {
      core.info(
        `'${reviewer_login}' is not in list of requested reviewers: ${reviewers.join(
          ', ',
        )}. PR will not be auto-approved.`,
      );
    }
  } else {
    core.info(
      `PR repo is ${source_git}, which is not our repo ${target_git}. We are not allowed to get API token in such context.`,
    );
  }
};

main().catch((err) => core.setFailed(err.message));
