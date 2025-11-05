import { describe, it } from 'node:test';
import * as assert from 'node:assert';
import mock from 'mock-fs';
import { parseGitDiff, getLanguageFromFilePath, generateXmlReport } from './diff_parser.js';

describe('parseGitDiff', () => {
  it('should handle empty input', () => {
    const result = parseGitDiff('');
    assert.deepStrictEqual(result, []);
  });

  it('should handle diff variations without b/ prefix', () => {
    const diff = `--- a/values-fr/strings.xml
+++ values-fr/strings.xml
@@ -1,1 +1,1 @@
-<string name="test">old</string>
+<string name="test">new</string>`;

    const result = parseGitDiff(diff);

    assert.strictEqual(result.length, 1);
    assert.strictEqual(result[0].file, 'values-fr/strings.xml');
    assert.strictEqual(result[0].id, 'test');
    assert.strictEqual(result[0].value, 'new');
  });

  it('should handle whitespace variations', () => {
    const diff = `--- a/values-fr/strings.xml
+++ b/values-fr/strings.xml
@@ -1,1 +1,1 @@
-<string name="test">old</string>
+    <string name="test" >new</string>`;

    const result = parseGitDiff(diff);

    assert.strictEqual(result.length, 1);
    assert.strictEqual(result[0].id, 'test');
    assert.strictEqual(result[0].value, 'new');
  });

  it('should parse multiple files in single diff', () => {
    const diff = `--- a/values-fr/strings.xml
+++ b/values-fr/strings.xml
@@ -1,1 +1,1 @@
-<string name="test">old</string>
+<string name="test">new</string>
--- a/values-es/strings.xml
+++ b/values-es/strings.xml
@@ -1,1 +1,1 @@
-<string name="test">viejo</string>
+<string name="test">nuevo</string>`;

    const result = parseGitDiff(diff);

    assert.strictEqual(result.length, 2);

    // Verify French change
    assert.strictEqual(result[0].file, 'values-fr/strings.xml');
    assert.strictEqual(result[0].id, 'test');
    assert.strictEqual(result[0].value, 'new');

    // Verify Spanish change
    assert.strictEqual(result[1].file, 'values-es/strings.xml');
    assert.strictEqual(result[1].id, 'test');
    assert.strictEqual(result[1].value, 'nuevo');
  });

  it('should ignore invalid XML content', () => {
    const diff = `--- a/values-fr/strings.xml
+++ b/values-fr/strings.xml
@@ -1,2 +1,2 @@
-<string name="test">old</string>
+<string name="test">new</string>
+<invalid>not a string</invalid>`;

    const result = parseGitDiff(diff);

    assert.strictEqual(result.length, 1);
    assert.strictEqual(result[0].id, 'test');
    assert.strictEqual(result[0].value, 'new');
  });

  it('should handle complex string values with special characters', () => {
    const diff = `--- a/values-fr/strings.xml
+++ b/values-fr/strings.xml
@@ -1,1 +1,1 @@
-<string name="test">old</string>
+<string name="test">new &amp; improved</string>`;

    const result = parseGitDiff(diff);

    assert.strictEqual(result.length, 1);
    assert.strictEqual(result[0].id, 'test');
    assert.strictEqual(result[0].value, 'new &amp; improved');
  });

  it('should handle malformed input gracefully', () => {
    const diff = `not a valid git diff
random content
no file headers`;

    const result = parseGitDiff(diff);

    assert.deepStrictEqual(result, []);
  });
});

