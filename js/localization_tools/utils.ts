import * as fs from 'fs';
import * as yaml from 'js-yaml';
import * as path from 'path';

// Minimal crowdin yaml structure

interface CrowdinConfig {
  files: { source: string }[];
}

const isValidStringResourceFolderName = (folderName: string): boolean => {
  return (
    folderName === 'values' ||
    // Exclude values-vXX directories (as in API levels)
    // Match values-xx or values-xx-rYY)
    folderName.match(/^values-([a-z]{2,3}(-r[A-Z]+)?)$/) !== null
  );
};

export const locateStringResourcesFoldersInRes = (resFolder: string): string[] => {
  return fs
    .readdirSync(resFolder, { withFileTypes: true })
    .filter((dirent) => dirent.isDirectory())
    .filter((dirent) => isValidStringResourceFolderName(dirent.name))
    .map((dirent) => path.join(dirent.parentPath, dirent.name, 'strings.xml'))
    .filter(fs.existsSync);
};

export const locateStringResourcesFolders = (repoRoot: string, crowdinFile: string): string[] => {
  const fileContent = fs.readFileSync(crowdinFile, 'utf8');
  const data = yaml.load(fileContent) as CrowdinConfig;

  if (data && data.files && Array.isArray(data.files)) {
    return data.files
      .map((file: { source: string }) => file.source)
      .map((sourcePath) => path.dirname(path.dirname(path.join(repoRoot, sourcePath))))
      .map(locateStringResourcesFoldersInRes)
      .reduce((accumulator, currentArray) => {
        return accumulator.concat(currentArray);
      }, []);
  } else {
    throw new Error(`Failed to read valid values (or folders) from ${crowdinFile}!`);
  }
};
