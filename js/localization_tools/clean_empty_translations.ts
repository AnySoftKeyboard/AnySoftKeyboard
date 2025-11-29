import * as fs from 'fs';
import * as path from 'path';
import { parseGitDiff, getDefaultStringValue } from './diff_parser.js';

export const cleanEmptyTranslations = (repoRoot: string, diff: string): void => {
  const changedStrings = parseGitDiff(diff);

  // Group by file to minimize file I/O
  const changesByFile = new Map<string, string[]>();

  for (const change of changedStrings) {
    // Check if the new value is empty
    if (change.value.trim() === '') {
      // Check if the default value is NOT empty
      const defaultValue = getDefaultStringValue(repoRoot, change.file, change.id);
      if (defaultValue && defaultValue.trim() !== '') {
        console.log(
          `Found invalid empty translation for ${change.id} in ${change.file}. Default value: "${defaultValue}"`,
        );
        if (!changesByFile.has(change.file)) {
          changesByFile.set(change.file, []);
        }
        changesByFile.get(change.file)!.push(change.id);
      }
    }
  }

  // Process files
  for (const [filePath, idsToRemove] of changesByFile.entries()) {
    const fullPath = path.join(repoRoot, filePath);
    if (!fs.existsSync(fullPath)) {
      console.warn(`File not found: ${fullPath}`);
      continue;
    }

    let content = fs.readFileSync(fullPath, 'utf-8');
    const originalContent = content;

    for (const id of idsToRemove) {
      // Regex to find the string element with this name
      // <string name="id">...</string>
      // We need to match across multiple lines.
      // [\s\S]*? matches any character including newlines, non-greedily.
      const safeId = id.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      // We use \r?\n to ensure we consume the newline at the end of the string element.
      // This prevents \s* from consuming the indentation of the next line.
      const regex = new RegExp(`^\\s*<string\\s+name\\s*=\\s*"${safeId}"\\s*>([\\s\\S]*?)<\\/string>\\s*\\r?\\n`, 'gm');
      content = content.replace(regex, '');
    }

    if (content !== originalContent) {
      fs.writeFileSync(fullPath, content, 'utf-8');
      console.log(`Updated ${filePath}, removed ${idsToRemove.length} invalid empty strings.`);
    }
  }
};
