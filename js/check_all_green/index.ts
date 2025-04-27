import { Command } from 'commander';
import { checkJobStatuses } from './green_checker.js';
import { exit } from 'process';
import * as fs from 'fs';

const program = new Command();

program.requiredOption('--github_event_file <path_to_file>', 'Path to json file containing the github event');

const main = async () => {
  program.parse();
  const opts = program.opts();

  const jsonContent = fs.readFileSync(opts.github_event_file, 'utf8');
  if (!checkJobStatuses(jsonContent)) {
    throw new Error("not all checks passed!")
  }
};

main().catch((err) => {
  console.error(err);
  exit(1);
});
