import { Command } from 'commander';
import {
  Contributor,
  constructContributorsContext,
  generateMarkdownList,
  getContributors,
  sortContributors,
} from './contributors';
import { setFailed } from '@actions/core';
import { writeFileSync } from 'fs';

const program = new Command();
program.name('generate-contributors').description('CLI to generate contributors list from GitHub').version('0.0.1');

program
  .command('generate')
  .requiredOption('--api_token <token>', 'GitHub API token')
  .requiredOption('--output_file <path>', 'Path to output')
  .action(async (options) => {
    console.log(`Generating contributors list to ${options.output_file}.`);
    await getContributors(options.api_token)
      .then((contributors: Contributor[]) => {
        console.log(`Got ${contributors.length} contributors.`);
        return contributors;
      })
      .then(sortContributors)
      .then((contributors) => {
        return contributors.slice(0, 100);
      })
      .then(generateMarkdownList)
      .then((markdownTable) => {
        console.log(markdownTable);
        return markdownTable;
      })
      .then(constructContributorsContext)
      .then((content) =>
        writeFileSync(options.output_file, content, {
          encoding: 'utf-8',
          flag: 'w',
        }),
      );
  });

const main = async () => {
  program.parse();
};

main().catch((err) => setFailed(err.message));
