import { describe, it } from 'node:test';
import * as assert from 'node:assert';
import { parseGitDiff } from './diff_parser.js';

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
