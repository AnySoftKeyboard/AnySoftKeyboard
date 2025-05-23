import test from 'node:test';
import assert from 'node:assert';
import * as fs from 'fs';
import * as path from 'path';
import { generateLocaleArrayXml } from './locales_generator.js';

test.describe('locales generation tests', () => {
  const tmpRoot = fs.mkdtempSync('locales-test-');
  const tmp = path.join(tmpRoot, 'res');
  fs.mkdirSync(tmp);

  function createLocaleDirs(basePath: string, dirs: string[]) {
    dirs.forEach((dir) => {
      fs.mkdirSync(path.join(basePath, dir));
      fs.writeFileSync(path.join(basePath, dir, 'strings.xml'), 'blah');
    });
  }

  test('generateLocaleArrayXml generates correct XML for locales', () => {
    const localeDirs = [
      'values-en',
      'values-fr',
      'values-es',
      'values-ru',
      'values-v21', // should be ignored
      'values-en-rUS',
      'values-pt-rBR',
      'values', // should be ignored
      'not-a-locale', // should be ignored
    ];
    createLocaleDirs(tmp, localeDirs);
    const outputFile = path.join(tmp, 'output.xml');

    generateLocaleArrayXml(tmp, outputFile);

    const xml = fs.readFileSync(outputFile, 'utf8');
    
    // Extract all items into an array
    const items = [];
    const itemRegex = /<item>(.*?)<\/item>/g;
    let match;
    while ((match = itemRegex.exec(xml)) !== null) {
      items.push(match[1]);
    }

    // Define the expected order and content of locales
    const expectedLocales = ["System", "en", "en-US", "es", "fr", "pt", "pt-BR", "ru"];
    
    // Assert that the extracted items match the expected locales
    assert.deepStrictEqual(items, expectedLocales, "Locales are not in the expected order or content");

    // These checks are implicitly covered by deepStrictEqual, but it's good to be explicit about what should NOT be there.
    assert.doesNotMatch(xml, /v21/, "Should not contain v21");
    assert.doesNotMatch(xml, /not-a-locale/, "Should not contain not-a-locale");
    assert.doesNotMatch(xml, /<item>values<\/item>/, "Should not contain 'values' as an item");
  });

  test.after(() => {
    // Cleanup temp files and dirs
    fs.rmSync(tmpRoot, { recursive: true, force: true });
  });
});
