import * as fs from 'fs';
import * as path from 'path';

interface ChangedString {
  file: string;
  id: string;
  value: string;
}

export const parseGitDiff = (diff: string): ChangedString[] => {
  // Input validation
  if (!diff) {
    return [];
  }

  const changedStrings: ChangedString[] = [];
  const lines = diff.split('\n');
  let currentFile = '';

  // More robust regex patterns
  const fileRegex = /^\+\+\+ (?:b\/)?(.+strings\.xml)$/;
  const lineNumRegex = /^@@ -\d+(?:,\d+)? \+(\d+)(?:,\d+)? @@/;
  // Handle multi-line strings, XML entities, and whitespace variations
  const stringResourceRegex = /^\s*<string\s+name\s*=\s*"([^"]+)"\s*>(.*?)<\/string>\s*$/;

  const parseStringResource = (line: string): { id: string; value: string } | null => {
    // Trim whitespace and normalize
    const trimmedLine = line.trim();
    const match = trimmedLine.match(stringResourceRegex);
    if (match) {
      return {
        id: match[1],
        value: match[2].trim(),
      };
    }
    return null;
  };

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];

    // Skip empty lines
    if (!line) {
      continue;
    }

    // Check for file header
    const fileMatch = line.match(fileRegex);
    if (fileMatch) {
      currentFile = fileMatch[1];
      continue;
    }

    // Skip lines until we find a file
    if (!currentFile) {
      continue;
    }

    // Check for hunk header (we still need to detect these to skip them)
    const lineNumMatch = line.match(lineNumRegex);
    if (lineNumMatch) {
      continue;
    }

    // Process diff lines
    if (line.startsWith('+')) {
      // Added line in new file
      const parsedString = parseStringResource(line.substring(1));
      if (parsedString) {
        changedStrings.push({
          file: currentFile,
          ...parsedString,
        });
      }
    } else if (line.startsWith('-')) {
      // Removed line from old file - ignore
      continue;
    } else if (line.startsWith(' ')) {
      // Context line (unchanged) - ignore
      continue;
    } else if (line.startsWith('\\')) {
      // No newline at end of file marker - ignore
      continue;
    } else {
      // Any other line (like diff metadata) - ignore
      continue;
    }
  }

  return changedStrings;
};

export const getDefaultStringValue = (repoRoot: string, changedFilePath: string, stringId: string): string | null => {
  const resFolder = path.dirname(path.dirname(changedFilePath));
  const defaultStringsFile = path.join(resFolder, 'values', 'strings.xml');
  const fullPath = path.join(repoRoot, defaultStringsFile);

  if (!fs.existsSync(fullPath)) {
    console.error(
      `Error while getDefaultStringValue. Could not find ${fullPath} for ${changedFilePath} and string id ${stringId}.`,
    );
    return null;
  }

  const content = fs.readFileSync(fullPath, 'utf-8');

  // Use regex that matches across lines
  const stringResourceRegex = new RegExp(`^\\s*<string\\s+name\\s*=\\s*"${stringId}"\\s*>([\\s\\S]*?)<\\/string>`, 'm');
  const match = content.match(stringResourceRegex);
  if (match) {
    return match[1].trim();
  }
  console.error(
    `Error while getDefaultStringValue. Could not find string id ${stringId} in ${fullPath} for ${changedFilePath}.`,
  );
  return null;
};
