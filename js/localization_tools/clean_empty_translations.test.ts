import { describe, it, afterEach } from 'node:test';
import * as assert from 'node:assert';
import mock from 'mock-fs';
import * as fs from 'fs';
import { cleanEmptyTranslations } from './clean_empty_translations.js';

describe('cleanEmptyTranslations', () => {
  afterEach(() => {
    mock.restore();
  });

  it('should remove empty translations when default value is not empty', () => {
    mock({
      'values/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="test_string">Default Value</string>
</resources>`,
      'values-fr/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="test_string"></string>
    <string name="other_string">Valid</string>
</resources>`,
    });

    const diff = `+++ b/values-fr/strings.xml
@@ -1,1 +1,1 @@
+<string name="test_string"></string>`;

    cleanEmptyTranslations('.', diff);

    const content = fs.readFileSync('values-fr/strings.xml', 'utf-8');
    assert.ok(!content.includes('test_string'), 'Should have removed test_string');
    assert.ok(content.includes('other_string'), 'Should have kept other_string');
  });

  it('should NOT remove empty translations when default value IS empty', () => {
    mock({
      'values/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="empty_default"></string>
</resources>`,
      'values-fr/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="empty_default"></string>
</resources>`,
    });

    const diff = `+++ b/values-fr/strings.xml
@@ -1,1 +1,1 @@
+<string name="empty_default"></string>`;

    cleanEmptyTranslations('.', diff);

    const content = fs.readFileSync('values-fr/strings.xml', 'utf-8');
    assert.ok(content.includes('empty_default'), 'Should have kept empty_default');
  });

  it('should NOT remove non-empty translations', () => {
    mock({
      'values/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="test_string">Default Value</string>
</resources>`,
      'values-fr/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="test_string">Translated</string>
</resources>`,
    });

    const diff = `+++ b/values-fr/strings.xml
@@ -1,1 +1,1 @@
+<string name="test_string">Translated</string>`;

    cleanEmptyTranslations('.', diff);

    const content = fs.readFileSync('values-fr/strings.xml', 'utf-8');
    assert.ok(content.includes('test_string'), 'Should have kept test_string');
  });

  it('should handle multiple files', () => {
    mock({
      'values/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="s1">V1</string>
    <string name="s2">V2</string>
</resources>`,
      'values-fr/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="s1"></string>
</resources>`,
      'values-es/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="s2"></string>
</resources>`,
    });

    const diff = `+++ b/values-fr/strings.xml
@@ -1,1 +1,1 @@
+<string name="s1"></string>
+++ b/values-es/strings.xml
@@ -1,1 +1,1 @@
+<string name="s2"></string>`;

    cleanEmptyTranslations('.', diff);

    const frContent = fs.readFileSync('values-fr/strings.xml', 'utf-8');
    assert.ok(!frContent.includes('s1'), 'Should have removed s1 from fr');

    const esContent = fs.readFileSync('values-es/strings.xml', 'utf-8');
    assert.ok(!esContent.includes('s2'), 'Should have removed s2 from es');
  });

  it('should handle multiline default value', () => {
    mock({
      'values/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="multiline">
        Line 1
        Line 2
    </string>
</resources>`,
      'values-fr/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="multiline"></string>
</resources>`,
    });

    const diff = `+++ b/values-fr/strings.xml
@@ -1,1 +1,1 @@
+<string name="multiline"></string>`;

    cleanEmptyTranslations('.', diff);

    const content = fs.readFileSync('values-fr/strings.xml', 'utf-8');
    assert.ok(!content.includes('multiline'), 'Should have removed multiline string even if default is multiline');
  });

  it('should handle missing default file gracefully', () => {
    mock({
      'values-fr/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="test_string"></string>
</resources>`,
    });

    const diff = `+++ b/values-fr/strings.xml
@@ -1,1 +1,1 @@
+<string name="test_string"></string>`;

    // Should not throw
    cleanEmptyTranslations('.', diff);

    const content = fs.readFileSync('values-fr/strings.xml', 'utf-8');
    assert.ok(content.includes('test_string'), 'Should have kept test_string because default file is missing');
  });

  it('should preserve indentation of subsequent lines', () => {
    mock({
      'values/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="s1">V1</string>
    <string name="s2">V2</string>
</resources>`,
      'values-fr/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="s1"></string>
    <string name="s2">V2</string>
</resources>`,
    });

    const diff = `+++ b/values-fr/strings.xml
@@ -1,1 +1,1 @@
+<string name="s1"></string>`;

    cleanEmptyTranslations('.', diff);

    const content = fs.readFileSync('values-fr/strings.xml', 'utf-8');
    // Check that s2 is still indented correctly (4 spaces)
    // The previous regex might have consumed the indentation of s2
    assert.ok(content.includes('\n    <string name="s2">'), 'Should preserve indentation of s2');
  });

  it('should handle missing target file gracefully', () => {
    mock({
      'values/strings.xml': `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="test_string">Val</string>
</resources>`,
    });

    const diff = `+++ b/values-fr/strings.xml
@@ -1,1 +1,1 @@
+<string name="test_string"></string>`;

    // Should not throw
    cleanEmptyTranslations('.', diff);
  });
});
