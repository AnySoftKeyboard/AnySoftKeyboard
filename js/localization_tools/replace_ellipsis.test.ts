import * as test from 'node:test';
import * as assert from 'node:assert';
import * as fs from 'fs';
import * as path from 'path';
import { tmpdir } from 'os'; // Import tmpdir from os
import { join } from 'path'; // Import join from path
import { replaceEllipsisInFile } from './replace_ellipsis.js'; // Changed import path

test.describe('replaceEllipsisInFile', () => {
  let tmpDir; // Declare tmpDir

  test.beforeEach(() => {
    // Create a unique temporary directory before each test
    tmpDir = fs.mkdtempSync(path.join(tmpdir(), 'ellipsis-test-'));
  });

  test.it('should return false for files without ellipsis and leave the file unchanged', () => {
    const filePath = join(tmpDir, 'no_ellipsis.txt'); // Use join and tmpDir
    const originalContent = 'This is a test file without ellipsis.';
    fs.writeFileSync(filePath, originalContent, 'utf8');

    const result = replaceEllipsisInFile(filePath);
    assert.strictEqual(result, false, 'Function should return false');

    const currentContent = fs.readFileSync(filePath, 'utf8');
    assert.strictEqual(currentContent, originalContent, 'File content should remain unchanged');
  });

  test.it('should return true for files with ellipsis and replace ... with …', () => {
    const filePath = join(tmpDir, 'with_ellipsis.txt'); // Use join and tmpDir
    const originalContent = 'This is a test file with an ellipsis... here.';
    const expectedContent = 'This is a test file with an ellipsis… here.';
    fs.writeFileSync(filePath, originalContent, 'utf8');

    const result = replaceEllipsisInFile(filePath);
    assert.strictEqual(result, true, 'Function should return true');

    const currentContent = fs.readFileSync(filePath, 'utf8');
    assert.strictEqual(currentContent, expectedContent, 'File content should be updated with …');
  });

  test.it('should return true for files with mixed ... and … and only replace ...', () => {
    const filePath = join(tmpDir, 'mixed_ellipsis.txt'); // Use join and tmpDir
    const originalContent = 'This has new... and existing… ellipsis.';
    const expectedContent = 'This has new… and existing… ellipsis.';
    fs.writeFileSync(filePath, originalContent, 'utf8');

    const result = replaceEllipsisInFile(filePath);
    assert.strictEqual(result, true, 'Function should return true as ... was replaced');

    const currentContent = fs.readFileSync(filePath, 'utf8');
    assert.strictEqual(currentContent, expectedContent, 'Only ... should be replaced with …');
  });

  test.it('should return false for an empty file and leave the file empty', () => {
    const filePath = join(tmpDir, 'empty_file.txt'); // Use join and tmpDir
    fs.writeFileSync(filePath, '', 'utf8');

    const result = replaceEllipsisInFile(filePath);
    assert.strictEqual(result, false, 'Function should return false for an empty file');

    const currentContent = fs.readFileSync(filePath, 'utf8');
    assert.strictEqual(currentContent, '', 'File content should remain empty');
  });
});
