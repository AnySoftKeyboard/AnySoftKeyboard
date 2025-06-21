import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import assert from 'node:assert';
import test from 'node:test';
import { locateStringResourcesFolders, locateStringResourcesFoldersInRes } from './utils.js';

// --- Helper Functions ---
interface DirectoryStructure {
  [name: string]: string | DirectoryStructure | null;
}

function createTestDir(basePath: string, structure: DirectoryStructure): void {
  if (!fs.existsSync(basePath)) {
    fs.mkdirSync(basePath, { recursive: true });
  }
  for (const name in structure) {
    const itemPath = path.join(basePath, name);
    const item = structure[name];
    if (typeof item === 'string') {
      fs.writeFileSync(itemPath, item);
    } else if (item === null) {
      fs.writeFileSync(itemPath, '');
    } else if (typeof item === 'object' && item !== null) {
      fs.mkdirSync(itemPath, { recursive: true });
      createTestDir(itemPath, item as DirectoryStructure);
    }
  }
}

// --- Test Suites ---

test.describe('locateStringResourcesFoldersInRes', () => {
  const baseTmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'tmp_test_fixtures_inres-'));

  test('InRes: should find valid string resource folders with existing strings.xml', () => {
    const testResDir = path.join(baseTmpDir, 'res1');
    createTestDir(testResDir, {
      values: { 'strings.xml': '<resources></resources>' },
      'values-en': { 'strings.xml': '<resources></resources>' },
      'values-es-rUS': { 'strings.xml': '<resources></resources>' },
      drawable: { 'icon.png': null },
    });

    const result = locateStringResourcesFoldersInRes(testResDir);
    const normalizedResult = result.map((p) => p.replace(/\\/g, '/'));
    const expected = [
      path.join(testResDir, 'values', 'strings.xml'),
      path.join(testResDir, 'values-en', 'strings.xml'),
      path.join(testResDir, 'values-es-rUS', 'strings.xml'),
    ].map((p) => p.replace(/\\/g, '/'));

    assert.deepStrictEqual(normalizedResult.sort(), expected.sort(), 'Should find all valid strings.xml');
  });

  test('InRes: should exclude validly named folders if strings.xml is missing', () => {
    const testResDir = path.join(baseTmpDir, 'res2');
    createTestDir(testResDir, {
      values: { 'strings.xml': '<resources></resources>' },
      'values-fr': { 'some_other_file.txt': 'hello' },
    });

    const result = locateStringResourcesFoldersInRes(testResDir);
    const normalizedResult = result.map((p) => p.replace(/\\/g, '/'));
    const expected = [path.join(testResDir, 'values', 'strings.xml')].map((p) => p.replace(/\\/g, '/'));

    assert.deepStrictEqual(normalizedResult.sort(), expected.sort(), 'Should only find folders with strings.xml');
  });

  test('InRes: should ignore invalid folder names', () => {
    const testResDir = path.join(baseTmpDir, 'res3');
    createTestDir(testResDir, {
      'values-v21': { 'strings.xml': '<resources></resources>' },
      'drawable-hdpi': { 'image.png': null },
      'not-values': { 'data.json': null },
      values: { 'strings.xml': '<resources></resources>' },
    });

    const result = locateStringResourcesFoldersInRes(testResDir);
    const normalizedResult = result.map((p) => p.replace(/\\/g, '/'));
    const expected = [path.join(testResDir, 'values', 'strings.xml')].map((p) => p.replace(/\\/g, '/'));

    assert.deepStrictEqual(normalizedResult.sort(), expected.sort(), 'Should ignore incorrectly named folders');
  });

  test('InRes: should return an empty array for an empty directory', () => {
    const testResDir = path.join(baseTmpDir, 'res4_empty');
    fs.mkdirSync(testResDir, { recursive: true });

    const result = locateStringResourcesFoldersInRes(testResDir);
    assert.deepStrictEqual(result, [], 'Should return empty array for empty res dir');
  });

  test('InRes: should return an empty array if no folders contain strings.xml', () => {
    const testResDir = path.join(baseTmpDir, 'res5_no_strings');
    createTestDir(testResDir, {
      values: { 'other.xml': '<resources></resources>' },
      'values-de': { 'another.txt': 'text' },
    });

    const result = locateStringResourcesFoldersInRes(testResDir);
    assert.deepStrictEqual(result, [], 'Should return empty if no strings.xml found');
  });

  test('InRes: should handle folders that are not directories (though readdirSync withFileTypes filters this)', () => {
    const testResDir = path.join(baseTmpDir, 'res6_mixed_content');
    createTestDir(testResDir, {
      values: { 'strings.xml': '<resources></resources>' },
      'file.xml': 'this is a file, not a dir',
      'values-pt': { 'strings.xml': '<resources></resources>' },
    });

    const result = locateStringResourcesFoldersInRes(testResDir);
    const normalizedResult = result.map((p) => p.replace(/\\/g, '/'));
    const expected = [
      path.join(testResDir, 'values', 'strings.xml'),
      path.join(testResDir, 'values-pt', 'strings.xml'),
    ].map((p) => p.replace(/\\/g, '/'));

    assert.deepStrictEqual(normalizedResult.sort(), expected.sort(), 'Should correctly handle mixed content types');
  });
});

