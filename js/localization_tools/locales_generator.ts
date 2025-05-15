import * as fs from 'fs';

export const generateLocaleArrayXml = (resPath: string, outputFile: string): void => {
  console.log(`Will read locals from ${resPath}...`);
  const localeEntries: string[] = fs
    .readdirSync(resPath, { withFileTypes: true })
    .filter((dirent) => dirent.isDirectory())
    .map((dirent) => dirent.name)
    .filter((folderName) => folderName.match(/^values-v\d+$/) === null) // Exclude values-vXX directories (as in API levels)
    .map((folderName) => folderName.match(/^values-(\w{2,3}(-r\w+)?)$/)) // Match values-xx or values-xx-rYY
    .filter((match) => match !== null)
    .map((match) => match[1].replace('-r', '-'))
    .filter((locale) => locale !== '') // Remove any empty strings just in case
    .sort();

  const xmlContent = `<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools" tools:ignore="MissingTranslation">
    <!-- DO NOT TRANSLATE - this is auto generated -->

    <string name="settings_key_force_locale">settings_key_force_locale</string>
    <!-- this should hold the locales the app is translated to, and any addon-->
    <!-- https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/res/res/values/locale_config.xml -->
    <string-array name="settings_key_force_locale_values">
${localeEntries.map((entry) => `        <item>${entry}</item>`).join('\n')}
    </string-array>
</resources>
`;

  fs.writeFileSync(outputFile, xmlContent, 'utf8');
  console.log(`Generated ${outputFile} with ${localeEntries.length} locales.`);
};
