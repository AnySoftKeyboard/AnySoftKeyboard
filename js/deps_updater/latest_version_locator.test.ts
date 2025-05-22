import { getLatestGitHubRelease, getLatestMavenVersion } from './latest_version_locator.js';
import test from 'node:test';
import assert from 'node:assert';
import { Response } from 'node-fetch';

function makeFakeResponse({ ok, statusText, jsonData }: { ok: boolean; statusText: string; jsonData?: unknown }) {
  // node-fetch Response: body, init
  // If ok is false, set status to 404, else 200
  // statusText is set via init
  // jsonData is stringified for the body
  return new Response(JSON.stringify(jsonData), {
    status: ok ? 200 : 404,
    statusText,
    headers: { 'Content-Type': 'application/json' },
  });
}

function makeFakeFetcher(releases: unknown[], ok = true) {
  return async (_url: unknown, _init?: unknown) =>
    makeFakeResponse({ ok, statusText: ok ? 'OK' : 'Error', jsonData: releases });
}

test.describe('test getLatestGitHubRelease', async () => {
  test('uses only isStable versions', async () => {
    const releases = [
      { prerelease: false, tag_name: 'v1.2.3' },
      { prerelease: false, tag_name: 'v2.0.0-beta' },
      { prerelease: false, tag_name: 'v1.2.4' },
      { prerelease: false, tag_name: 'v1.2.5-alpha' },
      { prerelease: false, tag_name: 'v1.3.0' },
    ];
    const fakeFetcher = makeFakeFetcher(releases);
    const latest = await getLatestGitHubRelease('user', 'token', 'repo', fakeFetcher);
    assert.equal(latest, 'v1.3.0');
  });

  test('handles tags without v', async () => {
    const releases = [
      { prerelease: false, tag_name: '1.2.3' },
      { prerelease: false, tag_name: '2.0.0-beta' },
      { prerelease: false, tag_name: '1.3.0' },
    ];
    const fakeFetcher = makeFakeFetcher(releases);
    const latest = await getLatestGitHubRelease('user', 'token', 'repo', fakeFetcher);
    assert.equal(latest, '1.3.0');
  });

  test('sorts versions as expected', async () => {
    const releases = [
      { prerelease: false, tag_name: 'v1.0.0' },
      { prerelease: false, tag_name: 'v1.10.0' },
      { prerelease: false, tag_name: 'v1.2.0' },
    ];
    const fakeFetcher = makeFakeFetcher(releases);
    const latest = await getLatestGitHubRelease('user', 'token', 'repo', fakeFetcher);
    assert.equal(latest, 'v1.10.0');
  });

  test('throws if no stable release tags found', async () => {
    const releases = [
      { prerelease: false, tag_name: 'v2.0.0-beta' },
      { prerelease: false, tag_name: 'v1.2.5-alpha' },
    ];
    const fakeFetcher = makeFakeFetcher(releases);
    await assert.rejects(
      () => getLatestGitHubRelease('user', 'token', 'repo', fakeFetcher),
      /No stable release tags found\./,
    );
  });

  test('throws if fetch fails', async () => {
    // Simulate a failed fetch by returning a Response with ok: false
    const fakeFetcher = async (_url: unknown, _init?: unknown) =>
      new Response(null, { status: 404, statusText: 'Not Found' });
    await assert.rejects(
      () => getLatestGitHubRelease('user', 'token', 'repo', fakeFetcher),
      /Failed to fetch releases: Not Found/,
    );
  });
});

test.describe('test getLatestMavenVersion', async () => {
  function makeMavenXml(versions: string[]) {
    return `<?xml version="1.0" encoding="UTF-8"?>
      <metadata>
        <versioning>
          <versions>
            ${versions.map((v) => `<version>${v}</version>`).join('')}
          </versions>
        </versioning>
      </metadata>`;
  }

  test('uses only isStable versions', async () => {
    const versions = ['1.2.3', '2.0.0-beta', '1.2.4', '1.2.5-alpha', '1.3.0'];
    const xml = makeMavenXml(versions);
    const fakeFetcher = async (_url: unknown) =>
      new Response(xml, { status: 200, statusText: 'OK', headers: { 'Content-Type': 'application/xml' } });
    const latest = await getLatestMavenVersion('https://repo.maven.org/maven2', 'com.example', 'lib', fakeFetcher);
    assert.equal(latest, '1.3.0');
  });

  test('throws if no versions found', async () => {
    const xml = makeMavenXml(['2.0.0-beta']); // since this is not a stable version, we'll skip it.
    const fakeFetcher = async (_url: unknown) =>
      new Response(xml, { status: 200, statusText: 'OK', headers: { 'Content-Type': 'application/xml' } });
    await assert.rejects(
      () => getLatestMavenVersion('https://repo.maven.org/maven2', 'com.example', 'lib', fakeFetcher),
      /No stable versions found\./,
    );
  });

  test('throws if fetch fails', async () => {
    const fakeFetcher = async (_url: unknown) => new Response(null, { status: 404, statusText: 'Not Found' });
    await assert.rejects(
      () => getLatestMavenVersion('https://repo.maven.org/maven2', 'com.example', 'lib', fakeFetcher),
      /Failed to fetch https:\/\/repo.maven.org\/maven2\/com\/example\/lib\/maven-metadata.xml: Not Found/,
    );
  });

  test('constructs versionsUrl correctly', async () => {
    let capturedUrl = '';
    const versions = ['1.0.0'];
    const xml = makeMavenXml(versions);
    const fakeFetcher = async (url: unknown) => {
      capturedUrl = String(url);
      return new Response(xml, { status: 200, statusText: 'OK', headers: { 'Content-Type': 'application/xml' } });
    };
    await getLatestMavenVersion('https://repo.maven.org/maven2', 'com.example', 'lib', fakeFetcher);
    assert.equal(
      capturedUrl,
      'https://repo.maven.org/maven2/com/example/lib/maven-metadata.xml',
      'versionsUrl should be constructed correctly',
    );
  });
});
