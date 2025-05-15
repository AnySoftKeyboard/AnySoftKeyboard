import * as fs from 'fs';
import * as yaml from 'js-yaml';
import * as path from 'path';

// Minimal crowdin yaml structure

interface CrowdinConfig {
  files: { source: string }[];
}

export const deleteLocalizationFiles = (repoRoot: string, crowdinFile: string): void => {
  const fileContent = fs.readFileSync(crowdinFile, 'utf8');
  const data = yaml.load(fileContent) as CrowdinConfig;

  if (data && data.files && Array.isArray(data.files)) {
    const sourceFiles: string[] = data.files.map((file: { source: string }) => file.source);

    sourceFiles.forEach((sourcePath) => {
      // sourcePath is the default path for strings.xml
      console.info(`Deleting translations for ${sourcePath}...`);
      // What we want to delete is all the other strings.xml files in the sibling folders.
      // For example: /ime/app/src/main/res/values/strings.xml -> /ime/app/src/main/res/values-*/strings.xml
      const folderPath = path.dirname(path.dirname(path.join(repoRoot, sourcePath)));
      fs.readdirSync(folderPath, { withFileTypes: true })
        .filter((dirent) => dirent.isDirectory())
        .map((dirent) => dirent.name)
        .filter((folder) => folder.match(/^values-.*$/))
        .forEach((folder) => {
          const localizedStringsPath = path.join(folderPath, folder, 'strings.xml');
          fs.unlinkSync(localizedStringsPath);
          console.log(`Deleted: ${localizedStringsPath}`);
        });
    });
  }
};
