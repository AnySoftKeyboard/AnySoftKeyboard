import { Command, Option } from 'commander';
import { OctokitGitHubApi, GitHubApi, DeploymentState } from './github_api.js';
import { exit } from 'process';
import { DeploymentProcessor } from './deployment.js';
import { DeploymentStatusProcessor } from './deployment_status.js';

const program = new Command();

// Global options that are common to all commands
program
  .name('github-deployments')
  .description('GitHub Deployments CLI tool')
  .version('1.0.0')
  .requiredOption('--api-username <username>', 'API username')
  .requiredOption(
    '--token <githubClient>',
    'GitHub token',
    (token: string, _: GitHubApi) => new OctokitGitHubApi(token),
  )
  .requiredOption('--owner <owner>', 'Repository owner')
  .requiredOption('--repo <repo>', 'Repository name');

program
  .command('deploy')
  .description('Create a deployment request')
  .requiredOption('--sha <sha>', 'SHA to deploy. If HEAD, it will be calculated from refname.')
  .requiredOption('--refname <refname>', 'Name of branch to deploy.')
  .addOption(new Option('--shardName <shard>', 'ime or addons').makeOptionMandatory(true).choices(['ime', 'addons']))
  .addOption(
    new Option('--deployMode <deployMode>', 'force_new, force_promote')
      .makeOptionMandatory(true)
      .choices(['force_new', 'force_promote']),
  )
  .action(async (options, command) => {
    try {
      const globalOpts = command.parent.opts();
      const ghClient = globalOpts.githubClient as GitHubApi;
      const processor = new DeploymentProcessor(ghClient, globalOpts.owner, globalOpts.repo);

      const response = await processor.requestDeployment(
        globalOpts.deployMode,
        new Date().getUTCMilliseconds(),
        globalOpts.sha,
        globalOpts.refname,
        globalOpts.shard,
      );
      console.log(`Deployment request completed successfully:\n${JSON.stringify(response)}`);
    } catch (error) {
      console.error('Deployment request failed:', error);
      exit(1);
    }
  });

program
  .command('status')
  .description('Update deployment status')
  .requiredOption('--environment <environment>', 'Environment name')
  .requiredOption('--deployment-id <id>', 'Deployment ID')
  .requiredOption('--state <state>', 'Deployment state')
  .action(async (options, command) => {
    try {
      const globalOpts = command.parent.opts();
      const githubApi = new OctokitGitHubApi(globalOpts.token);
      const processor = new DeploymentStatusProcessor(githubApi, globalOpts.owner, globalOpts.repo);

      await processor.updateDeploymentStatus(
        options.environment,
        options.deploymentId,
        options.state as DeploymentState,
      );

      console.log('Status update completed successfully');
    } catch (error) {
      console.error('Status update failed:', error);
      exit(1);
    }
  });

program
  .command('success')
  .description('Update deployment to success status')
  .requiredOption('--environment <environment>', 'Environment name')
  .requiredOption('--sha <sha>', 'SHA to mark as successful')
  .action(async (options, command) => {
    try {
      const globalOpts = command.parent.opts();
      const githubApi = new OctokitGitHubApi(globalOpts.token);
      const processor = new DeploymentStatusProcessor(githubApi, globalOpts.owner, globalOpts.repo);
      await processor.updateDeploymentSuccess(options.sha, options.environment);

      console.log('Success update completed successfully');
    } catch (error) {
      console.error('Success update failed:', error);
      exit(1);
    }
  });

const main = async () => {
  await program.parseAsync();
};

main().catch((err) => {
  console.error('Application error:', err);
  exit(1);
});
