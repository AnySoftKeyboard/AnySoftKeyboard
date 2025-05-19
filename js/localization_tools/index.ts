import { Command } from 'commander';
import { deleteLocalizationFiles } from './deleter.js';
import { generateLocaleArrayXml } from './locales_generator.js';
import { exit } from 'process';
import { replaceEllipsis } from './replace_ellipsis.js';

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

program
  .command('replaceEllipsis')
  .requiredOption('--crowdinFile <path>', 'Path to crowdin config file')
  .description('Replace ... with … in all strings.xml files')
  .action(async (options) => {
    console.log('Replacing ... with … in all strings.xml files...');
    const workspaceDir = process.env.BUILD_WORKSPACE_DIRECTORY || process.cwd();
    replaceEllipsis(workspaceDir, options.crowdinFile);
  });

const main = async () => {
  program.parse();
};

main().catch((_) => exit(-1));
