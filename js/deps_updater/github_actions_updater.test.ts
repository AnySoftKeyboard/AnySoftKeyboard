import { update_gh_actions } from './github_actions_updater.js'; // Adjust if necessary
import test from 'node:test';
import assert from 'node:assert';
import fs from 'fs';
import { getLatestGitHubRelease } from './latest_version_locator.js'; // Import for mocking

// Mock dependencies
test.mock.method(fs, 'globSync', (_pattern: string | URL, _options?: object) => {
  // This mock will be overridden in specific tests if needed.
  // Default to finding no files.
  return [];
});

// Mock getLatestGitHubRelease as it's a dependency not being tested here.
test.mock.fn(getLatestGitHubRelease, async () => 'v1.0.0');


test.describe('github_actions_updater tests', () => {
  test('update_gh_actions should be a function', () => {
    assert.strictEqual(typeof update_gh_actions, 'function', 'update_gh_actions should be a function.');
  });

  test('update_gh_actions should complete without error if no workflow files are found', async () => {
    // Ensure globSync returns empty for this specific test
    const globSyncMock = test.mock.method(fs, 'globSync', () => []);

    // Spy on other fs methods to ensure they are not called if no files are found
    const readFileSpy = test.mock.method(fs.promises, 'readFile', () => Promise.resolve(''));
    const readFileSyncSpy = test.mock.method(fs, 'readFileSync', () => '');
    const writeFileSpy = test.mock.method(fs.promises, 'writeFile', () => Promise.resolve());
    const consoleLogSpy = test.mock.method(console, 'log', () => {});


    await assert.doesNotThrow(
      async () => await update_gh_actions('/fake/path', 'testuser', 'testtoken'),
      'update_gh_actions should not throw if no files are found by globSync.'
    );

    assert.strictEqual(globSyncMock.mock.calls.length, 1, 'globSync should be called once.');
    // Check that console.log indicates 0 actions found, or similar message
     const logCalls = consoleLogSpy.mock.calls;
    assert.ok(logCalls.some(call => call.arguments[0].includes('Found 0 unique GitHub Actions.')), 'Should log that 0 actions were found.');


    // Ensure that file reading/writing and version checking are not attempted
    assert.strictEqual(readFileSpy.mock.calls.length, 0, 'fs.promises.readFile should not be called.');
    assert.strictEqual(readFileSyncSpy.mock.calls.length, 0, 'fs.readFileSync should not be called.');
    assert.strictEqual(writeFileSpy.mock.calls.length, 0, 'fs.promises.writeFile should not be called.');
    
    // Restore mocks
    globSyncMock.mock.restore();
    readFileSpy.mock.restore();
    readFileSyncSpy.mock.restore();
    writeFileSpy.mock.restore();
    consoleLogSpy.mock.restore();
  });

  // Add more tests here if other simple, easily mockable scenarios are identified.
  // For example, a test where one file is found, but it contains no actions.
  test('update_gh_actions should handle a file with no actions', async () => {
    const globSyncMock = test.mock.method(fs, 'globSync', () => ['fake.yml']);
    const readFileMock = test.mock.method(fs.promises, 'readFile', async () => 'key: value\nname: test'); // No 'uses:'
    const readFileSyncSpy = test.mock.method(fs, 'readFileSync', () => 'key: value\nname: test'); // for the write phase
    const writeFileSpy = test.mock.method(fs.promises, 'writeFile', () => Promise.resolve());
    const consoleLogSpy = test.mock.method(console, 'log', () => {});

    await update_gh_actions('/fake/path', 'testuser', 'testtoken');

    assert.strictEqual(globSyncMock.mock.calls.length, 1, 'globSync should be called once.');
    assert.strictEqual(readFileMock.mock.calls.length, 1, 'readFile should be called once for the found file.');
    
    const logCalls = consoleLogSpy.mock.calls;
    assert.ok(logCalls.some(call => call.arguments[0].includes('Found 0 unique GitHub Actions.')), 'Should log that 0 actions were found after processing file.');

    // Ensure no attempt to get latest versions or write files if no actions were parsed
    assert.strictEqual(getLatestGitHubRelease.mock.calls.length, 0, 'getLatestGitHubRelease should not be called if no actions found.');
    assert.strictEqual(writeFileSpy.mock.calls.length, 0, 'writeFile should not be called if no actions updated.');

    globSyncMock.mock.restore();
    readFileMock.mock.restore();
    readFileSyncSpy.mock.restore();
    writeFileSpy.mock.restore();
    consoleLogSpy.mock.restore();
    getLatestGitHubRelease.mock.resetCalls(); // Reset call count for this specific mock
  });
});
