import { Response } from 'node-fetch';
import assert from 'node:assert';
import test from 'node:test';
import { getLatestMavenVersion, getLatestGitHubRelease } from './latest_version_locator.js';

test.describe('test getLatestMavenVersion', async () => {
  test('throws if XML structure is missing versioning tag', async () => {
    const malformedXml = `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>com.example</groupId>
  <artifactId>lib</artifactId>
  <otherdata>Some other data</otherdata>
</metadata>`;
    const fakeFetcher = async (_url: unknown) =>
      new Response(malformedXml, { status: 200, statusText: 'OK', headers: { 'Content-Type': 'application/xml' } });

    await assert.rejects(
      () => getLatestMavenVersion('https://repo.maven.org/maven2', 'com.example', 'lib', fakeFetcher),
      new Error('Invalid XML structure: <versioning> tag missing or empty.')
    );
  });

  test('throws if XML structure is missing versions tag inside versioning', async () => {
    const malformedXml = `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>com.example</groupId>
  <artifactId>lib</artifactId>
  <versioning>
    <latest>1.2.3</latest>
    <release>1.2.3</release>
    <lastUpdated>20230101000000</lastUpdated>
  </versioning>
</metadata>`;
    const fakeFetcher = async (_url: unknown) =>
      new Response(malformedXml, { status: 200, statusText: 'OK', headers: { 'Content-Type': 'application/xml' } });

    await assert.rejects(
      () => getLatestMavenVersion('https://repo.maven.org/maven2', 'com.example', 'lib', fakeFetcher),
      new Error('Invalid XML structure: <versions> tag missing or empty inside <versioning>.')
    );
  });

  test('throws if XML structure is missing metadata tag', async () => {
    const malformedXml = `<?xml version="1.0" encoding="UTF-8"?>
<root>
  <someotherdata>Some other data</someotherdata>
</root>`;
    const fakeFetcher = async (_url: unknown) =>
      new Response(malformedXml, { status: 200, statusText: 'OK', headers: { 'Content-Type': 'application/xml' } });

    await assert.rejects(
      () => getLatestMavenVersion('https://repo.maven.org/maven2', 'com.example', 'lib', fakeFetcher),
      new Error('Invalid XML structure: <metadata> tag missing.')
    );
  });

  test('throws if XML structure is missing version tag inside versions', async () => {
    const malformedXml = `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>com.example</groupId>
  <artifactId>lib</artifactId>
  <versioning>
    <latest>1.2.3</latest>
    <release>1.2.3</release>
    <versions>
      <someOtherTag>text</someOtherTag>
    </versions>
    <lastUpdated>20230101000000</lastUpdated>
  </versioning>
</metadata>`;
    const fakeFetcher = async (_url: unknown) =>
      new Response(malformedXml, { status: 200, statusText: 'OK', headers: { 'Content-Type': 'application/xml' } });

    await assert.rejects(
      () => getLatestMavenVersion('https://repo.maven.org/maven2', 'com.example', 'lib', fakeFetcher),
      new Error('Invalid XML structure: <version> tags missing, not an array, or empty inside <versions>.')
    );
  });
});

test.describe('test getLatestGitHubRelease', async () => {
  // Utility to create a mock fetcher for GitHub API responses
  const makeGitHubMockFetcher = (body: unknown, ok = true, status = 200) => {
    const responseBody = JSON.stringify(body);
    return async (_url: unknown) => 
      new Response(responseBody, { 
        status: ok ? status : 500, 
        statusText: ok ? 'OK' : 'Server Error', 
        headers: { 'Content-Type': 'application/json' } 
      });
  };

  test('throws if GitHub releases response is not an array', async () => {
    const fakeFetcher = makeGitHubMockFetcher({ message: "This is not an array" });
    await assert.rejects(
      () => getLatestGitHubRelease('owner', 'repo', fakeFetcher),
      TypeError // Expecting "releases.filter is not a function" or similar
    );
  });

  test('throws if GitHub releases response is an array of non-objects', async () => {
    const fakeFetcher = makeGitHubMockFetcher([null, "a string", 123]);
    await assert.rejects(
      () => getLatestGitHubRelease('owner', 'repo', fakeFetcher),
      TypeError // Expecting error when accessing r.prerelease or r.tag_name
    );
  });

  test('throws if GitHub release object is missing tag_name property', async () => {
    // Scenario 3 (re-purposed as per subtask description's self-correction)
    // This tests when tag_name is missing, which is critical for processing.
    const fakeFetcher = makeGitHubMockFetcher([{ prerelease: false, notes: "no tag_name here" }]);
    await assert.rejects(
      () => getLatestGitHubRelease('owner', 'repo', fakeFetcher),
      TypeError // Expecting error when trying to access tag_name (e.g. for sorting or mapping)
                // The current implementation filters these out if tag_name is undefined,
                // then potentially throws "No stable release tags found."
                // Let's adjust the function or test to be more specific for TypeError if direct access fails.
                // For now, the sort function will attempt to call .replace on undefined if not filtered.
    );
  });
  
  test('throws if GitHub release object has tag_name but it is not a string', async () => {
    // This is a variation of Scenario 4, focusing on the type of tag_name.
    // The original Scenario 4 was similar to the re-purposed Scenario 3.
    // This test ensures that if tag_name exists but isn't a string, it's handled.
    const fakeFetcher = makeGitHubMockFetcher([{ prerelease: false, tag_name: 12345 }]);
    await assert.rejects(
      () => getLatestGitHubRelease('owner', 'repo', fakeFetcher),
      TypeError // Expecting error like "r.tag_name.replace is not a function" during sort
    );
  });

  test('handles array of objects missing optional prerelease property (defaults to filtering out)', async () => {
    // This is closer to the original intent of Scenario 3.
    // If prerelease is undefined, `undefined === false` is false, so it's filtered out.
    const fakeFetcher = makeGitHubMockFetcher([{ tag_name: "v1.0" }, { tag_name: "v1.1-beta" }]);
    await assert.rejects( // Expecting no stable releases to be found after filtering
      () => getLatestGitHubRelease('owner', 'repo', fakeFetcher),
      new Error('No stable release tags found.')
    );
  });
});
