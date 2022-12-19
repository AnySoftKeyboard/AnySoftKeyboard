'use strict'

const core = require('@actions/core');
const github = require('@actions/github');

const main = async () => {
  const token = core.getInput('token');
  const users = core.getInput('allowed_users')
                  .split(',')
                  .map(u => u.trim())
                  .filter(u => u.length > 0);

  const context = github.context;
  const sender_login = context.payload.sender.login;
  if (users.includes(sender_login)) {
    core.info(`User '${sender_login}' PR will be approved.`);
    const octokit = github.getOctokit(token);
  
    await octokit.rest.pulls.createReview({
      ...context.repo,
      pull_number: context.payload.number,
      event: 'APPROVE'
    });
  } else {
    core.debug(`User '${sender_login}' is not in allowed list: ${users.join(",")}. PR will not be auto-approved.`);
  }
}

main().catch(err => core.setFailed(err.message));
