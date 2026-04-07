import { Command } from 'commander';
import { TranslationVerifier } from './translation_verification.js';
import { exit } from 'process';
import * as fs from 'fs';

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

const main = async () => {
  program.parse();
};

main().catch((_) => exit(-1));
