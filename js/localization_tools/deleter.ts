import * as fs from 'fs';
import { locateStringResourcesFolders } from './utils.js';

export const deleteLocalizationFiles = (repoRoot: string, crowdinFile: string): void => {
  locateStringResourcesFolders(repoRoot, crowdinFile)
    .filter((translationFile) => !translationFile.endsWith('/values/strings.xml')) // do not delete the source of translations
    .forEach((translationFile) => {
      fs.unlinkSync(translationFile);
      console.log(`Deleted: ${translationFile}`);
    });
};
