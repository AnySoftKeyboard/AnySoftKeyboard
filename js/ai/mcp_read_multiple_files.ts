import { DynamicTool } from '@langchain/core/tools';
import * as fs from 'fs';
import * as path from 'path';

export const createReadMultipleFilesTool = (): DynamicTool => {
  return new DynamicTool({
    name: 'read_multiple_files',
    description:
      'Read the content of multiple files. Input should be a comma-separated list of file paths relative to the repository root.',
    func: async (input: string) => {
      try {
        const filePaths = input.split(',').map((path) => path.trim());
        const results: string[] = [];

        for (const filePath of filePaths) {
          try {
            const fullPath = path.resolve(filePath);
            if (fs.existsSync(fullPath)) {
              const content = fs.readFileSync(fullPath, 'utf-8');
              results.push(`=== ${filePath} ===\n${content}\n`);
            } else {
              results.push(`=== ${filePath} ===\nFile not found: ${filePath}\n`);
            }
          } catch (error) {
            results.push(
              `=== ${filePath} ===\nError reading file: ${error instanceof Error ? error.message : error}\n`,
            );
          }
        }

        return results.join('\n');
      } catch (error) {
        return `Error processing file paths: ${error instanceof Error ? error.message : error}`;
      }
    },
  });
};
