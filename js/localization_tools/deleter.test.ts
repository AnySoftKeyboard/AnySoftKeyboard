import test from 'node:test';
import assert from 'node:assert';
import * as fs from 'fs';
import * as path from 'path';
import yaml from 'js-yaml';
import { deleteLocalizationFiles } from './deleter.js';

// Helper to create a file with content
function writeFile(filePath: string, content: string = 'test') {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
  fs.writeFileSync(filePath, content);
}

test.describe('deleteLocalizationFiles', () => {
  const tmp = fs.mkdtempSync(path.join(process.cwd(), 'deleter-test-'));
  const resDir = path.join(tmp, 'res');
  const valuesDir = path.join(resDir, 'values');
  const valuesEnDir = path.join(resDir, 'values-en');
  const valuesFrDir = path.join(resDir, 'values-fr');
  const stringsXml = 'strings.xml';

  // Setup mock structure
  writeFile(path.join(valuesDir, stringsXml));
  writeFile(path.join(valuesEnDir, stringsXml));
  writeFile(path.join(valuesFrDir, stringsXml));

  // Create mock crowdin yaml
  const crowdinConfig = {
    files: [{ source: `res/values/strings.xml` }],
  };
  const crowdinFile = path.join(tmp, 'crowdin.yaml');
  fs.writeFileSync(crowdinFile, yaml.dump(crowdinConfig));

  test('should delete all values-*/strings.xml except values/strings.xml', () => {
    // Precondition: all files exist
    assert.ok(fs.existsSync(path.join(valuesDir, stringsXml)));
    assert.ok(fs.existsSync(path.join(valuesEnDir, stringsXml)));
    assert.ok(fs.existsSync(path.join(valuesFrDir, stringsXml)));

    deleteLocalizationFiles(tmp, crowdinFile);

    // values/strings.xml should remain
    assert.ok(fs.existsSync(path.join(valuesDir, stringsXml)));
    // values-en/strings.xml and values-fr/strings.xml should be deleted
    assert.ok(!fs.existsSync(path.join(valuesEnDir, stringsXml)));
    assert.ok(!fs.existsSync(path.join(valuesFrDir, stringsXml)));
  });

  test.after(() => {
    fs.rmSync(tmp, { recursive: true, force: true });
  });
});
