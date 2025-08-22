import { Command } from 'commander';
import { TranslationVerifier } from './translation_verification.js';
import { AiCodeReviewer } from './ai_code_review.js';
import { exit } from 'process';
import * as fs from 'fs';
import { execSync } from 'child_process';

const program = new Command();
program.name('ai_tools').description('CLI for AI-powered tools using LangChain and Gemini').version('0.0.1');

program
  .command('translationsVerification')
  .requiredOption('--gemini-api-key <key>', 'Gemini API key')
  .requiredOption('--diff-file <path>', 'file containing the translations diff')
  .option('--output-file <path>', 'file path to write the LLM response (optional)')
  .description('Use LLM to verify translations')
  .action(async (options) => {
    try {
      // Validate file exists
      if (!fs.existsSync(options.diffFile)) {
        console.error(`Error: File '${options.diffFile}' does not exist`);
        exit(1);
      }

      console.log('Initializing model...');
      const verifier = new TranslationVerifier(options.geminiApiKey);

      console.log(`Reading translation file: ${options.diffFile}`);
      const diff = fs.readFileSync(options.diffFile, 'utf-8');

      console.log('Verifying translations...');
      const response = await verifier.verify(diff);

      // Write to output file if specified
      if (options.outputFile) {
        try {
          fs.writeFileSync(options.outputFile, response, 'utf-8');
          console.log(`\nReport written to: ${options.outputFile}`);
        } catch (writeError) {
          console.error(
            `Error writing to output file: ${writeError instanceof Error ? writeError.message : writeError}`,
          );
          exit(1);
        }
      }

      console.log('\n=== Translation Verification Report ===');
      console.log(response);
    } catch (error) {
      console.error('Error:', error instanceof Error ? error.message : error);
      exit(1);
    }
  });

program
  .command('codeReview')
  .requiredOption('--gemini-api-key <key>', 'Gemini API key')
  .requiredOption('--base-sha <base_sha>', 'Base SHA of the changes')
  .requiredOption('--head-sha <head_sha>', 'Last commit of the changes')
  .option('--output-file <path>', 'file path to write the LLM response (optional)')
  .description('Use LLM code review a git diff')
  .action(async (options) => {
    try {
      console.log('Creating git diff...');

      // Create git diff from base-sha to head-sha
      const diff = execSync(`git diff ${options.baseSha} ${options.headSha}`, { encoding: 'utf-8' });

      console.log(`Git diff created from ${options.baseSha} to ${options.headSha}`);
      console.log(`Diff length: ${diff.length} characters`);

      const reviewer = new AiCodeReviewer(options.geminiApiKey);
      const report = await reviewer.review(diff);
      console.info(report);
    } catch (error) {
      console.error('Error:', error instanceof Error ? error.message : error);
      exit(1);
    }
  });
const main = async () => {
  program.parse();
};

main().catch((_) => exit(-1));
