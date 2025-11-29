import { Command } from 'commander';
import { deleteLocalizationFiles } from './deleter.js';
import { generateLocaleArrayXml } from './locales_generator.js';
import { exit } from 'process';
import { replaceEllipsis } from './replace_ellipsis.js';
import * as fs from 'fs';
import { generateXmlReport, parseGitDiff } from './diff_parser.js';
import { cleanEmptyTranslations } from './clean_empty_translations.js';

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

program
  .command('diffReport')
  .option('-i, --input <inputFile>', 'Input file path', '-')
  .option('-o, --output <outputFile>', 'Output file path', 'string_changes.xml')
  .action((options) => {
    const workspaceDir = process.env.BUILD_WORKSPACE_DIRECTORY || process.cwd();
    const diff = fs.readFileSync(options.input === '-' ? 0 : options.input, 'utf-8');
    const changedStrings = parseGitDiff(diff);
    const xmlReport = generateXmlReport(workspaceDir, changedStrings);
    fs.writeFileSync(options.output, xmlReport, 'utf-8');
    console.log(`XML report generated at ${options.output}`);
  });

program
  .command('cleanEmptyTranslations')
  .requiredOption('--diff-file <diffFile>', 'Path to the diff file')
  .action((options) => {
    const workspaceDir = process.env.BUILD_WORKSPACE_DIRECTORY || process.cwd();
    const diff = fs.readFileSync(options.diffFile, 'utf-8');
    cleanEmptyTranslations(workspaceDir, diff);
  });
const main = async () => {
  program.parse();
};

main().catch((_) => exit(-1));
