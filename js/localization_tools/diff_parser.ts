import { execSync } from 'child_process';
import * as fs from 'fs';
import * as path from 'path';
import { locateStringResourcesFoldersInRes } from './utils';
import { create } from 'xmlbuilder2';

interface ChangedString {
  file: string;
  lineNumber: number;
  id: string;
  value: string;
}

export const getGitDiff = (): string => {
  try {
    return execSync('git diff').toString();
  } catch (error) {
    console.error('Error getting git diff:', error);
    return '';
  }
};

export const parseGitDiff = (diff: string): ChangedString[] => {
  const changedStrings: ChangedString[] = [];
  const lines = diff.split('\n');
  let currentFile = '';
  let lineNumber = 0;

  const fileRegex = /^\+\+\+ b\/(.*strings.xml)$/;
  const lineNumRegex = /^@@ -\d+,\d+ \+(\d+),\d+ @@/;
  const stringResourceRegex = /<string name="([^"]+)">(.+)<\/string>/;

  const parseStringResource = (line: string): { id: string; value: string } | null => {
    const match = line.match(stringResourceRegex);
    if (match) {
      return {
        id: match[1],
        value: match[2],
      };
    }
    return null;
  };

  for (const line of lines) {
    const fileMatch = line.match(fileRegex);
    if (fileMatch) {
      currentFile = fileMatch[1];
      continue;
    }

    if (!currentFile) {
      continue;
    }

    const lineNumMatch = line.match(lineNumRegex);
    if (lineNumMatch) {
      lineNumber = parseInt(lineNumMatch[1], 10);
      continue;
    }

    if (line.startsWith('+')) {
      const parsedString = parseStringResource(line.substring(1));
      if (parsedString) {
        changedStrings.push({
          file: currentFile,
          lineNumber: lineNumber,
          ...parsedString,
        });
      }
      lineNumber++;
    } else if (!line.startsWith('-')) {
      lineNumber++;
    }
  }

  return changedStrings;
};

const getDefaultStringValue = (changedFilePath: string, stringId: string): string | null => {
  const resFolder = path.dirname(path.dirname(changedFilePath));
  const defaultStringsFile = path.join(resFolder, 'values', 'strings.xml');

  if (!fs.existsSync(defaultStringsFile)) {
    return null;
  }

  const content = fs.readFileSync(defaultStringsFile, 'utf-8');
  const lines = content.split('\n');

  const stringResourceRegex = /<string name="([^"]+)">(.+)<\/string>/;

  for (const line of lines) {
    const match = line.match(stringResourceRegex);
    if (match && match[1] === stringId) {
      return match[2];
    }
  }

  return null;
};

const getLanguageFromFilePath = (filePath: string): string | null => {
  const match = filePath.match(/values-([a-z]{2,3}(-r[A-Z]+)?)\/strings.xml$/);
  if (match) {
    return match[1].replace('-r', '-');
  }
  return null;
};

export const generateXmlReport = (changedStrings: ChangedString[]): string => {
  const root = create({ version: '1.0' }).ele('changes');

  const changes = new Map<string, any>();

  for (const changedString of changedStrings) {
    const defaultText = getDefaultStringValue(changedString.file, changedString.id);
    if (defaultText) {
      if (!changes.has(changedString.id)) {
        const changeNode = root.ele('change', { id: changedString.id, default: defaultText });
        changes.set(changedString.id, changeNode);
      }

      const lang = getLanguageFromFilePath(changedString.file);
      if (lang) {
        changes.get(changedString.id).ele('translation', { lang: lang }).txt(changedString.value);
      }
    }
  }

  return root.end({ prettyPrint: true });
};
