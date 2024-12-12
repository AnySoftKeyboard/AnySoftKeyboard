import { context } from '@actions/github';
import { Command } from 'commander';
import { getActionInputs, shouldApprove, approvePr } from './approval.js';
import { exit } from 'process';

const program = new Command();

program
  .requiredOption('--token <token>', 'GitHub token')
  .requiredOption('--review_as <reviewer>', 'Reviewer name')
  .requiredOption('--allowed_users <users>', 'Comma-separated list of allowed users');

const main = async () => {
  program.parse();
  const opts = program.opts();
  
  const actionInputs = getActionInputs(
    {
      token: opts['token'],
      review_as: opts['review_as'],
      allowed_review_for: opts['allowed_users'],
    },
    context.payload
  );

  if (shouldApprove(actionInputs)) {
    await approvePr(actionInputs.token);
  }
};

main().catch((err) => {
  console.error(err);
  exit(-1);
});