test.describe('locateStringResourcesFolders', () => {
  const baseTmpRepoDir = fs.mkdtempSync(path.join(os.tmpdir(), 'tmp_test_repo_root-'));

  test('Folders: should locate string resource folders based on a mock crowdin.yml', () => {
    const testRepoDir = path.join(baseTmpRepoDir, 'repo1');
    const crowdinFilePath = path.join(testRepoDir, 'crowdin.yml');

    const mockCrowdinContent = `
files:
  - source: /ime/app/src/main/res/values/strings.xml
    translation: /ime/app/src/main/res/values-%android_code%/strings.xml
  - source: /ime/remote/src/main/res/values/strings.xml
    translation: /ime/remote/src/main/res/values-%android_code%/strings.xml
`;
    createTestDir(testRepoDir, {
      'crowdin.yml': mockCrowdinContent,
      ime: {
        app: {
          src: {
            main: {
              res: {
                values: { 'strings.xml': '<resources></resources>' },
                'values-en': { 'strings.xml': '<resources></resources>' },
              },
            },
          },
        },
        remote: {
          src: {
            main: {
              res: {
                values: { 'strings.xml': '<resources></resources>' },
                'values-fr': { 'strings.xml': '<resources></resources>' },
                drawable: { 'img.png': null },
              },
            },
          },
        },
      },
    });

    const result = locateStringResourcesFolders(testRepoDir, crowdinFilePath);
    const normalizedResult = result.map((p) => p.replace(/\\/g, '/'));
    const expected = [
      path.join(testRepoDir, 'ime/app/src/main/res/values/strings.xml'),
      path.join(testRepoDir, 'ime/app/src/main/res/values-en/strings.xml'),
      path.join(testRepoDir, 'ime/remote/src/main/res/values/strings.xml'),
      path.join(testRepoDir, 'ime/remote/src/main/res/values-fr/strings.xml'),
    ].map((p) => p.replace(/\\/g, '/'));

    assert.deepStrictEqual(
      normalizedResult.sort(),
      expected.sort(),
      'Should find all strings.xml from crowdin sources',
    );
  });

  test('Folders: should return empty array if no valid res folders found from crowdin sources', () => {
    const testRepoDir = path.join(baseTmpRepoDir, 'repo2');
    const crowdinFilePath = path.join(testRepoDir, 'crowdin.yml');
    const mockCrowdinContent = `
files:
  - source: /ime/nomatch/src/main/res/values/strings.xml
`;
    createTestDir(testRepoDir, {
      'crowdin.yml': mockCrowdinContent,
      ime: {
        nomatch: {
          src: {
            main: {
              res: {
                'values-es': { 'other.xml': 'data' },
              },
            },
          },
        },
      },
    });

    const result = locateStringResourcesFolders(testRepoDir, crowdinFilePath);
    assert.deepStrictEqual(result, [], 'Should be empty if sources lead to no valid string folders');
  });

  test('Folders: should throw an error for malformed crowdin file (e.g. missing "files" key)', () => {
    const testRepoDir = path.join(baseTmpRepoDir, 'repo3');
    const crowdinFilePath = path.join(testRepoDir, 'crowdin.yml');
    const mockCrowdinContent = 'invalid_yaml_structure: true';
    createTestDir(testRepoDir, { 'crowdin.yml': mockCrowdinContent });

    assert.throws(
      () => locateStringResourcesFolders(testRepoDir, crowdinFilePath),
      new Error(`Failed to read valid values (or folders) from ${crowdinFilePath.replace(/\\/g, '/')}!`),
      'Error message mismatch for malformed YAML',
    );
  });

  test('Folders: should throw an error for crowdin file where "files" is not an array', () => {
    const testRepoDir = path.join(baseTmpRepoDir, 'repo4');
    const crowdinFilePath = path.join(testRepoDir, 'crowdin.yml');
    const mockCrowdinContent = 'files: "this_is_not_an_array"';
    createTestDir(testRepoDir, { 'crowdin.yml': mockCrowdinContent });

    const expectedPath = crowdinFilePath.replace(/\\/g, '/');
    assert.throws(
      () => locateStringResourcesFolders(testRepoDir, crowdinFilePath),
      new Error(`Failed to read valid values (or folders) from ${expectedPath}!`),
      'Error message mismatch for "files" not array',
    );
  });

  test('Folders: should handle case where crowdin file does not exist', () => {
    const testRepoDir = path.join(baseTmpRepoDir, 'repo5');
    const crowdinFilePath = path.join(testRepoDir, 'non_existent_crowdin.yml');

    assert.throws(
      () => locateStringResourcesFolders(testRepoDir, crowdinFilePath),
      (err: NodeJS.ErrnoException) => {
        // Check if the error is an ENOENT error
        return err.code === 'ENOENT';
      },
      'Should throw ENOENT error if crowdin file does not exist',
    );
  });

  test('Folders: should correctly use repoRoot and handle sources not starting with /', () => {
    const testRepoDir = path.join(baseTmpRepoDir, 'repo6');
    const crowdinFilePath = path.join(testRepoDir, 'crowdin.yml');
    const mockCrowdinContent = `
files:
  - source: module/res/values/strings.xml
`;
    createTestDir(testRepoDir, {
      'crowdin.yml': mockCrowdinContent,
      module: {
        res: {
          values: { 'strings.xml': '<resources></resources>' },
          'values-de': { 'strings.xml': '<resources></resources>' },
        },
      },
    });

    const result = locateStringResourcesFolders(testRepoDir, crowdinFilePath);
    const normalizedResult = result.map((p) => p.replace(/\\/g, '/'));
    const expected = [
      path.join(testRepoDir, 'module/res/values/strings.xml'),
      path.join(testRepoDir, 'module/res/values-de/strings.xml'),
    ].map((p) => p.replace(/\\/g, '/'));

    assert.deepStrictEqual(normalizedResult.sort(), expected.sort(), 'Should handle source paths relative to repoRoot');
  });
});
