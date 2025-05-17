import { Command } from 'commander';
import { deleteLocalizationFiles } from './deleter.js';
import { generateLocaleArrayXml } from './locales_generator.js';
import { exit } from 'process';

const program = new Command();
program.name('localization_tools').description('CLI for various localization tools').version('0.0.1');

program
  .command('delete')
  .requiredOption('--crowdinFile <path>', 'Path to crowdin config file')
  .action(async (options) => {
    console.log('Deleting localization files...');
    const workspaceDir = process.env.BUILD_WORKSPACE_DIRECTORY || process.cwd();
    deleteLocalizationFiles(workspaceDir, options.crowdinFile);
  });

program
  .command('generateLocale')
  .requiredOption('--resourcesFolder <path>', 'Path to res folder')
  .requiredOption('--targetFile <path>', 'Path to locales xml file')
  .action(async (options) => {
    console.log('Generating locales array...');
    generateLocaleArrayXml(options.resourcesFolder, options.targetFile);
  });

const main = async () => {
  program.parse();
};

main().catch((_) => exit(-1));
