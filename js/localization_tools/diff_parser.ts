import * as fs from 'fs';
import * as path from 'path';
import { Builder } from 'xml2js';

interface ChangedString {
  file: string;
  id: string;
  value: string;
}

interface ChangeItem {
  $: { id: string; default: string };
  translation: Array<{
    $: { localeCode: string; localeName: string };
    _: string;
  }>;
}

// Get human-readable locale name using Intl.DisplayNames
const getLocaleName = (localeCode: string): string => {
  try {
    // Try to get the language name
    const languageName = new Intl.DisplayNames(['en'], { type: 'language' }).of(localeCode);

    // If it's a regional variant, try to get the region name too
    if (localeCode.includes('-')) {
      const region = localeCode.split('-')[1];
      const regionName = new Intl.DisplayNames(['en'], { type: 'region' }).of(region);

      // Only add region if it's not already included in the language name
      if (regionName && regionName !== region && languageName && !languageName.includes(regionName)) {
        return `${languageName} (${regionName})`;
      }
    }

    return languageName || localeCode;
  } catch (error) {
    // Fallback to the original locale code if Intl.DisplayNames fails
    console.warn(`Error while getLocaleName for '${localeCode}': ${error}`);
    return localeCode;
  }
};

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

export const getLanguageFromFilePath = (filePath: string): string | null => {
  // Handle various Android resource folder patterns
  const match = filePath.match(/values-([a-z]{2,3}(?:-[A-Z]{2})?(?:-r[A-Za-z]+)?)\/strings\.xml$/);
  if (match) {
    return match[1].replace('-r', '-');
  }
  return null;
};

export const generateXmlReport = (repoRoot: string, changedStrings: ChangedString[]): string => {
  const builder = new Builder({
    headless: false, // Include XML declaration
    renderOpts: { pretty: true, indent: '  ' },
    xmldec: { version: '1.0' }, // Only include version, not encoding or standalone
  });

  const changes: { [key: string]: ChangeItem } = {};

  for (const changedString of changedStrings) {
    // Validate changedString structure
    if (!changedString || typeof changedString.id !== 'string' || typeof changedString.value !== 'string') {
      continue;
    }

    const defaultText = getDefaultStringValue(repoRoot, changedString.file, changedString.id);
    if (defaultText) {
      if (!changes[changedString.id]) {
        changes[changedString.id] = {
          $: { id: changedString.id, default: defaultText },
          translation: [],
        };
      }

      const lang = getLanguageFromFilePath(changedString.file);
      if (lang) {
        const localeName = getLocaleName(lang);
        changes[changedString.id].translation.push({
          $: {
            localeCode: lang,
            localeName: localeName,
          },
          _: changedString.value,
        });
      }
    }
  }

  // Create the proper structure for xml2js
  const xmlObject = {
    changes: {
      change: Object.values(changes),
    },
  };

  return builder.buildObject(xmlObject);
};
