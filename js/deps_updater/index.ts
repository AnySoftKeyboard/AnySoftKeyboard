import { Command } from 'commander';
import { exit } from 'process';
import { update_gh_actions } from './github_actions_updater.js';
import { update_gradle_deps } from './gradle_deps_updater.js';

const program = new Command();
program.name('deps_updater').description('CLI updating 3rd party deps in the repo').version('0.0.1');

program
  .command('gh_actions')
  .requiredOption('--workflowFolderPath <path>', 'Path to the workflow folder')
  .requiredOption('--gh_user <username>', 'GitHub username')
  .requiredOption('--gh_token <token>', "GitHub user's token")
  .action(async (options) => {
    update_gh_actions(options.workflowFolderPath, options.gh_user, options.gh_token);
  });

program
  .command('gradle_deps')
  .requiredOption('--rootFolder <path>', 'Path to the gradle root folder')
  .action(async (options) => {
    update_gradle_deps(options.rootFolder);
  });
const main = async () => {
  program.parse();
};

main().catch((_) => exit(-1));
