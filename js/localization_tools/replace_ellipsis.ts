import fs from 'fs';
import { locateStringResourcesFolders } from './utils.js';

/**
 * Replace all occurrences of '...' with '…' in the given file.
 */
function replaceEllipsisInFile(filePath: string): boolean {
  const content = fs.readFileSync(filePath, 'utf8');
  const replaced = content.replace(/\.\.\./g, '…');
  if (content !== replaced) {
    fs.writeFileSync(filePath, replaced, 'utf8');
    return true;
  } else {
    return false;
  }
}

// Main execution
export const replaceEllipsis = (repoRoot: string, crowdinFile: string): void => {
  locateStringResourcesFolders(repoRoot, crowdinFile)
    .map((filePath) => [filePath, replaceEllipsisInFile(filePath)])
    .filter((pair) => pair[1])
    .forEach((updatedPair) => console.log(`Updated ${updatedPair[0]}`));
};
