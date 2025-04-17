import { UniqueAddOnIdChecker } from './unique_addon_id.js';
import type { Checker } from './checker.js';
import { Command } from 'commander';
import { exit } from 'process';

function skip_predicate(dirname: string): boolean {
  const lowerCase = dirname.toLowerCase();
  return (
    lowerCase.indexOf('build') >= 0 ||
    lowerCase.indexOf('.git') >= 0 ||
    lowerCase.indexOf('node_modules') >= 0 ||
    lowerCase.indexOf('bazel-') >= 0
  );
}

const program = new Command();
program.name('checkers').description('CLI to run various checkers on the codebase.').version('0.0.1');
program.requiredOption('--root_dir <path>', 'Path to the root of the repo.').action(async (options) => {
  const checkers: Checker[] = [new UniqueAddOnIdChecker()];

  checkers.forEach((checker) => {
    console.log(`Running checker ${checker.name}`);
    checker.check(options.root_dir, skip_predicate);
  });
});

const main = async () => {
  program.parse();
};

main().catch((_) => exit(-1));