describe('getLanguageFromFilePath', () => {
  it('should extract basic language codes', () => {
    assert.strictEqual(getLanguageFromFilePath('values-fr/strings.xml'), 'fr');
    assert.strictEqual(getLanguageFromFilePath('values-es/strings.xml'), 'es');
    assert.strictEqual(getLanguageFromFilePath('values-de/strings.xml'), 'de');
    assert.strictEqual(getLanguageFromFilePath('values-it/strings.xml'), 'it');
    assert.strictEqual(getLanguageFromFilePath('values-ja/strings.xml'), 'ja');
  });

  it('should extract 3-letter language codes', () => {
    assert.strictEqual(getLanguageFromFilePath('values-chi/strings.xml'), 'chi');
    assert.strictEqual(getLanguageFromFilePath('values-hin/strings.xml'), 'hin');
    assert.strictEqual(getLanguageFromFilePath('values-eng/strings.xml'), 'eng');
  });

  it('should handle regional variants with r prefix', () => {
    assert.strictEqual(getLanguageFromFilePath('values-en-rUS/strings.xml'), 'en-US');
    assert.strictEqual(getLanguageFromFilePath('values-pt-rBR/strings.xml'), 'pt-BR');
    assert.strictEqual(getLanguageFromFilePath('values-zh-rCN/strings.xml'), 'zh-CN');
    assert.strictEqual(getLanguageFromFilePath('values-zh-rTW/strings.xml'), 'zh-TW');
    assert.strictEqual(getLanguageFromFilePath('values-sr-rLatn/strings.xml'), 'sr-Latn');
  });

  it('should handle language with country codes without r prefix', () => {
    assert.strictEqual(getLanguageFromFilePath('values-en-US/strings.xml'), 'en-US');
    assert.strictEqual(getLanguageFromFilePath('values-pt-BR/strings.xml'), 'pt-BR');
    assert.strictEqual(getLanguageFromFilePath('values-es-MX/strings.xml'), 'es-MX');
  });

  it('should return null for invalid paths', () => {
    // Default values folder (no language)
    assert.strictEqual(getLanguageFromFilePath('values/strings.xml'), null);

    // Non-strings.xml files
    assert.strictEqual(getLanguageFromFilePath('values-fr/colors.xml'), null);
    assert.strictEqual(getLanguageFromFilePath('values-fr/dimens.xml'), null);

    // Invalid folder patterns
    assert.strictEqual(getLanguageFromFilePath('invalid-fr/strings.xml'), null);
    assert.strictEqual(getLanguageFromFilePath('values-/strings.xml'), null);
    assert.strictEqual(getLanguageFromFilePath('values-123/strings.xml'), null);

    // Empty input
    assert.strictEqual(getLanguageFromFilePath(''), null);

    // Invalid language code patterns
    assert.strictEqual(getLanguageFromFilePath('values-F/strings.xml'), null); // Too short
    assert.strictEqual(getLanguageFromFilePath('values-FREN/strings.xml'), null); // Too long without country
  });

  it('should handle full file paths', () => {
    assert.strictEqual(getLanguageFromFilePath('app/src/main/res/values-fr/strings.xml'), 'fr');
    assert.strictEqual(getLanguageFromFilePath('/project/android/app/src/main/res/values-es-ES/strings.xml'), 'es-ES');
    assert.strictEqual(getLanguageFromFilePath('ime/app/src/main/res/values-zh-rCN/strings.xml'), 'zh-CN');
    assert.strictEqual(getLanguageFromFilePath('path/to/project/res/values-pt-rBR/strings.xml'), 'pt-BR');
  });

  it('should handle real-world Android locale examples', () => {
    // Common Android locales
    assert.strictEqual(getLanguageFromFilePath('values-ar/strings.xml'), 'ar'); // Arabic
    assert.strictEqual(getLanguageFromFilePath('values-iw/strings.xml'), 'iw'); // Hebrew (legacy code)
    assert.strictEqual(getLanguageFromFilePath('values-in/strings.xml'), 'in'); // Indonesian (legacy code)

    // Chinese variants
    assert.strictEqual(getLanguageFromFilePath('values-zh-rCN/strings.xml'), 'zh-CN'); // Chinese Simplified
    assert.strictEqual(getLanguageFromFilePath('values-zh-rTW/strings.xml'), 'zh-TW'); // Chinese Traditional
    assert.strictEqual(getLanguageFromFilePath('values-zh-rHK/strings.xml'), 'zh-HK'); // Chinese Hong Kong

    // Portuguese variants
    assert.strictEqual(getLanguageFromFilePath('values-pt/strings.xml'), 'pt'); // Portuguese
    assert.strictEqual(getLanguageFromFilePath('values-pt-rBR/strings.xml'), 'pt-BR'); // Portuguese Brazil
    assert.strictEqual(getLanguageFromFilePath('values-pt-rPT/strings.xml'), 'pt-PT'); // Portuguese Portugal

    // Spanish variants
    assert.strictEqual(getLanguageFromFilePath('values-es/strings.xml'), 'es'); // Spanish
    assert.strictEqual(getLanguageFromFilePath('values-es-rMX/strings.xml'), 'es-MX'); // Spanish Mexico
    assert.strictEqual(getLanguageFromFilePath('values-es-rES/strings.xml'), 'es-ES'); // Spanish Spain

    // Serbian script variants
    assert.strictEqual(getLanguageFromFilePath('values-sr-rLatn/strings.xml'), 'sr-Latn'); // Serbian Latin
    assert.strictEqual(getLanguageFromFilePath('values-sr-rCyrl/strings.xml'), 'sr-Cyrl'); // Serbian Cyrillic

    // Other common locales
    assert.strictEqual(getLanguageFromFilePath('values-ko/strings.xml'), 'ko'); // Korean
    assert.strictEqual(getLanguageFromFilePath('values-th/strings.xml'), 'th'); // Thai
    assert.strictEqual(getLanguageFromFilePath('values-vi/strings.xml'), 'vi'); // Vietnamese
    assert.strictEqual(getLanguageFromFilePath('values-hi/strings.xml'), 'hi'); // Hindi
    assert.strictEqual(getLanguageFromFilePath('values-ru/strings.xml'), 'ru'); // Russian
  });
});

