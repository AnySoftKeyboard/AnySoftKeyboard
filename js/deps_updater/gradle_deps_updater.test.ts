import { update_gradle_deps } from './gradle_deps_updater.js'; // Adjust if necessary
import test from 'node:test';
import assert from 'node:assert';
import fs from 'fs';
import * as latestVersionLocator from './latest_version_locator.js'; // Import for mocking getLatestMavenVersion
import type { Dep } from './gradle_deps_to_update.js'; // For type usage

// Mock dependencies
test.mock.method(fs, 'globSync', (_pattern: string | URL, _options?: object) => {
  // Default to finding no files. Override in specific tests if needed.
  return [];
});
test.mock.method(fs, 'readFileSync', (_path: fs.PathOrFileDescriptor, _options?: object | null) => {
  return ''; // Default to empty content. Override if specific content needed.
});
test.mock.method(fs.promises, 'writeFile', async (_file: fs.PathOrFileDescriptor, _data: string | Uint8Array, _options?: object | null) => {
  return Promise.resolve();
});

// Mock getLatestMavenVersion as it's a dependency not being tested here.
const getLatestMavenVersionMock = test.mock.fn(latestVersionLocator, 'getLatestMavenVersion', async () => '1.0.0');


test.describe('gradle_deps_updater tests', () => {
  test('update_gradle_deps should be a function', () => {
    assert.strictEqual(typeof update_gradle_deps, 'function', 'update_gradle_deps should be a function.');
  });

  test('update_gradle_deps should complete without error if no gradle files are found', async () => {
    const globSyncMock = test.mock.method(fs, 'globSync', () => []);
    // Spies to ensure no further action if no files
    const readFileSyncSpy = test.mock.method(fs, 'readFileSync');
    const writeFileSpy = test.mock.method(fs.promises, 'writeFile');
    const consoleLogSpy = test.mock.method(console, 'log', () => {});


    // Sample non-empty dependencies array for this test
    const sampleDeps: Dep[] = [{ mavenUrl: 'url', groupId: 'g', artifactId: 'a', friendlyName: 'FN'}];

    await assert.doesNotThrow(
      async () => await update_gradle_deps('/fake/root', sampleDeps),
      'update_gradle_deps should not throw if no files are found by globSync.'
    );

    assert.strictEqual(getLatestMavenVersionMock.mock.calls.length, sampleDeps.length, 'getLatestMavenVersion should be called for each dependency.');
    assert.strictEqual(globSyncMock.mock.calls.length, 1, 'globSync should be called once.');
    assert.strictEqual(readFileSyncSpy.mock.calls.length, 0, 'readFileSync should not be called if no files.');
    assert.strictEqual(writeFileSpy.mock.calls.length, 0, 'writeFile should not be called if no files.');

    // Restore mocks
    globSyncMock.mock.restore();
    readFileSyncSpy.mock.restore();
    writeFileSpy.mock.restore();
    consoleLogSpy.mock.restore();
    getLatestMavenVersionMock.mock.resetCalls();
  });

  test('update_gradle_deps should complete without error if depsToUpdate is empty', async () => {
    const globSyncMock = test.mock.method(fs, 'globSync', () => ['/fake/build.gradle']); // Pretend a file is found
    const readFileSyncSpy = test.mock.method(fs, 'readFileSync');
    const writeFileSpy = test.mock.method(fs.promises, 'writeFile');
    const consoleLogSpy = test.mock.method(console, 'log', () => {});


    const emptyDeps: Dep[] = [];
    await assert.doesNotThrow(
      async () => await update_gradle_deps('/fake/root', emptyDeps),
      'update_gradle_deps should not throw if depsToUpdate is empty.'
    );

    assert.strictEqual(getLatestMavenVersionMock.mock.calls.length, 0, 'getLatestMavenVersion should not be called if depsToUpdate is empty.');
    // globSync would still be called before checking depsToUpdate length in the current implementation structure.
    assert.strictEqual(globSyncMock.mock.calls.length, 1, 'globSync should still be called.');
    assert.strictEqual(readFileSyncSpy.mock.calls.length, 0, 'readFileSync should not be called if no deps to update versions for.');
    assert.strictEqual(writeFileSpy.mock.calls.length, 0, 'writeFile should not be called if no deps to update versions for.');
    
    // Restore mocks
    globSyncMock.mock.restore();
    readFileSyncSpy.mock.restore();
    writeFileSpy.mock.restore();
    consoleLogSpy.mock.restore();
    getLatestMavenVersionMock.mock.resetCalls();
  });
});
