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
    // Check that the XML contains the expected locales
    assert.match(xml, /<item>en<\/item>/);
    assert.match(xml, /<item>fr<\/item>/);
    assert.match(xml, /<item>es<\/item>/);
    assert.match(xml, /<item>ru<\/item>/);
    assert.match(xml, /<item>en-US<\/item>/);
    assert.match(xml, /<item>pt-BR<\/item>/);
    // Should not contain v21, values, or not-a-locale
    assert.doesNotMatch(xml, /v21/);
    assert.doesNotMatch(xml, /not-a-locale/);
    assert.doesNotMatch(xml, /<item>values<\/item>/);
  });

  test.after(() => {
    // Cleanup temp files and dirs
    fs.rmSync(tmpRoot, { recursive: true, force: true });
  });
});