describe('generateXmlReport', () => {
  it('should handle empty input array', () => {
    const result = generateXmlReport('.', []);
    const expected = '<?xml version="1.0"?>\n<changes/>';
    assert.strictEqual(result, expected);
  });

  it('should generate valid XML structure', () => {
    // Mock the default strings.xml file
    mock({
      'values/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="test">Default Test Value</string>
</resources>`,
    });

    const changedStrings = [{ file: 'values-fr/strings.xml', id: 'test', value: 'Test Value' }];

    const result = generateXmlReport('.', changedStrings);

    // Should start with XML declaration
    assert.ok(result.startsWith('<?xml version="1.0"?>'));

    // Should have proper root element
    assert.ok(result.includes('<changes>') || result.includes('<changes/>'));
    assert.ok(result.includes('</changes>') || result.includes('<changes/>'));

    mock.restore();
  });

  it('should handle multiple ChangedString objects', () => {
    // Mock the default strings.xml files for each locale
    mock({
      'values/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="hello">Hello</string>
    <string name="goodbye">Goodbye</string>
    <string name="welcome">Welcome</string>
</resources>`,
    });

    const changedStrings = [
      { file: 'values-fr/strings.xml', id: 'hello', value: 'Bonjour' },
      { file: 'values-es/strings.xml', id: 'goodbye', value: 'Adiós' },
      { file: 'values-de/strings.xml', id: 'welcome', value: 'Willkommen' },
    ];

    const result = generateXmlReport('.', changedStrings);

    // Should generate valid XML
    assert.ok(result.startsWith('<?xml version="1.0"?>'));
    assert.ok(result.includes('<changes'));
    // Handle both self-closing and regular closing tags
    assert.ok(result.includes('</changes>') || result.includes('<changes/>'));

    mock.restore();
  });

  it('should handle strings with special characters', () => {
    // Mock the default strings.xml file
    mock({
      'values/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="complex_string">Default complex string</string>
</resources>`,
    });

    const changedStrings = [
      {
        file: 'values-fr/strings.xml',
        id: 'complex_string',
        value: 'Bonjour &amp; au revoir <br/> avec des caractères spéciaux',
      },
    ];

    const result = generateXmlReport('.', changedStrings);

    // Should generate valid XML even with special characters
    assert.ok(result.startsWith('<?xml version="1.0"?>'));
    assert.ok(result.includes('<changes'));
    // Handle both self-closing and regular closing tags
    assert.ok(result.includes('</changes>') || result.includes('<changes/>'));

    mock.restore();
  });

  it('should include localeCode and localeName attributes in translation nodes', () => {
    // Mock the default strings.xml file with the correct path structure
    // The function expects: path.dirname(path.dirname(changedFilePath)) + '/values/strings.xml'
    // For 'res/values-eu-ES/strings.xml', it should look for 'res/values/strings.xml'
    mock({
      'res/values/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="test_string">Test Value</string>
</resources>`,
    });

    const changedStrings = [
      {
        file: 'res/values-eu-ES/strings.xml',
        id: 'test_string',
        value: 'Proba Balioa',
      },
    ];

    const result = generateXmlReport('.', changedStrings);

    // Should include the new attributes with correct values
    assert.ok(result.includes('localeCode="eu-ES"'), 'Should include localeCode attribute');
    assert.ok(result.includes('localeName="Basque (Spain)"'), 'Should include localeName attribute');

    // Should contain the translation text
    assert.ok(result.includes('Proba Balioa'), 'Should include the translation text');

    // Should have proper XML structure
    assert.ok(
      result.includes('<change id="test_string" default="Test Value">'),
      'Should include change node with default value',
    );

    // Clean up
    mock.restore();
  });
});
