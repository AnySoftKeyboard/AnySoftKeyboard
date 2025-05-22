import fetch from 'node-fetch';
import { parseStringPromise } from 'xml2js';

export const VERSION_REGEX: RegExp = /[v]?\d+\.\d+(?:[.\w-]*)?/g;

const isStable = (version: string): boolean => !/alpha|beta|-rc/i.test(version);

export type FetcherFunctionType = typeof fetch;

const filterToDistinct = <T>(value: T, index: number, array: T[]): boolean => array.indexOf(value) === index;

const sortVersions = (a: string, b: string): number =>
  b.localeCompare(a, undefined, { numeric: true, sensitivity: 'base' });

export const getLatestGitHubRelease = (
  gh_user: string,
  gh_token: string,
  gh_repo: string,
  fetcher: FetcherFunctionType = fetch,
): Promise<string> => {
  const url = `https://api.github.com/repos/${gh_repo}/releases`;
  return fetcher(url, {
    headers: {
      Authorization: `Basic ${Buffer.from(`${gh_user}:${gh_token}`).toString('base64')}`,
      'User-Agent': 'third-party-update-script',
    },
  })
    .then((res) => {
      if (!res.ok) {
        throw new Error(`Failed to fetch releases: ${res.statusText}`);
      }
      return res.json();
    })
    .then((res) => {
      const releases = res as { prerelease: boolean; tag_name: string }[];
      const tags: string[] = releases
        .filter((r: { prerelease: boolean }) => r.prerelease === false)
        .map((r: { tag_name: string }) => r.tag_name)
        .flatMap((tag: string) => tag.match(VERSION_REGEX) || [])
        .filter(isStable)
        .filter(filterToDistinct)
        .sort(sortVersions);
      if (tags.length === 0) {
        throw new Error('No stable release tags found.');
      }
      return tags[0];
    });
};

export const getLatestMavenVersion = (
  mavenUrl: string,
  groupId: string,
  artifactId: string,
  fetcher: FetcherFunctionType = fetch,
): Promise<string> => {
  const versionsUrl = `${mavenUrl}/${groupId.replace(/\./g, '/')}/${artifactId}/maven-metadata.xml`;
  return fetcher(versionsUrl)
    .then((res) => {
      if (!res.ok) {
        throw new Error(`Failed to fetch ${versionsUrl}: ${res.statusText}`);
      }
      return res.text();
    })
    .then((xml) => parseStringPromise(xml))
    .then((parsed) => {
      const versions: string[] = parsed.metadata.versioning[0].versions[0].version;
      const stableVersions = versions.filter(isStable).filter(filterToDistinct).sort(sortVersions);
      if (stableVersions.length === 0) {
        throw new Error('No stable versions found.');
      }
      return stableVersions[0];
    });
};
