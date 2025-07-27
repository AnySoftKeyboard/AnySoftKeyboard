import { describe, it, afterEach } from 'node:test';
import * as assert from 'node:assert';
import * as fs from 'fs';
import * as path from 'path';
import { fileURLToPath } from 'url';
import mock from 'mock-fs';
import { parseGitDiff, generateXmlReport } from './diff_parser.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

describe('diff_parser', () => {
  afterEach(() => {
    mock.restore();
  });

  it('should parse git diff and generate XML report', () => {
    const fakeDiff = fs.readFileSync(path.join(__dirname, 'fake_diff.txt'), 'utf-8');
    mock({
      'ime/app/src/main/res/values/strings.xml':
        '<?xml version="1.0" encoding="utf-8"?><resources><string name="test_string">default value</string></resources>',
      'fake_diff.txt': fakeDiff,
    });

    const changedStrings = parseGitDiff(fs.readFileSync('fake_diff.txt', 'utf-8'));
    const xmlReport = generateXmlReport(changedStrings);

    const expectedXml = `<?xml version="1.0"?>
<changes>
  <change id="test_string" default="default value">
    <translation lang="fr">new value</translation>
    <translation lang="es">valor nuevo</translation>
  </change>
</changes>
`;
    assert.strictEqual(xmlReport, expectedXml.trim());
  });
});
